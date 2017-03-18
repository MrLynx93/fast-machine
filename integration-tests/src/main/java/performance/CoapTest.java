package performance;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.CoapClientConf;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.internal.transport.coap.CoapConfiguration;
import performance.model.TestInstanceProxy;
import util.model.PingInstanceProxy;

import java.util.List;

public class CoapTest extends AbstractCoapTest {

    public static void main(String[] args) throws InterruptedException {
        new CoapTest().test();
    }

    Client configureClient(int i, List<Server> servers) {
        CoapClientConf configuration = new CoapClientConf();
        configuration.setPort(29000 + i);
        configuration.setDtls(false);
        return new Client("client_" + (29000 + i), factoryBootstrap(i, servers), configuration);
    }

    Server configureServer(int number) {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setTransport(ServerConfiguration.TRASPORT_COAP);
        configuration.setName("server_" + (19000 + number));
        configuration.addObjectSupport(TestInstanceProxy.class);
        configuration.addObjectSupport(PingInstanceProxy.class);

        CoapConfiguration transportConfiguration = new CoapConfiguration();
        transportConfiguration.setPort(19000 + number);
        transportConfiguration.setDtls(false);
        return new Server(configuration, transportConfiguration);
    }
//host 127.0.0.1 and (port 19001 or port 19002 or port 19003 or port 19004 or port 19005)
}
