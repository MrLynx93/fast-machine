package com.agh.fastmachine.server.internal.transport.mqtt;

import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import lombok.Data;

import java.nio.ByteBuffer;
import java.util.Arrays;


@Data
public class Lwm2mMqttRequest extends Lwm2mRequest {
    private MqttTopic topic;

    private Lwm2mMqttRequest(MqttTopic topic, ContentType contentType, byte[] payload) {
        super(contentType, payload);
        this.topic = topic;
    }

    public static Lwm2mMqttRequest of(String topic, ContentType contentType, byte[] payload) {
        return new Lwm2mMqttRequest(MqttTopic.fromString(topic), contentType, payload);
    }

    public static Lwm2mMqttRequest of(String topic, ContentType contentType) {
        return new Lwm2mMqttRequest(MqttTopic.fromString(topic), contentType, null);
    }

    public static Lwm2mMqttRequest of(String topic) {
        return new Lwm2mMqttRequest(MqttTopic.fromString(topic), NO_FORMAT, null);
    }

    public static Lwm2mMqttRequest parse(String topic, byte[] bytes) {
        return new Lwm2mMqttRequest(
                MqttTopic.fromString(topic),
                ContentType.fromMqtt(bytes[0]),
                Arrays.copyOfRange(bytes, 1, bytes.length));
    }

    @Override
    public String getToken() {
        return topic.getToken();
    }

    @Override
    public MessageType getType() {
        return topic.getMessageType();
    }

    public byte[] toBytes() {
        if (payload == null) {
            payload = new byte[0];
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(payload.length + 1);
        byteBuffer.put((byte) contentType.getMqttKey());
        byteBuffer.put(payload);
        return byteBuffer.array();
    }
}
