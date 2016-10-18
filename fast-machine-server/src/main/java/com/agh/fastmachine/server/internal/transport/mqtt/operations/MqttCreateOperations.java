package com.agh.fastmachine.server.internal.transport.mqtt.operations;

import com.agh.fastmachine.core.internal.parser.WriteParser;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttTransport;
import com.agh.fastmachine.server.internal.transport.PendingRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.operations.CreateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.agh.fastmachine.server.internal.transport.mqtt.message.ContentType.TLV;

public class MqttCreateOperations implements CreateOperations {
    private static final Logger LOG = LoggerFactory.getLogger(MqttCreateOperations.class);
    private final MqttTransport transport;

    public MqttCreateOperations(MqttTransport transport) {
        this.transport = transport;
    }

    @Override
    public <T extends ObjectInstanceProxy> void create(T instance, ClientProxyImpl client) {
        byte[] payload = WriteParser.serialize(instance);
        String topic = transport.getRequestTopic(MessageType.M_CREATE, instance);
        Lwm2mRequest request = Lwm2mRequest.of(topic, TLV, payload);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            instance.internal().setId(response.getCreatedInstanceId());
            connectToRemote(instance, client);
            LOG.debug("Created object instance: {}/{}", instance.getObject().getId(), response.getCreatedInstanceId());
        }
    }

    @Override
    public <T extends ObjectInstanceProxy> void create(T instance, ClientProxyImpl client, int instanceId) {
        instance.internal().setId(instanceId); // TODO should this be in payload?
        byte[] payload = WriteParser.serialize(instance);
        String topic = transport.getRequestTopic(MessageType.M_CREATE, instance);
        Lwm2mRequest request = Lwm2mRequest.of(topic, TLV, payload);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            instance.internal().setId(response.getCreatedInstanceId());
            connectToRemote(instance, client);
            LOG.debug("Created object instance: {}/{}", instance.getObject().getId(), response.getCreatedInstanceId());
        }
    }

    private void connectToRemote(ObjectInstanceProxy instance, ClientProxyImpl client) {
        client.getServer().internal().getObjectTreeCreator().connectToRemoteClient(instance, client); // TODO It doesn't look good
    }
}
