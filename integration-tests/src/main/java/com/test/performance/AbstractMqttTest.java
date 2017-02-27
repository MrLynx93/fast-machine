package com.test.performance;

import com.agh.fastmachine.core.api.model.resourcevalue.BooleanResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.builtin.AccessControlObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.builtin.SecurityObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.builtin.ServerObjectInstanceProxy;
import com.agh.fastmachine.server.bootstrap.BootstrapSequence;
import com.agh.fastmachine.server.bootstrap.BootstrapServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractMqttTest extends AbstractTest {
    public static final int CREATE = 1 << 4;
    public static final int READ = 1;
    public static final int WRITE = 1 << 1;
    public static final int EXECUTE = 1 << 2;
    public static final int DELETE = 1 << 3;

    public void test() throws InterruptedException {
        List<Server> servers = new ArrayList<>();
        for (int i = 0; i < SERVERS_NUMBER; i++) {
            Server server = configureServer(i + 1);
            prepareTest(server);
            servers.add(server);
        }
        BootstrapServer bootstrapServer = configureBootstrapServer(servers);

        bootstrapServer.start();
        servers.forEach(Server::start);

        System.out.println("You should now run all clients.");
        deregisteredCount.await();
        servers.forEach(server -> server.getStats().logStats());
        servers.forEach(Server::stop);
        System.exit(0);
    }

    abstract BootstrapServer configureBootstrapServer(List<Server> servers);

    abstract Server configureServer(int number);

    BootstrapSequence configureBootstrapSequence(List<Server> servers) {
        BootstrapSequence sequence = new BootstrapSequence()
                .writeInstance(bootstrapSecurityInstance())
                .writeObject(accessControlObject());

        for (Server server : servers) {
            sequence.writeInstance(securityInstance(server));
            sequence.writeInstance(serverInstance(server));
        }
        return sequence.finish();
    }

    ObjectBaseProxy accessControlObject() {
        Map<Integer, AccessControlObjectInstanceProxy> instances = new HashMap<>();

        // AC for object
        AccessControlObjectInstanceProxy testObjectAccess = new AccessControlObjectInstanceProxy(0);
        testObjectAccess.objectId.setValue(new IntegerResourceValue(20004));
        testObjectAccess.instanceId.setValue(new IntegerResourceValue(66535));
        testObjectAccess.accessControlOwner.setValue(new IntegerResourceValue(66535));
        testObjectAccess.accessControlList.setValue(new IntegerResourceValue(CREATE), 0);
        instances.put(0, testObjectAccess);

        // AC for general instance - only read allowed for everyone
        AccessControlObjectInstanceProxy testGeneralInstanceAccess = new AccessControlObjectInstanceProxy(1);
        testGeneralInstanceAccess.objectId.setValue(new IntegerResourceValue(20004));
        testGeneralInstanceAccess.instanceId.setValue(new IntegerResourceValue(0));
        testGeneralInstanceAccess.accessControlOwner.setValue(new IntegerResourceValue(66535));
        testGeneralInstanceAccess.accessControlList.setValue(new IntegerResourceValue(READ), 0);
        instances.put(1, testGeneralInstanceAccess);

        return new ObjectBaseProxy(2, instances);
    }

    SecurityObjectInstanceProxy bootstrapSecurityInstance() {
        SecurityObjectInstanceProxy inst = new SecurityObjectInstanceProxy(0);
        inst.serverUri.setValue(new StringResourceValue("bootstrap-server"));
        inst.bootstrapServer.setValue(new BooleanResourceValue(true));
        inst.shortServerId.setValue(new IntegerResourceValue(0));
        inst.clientHoldOffTime.setValue(new IntegerResourceValue(60));
        return inst;
    }

    SecurityObjectInstanceProxy securityInstance(Server server) {
        int shortServerId = Integer.valueOf(server.getName().split("_")[1]);

        SecurityObjectInstanceProxy inst = new SecurityObjectInstanceProxy(shortServerId);
        inst.serverUri.setValue(new StringResourceValue(server.getName()));
        inst.bootstrapServer.setValue(new BooleanResourceValue(false));
        inst.shortServerId.setValue(new IntegerResourceValue(shortServerId));
        return inst;
    }

    ServerObjectInstanceProxy serverInstance(Server server) {
        int shortServerId = Integer.valueOf(server.getName().split("_")[1]);

        ServerObjectInstanceProxy inst = new ServerObjectInstanceProxy(shortServerId);
        inst.shortServerId.setValue(new IntegerResourceValue(shortServerId));
        inst.storeNotifications.setValue(new BooleanResourceValue(false));
        inst.lifetime.setValue(new IntegerResourceValue(LIFETIME));
        inst.binding.setValue(new StringResourceValue("T"));
        return inst;
    }
}
