package com.agh.fastmachine.server.internal.transport.mqtt.operations;

import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttTransport;
import com.agh.fastmachine.server.internal.transport.PendingRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.operations.DeleteOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttDeleteOperations implements DeleteOperations {
    private static final Logger LOG = LoggerFactory.getLogger(MqttDeleteOperations.class);
    private final MqttTransport transport;

    public MqttDeleteOperations(MqttTransport transport) {
        this.transport = transport;
    }

    @Override
    public void delete(ObjectInstanceProxy instance) {
        String topic = transport.getRequestTopic(MessageType.M_DELETE, instance);
        Lwm2mRequest request = Lwm2mRequest.of(topic);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            LOG.debug("Deleted instance: {}/{}", instance.getObject().getId(), instance.getId());
        }
    }
}
