package com.test.performance.coap.server;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.internal.transport.coap.CoapConfiguration;
import com.test.performance.model.TestInstanceProxy;
import com.test.util.model.PingInstanceProxy;

public class CoapTestServer extends AbstractCoapTestServer {

    public static void main(String[] args) throws InterruptedException {
        new CoapTestServer().test();
    }

    @Override
    Server configureServer(int i) {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setTransport(ServerConfiguration.TRASPORT_COAP);
        configuration.setName("server_" + (19000 + i));
        configuration.addObjectSupport(TestInstanceProxy.class);
        configuration.addObjectSupport(PingInstanceProxy.class);

        CoapConfiguration transportConfiguration = new CoapConfiguration();
        transportConfiguration.setPort(19000 + i);
        transportConfiguration.setDtls(false);
        return new Server(configuration, transportConfiguration);
    }
}
