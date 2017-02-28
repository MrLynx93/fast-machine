package com.test.performance.coap.client;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.CoapClientConf;

import java.io.IOException;

public class CoapDtlsTestClient extends AbstractCoapTestClient {

    public static void main(String[] args) throws InterruptedException, IOException {
        new CoapDtlsTestClient().test();
    }

    @Override
    Client configureClient(int i) {
        CoapClientConf configuration = new CoapClientConf();
        configuration.setPort(2900 + i);
        configuration.setDtls(true);
        configuration.setKeyStorePassword("123456");
        configuration.setKeyStoreLocation("keyStore.jks");
        configuration.setTrustStorePassword("123456");
        configuration.setTrustStoreLocation("trustStore.jks");
        return new Client("client_" + (2900 + i), factoryBootstrap(i), configuration);
    }
}
