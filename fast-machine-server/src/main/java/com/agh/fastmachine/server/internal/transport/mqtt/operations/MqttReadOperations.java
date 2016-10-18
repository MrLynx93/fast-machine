package com.agh.fastmachine.server.internal.transport.mqtt.operations;

import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.parser.ReadParser;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectMultipleResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.parser.ServerReadParser;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttTransport;
import com.agh.fastmachine.server.internal.transport.PendingRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.operations.ReadOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.agh.fastmachine.server.internal.transport.mqtt.message.ContentType.*;

public class MqttReadOperations implements ReadOperations {
    private static final Logger LOG = LoggerFactory.getLogger(MqttReadOperations.class);
    private final ReadParser readParser = new ServerReadParser();
    private final MqttTransport transport;

    public MqttReadOperations(MqttTransport transport) {
        this.transport = transport;
    }

    @Override
    public <T extends ObjectInstanceProxy> void read(ObjectBaseProxy<T> object) {
        String topic = transport.getRequestTopic(MessageType.M_READ, object);
        Lwm2mRequest request = Lwm2mRequest.of(topic, TLV);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            ObjectBaseProxy newValue = readParser.deserialize(object, response.getPayload());
            object.internal().update(newValue);
            LOG.debug("Read object: /{}", object.getId());
        }
    }

    @Override
    public void read(ObjectInstanceProxy instance) {
        String topic = transport.getRequestTopic(MessageType.M_READ, instance);
        Lwm2mRequest request = Lwm2mRequest.of(topic, TLV);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            ObjectInstanceProxy newValue = readParser.deserialize(instance, response.getPayload());
            instance.internal().update(newValue);
            LOG.debug("Read instance: /{}/{}", instance.getObject().getId(), instance.getId());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(ObjectResourceProxy<?> resource) {
        String topic = transport.getRequestTopic(MessageType.M_READ, resource);
        Lwm2mRequest request = Lwm2mRequest.of(topic, getContentType(resource));

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.getContentType() == OPAQUE) {
            OpaqueResourceValue newValue = new OpaqueResourceValue(response.getPayload());
            resource.internal().update(newValue);
        } else if (response.getContentType() == TLV) {
            ObjectMultipleResourceProxy newValue = (ObjectMultipleResourceProxy<?>) readParser.deserialize(resource, response.getPayload());
            resource.internal().update(newValue);
        } else {
            ResourceValue<?> newValue = readParser.deserialize(resource.getValue(), response.getPayload());
            resource.internal().update(newValue);
        }
    }


}
