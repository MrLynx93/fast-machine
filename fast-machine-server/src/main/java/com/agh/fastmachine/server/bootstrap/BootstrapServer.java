package com.agh.fastmachine.server.bootstrap;

import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttRequest;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BootstrapServer { // TODO wait for bootstrap request
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

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void setSequenceForClient(String clientId, BootstrapSequence sequence) {
        sequence.setServerId(this.serverId);
        sequenceForClient.put(clientId, sequence);
    }

    private void bootstrapClient(String clientName) {
        try {
            System.out.println("Bootstrapping client " + clientName);
            List<BootstrapOperation> operations = sequenceForClient.get(clientName).getOperations();
            for (BootstrapOperation operation : operations) {
                Lwm2mMqttRequest request = operation.getRequest();
                mqttClient.publish("lynx/" + request.getTopic().toString(), request.toMqttMessage());
                System.out.println("published " + request.getTopic().toString());
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            String clientName = clientFromTopic(topic);
            boolean active = mqttMessage.getPayload()[0] == 1;

            if (active) {
                bootstrapClient(clientName);
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
