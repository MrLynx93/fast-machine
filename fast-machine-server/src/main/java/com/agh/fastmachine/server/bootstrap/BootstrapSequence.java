package com.agh.fastmachine.server.bootstrap;

import com.agh.fastmachine.core.internal.parser.WriteParser;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectNodeProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.mqtt.MQTT;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttRequest;

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BootstrapSequence {
    private static final char[] CHARSET = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
    private static final int TOKEN_LENGTH = 8;

    private List<BootstrapOperation> operations = new LinkedList<>();
    private int serverId;
    private String clientId;

    private BootstrapSequence(String clientId) {
        this.clientId = clientId;
    }

    public static BootstrapSequence sequenceFor(String clientId) {
        return new BootstrapSequence(clientId);
    }

    /******** Operations *********/

    public BootstrapSequence deleteAll() {
        operations.add(new DeleteOperation(LWM2M.Path.empty()));
        return this;
    }

    public BootstrapSequence delete(int objectId) {
        operations.add(new DeleteOperation(LWM2M.Path.of(objectId)));
        return this;
    }

    public BootstrapSequence deleteInstance(int objectId, int instanceId) {
        operations.add(new DeleteOperation(LWM2M.Path.of(objectId, instanceId)));
        return this;
    }

    public BootstrapSequence deleteResource(int objectId, int instanceId, int resourceId) {
        operations.add(new DeleteOperation(LWM2M.Path.of(objectId, instanceId, resourceId)));
        return this;
    }

    public BootstrapSequence writeObject(ObjectBaseProxy object) {
        operations.add(new WriteOperation(object));
        return this;
    }

    public BootstrapSequence writeInstance(ObjectInstanceProxy instance) {
        operations.add(new WriteOperation(instance));
        return this;
    }

    public BootstrapSequence writeResource(ObjectResourceProxy resource) {
        operations.add(new WriteOperation(resource));
        return this;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    List<BootstrapOperation> getOperations() {
        return operations;
    }

    private class DeleteOperation implements BootstrapOperation {
        private LWM2M.Path path;

        private DeleteOperation(LWM2M.Path path) {
            this.path = path;
        }

        @Override
        public Lwm2mMqttRequest getRequest() {
            path = LWM2M.Path.empty();
            MQTT.Topic topic = new MQTT.Topic(
                    LWM2M.Operation.BS_DELETE,
                    "req",
                    generateToken(),
                    BootstrapSequence.this.clientId,
                    String.valueOf(BootstrapSequence.this.serverId),
                    path
            );
            return new Lwm2mMqttRequest(topic, LWM2M.ContentType.NO_FORMAT);
        }
    }

    private class WriteOperation implements BootstrapOperation {
        private ObjectNodeProxy node;

        private WriteOperation(ObjectNodeProxy node) {
            this.node = node;
        }

        @Override
        public Lwm2mMqttRequest getRequest() {
            MQTT.Topic topic = new MQTT.Topic(
                    LWM2M.Operation.BS_WRITE,
                    "req",
                    generateToken(),
                    BootstrapSequence.this.clientId,
                    String.valueOf(BootstrapSequence.this.serverId),
                    node.getPath()
            );
            return new Lwm2mMqttRequest(topic, LWM2M.ContentType.TLV, WriteParser.serialize(node));
        }
    }


    public String generateToken() {
        Random random = new SecureRandom();
        char[] result = new char[TOKEN_LENGTH];
        for (int i = 0; i < result.length; i++) {
            // picks a random index out of character set > random character
            int randomCharIndex = random.nextInt(CHARSET.length);
            result[i] = CHARSET[randomCharIndex];
        }
        return new String(result);
    }
}
