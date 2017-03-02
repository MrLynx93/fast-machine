package com.agh.fastmachine.server.internal.transport.mqtt;

import com.agh.fastmachine.server.internal.client.ClientManager;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.service.BootstrapService;
import com.agh.fastmachine.server.internal.service.RegistrationService;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationObjectInfo;
import com.agh.fastmachine.server.internal.transport.*;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttRegisterRequest;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttRequest;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttResponse;
import com.agh.fastmachine.server.internal.transport.mqtt.message.MqttRequestBuilder;
import com.agh.fastmachine.server.internal.transport.stats.Event;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MqttTransport extends Transport<MqttConfiguration, Lwm2mMqttRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(MqttTransport.class);
    private static final int TIMEOUT = 32000;

    private MqttClient mqttClient;
    private RegistrationService registrationService;
    private ClientManager clientManager;
    private BootstrapService bootstrapService;

    public MqttTransport() {
        requestBuilder = new MqttRequestBuilder();
    }

    @Override
    public void start(MqttConfiguration configuration) {
        try {
            this.configuration = configuration;
            this.registrationService = configuration.getServer().internal().getRegistrationService();
            this.bootstrapService = configuration.getServer().internal().getBootstrapService();
            this.clientManager = configuration.getServer().internal().getClientManager();
            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setCleanSession(true);

            if (configuration.isDtls()) {
                mqttClient = new MqttClient("ssl://" + configuration.getBrokerAddress(), configuration.getServer().getName());
                try {
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                    KeyStore keyStore = KeyStore.getInstance("JKS");
                    InputStream inKeyStore = getClass().getClassLoader().getResourceAsStream(configuration.getKeyStoreLocation());
                    keyStore.load(inKeyStore, configuration.getKeyStorePassword().toCharArray());

                    trustManagerFactory.init(keyStore);
                    sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
                    options.setSocketFactory(sslContext.getSocketFactory());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } else {
                mqttClient = new MqttClient("tcp://" + configuration.getBrokerAddress(), configuration.getServerName());
            }





            mqttClient.connect(options);
            mqttClient.setCallback(mqttCallback);
            mqttClient.setTimeToWait(5000);
            mqttClient.subscribe("lynx/br/req/+/+/" + configuration.getServerName(), configuration.getQos());
            mqttClient.subscribe("lynx/bw/res/+/+/" + configuration.getServerName() + "/#", configuration.getQos());
            mqttClient.subscribe("lynx/bd/res/+/+/" + configuration.getServerName(), configuration.getQos());
            mqttClient.subscribe("lynx/bd/res/+/+/" + configuration.getServerName() + "/#", configuration.getQos());
            mqttClient.subscribe("lynx/bf/res/+/+/" + configuration.getServerName(), configuration.getQos());

            mqttClient.subscribe("lynx/rr/req/+/+/" + configuration.getServerName(), configuration.getQos());
            mqttClient.subscribe("lynx/ru/req/+/+/" + configuration.getServerName(), configuration.getQos());
            mqttClient.subscribe("lynx/rd/req/+/+/" + configuration.getServerName(), configuration.getQos());

            mqttClient.subscribe("lynx/mr/res/+/+/" + configuration.getServerName() + "/#", configuration.getQos());
            mqttClient.subscribe("lynx/mw/res/+/+/" + configuration.getServerName() + "/#", configuration.getQos());
            mqttClient.subscribe("lynx/me/res/+/+/" + configuration.getServerName() + "/+/+/+", configuration.getQos());
            mqttClient.subscribe("lynx/mc/res/+/+/" + configuration.getServerName() + "/+/+", configuration.getQos());
            mqttClient.subscribe("lynx/md/res/+/+/" + configuration.getServerName() + "/+/+", configuration.getQos());
            mqttClient.subscribe("lynx/ma/res/+/+/" + configuration.getServerName() + "/#", configuration.getQos());
            mqttClient.subscribe("lynx/mm/res/+/+/" + configuration.getServerName() + "/#", configuration.getQos()); // TODO in doc md -> mm

            mqttClient.subscribe("lynx/io/res/+/+/" + configuration.getServerName() + "/#", configuration.getQos());
            mqttClient.subscribe("lynx/in/req/+/+/" + configuration.getServerName() + "/#", configuration.getQos());
            mqttClient.subscribe("lynx/ic/res/+/+/" + configuration.getServerName() + "/#", configuration.getQos());


        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doSendRequest(ClientProxyImpl client, Lwm2mMqttRequest request) throws Exception {
        doSendMessage("lynx/" + request.getTopic().toString(), request.toMqttMessage());
        stats.addEvent(client, Event.downlinkRequestSendSuccess(request.getOperation()));
        LOG.debug("Sent request {}", request.getTopic());
    }

    private void doSendResponse(ClientProxyImpl client, Lwm2mMqttResponse response) throws Exception {
        doSendMessage("lynx/" + response.getTopic().toString(), response.toMqttMessage());
        stats.addEvent(client, Event.uplinkResponseSendSuccess(response.getTopic().getOperation()));
        LOG.debug("Sent response {}", response.getTopic());
    }

    private void doSendMessage(String topic, MqttMessage message) throws MqttException {
        try {
            message.setQos(configuration.getQos());
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            if (configuration.getQos() != 0 || (configuration.getQos() == 0 && e.getReasonCode() != TIMEOUT)) {
                throw e;
            }
        }
    }

    @Override
    protected boolean isNotify(Lwm2mRequest request, Lwm2mResponse response) {
        return request.getOperation() == LWM2M.Operation.I_OBSERVE && response.isSuccess();
    }

    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topicString, MqttMessage message) throws Exception {
            MQTT.Topic topic = MQTT.Topic.fromString(topicString.replaceFirst("lynx/", ""));
            LOG.debug("Received " + topic);

            if (topic.getType().equals("req")) {
                switch (topic.getOperation()) {
                    case BS_REQ:
                        // TODO handleBootstrap(Lwm2mMqttRequest.fromMqtt(message));
                        break;
                    case R_REGISTER:
                        handleRegister(Lwm2mMqttRegisterRequest.fromMqtt(message, topic));
                        break;
                    case R_UPDATE:
                        handleUpdate(Lwm2mMqttRegisterRequest.fromMqtt(message, topic));
                        break;
                    case R_DEREGISTER:
                        handleDeregister(Lwm2mMqttRequest.fromMqtt(message, topic));
                        break;
                }
                // TODO count deregister
                ClientProxyImpl client = registeredClients.get(topic.getClientId());
                if (client != null) {
                    stats.addEvent(client, Event.uplinkRequestReceiveSuccess(topic.getOperation()));
                }
            }
            if (topic.getType().equals("res")) {
                handleResponse(Lwm2mMqttResponse.fromMqtt(message, topic));
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            // TODO maybe here?????
        }

        @Override
        public void connectionLost(Throwable throwable) {
            System.out.println("Connection lost.");
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }

    };

    private void handleRegister(Lwm2mMqttRegisterRequest request) {
        RegistrationInfo registrationInfo = parseRegistrationInfo(request);
        String endpointClientName = registrationInfo.endpointClientName;

        if (!registrationService.isClientRegistered(endpointClientName)) {
            ClientProxyImpl clientProxy = clientManager.createClient(endpointClientName);
            clientProxy.setServerId(configuration.getServerName());
            clientProxy.setRegisterTime(new Date());
            clientProxy.setLastUpdateTime(new Date());
            registrationService.registerClientProxy(clientProxy, registrationInfo);
            registeredClients.put(request.getTopic().getClientId(), clientProxy);

            Lwm2mMqttResponse response = createRegisterResponse(request);
            try {
                doSendResponse(clientProxy, response);
                registrationService.registerFinished(clientProxy);
            } catch (Exception e) {
                e.printStackTrace();
                stats.addEvent(clientProxy, Event.uplinkResponseSendTimeout(response.getTopic().getOperation()));
                LOG.error("Couldn't send response to {}", request);
            }
        }
    }

    private void handleUpdate(Lwm2mMqttRegisterRequest request) {
        ClientProxyImpl clientProxy = registeredClients.get(request.getTopic().getClientId());
        if (clientProxy == null) {
            System.out.println("clientId " + request.getTopic().getClientId());
        }
        clientProxy.setLastUpdateTime(new Date());
        RegistrationInfo updatedInfo = parseRegistrationInfo(request);
        registrationService.handleUpdateForClientProxy(clientProxy, updatedInfo);

        Lwm2mMqttResponse response = createUpdateResponse(request);
        try {
            doSendResponse(clientProxy, response);
            registrationService.updateFinished(clientProxy);
        } catch (Exception e) {
            stats.addEvent(clientProxy, Event.uplinkResponseSendTimeout(response.getTopic().getOperation()));
            LOG.error("Couldn't send response to {}", request);
        }
    }

    private void handleDeregister(Lwm2mMqttRequest request) {
        ClientProxyImpl clientProxy = registeredClients.get(request.getTopic().getClientId());
        stats.addEvent(clientProxy, Event.uplinkRequestReceiveSuccess(LWM2M.Operation.R_DEREGISTER));
        registrationService.deregisterClientProxy(clientProxy);
        registeredClients.remove(request.getTopic().getClientId());
        clientProxy.setRegistrationInfo(null);

        Lwm2mMqttResponse response = createDeregisterResponse(request);
        try {
            doSendResponse(clientProxy, response);
            registrationService.deregisterFinished(clientProxy);
            LOG.debug("Successfully deregistered client {} from server {}", clientProxy.getClientEndpointName(), this.configuration.getServerName());
        } catch (Exception e) {
            stats.addEvent(clientProxy, Event.uplinkResponseSendTimeout(response.getTopic().getOperation()));
            LOG.error("Couldn't send response to {}", request);
        }
    }

    public RegistrationInfo parseRegistrationInfo(Lwm2mMqttRegisterRequest request) {
        RegisterHeader header = request.getHeader();
        RegistrationInfo info = new RegistrationInfo();
        info.endpointClientName = header.getEndpointClientName();
        info.lifetime = header.getLifetime();
        info.lwm2mVersion = header.getVersion();
        info.bindingMode = header.getBindingMode();
        info.smsNumber = header.getSmsNumber();
        info.objects = parseObjects(request.getPayloadText());
        return info;
    }

    private Lwm2mMqttResponse createUpdateResponse(Lwm2mMqttRegisterRequest request) {
        MQTT.Topic topic = new MQTT.Topic(
                LWM2M.Operation.R_UPDATE,
                "res",
                request.getToken(),
                request.getTopic().getClientId(),
                request.getTopic().getServerId(),
                LWM2M.Path.empty()
        );

        return new Lwm2mMqttResponse(
                topic,
                LWM2M.ContentType.NO_FORMAT,
                LWM2M.ResponseCode.CHANGED
        );
    }

    private Lwm2mMqttResponse createRegisterResponse(Lwm2mMqttRegisterRequest request) {
        MQTT.Topic topic = new MQTT.Topic(
                LWM2M.Operation.R_REGISTER,
                "res",
                request.getToken(),
                request.getTopic().getClientId(),
                request.getTopic().getServerId(),
                LWM2M.Path.empty()
        );

        return new Lwm2mMqttResponse(
                topic,
                LWM2M.ContentType.NO_FORMAT,
                LWM2M.ResponseCode.CREATED);
    }

    private Lwm2mMqttResponse createDeregisterResponse(Lwm2mMqttRequest request) {
        MQTT.Topic topic = new MQTT.Topic(
                LWM2M.Operation.R_DEREGISTER,
                "res",
                request.getToken(),
                request.getTopic().getClientId(),
                request.getTopic().getServerId(),
                LWM2M.Path.empty()
        );

        return new Lwm2mMqttResponse(
                topic,
                LWM2M.ContentType.NO_FORMAT,
                LWM2M.ResponseCode.DELETED);
    }


    private static final Pattern PATTERN = Pattern.compile("<(?<url>.*?)/(?<object>\\d+)(?:/(?<instance>\\d+)/?)?>");

    private List<RegistrationObjectInfo> parseObjects(String payload) {
        if (payload == null || payload.length() == 0) {
            return new ArrayList<>();
        }

        List<RegistrationObjectInfo> objects = new ArrayList<>();
        List<String> elements = Arrays.asList(payload.split(","));
        for (String element : elements) {
            Matcher matcher = PATTERN.matcher(element);
            if (matcher.find()) {
                String url = matcher.group("url");
                Integer objectId = Integer.valueOf(matcher.group("object"));
                Integer instanceId = null;
                if (matcher.group("instance") != null && !matcher.group("instance").equals("")) {
                    instanceId = Integer.valueOf(matcher.group("instance"));
                }
                objects.add(new RegistrationObjectInfo(url, objectId, instanceId));
            }
        }
        return objects;
    }

}
