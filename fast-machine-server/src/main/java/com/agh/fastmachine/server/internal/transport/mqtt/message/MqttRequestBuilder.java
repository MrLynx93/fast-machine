package com.agh.fastmachine.server.internal.transport.mqtt.message;

import com.agh.fastmachine.core.internal.parser.WriteAttributesParser;
import com.agh.fastmachine.core.internal.parser.WriteParser;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectNodeProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.RequestBuilder;
import com.agh.fastmachine.server.internal.transport.mqtt.MQTT;

import static com.agh.fastmachine.server.internal.transport.LWM2M.Operation.*;

public class MqttRequestBuilder extends RequestBuilder<Lwm2mMqttRequest> {

    public MqttRequestBuilder(Server server) {
        super(server);
    }

    @Override
    public Lwm2mMqttRequest buildCreateRequest(ObjectInstanceProxy instance, int id) {
        MQTT.Topic topic = createTopic(instance, M_CREATE);
        topic.setPath(LWM2M.Path.of(instance.getObjectId(), id));

        return new Lwm2mMqttRequest(
                topic,
                LWM2M.ContentType.TLV,
                WriteParser.serialize(instance)
        );
    }

    @Override
    public Lwm2mMqttRequest buildCreateRequest(ObjectInstanceProxy instance) { // TODO insert ClientProxy (and id) into instance
        MQTT.Topic topic = createTopic(instance, M_CREATE);
        topic.setPath(LWM2M.Path.of(instance.getObjectId()));

        return new Lwm2mMqttRequest(
                topic,
                LWM2M.ContentType.TLV,
                WriteParser.serialize(instance)
        );
    }

    @Override
    public Lwm2mMqttRequest buildDeleteRequest(ObjectInstanceProxy instance) {
        return new Lwm2mMqttRequest(
                createTopic(instance, M_DELETE),
                LWM2M.ContentType.NO_FORMAT
        );
    }

    @Override
    public Lwm2mMqttRequest buildDiscoverRequest(ObjectNodeProxy node) {
        return new Lwm2mMqttRequest(
                createTopic(node, M_DISCOVER),
                LWM2M.ContentType.LINK_FORMAT
        );
    }

    @Override
    public Lwm2mMqttRequest buildExecuteRequest(ObjectResourceProxy<?> resource, String arguments) {
        return new Lwm2mMqttRequest(
                createTopic(resource, M_EXECUTE),
                LWM2M.ContentType.PLAIN_TEXT, // TODO argument should be String!!!
                arguments.getBytes()
        );
    }

    @Override
    public Lwm2mMqttRequest buildObserveRequest(ObjectNodeProxy<?> node) {
        return new Lwm2mMqttRequest(
                createTopic(node, I_OBSERVE),
                LWM2M.ContentType.NO_FORMAT // TODO is that correct? Response for observe is empty?
        );
    }

    @Override
    public Lwm2mMqttRequest buildCancelObserveRequest(ObjectNodeProxy<?> node) {
        return new Lwm2mMqttRequest(
                createTopic(node, I_CANCEL_OBSERVATION),
                LWM2M.ContentType.NO_FORMAT
        );
    }

    @Override
    public Lwm2mMqttRequest buildReadRequest(ObjectBaseProxy<?> object) {
        return new Lwm2mMqttRequest(
                createTopic(object, M_READ),
                LWM2M.ContentType.TLV
        );
    }

    @Override
    public Lwm2mMqttRequest buildReadRequest(ObjectInstanceProxy instance) {
        return new Lwm2mMqttRequest(
                createTopic(instance, M_READ),
                LWM2M.ContentType.TLV
        );
    }

    @Override
    public Lwm2mMqttRequest buildReadRequest(ObjectResourceProxy<?> resource) {
        return new Lwm2mMqttRequest(
                createTopic(resource, M_READ),
                getResourceContentType(resource)
        );
    }

    @Override
    public Lwm2mMqttRequest buildWriteAttributesRequest(ObjectNodeProxy<?> node) {
        String string = node.getAttributes().buildStringWithDelimiter("&").toString();
        if (string.startsWith("&")) {
            string = string.substring(1);
        }

        return new Lwm2mMqttRequest(
                createTopic(node, M_WRITE_ATTRIBUTE),
                LWM2M.ContentType.LINK_FORMAT,
                string.getBytes()
        );
    }

    @Override
    public Lwm2mMqttRequest buildWriteRequest(ObjectInstanceProxy instance) {
        return new Lwm2mMqttRequest(
                createTopic(instance, M_WRITE),
                LWM2M.ContentType.TLV,
                WriteParser.serialize(instance)
        );
    }

    @Override
    public Lwm2mMqttRequest buildWriteRequest(ObjectResourceProxy resource) {
        return new Lwm2mMqttRequest(
                createTopic(resource, M_WRITE),
                getResourceFormat(resource),
                WriteParser.serialize(resource)
        );
    }

    private MQTT.Topic createTopic(ObjectNodeProxy node, LWM2M.Operation operation) {
        return new MQTT.Topic(
                operation,
                "req",
                generateToken(),
                node.getClientProxy() == null ? "%" : node.getClientProxy().getClientEndpointName(),
                server.getName(),
                node.getPath()
        );
    }

}
