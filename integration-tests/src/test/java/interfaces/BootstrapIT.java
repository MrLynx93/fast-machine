package interfaces;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.fail;


public class BootstrapIT {
    public static final String CLIENT_URL = "coap://127.0.0.1:5444";
    private static final String ENDPOINT_CLIENT_NAME = "132:123:123";

    @Ignore
    @Test
    public void shouldSupportClientInitiatedBootstrap() throws Exception {
        fail();
    }

    @Ignore
    @Test
    public void shouldSupportServerInitiatedBootstrap() throws Exception {
        fail();
    }
}
