package com.agh.fastmachine.server.internal.transport.mqtt;

import com.agh.fastmachine.server.internal.transport.TransportConfiguration;
import lombok.Data;

@Data
public class MqttConfiguration extends TransportConfiguration {
    private String brokerAddress;
    private String serverName;
    private int qos;
    private boolean dtls;
    private String keyStorePassword;
    private String keyStoreLocation;
    private String trustStorePassword;
    private String trustStoreLocation;
}
