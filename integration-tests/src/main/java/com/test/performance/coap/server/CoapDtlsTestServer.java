package com.test.performance.coap.server;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.internal.transport.coap.CoapConfiguration;
import com.test.performance.model.TestInstanceProxy;
import com.test.util.model.PingInstanceProxy;

public class CoapDtlsTestServer extends AbstractCoapTestServer {

    public static void main(String[] args) throws InterruptedException {
        new CoapDtlsTestServer().test();
    }

    @Override
    Server configureServer(int i) {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setTransport(ServerConfiguration.TRASPORT_COAP);
        configuration.setName("server_" + (1900 + i));
        configuration.addObjectSupport(TestInstanceProxy.class);
        configuration.addObjectSupport(PingInstanceProxy.class);

        CoapConfiguration transportConfiguration = new CoapConfiguration();
        transportConfiguration.setPort(1900 + i);
        transportConfiguration.setDtls(true);
        transportConfiguration.setKeyStorePassword("123456");
        transportConfiguration.setKeyStoreLocation("keyStore.jks");
        transportConfiguration.setTrustStorePassword("123456");
        transportConfiguration.setTrustStoreLocation("trustStore.jks");
        return new Server(configuration, transportConfiguration);
    }

}
