package com.test.model.test;

import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.client.internal.message.request.Lwm2mRequest;
import com.agh.fastmachine.client.internal.message.response.Lwm2mResponse;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;

import java.util.concurrent.CountDownLatch;

import static com.agh.fastmachine.core.api.model.Operations.READ;
import static com.agh.fastmachine.core.api.model.Operations.WRITE;

public class TestObjectInstance extends ObjectInstance {
    CountDownLatch counter;

    @Lwm2mResource(id = 0, permissions = READ)
    public ObjectResource<StringResourceValue> clientId = new ClientIdResource();


    @Lwm2mResource(id = 1, permissions = READ)
    public ObjectResource<StringResourceValue> serverId = new ObjectResource<>();


    @Lwm2mResource(id = 2, permissions = READ | WRITE)
    public ObjectResource<StringResourceValue> payload = new ObjectResource<>();

    public void setCounter(CountDownLatch counter) {
        this.counter = counter;
    }

    public class ClientIdResource extends ObjectResource<StringResourceValue> {

        @Override
        public Lwm2mResponse handleRead(Lwm2mRequest request) {
            Lwm2mResponse lwm2mResponse = super.handleRead(request);
            counter.countDown();
            return lwm2mResponse;
        }

    }

    public TestObjectInstance(int id) {
        super(id);
    }
}
