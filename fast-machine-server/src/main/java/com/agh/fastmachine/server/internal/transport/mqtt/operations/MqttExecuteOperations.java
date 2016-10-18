package com.agh.fastmachine.server.internal.transport.mqtt.operations;

import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttTransport;
import com.agh.fastmachine.server.internal.transport.PendingRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.operations.ExecuteOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttExecuteOperations implements ExecuteOperations {
    private static final Logger LOG = LoggerFactory.getLogger(MqttExecuteOperations.class);
    private final MqttTransport transport;

    public MqttExecuteOperations(MqttTransport transport) {
        this.transport = transport;
    }

    @Override
    public void execute(ObjectResourceProxy<?> resource, byte[] arguments) {
        String topic = transport.getRequestTopic(MessageType.M_EXECUTE, resource);
        Lwm2mRequest request = Lwm2mRequest.of(topic, ContentType.PLAIN_TEXT, arguments);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();

        if (response.isSuccess()) {
            LOG.debug("Executed resource {}/{}/{}. Lwm2mResponse text: {}",
                    resource.getInstance().getObject().getId(),
                    resource.getInstance().getId(),
                    resource.getId(),
                    response.getPayloadText());
        }
    }

}
