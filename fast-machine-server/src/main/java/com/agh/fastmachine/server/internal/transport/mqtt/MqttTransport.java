package com.agh.fastmachine.server.internal.transport.mqtt;

import com.agh.fastmachine.server.internal.client.ClientManager;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.service.BootstrapService;
import com.agh.fastmachine.server.internal.service.RegistrationService;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationObjectInfo;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.Transport;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttRegisterRequest;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttRequest;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttResponse;
import com.agh.fastmachine.server.internal.transport.mqtt.message.MqttRequestBuilder;
import org.eclipse.paho.client.mqttv3.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MqttTransport extends Transport<MqttConfiguration, Lwm2mMqttRequest> {
    private Map<String, ClientProxyImpl> registeredClients = new HashMap<>();
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
            mqttClient = new MqttClient(configuration.getBrokerAddress(), "server");
            registrationService = configuration.getServer().internal().getRegistrationService();
            bootstrapService = configuration.getServer().internal().getBootstrapService();
            clientManager = configuration.getServer().internal().getClientManager();

            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setCleanSession(true);

            mqttClient.connect(options);
            mqttClient.setCallback(mqttCallback);
            mqttClient.subscribe("lynx/br/req/+/+/" + configuration.getServerId());
            mqttClient.subscribe("lynx/bw/res/+/+/" + configuration.getServerId() + "/#");
            mqttClient.subscribe("lynx/bd/res/+/+/" + configuration.getServerId());
            mqttClient.subscribe("lynx/bd/res/+/+/" + configuration.getServerId() + "/#");
            mqttClient.subscribe("lynx/bf/res/+/+/" + configuration.getServerId());

            mqttClient.subscribe("lynx/rr/req/+/+/" + configuration.getServerId());
            mqttClient.subscribe("lynx/ru/req/+/+/" + configuration.getServerId());
            mqttClient.subscribe("lynx/rd/req/+/+/" + configuration.getServerId());

            mqttClient.subscribe("lynx/mr/res/+/+/" + configuration.getServerId() + "/#");
            mqttClient.subscribe("lynx/mw/res/+/+/" + configuration.getServerId() + "/#");
            mqttClient.subscribe("lynx/me/res/+/+/" + configuration.getServerId() + "/+/+/+");
            mqttClient.subscribe("lynx/mc/res/+/+/" + configuration.getServerId() + "/#");
            mqttClient.subscribe("lynx/md/res/+/+/" + configuration.getServerId() + "/+/+");
            mqttClient.subscribe("lynx/ma/res/+/+/" + configuration.getServerId() + "/#");
            mqttClient.subscribe("lynx/mm/res/+/+/" + configuration.getServerId() + "/#"); // TODO in doc md -> mm

            mqttClient.subscribe("lynx/io/res/+/+/" + configuration.getServerId() + "/#");
            mqttClient.subscribe("lynx/in/req/+/+/" + configuration.getServerId() + "/#");
            mqttClient.subscribe("lynx/ic/res/+/+/" + configuration.getServerId() + "/#");


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
    protected void doSendRequest(Lwm2mMqttRequest request) {
        try {
            mqttClient.publish("lynx/" + request.getTopic().toString(), request.toMqttMessage());
            System.out.println("Sent  " + request.getTopic());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void doSendResponse(Lwm2mMqttResponse response) {
        try {
            mqttClient.publish("lynx/" + response.getTopic().toString(), response.toMqttMessage());
        } catch (MqttException e) {
            e.printStackTrace();
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
            System.out.println("Received " + topic);

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
            }

            if (topic.getType().equals("res")) {
                Lwm2mResponse response = Lwm2mMqttResponse.fromMqtt(message, topic);
                handleResponse(response);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }

        @Override
        public void connectionLost(Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    };

    private void handleRegister(Lwm2mMqttRegisterRequest request) {
        RegistrationInfo registrationInfo = parseRegistrationInfo(request);
        String endpointClientName = registrationInfo.endpointClientName;

        if (!registrationService.isClientRegistered(endpointClientName)) {
            ClientProxyImpl clientProxy = clientManager.createClient(endpointClientName);
            clientProxy.setServerId(configuration.getServerId());
            clientProxy.setRegisterTime(new Date());
            clientProxy.setLastUpdateTime(new Date());
            registrationService.registerClientProxy(clientProxy, registrationInfo);
            registeredClients.put(request.getTopic().getClientId(), clientProxy);

            Lwm2mMqttResponse response = createRegisterResponse(request);
            doSendResponse(response);
            registrationService.registerFinished(clientProxy);
        }
    }

    private void handleUpdate(Lwm2mMqttRegisterRequest request) {
        ClientProxyImpl clientProxy = registeredClients.get(request.getTopic().getClientId());
        clientProxy.setLastUpdateTime(new Date());
        RegistrationInfo updatedInfo = parseRegistrationInfo(request);
        registrationService.handleUpdateForClientProxy(clientProxy, updatedInfo);

        Lwm2mMqttResponse response = createUpdateResponse(request);
        doSendResponse(response);
        registrationService.updateFinished(clientProxy);
    }

    private void handleDeregister(Lwm2mMqttRequest request) {
        ClientProxyImpl clientProxy = registeredClients.get(request.getTopic().getClientId());
        registrationService.deregisterClientProxy(clientProxy);
        registeredClients.remove(request.getTopic().getClientId());
        clientProxy.setRegistrationInfo(null);

        Lwm2mMqttResponse response = createDeregisterResponse(request);
        doSendResponse(response);
        registrationService.deregisterFinished(clientProxy);
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
