package com.agh.fastmachine.server.internal.transport.mqtt.operations;

import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectMultipleResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttTransport;
import com.agh.fastmachine.server.internal.transport.PendingRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.operations.ObserveOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.agh.fastmachine.server.internal.transport.mqtt.message.ContentType.OPAQUE;
import static com.agh.fastmachine.server.internal.transport.mqtt.message.ContentType.PLAIN_TEXT;
import static com.agh.fastmachine.server.internal.transport.mqtt.message.ContentType.TLV;

public class MqttObserveOperations implements ObserveOperations {
    private static final Logger LOG = LoggerFactory.getLogger(MqttObserveOperations.class);
    private final MqttTransport transport;

    public MqttObserveOperations(MqttTransport transport) {
        this.transport = transport;
    }

    @Override
    public <T extends ObjectInstanceProxy> void observe(ObjectBaseProxy<T> object, ObservationListener<ObjectBaseProxy<T>> listener) {
        String topic = transport.getRequestTopic(MessageType.I_OBSERVE, object);
        Lwm2mRequest request = Lwm2mRequest.of(topic, TLV);

        PendingRequest pendingRequest = transport.sendRequest(request, listener);
        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            LOG.debug("Started observing object: /{}", object.getId());
        }
    }

    @Override
    public void observe(ObjectInstanceProxy instance, ObservationListener<ObjectInstanceProxy> listener) {
        String topic = transport.getRequestTopic(MessageType.I_OBSERVE, instance);
        Lwm2mRequest request = Lwm2mRequest.of(topic, TLV);

        PendingRequest pendingRequest = transport.sendRequest(request, listener);
        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            LOG.debug("Started observing instance: /{}/{}", instance.getObject().getId(), instance.getId());
        }
    }

    @Override
    public <T extends ResourceValue<?>> void observe(ObjectResourceProxy<T> resource, ObservationListener<ObjectResourceProxy<T>> listener) {
        String topic = transport.getRequestTopic(MessageType.I_OBSERVE, resource);
        Lwm2mRequest request = Lwm2mRequest.of(topic, getContentType(resource));

        PendingRequest pendingRequest = transport.sendRequest(request, listener);
        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            LOG.debug("Started observing resource: /{}/{}/{}",
                    resource.getInstance().getObject().getId(),
                    resource.getInstance().getId(),
                    resource.getId());
        }
    }

    @Override // TODO token
    public void cancelObservation(ObjectBaseProxy object) {
        String topic = transport.getRequestTopic(MessageType.I_CANCEL_OBSERVATION, object);
        Lwm2mRequest request = Lwm2mRequest.of(topic);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            LOG.debug("Stopped observing object: /{}", object.getId());
        }
    }

    @Override
    public void cancelObservation(ObjectInstanceProxy instance) {
        String topic = transport.getRequestTopic(MessageType.I_CANCEL_OBSERVATION, instance);
        Lwm2mRequest request = Lwm2mRequest.of(topic);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            LOG.debug("Stopped observing instance: /{}/{}", instance.getObject().getId(), instance.getId());
        }
    }

    @Override
    public void cancelObservation(ObjectResourceProxy resource) {
        String topic = transport.getRequestTopic(MessageType.I_CANCEL_OBSERVATION, resource);
        Lwm2mRequest request = Lwm2mRequest.of(topic);

        PendingRequest pendingRequest = transport.sendRequest(request);
        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            LOG.debug("Stopped observing instance: /{}/{}/{}",
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
