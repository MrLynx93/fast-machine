package com.agh.fastmachine.server.bootstrap;

import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.PendingRequest;
import com.agh.fastmachine.server.internal.transport.mqtt.MQTT;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttRequest;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttResponse;
import org.eclipse.paho.client.mqttv3.*;

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
            mqttClient.subscribe("lynx/bd/res/#");
            mqttClient.subscribe("lynx/bw/res/#");
            mqttClient.subscribe("lynx/bf/res/#");

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void setSequenceForClient(String clientId, BootstrapSequence sequence) {
        sequence.setServerId(this.serverId);
        sequenceForClient.put(clientId, sequence);
    }

    private void bootstrapClient(String clientName) {
        System.out.println("Bootstrapping client " + clientName);
        List<BootstrapOperation> operations = sequenceForClient.get(clientName).getOperations();
        for (BootstrapOperation operation : operations) {
            Lwm2mMqttRequest request = operation.getRequest();
            PendingRequest pendingRequest = sendRequest(request);
            pendingRequest.waitForCompletion();
        }
    }

    public PendingRequest sendRequest(Lwm2mMqttRequest request) {
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
