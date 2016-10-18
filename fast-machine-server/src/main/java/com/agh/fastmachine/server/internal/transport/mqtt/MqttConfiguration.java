package com.agh.fastmachine.server.internal.transport.mqtt;

import com.agh.fastmachine.server.internal.transport.TransportConfiguration;
import lombok.Data;

@Data
public class MqttConfiguration extends TransportConfiguration {
    private String brokerAddress;
    private String serverId;
    private int qos;
}
