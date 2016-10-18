package com.agh.fastmachine.server.internal.transport.coap;

import com.agh.fastmachine.server.internal.transport.TransportConfiguration;

public class CoapConfiguration extends TransportConfiguration {
    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
