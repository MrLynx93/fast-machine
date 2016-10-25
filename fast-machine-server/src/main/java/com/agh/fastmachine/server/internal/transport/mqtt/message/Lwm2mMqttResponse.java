package com.agh.fastmachine.server.internal.transport.mqtt.message;

import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.mqtt.MQTT;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.ByteBuffer;
import java.util.Arrays;

@Data
public class Lwm2mMqttResponse extends Lwm2mResponse {
    private MQTT.Topic topic;

    private Lwm2mMqttResponse(MQTT.Topic topic, LWM2M.ContentType contentType, LWM2M.ResponseCode responseCode, byte[] payload) {
        super(contentType, responseCode, payload);
        this.topic = topic;
    }

    public Lwm2mMqttResponse(MQTT.Topic topic, LWM2M.ContentType contentType, LWM2M.ResponseCode responseCode) {
        this(topic, contentType, responseCode, null); // todo responsebuilder
    }

    public static Lwm2mMqttResponse fromMqtt(MqttMessage message, MQTT.Topic topic) {
        byte[] payload = Arrays.copyOfRange(message.getPayload(), 2, message.getPayload().length);
        LWM2M.ContentType contentType = MQTT.parseContentTypeFromCode(message.getPayload()[0]);
        LWM2M.ResponseCode responseCode = MQTT.parseResponseCodeFromByte(message.getPayload()[1]);
        return new Lwm2mMqttResponse(
                topic,
                contentType,
                responseCode,
                payload
        );
    }

    @Override
    public Integer getCreatedInstanceId() {
        return topic.getPath().getInstanceId();
    }

    public String getToken() {
        return topic.getToken();
    }

    public MqttMessage toMqttMessage() {
        return new MqttMessage(toBytes());
    }

    public byte[] toBytes() {
        if (payload == null || payload.length == 0) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(2);
            byteBuffer.put((byte) MQTT.getContentTypeCode(contentType));
            byteBuffer.put(MQTT.getResponseCodeByte(responseCode));
            return byteBuffer.array();
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(payload.length + 2);
        byteBuffer.put((byte) MQTT.getContentTypeCode(contentType));
        byteBuffer.put(MQTT.getResponseCodeByte(responseCode));
        byteBuffer.put(payload);
        return byteBuffer.array();
    }

}
