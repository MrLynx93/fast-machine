package com.agh.fastmachine.client.internal.service.registration;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectInstance;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class RegistrationInterface {
    private static final Logger LOG = LoggerFactory.getLogger(RegistrationInterface.class);

    public static final int DEFAULT_LIFETIME = 86400;
    public static final String DEFAULT_BINDING_MODE = "U";
    private Map<Integer, ServerInfo> serverInfoMap = new HashMap<>();
    private UpdateInterface updateInterface;
    private Client client;

    public RegistrationInterface(Client client) {
        this.client = client;
        updateInterface = new UpdateInterface(serverInfoMap, client);
    }

    public void registerIntoServer(ServerObjectInstance server) {
        SecurityObjectInstance security = client.getServerSecurityMap().get(server.shortServerId.getValue().value);
        Request registerRequest = new Request(CoAP.Code.POST);
        registerRequest.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_LINK_FORMAT);
        registerRequest.setPayload(createRegisterPayload().getBytes(Charset.forName("UTF-8")));
        registerRequest.setURI(createRegisterUri(server, security));
        registerRequest.send(client.getEndpoint());

        InetSocketAddress clientAddress = new InetSocketAddress(registerRequest.getDestination(), registerRequest.getDestinationPort());
        client.getServerObjectResolver().addServerObjectInstance(clientAddress, server);
        client.getObjectInitializer().initializeWriteAttributes(server.shortServerId.getValue().value);
//        client.getAccessRightsVisitor().addServerObjectInstance(server.shortServerId.getValue().value, server);

        try {
            Response response = registerRequest.waitForResponse();
            if (response == null) {
                LOG.error("Did not receive response while registering to server: {}", security.serverUri.getValue().value);
                throw new RuntimeException("Didn't get response while registering");
            }
            if (response.getCode() != CoAP.ResponseCode.CREATED) {
                LOG.error("Failed to register to server: {}. ResponseCode: {}", security.serverUri.getValue().value, response.getCode());
                throw new RuntimeException("Failed to register. ResponseCode: " + response.getCode());
            }
            String registrationLocation = createRegisterUpdateUri(security, response);
            Integer lifetime = server.lifetime.getValue().value;

            ServerInfo serverInfo = new ServerInfo(registrationLocation, lifetime);
            serverInfoMap.put(server.shortServerId.getValue().value, serverInfo);
            updateInterface.startUpdating(server);
            LOG.debug("Successfully registered into server {}", security.serverUri.getValue().value);
        } catch (InterruptedException ignored) {
        }
    }

    public void deregister(ServerObjectInstance server) {
        Request deregisterRequest = new Request(CoAP.Code.DELETE);
        String registerEndpoint = serverInfoMap.get(server.shortServerId.getValue().value).serverRegisterEndpoint;

        deregisterRequest.setURI(registerEndpoint);
        deregisterRequest.send(client.getEndpoint());
        try {
            Response response = deregisterRequest.waitForResponse(10000);
            if(response == null ) {
                LOG.error("Did not receive response while deregistering from server: {}", registerEndpoint);
                throw new RuntimeException("Didn't get response while deregistering");
            }
            if (response.getCode() != CoAP.ResponseCode.DELETED) {
                LOG.error("Failed to register to server: {}. ResponseCode: {}", registerEndpoint, response.getCode());
                throw new RuntimeException("Failed to deregister. ResponseCode: " + response.getCode());
            }
            updateInterface.stopUpdating(server);
            LOG.debug("Successfully deregistered from server {}", registerEndpoint);
        } catch (InterruptedException ignored) {
        }
    }



    private String createRegisterUpdateUri(SecurityObjectInstance security, Response response) {
        String serverUri = security.serverUri.getValue().value;
        String registerLocationPath = response.getOptions().getLocationPathString();

        StringBuilder uri = new StringBuilder();
        uri.append(serverUri);
        if (!serverUri.endsWith("/")) {
            uri.append("/");
        }
        uri.append(registerLocationPath);
        return uri.toString();
    }

    private String createRegisterUri(ServerObjectInstance server, SecurityObjectInstance security) {
        StringBuilder uri = new StringBuilder();
        uri.append(security.serverUri.getValue().value);
        if (!security.serverUri.getValue().value.endsWith("/")) {
            uri.append("/");
        }
        uri.append("rd?ep=").append(client.getEndpointClientName());

        IntegerResourceValue serverLifetime = server.lifetime.getValue();
        if (serverLifetime != null && serverLifetime.value != null && serverLifetime.value != DEFAULT_LIFETIME) {
            uri.append("&lt=").append(serverLifetime.value);
        }
        if (server.binding.getValue() != null && !DEFAULT_BINDING_MODE.equals(server.binding.getValue().value)) {
            uri.append("&b=").append(server.binding.getValue().value);
        }
        return uri.toString();
    }

    private String createRegisterPayload() {
        StringBuilder payload = new StringBuilder();
        String rootPath = client.getRootPath() + "/";

        if (!client.getRootPath().isEmpty()) {
            payload.append("<").append(client.getRootPath()).append(">;rt=\"oma.lwm2m\"");
        }
        //TODO after adding json support, add that information to schema string, page 21
        for (ObjectBase<?> objectBase : client.getObjectBaseMap().values()) {
            if (objectBase.getObjectInstances().isEmpty()) {
                payload
                        .append(",<")
                        .append(rootPath)
                        .append(objectBase.getId())
                        .append(">");
            } else {
                for (int instanceId : objectBase.getObjectInstances().keySet()) {
                    payload.append(",<")
                            .append(rootPath)
                            .append(objectBase.getId())
                            .append("/")
                            .append(instanceId).append(">");
                }
            }
        }
        if (payload.charAt(0) == ',') {
            payload.deleteCharAt(0);
        }
        return payload.toString();
    }

}


