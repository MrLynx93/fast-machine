package performance;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.internal.transport.coap.CoapConfiguration;
import util.model.PingInstanceProxy;

public class CoapTest extends BaseTest {

    public static void main(String[] args) throws InterruptedException {
        CoapTest coapTest = new CoapTest();
        coapTest.doTest();
    }

    @Override
    public Server configureServer(int number) { // todo name
        Server server = new Server();

        ServerConfiguration configuration = new ServerConfiguration();
        configuration.addObjectSupport(TestInstanceProxy.class);
        configuration.addObjectSupport(PingInstanceProxy.class);
        server.setConfiguration(configuration);

        CoapConfiguration transportConfiguration = new CoapConfiguration();
        transportConfiguration.setPort(1900 + number);

        configuration.setTransport(ServerConfiguration.TRASPORT_COAP);
        return server;
    }
}
