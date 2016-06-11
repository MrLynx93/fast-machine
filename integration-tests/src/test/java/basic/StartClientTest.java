package basic;

import static util.TestUtil.startExampleClient;

public class StartClientTest {
    public static final String LESHAN_SERVER_URL = "coap://leshan.eclipse.org/";

    public static void main(String[] args) {
        startExampleClient("endpoint_client_name_fast_machine", LESHAN_SERVER_URL);
    }
}