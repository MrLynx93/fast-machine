package com.agh.fastmachine.server.internal.transport.mqtt.message;

import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.mqtt.MQTT;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.ByteBuffer;
import java.util.Arrays;


@Data
public class Lwm2mMqttRequest extends Lwm2mRequest {
    private MQTT.Topic topic;

    Lwm2mMqttRequest(MQTT.Topic topic, LWM2M.ContentType contentType, byte[] payload, Boolean observeFlag) {
        super(contentType, observeFlag, payload);
        this.topic = topic;
    }

    public Lwm2mMqttRequest(MQTT.Topic topic, LWM2M.ContentType contentType, byte[] payload) {
        super(contentType, false, payload);
        this.topic = topic;
    }

    public Lwm2mMqttRequest(MQTT.Topic topic, LWM2M.ContentType contentType) {
        super(contentType, false, null);
        this.topic = topic;
    }

    // TODO This will be used in Bootstrap Request
    public static Lwm2mMqttRequest fromMqtt(MqttMessage mqttMessage, MQTT.Topic topic) {
        return new Lwm2mMqttRequest(
                topic,
                MQTT.parseContentTypeFromCode(mqttMessage.getPayload()[0]),
                Arrays.copyOfRange(mqttMessage.getPayload(), 1, mqttMessage.getPayload().length), (mqttMessage.getPayload()[0] & 0b10000000) > 0
        );
    }

    @Override
    public String getToken() {
        return topic.getToken();
    }

    @Override
    public LWM2M.Operation getOperation() {
        return topic.getOperation();
    }

    public MqttMessage toMqttMessage() {
        return new MqttMessage(toBytes());
    }

    public byte[] toBytes() {
        byte firstByte = (byte) MQTT.getContentTypeCode(contentType);
        if (observeFlag) {
            firstByte &= 0b10000000;
        }

        if (payload == null || payload.length == 0) {
            byte[] bytes = new byte[1];
            bytes[0] = firstByte;
            return bytes;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(payload.length + 1);
        byteBuffer.put(firstByte);
        byteBuffer.put(payload);
        return byteBuffer.array();
    }
}
