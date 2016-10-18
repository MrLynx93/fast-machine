package com.agh.fastmachine.server.internal.transport.mqtt;

import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.PendingRequest;
import com.agh.fastmachine.server.internal.transport.Transport;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.mqtt.operations.*;
import com.agh.fastmachine.server.internal.transport.operations.Operations;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.agh.fastmachine.server.internal.transport.operations.Operations.Type.*;

public class MqttTransport extends Transport<MqttConfiguration> {
    private final Map<Operations.Type, Operations> operations;
    private Map<String, PendingRequest> pendingRequests = new ConcurrentHashMap<>();
    private Map<String, ObservationListener> observeSessions = new ConcurrentHashMap<>();
    private MqttClient mqttClient;

    public MqttTransport() {
        operations = new HashMap<>();
        operations.put(CREATE, new MqttCreateOperations(this));
        operations.put(DELETE, new MqttDeleteOperations(this));
        operations.put(DISCOVER, new MqttDiscoverOperations(this));
        operations.put(EXECUTE, new MqttExecuteOperations(this));
        operations.put(OBSERVE, new MqttObserveOperations(this));
        operations.put(READ, new MqttReadOperations(this));
        operations.put(WRITE, new MqttWriteOperations(this));
        operations.put(WRITE_ATTRIBUTES, new MqttWriteAttributeOperations(this));
    }

    @Override
    public void start(MqttConfiguration configuration) {
        try {
            mqttClient = new MqttClient(configuration.getBrokerAddress(), configuration.getServerId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setCleanSession(true);

            mqttClient.connect(options);
            mqttClient.setCallback(mqttCallback);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Operations getOperations(Operations.Type type) {
        return operations.get(type);
    }

    public PendingRequest sendRequest(Lwm2mRequest request) {
        return sendRequest(request, null);
    }

    public PendingRequest sendRequest(Lwm2mRequest request, ObservationListener<?> listener) {
        try {
            PendingRequest pendingRequest = new PendingRequest(request);
            pendingRequests.put(pendingRequest.getToken(), pendingRequest);
            if (listener != null) {
                observeSessions.put(pendingRequest.getToken(), listener);
            }
            mqttClient.publish(request.getTopic().toString(), request.getPayload(), 0, false);
            return pendingRequest;
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }

    public String getRequestTopic(MessageType type, ObjectBaseProxy object) {
        return String.format("%s/req/%s/%s/%s/%d",
                type.getType(),
                generateToken(),
                object.getClientProxy().getClientId(),
                configuration.getServerId(),
                object.getId());
    }

    public String getRequestTopic(MessageType type, ObjectInstanceProxy instance) {
        return String.format("%s/req/%s/%s/%s/%d/%d",
                type.getType(),
                generateToken(),
                configuration.getServerId(),
                instance.getClientProxy().getClientId(),
                instance.getObject().getId(),
                instance.getId());
    }

    public String getRequestTopic(MessageType type, ObjectResourceProxy resource) {
        return String.format("%s/req/%s/%s/%s/%d/%d/%d",
                type.getType(),
                generateToken(),
                resource.getClientProxy().getClientId(),
                configuration.getServerId(),
                resource.getInstance().getObject().getId(),
                resource.getInstance().getId(),
                resource.getId());
    }


    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topicString, MqttMessage message) throws Exception {
            Lwm2mResponse response = Lwm2mResponse.parse(topicString, message.getPayload());
            PendingRequest pendingRequest = pendingRequests.get(response.getToken());
            pendingRequest.complete(response);

            if ())


            if (!response.isNotify()) {
                pendingRequests.remove(pendingRequest.getToken());
            }
        }

        @Override
        public void connectionLost(Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }
    };

    private String generateToken() {
        return ""; // tODO
    }
}
