package com.agh.fastmachine.server.bootstrap;


import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttRequest;

interface BootstrapOperation {
    Lwm2mMqttRequest getRequest();
}
