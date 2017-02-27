package com.test.performance.coap.client;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.CoapClientConf;

public class CoapTestClient extends AbstractCoapTestClient {

    public static void main(String[] args) throws InterruptedException {
        new CoapTestClient().test();
    }

    @Override
    Client configureClient(int i) {
        CoapClientConf configuration = new CoapClientConf();
        configuration.setPort(29000 + i);
        configuration.setDtls(false);
        return new Client("client_" + (29000 + i), factoryBootstrap(i), configuration);
    }
}
