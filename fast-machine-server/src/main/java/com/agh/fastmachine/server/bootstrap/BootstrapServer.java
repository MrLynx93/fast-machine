package com.agh.fastmachine.server.bootstrap;

import com.agh.fastmachine.server.api.listener.BootstrapListener;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.PendingRequest;
import com.agh.fastmachine.server.internal.transport.mqtt.MQTT;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttRequest;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttResponse;
import org.eclipse.paho.client.mqttv3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO wait for bootstrap request
public class BootstrapServer {
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private Map<String, PendingRequest> pendingRequests = new ConcurrentHashMap<>();
    private Map<String, BootstrapSequence> sequenceForPattern = new LinkedHashMap<>();
    private MqttClient mqttClient;
    private final MqttConfiguration mqttConfiguration;
    private BootstrapListener bootstrapListener;
    private String name;

    public BootstrapServer(MqttConfiguration mqttConfiguration) {
        this.name = mqttConfiguration.getServerName();
        this.mqttConfiguration = mqttConfiguration;
    }

    public void start() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setCleanSession(true);

            if (mqttConfiguration.isDtls()) {
                mqttClient = new MqttClient("ssl://" + mqttConfiguration.getBrokerAddress(), name);
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                KeyStore keyStore = KeyStore.getInstance("JKS");
                InputStream inKeyStore = getClass().getClassLoader().getResourceAsStream(mqttConfiguration.getKeyStoreLocation());
                keyStore.load(inKeyStore, mqttConfiguration.getKeyStorePassword().toCharArray());

                trustManagerFactory.init(keyStore);
                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
                options.setSocketFactory(sslContext.getSocketFactory());
            } else {
                mqttClient = new MqttClient("tcp://" + mqttConfiguration.getBrokerAddress(), name);
            }

            mqttClient.connect(options);
            mqttClient.setCallback(mqttCallback);
            mqttClient.subscribe("lynx/clients/+");
            mqttClient.subscribe("lynx/br/res/#");
            mqttClient.subscribe("lynx/bd/res/#");
            mqttClient.subscribe("lynx/bw/res/#");
            mqttClient.subscribe("lynx/bf/res/#");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSequenceForPattern(String pattern, BootstrapSequence sequence) {
        sequence.setServerId(this.name);
        sequenceForPattern.put(pattern, sequence);
    }

    private void bootstrapClient(String clientName) {
        boolean matchesAny = false;
        for (String patternString : sequenceForPattern.keySet()) {
            Matcher matcher = Pattern.compile(patternString).matcher(clientName);
            if (matcher.matches()) {
                doBootstrap(clientName, sequenceForPattern.get(patternString));
                matchesAny = true;
                break;
            }
        }
        if (!matchesAny) {
            System.out.println("[Error] Didn't find a matching sequence for client " + clientName);
        }
    }

    private void doBootstrap(String clientName, BootstrapSequence sequence) {
        System.out.println("Bootstrapping client " + clientName);

        for (BootstrapOperation operation : sequence.getOperations()) {
            Lwm2mMqttRequest request = operation.getRequest();
            request.getTopic().setClientId(clientName);

            PendingRequest pendingRequest = sendRequest(request);
            try {
                pendingRequest.waitForCompletion();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("NIE UDALO SIE WYSLAC");
            }


            if (bootstrapListener != null) {
                if (request.getTopic().getOperation() == LWM2M.Operation.BS_WRITE) {
                    bootstrapListener.onBootstrapWrite(clientName);
                }
                if (request.getTopic().getOperation() == LWM2M.Operation.BS_DELETE) {
                    bootstrapListener.onBootstrapDelete(clientName);
                }
                if (request.getTopic().getOperation() == LWM2M.Operation.BS_FINISH) {
                    bootstrapListener.onBootstrapFinish(clientName);
                }
            }
        }
    }

    private PendingRequest sendRequest(Lwm2mMqttRequest request) {
        try {
            PendingRequest pendingRequest = new PendingRequest(request);
            pendingRequests.put(request.getToken(), pendingRequest);
            mqttClient.publish("lynx/" + request.getTopic().toString(), request.toMqttMessage());
            return pendingRequest;
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void respondToRequestBootstrap(String topicStr) throws MqttException {
        MQTT.Topic responseTopic = MQTT.Topic.fromString(topicStr);
        responseTopic.setType("res");
        Lwm2mMqttResponse response = new Lwm2mMqttResponse(responseTopic, LWM2M.ContentType.NO_FORMAT, LWM2M.ResponseCode.CHANGED);
        mqttClient.publish("lynx/" + response.getTopic().toString(), response.toMqttMessage());
    }

    public void setBootstrapListener(BootstrapListener bootstrapListener) {
        this.bootstrapListener = bootstrapListener;
    }

    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topicString, MqttMessage mqttMessage) throws Exception {
            final String topicStr = topicString.replaceFirst("lynx/", "");
            System.out.println("Received " + topicStr);

            if (topicStr.startsWith("clients")) {
                if ("1".equals(mqttMessage.toString())) {
                    executor.submit(() -> bootstrapClient(clientFromTopic(topicStr)));
                }
            } else {
                MQTT.Topic topic = MQTT.Topic.fromString(topicStr);
                if ("req".equals(topic.getType())) {
                    Lwm2mRequest request = Lwm2mMqttRequest.fromMqtt(mqttMessage, topic);
                    if (request.getOperation() == LWM2M.Operation.BS_REQ) {
                        // Respond to bootstrap request
                        respondToRequestBootstrap(topicStr);
                        bootstrapListener.onBootstrapRequest(topic.getClientId());
                        executor.submit(() -> bootstrapClient(clientFromTopic(topicStr)));
                    }
                }
                if ("res".equals(topic.getType())) {
                    Lwm2mMqttResponse response = Lwm2mMqttResponse.fromMqtt(mqttMessage, topic);
                    PendingRequest pendingRequest = pendingRequests.get(topic.getToken());
                    pendingRequest.complete(response);
                }
            }
        }

        @Override
        public void connectionLost(Throwable throwable) {

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }

        private String clientFromTopic(String topic) {
            return topic.replaceFirst("lynx/", "").split("/", -1)[1];
        }
    };


}
