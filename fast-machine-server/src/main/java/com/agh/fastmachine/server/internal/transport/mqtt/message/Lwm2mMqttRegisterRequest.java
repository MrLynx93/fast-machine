package com.agh.fastmachine.server.internal.transport.mqtt.message;

import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import lombok.Data;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.agh.fastmachine.server.internal.transport.mqtt.message.ContentType.NO_FORMAT;

@Data
public class Lwm2mMqttRegisterRequest extends Lwm2mRequest {
    private RegisterHeader header;

    private Lwm2mMqttRegisterRequest(MqttTopic topic, ContentType contentType, RegisterHeader header, byte[] payload) {
        super(topic, contentType, payload);
        this.header = header;
    }

    public static Lwm2mMqttRegisterRequest of(String topic, RegisterHeader header, byte[] payload) {
        return new Lwm2mMqttRegisterRequest(MqttTopic.fromString(topic), NO_FORMAT, header, payload);
    }

    public static Lwm2mMqttRegisterRequest of(String topic, RegisterHeader header) {
        return new Lwm2mMqttRegisterRequest(MqttTopic.fromString(topic), NO_FORMAT, header, null);
    }

    public static Lwm2mMqttRegisterRequest parse(String topic, byte[] bytes) {
        String[] headerAndPayload = Arrays.toString(Arrays.copyOfRange(bytes, 1, bytes.length)).split("\\?", 2);
        return new Lwm2mMqttRegisterRequest(
                MqttTopic.fromString(topic),
                ContentType.from(bytes[0]),
                RegisterHeader.fromString(headerAndPayload[0]),
                headerAndPayload[1].getBytes()
        );
    }

    @Override
    public byte[] toBytes() {
        if (payload == null) {
            payload = new byte[0];
        }
        byte[] header = this.header.toBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(payload.length + header.length + 1);
        byteBuffer.put((byte) contentType.getValue());
        byteBuffer.put(header);
        byteBuffer.put(payload);
        return byteBuffer.array();
    }
}