package performance;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.CoapClientConf;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.internal.transport.coap.CoapConfiguration;
import performance.model.TestInstanceProxy;
import util.model.PingInstanceProxy;

import java.util.List;

public class CoapDtlsTest extends AbstractCoapTest {

    public static void main(String[] args) throws InterruptedException {
        new CoapDtlsTest().test();
    }

    @Override
    Client configureClient(int i, List<Server> servers) {
        CoapClientConf configuration = new CoapClientConf();
        configuration.setPort(2900 + i);
        configuration.setDtls(true);
        configuration.setKeyStorePassword("123456");
        configuration.setKeyStoreLocation("keyStore.jks");
        configuration.setTrustStorePassword("123456");
        configuration.setTrustStoreLocation("trustStore.jks");
        return new Client("client_" + (2900 + i), factoryBootstrap(i, servers), configuration);
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
