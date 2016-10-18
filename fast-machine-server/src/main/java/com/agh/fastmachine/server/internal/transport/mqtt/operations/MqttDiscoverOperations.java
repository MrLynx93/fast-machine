package com.agh.fastmachine.server.internal.transport.mqtt.operations;

import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.internal.parser.WriteAttributesParser;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttTransport;
import com.agh.fastmachine.server.internal.transport.PendingRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.operations.DiscoverOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.agh.fastmachine.server.internal.transport.mqtt.message.ContentType.PLAIN_TEXT;

public class MqttDiscoverOperations implements DiscoverOperations {
    private static final Logger LOG = LoggerFactory.getLogger(MqttDiscoverOperations.class);
    private final WriteAttributesParser parser = new WriteAttributesParser();
    private final MqttTransport transport;

    public MqttDiscoverOperations(MqttTransport transport) {
        this.transport = transport;
    }

    @Override
    public <T extends ObjectInstanceProxy> void discover(ObjectBaseProxy<T> object) {
        String topic = transport.getRequestTopic(MessageType.M_DISCOVER, object);
        Lwm2mRequest request = Lwm2mRequest.of(topic, PLAIN_TEXT);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            Attributes attributes = parser.parseWriteAttributes(response.getPayloadText());
            if (attributes != null) {
                object.internal().updateAttributes(attributes);
            }
            List<Integer> supportedResources = parser.parseSupportedResources(response.getPayloadText());
            for (T instance : object.getObjectInstances().values()) {
                for (Map.Entry<Integer, ? extends ObjectResourceProxy<?>> resourceEntry : instance.getResources().entrySet()) {
                    ObjectResourceProxy<?> resource = resourceEntry.getValue();
                    resource.internal().setSupported(supportedResources.contains(resourceEntry.getKey()));
                }
            }
            LOG.debug("Executed discover on object /{}", object.getId());
        }
    }

    @Override
    public void discover(ObjectInstanceProxy instance) {
        String topic = transport.getRequestTopic(MessageType.M_DISCOVER, instance);
        Lwm2mRequest request = Lwm2mRequest.of(topic, PLAIN_TEXT);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            Attributes attributes = parser.parseWriteAttributes(response.getPayloadText());
            if (attributes != null) {
                instance.internal().updateAttributes(attributes);
            }
            LOG.debug("Executed discover on: {}/{}", instance.getObject().getId(), instance.getId());
        }
    }

    @Override
    public void discover(ObjectResourceProxy<?> resource) {
        String topic = transport.getRequestTopic(MessageType.M_DISCOVER, resource);
        Lwm2mRequest request = Lwm2mRequest.of(topic, PLAIN_TEXT);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            Attributes attributes = parser.parseResourceWriteAttributes(response.getPayloadText(), resource);
            if (attributes != null) {
                resource.internal().updateAttributes(attributes);
            }
            LOG.debug("Executed discover on: {}/{}/{}",
                    resource.getInstance().getObject().getId(),
                    resource.getInstance().getId(),
                    resource.getId());
        }
    }
}
