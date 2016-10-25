package com.agh.fastmachine.server.internal.transport.mqtt.message;

import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.mqtt.MQTT;
import com.agh.fastmachine.server.internal.transport.mqtt.RegisterHeader;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.ByteBuffer;
import java.util.Arrays;

@Data
public class Lwm2mMqttRegisterRequest extends Lwm2mMqttRequest {
    private RegisterHeader header;

    public Lwm2mMqttRegisterRequest(MQTT.Topic topic, LWM2M.ContentType contentType, RegisterHeader header, byte[] payload) {
        super(topic, contentType, payload, null);
        this.header = header;
    }

    public static Lwm2mMqttRegisterRequest fromMqtt(MqttMessage mqttMessage, MQTT.Topic topic) {
        String fullPayload = new String(Arrays.copyOfRange(mqttMessage.getPayload(), 1, mqttMessage.getPayload().length));
        String[] elems = fullPayload.split("\\?");

        return new Lwm2mMqttRegisterRequest(
                topic,
                MQTT.parseContentTypeFromCode(mqttMessage.getPayload()[0]),
                RegisterHeader.fromString(elems[0]),
                elems[0].getBytes()
        );
    }

    @Override
    public byte[] toBytes() {
        byte[] header = this.header.toBytes();

        if (payload == null || payload.length == 0) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(header.length + 2);
            byteBuffer.put((byte) MQTT.getContentTypeCode(contentType));
            byteBuffer.put((byte) '?');
            return byteBuffer.array();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(header.length + payload.length + 2);
        byteBuffer.put((byte) MQTT.getContentTypeCode(contentType));
        byteBuffer.put(header);
        byteBuffer.put((byte) '?');
        return byteBuffer.array();
    }
}