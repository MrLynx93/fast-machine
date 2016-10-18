package com.agh.fastmachine.server.internal.transport.mqtt;

import com.agh.fastmachine.server.internal.transport.mqtt.message.MqttTopic;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.mqtt.message.ResponseCode;
import lombok.Data;

import java.nio.ByteBuffer;
import java.util.Arrays;

@Data
public class Lwm2mMqttResponse extends Lwm2mResponse {
    private MqttTopic topic;

    private Lwm2mMqttResponse(MqttTopic topic, ContentType contentType, Boolean observeFlag, ResponseCode responseCode, byte[] payload) {
        super(contentType, observeFlag, responseCode, payload);
        this.topic = topic;
    }

    public static Lwm2mMqttResponse parse(String topic, byte[] bytes) {
        return new Lwm2mMqttResponse(
                MqttTopic.fromString(topic),
                ContentType.fromMqtt(bytes[0]),
                (bytes[0] & 0b10000000) > 0,
                ResponseCode.from(bytes[1]),
                Arrays.copyOfRange(bytes, 2, bytes.length));
    }

    @Override
    public int getCreatedInstanceId() {
        return topic.getInstanceId();
    }

    public String getToken() {
        return topic.getToken();
    }

    public byte[] toBytes() {
        if (payload == null) {
            payload = new byte[0];
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(payload.length + 2);
        byteBuffer.put(getFirstByte());
        byteBuffer.put(responseCode.toByte());
        byteBuffer.put(payload);
        return byteBuffer.array();
    }

    private byte getFirstByte() {
        byte firstByte = (byte) contentType.getMqttKey();
        if (observeFlag) {
            firstByte &= 0b10000000;
        }
        return firstByte;
    }
}
