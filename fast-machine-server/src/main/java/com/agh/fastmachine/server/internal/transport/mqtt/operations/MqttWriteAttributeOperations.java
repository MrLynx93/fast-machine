package com.agh.fastmachine.server.internal.transport.mqtt.operations;

import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttTransport;
import com.agh.fastmachine.server.internal.transport.PendingRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.operations.WriteAttributeOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttWriteAttributeOperations implements WriteAttributeOperations {
    private static final Logger LOG = LoggerFactory.getLogger(MqttWriteAttributeOperations.class);
    private final MqttTransport transport;

    public MqttWriteAttributeOperations(MqttTransport transport) {
        this.transport = transport;
    }

    @Override
    public <T extends ObjectInstanceProxy> void writeAttributes(ObjectBaseProxy<T> object) {
        String topic = transport.getRequestTopic(MessageType.M_WRITE_ATTRIBUTE, object);
        Lwm2mRequest request = Lwm2mRequest.of(topic, ContentType.PLAIN_TEXT, object.getAttributes().toRestString().getBytes());

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            LOG.debug("Write attributes to: /{}", object.getId());
        }
    }

    @Override
    public void writeAttributes(ObjectInstanceProxy instance) {
        String topic = transport.getRequestTopic(MessageType.M_WRITE_ATTRIBUTE, instance);
        Lwm2mRequest request = Lwm2mRequest.of(topic, ContentType.PLAIN_TEXT, instance.getAttributes().toRestString().getBytes());

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            LOG.debug("Write attributes to: /{}/{}", instance.getObject().getId(), instance.getId());
        }
    }

    @Override
    public void writeAttributes(ObjectResourceProxy resource) {
        String topic = transport.getRequestTopic(MessageType.M_WRITE_ATTRIBUTE, resource);
        Lwm2mRequest request = Lwm2mRequest.of(topic, ContentType.PLAIN_TEXT, resource.getAttributes().toRestString().getBytes());

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            LOG.debug("Write attributes to: /{}/{}/{}",
                    resource.getInstance().getObject().getId(),
                    resource.getInstance().getId(),
                    resource.getId());
        }
    }
}
