package basic;

import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.coap.CoapConfiguration;
import com.agh.fastmachine.server.internal.transport.stats.TimeoutException;
import com.test.model.test.TestInstanceProxy;
import util.model.AndroidUtilsInstanceProxy;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartCoapServer {
    private static final boolean TLS = false;
    private static final String SERVER_NAME = "main_server_coap";
    private static final String SERVER_IP = "34.252.159.36";
    private static final int LIFETIME = 3600;
    private static final long SLEEPTIME = 30;

    private static ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * This function is to experiment
     *
     */
    private static void handleClient(ClientProxyImpl client) {
        for (int i = 0; i < 100; i++) {
            ObjectBaseProxy<AndroidUtilsInstanceProxy> exampleObj = client.getObjectTree().getObjectForType(AndroidUtilsInstanceProxy.class);
            AndroidUtilsInstanceProxy instance = exampleObj.getInstance(0);

            try {
                instance.batteryLevel.read();
                String formattedDate = DateFormat.getDateTimeInstance().format(new Date());
                System.out.println("Last read sent: " + formattedDate);

            } catch (TimeoutException e) {
                System.out.println("Cos sie popsulo");
                e.printStackTrace();
            }

            sleepForMills();

            System.out.println(i);
        }
    }

    public static void main(String[] args) {
        Server server = configureServer(false);
        server.setRegistrationListener(registrationListener);
        server.start();
        System.out.println("You should now run all clients.");
    }

    public static Server configureServer(boolean dtls) {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setTransport(ServerConfiguration.TRASPORT_COAP);
        configuration.setName(SERVER_NAME);
        configuration.addObjectSupport(TestInstanceProxy.class);

        CoapConfiguration transportConfiguration = new CoapConfiguration();
        transportConfiguration.setPort(19001);
        transportConfiguration.setDtls(false);
        return new Server(configuration, transportConfiguration);
    }

    private static RegistrationListener registrationListener = new RegistrationListener() {
        @Override
        public void onRegister(ClientProxy client) {
            System.out.println("Registered client " + client.getClientEndpointName());
            executor.submit(() -> handleClient((ClientProxyImpl) client));
        }

        @Override
        public void onDeregister(ClientProxy client) {
            System.out.println("Deregistered client " + client.getClientEndpointName());
            // TODO deregister
        }

        @Override
        public void onUpdate(ClientProxy client) {
            String formattedDate = DateFormat.getDateTimeInstance().format(new Date());
            System.out.println("Last update received " + formattedDate);
        }
    };

    private static void sleepForMills() {
        try {
            Thread.sleep(SLEEPTIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
