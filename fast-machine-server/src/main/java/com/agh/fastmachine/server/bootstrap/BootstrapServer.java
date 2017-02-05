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

import javax.xml.ws.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootstrapServer { // TODO wait for bootstrap request
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private Map<String, PendingRequest> pendingRequests = new ConcurrentHashMap<>();
    private Map<String, BootstrapSequence> sequenceForClient = new HashMap<>();
    private MqttClient mqttClient;
    private BootstrapListener bootstrapListener;
    private String serverName;
    private int serverId;

    public BootstrapServer(String serverName, int serverId) {
        this.serverName = serverName;
        this.serverId = serverId;
    }

    public void start(MqttConfiguration configuration) {
        try {
            mqttClient = new MqttClient(configuration.getBrokerAddress(), serverName);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setCleanSession(true);

            mqttClient.connect();
            mqttClient.setCallback(mqttCallback);
            mqttClient.subscribe("lynx/clients/+");
            mqttClient.subscribe("lynx/br/res/#");
            mqttClient.subscribe("lynx/bd/res/#");
            mqttClient.subscribe("lynx/bw/res/#");
            mqttClient.subscribe("lynx/bf/res/#");

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public int getServerId() {
        return serverId;
    }

    public void setSequenceForClient(String clientId, BootstrapSequence sequence) {
        sequence.setServerId(this.serverId);
        sequenceForClient.put(clientId, sequence);
    }

    private void bootstrapClient(String clientName) {
        System.out.println("Bootstrapping client " + clientName);
        BootstrapSequence sequence = sequenceForClient.get(clientName);
        if (sequence == null) {
            sequence = sequenceForClient.get("*");
        }

        for (BootstrapOperation operation : sequence.getOperations()) {
            Lwm2mMqttRequest request = operation.getRequest();
            request.getTopic().setClientId(clientName);

            PendingRequest pendingRequest = sendRequest(request);
            try {
                pendingRequest.waitForCompletion();
            } catch (Exception e) {
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
