package com.agh.fastmachine.server.internal.transport.mqtt.operations;

import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.core.internal.parser.WriteParser;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectMultipleResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttTransport;
import com.agh.fastmachine.server.internal.transport.PendingRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.operations.WriteOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.agh.fastmachine.server.internal.transport.mqtt.message.ContentType.OPAQUE;
import static com.agh.fastmachine.server.internal.transport.mqtt.message.ContentType.PLAIN_TEXT;
import static com.agh.fastmachine.server.internal.transport.mqtt.message.ContentType.TLV;

public class MqttWriteOperations implements WriteOperations {
    private static final Logger LOG = LoggerFactory.getLogger(MqttWriteOperations.class);
    private final MqttTransport transport;

    public MqttWriteOperations(MqttTransport transport) {
        this.transport = transport;
    }

    @Override
    public void write(ObjectInstanceProxy instance) {
        String topic = transport.getRequestTopic(MessageType.M_WRITE, instance);
        Lwm2mRequest request = Lwm2mRequest.of(topic, TLV, WriteParser.serialize(instance));

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            LOG.debug("Executed write to: /{}/{}", instance.getObject().getId(), instance.getId());
        }
    }

    @Override
    public void write(ObjectResourceProxy resource) {
        String topic = transport.getRequestTopic(MessageType.M_WRITE, resource);
        Lwm2mRequest request = Lwm2mRequest.of(topic, getContentType(resource), WriteParser.serialize(resource));

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            LOG.debug("Executed write to: /{}/{}/{}",
                    resource.getInstance().getObject().getId(),
                    resource.getInstance().getId(),
                    resource.getId());
        }
    }

    private ContentType getContentType(ObjectResourceProxy resource) {
        if (OpaqueResourceValue.class.equals(resource.getValueType())) {
            return OPAQUE;
        }
        if (resource instanceof ObjectMultipleResourceProxy) {
            return TLV;
        }
        return PLAIN_TEXT;
    }
}
