package com.test.client;

import com.agh.fastmachine.client.api.Client;
import com.test.Tests;
import com.test.Utils;

import java.util.concurrent.CountDownLatch;

public class CoapStartTestClient {
    private static final CountDownLatch operationsCount = new CountDownLatch(Tests.ITERATIONS);
    private static boolean dtls;
    private static int clientIdx;

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 1) {
            return;
        }
        clientIdx = Integer.parseInt(args[0]);
        dtls = args.length == 2 && args[1].equals("dtls");

        // Start client
        Client client = Utils.CoAP.configureClient(clientIdx, dtls, operationsCount);
        client.start();

        // Wait for all operations, then stop client
        operationsCount.await();
        client.stop();
        System.exit(0);
    }
}
