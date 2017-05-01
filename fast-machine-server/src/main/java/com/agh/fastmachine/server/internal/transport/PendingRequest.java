package com.agh.fastmachine.server.internal.transport;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.mqtt.message.Lwm2mMqttRequest;
import com.agh.fastmachine.server.internal.transport.stats.TimeoutException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PendingRequest {
    private CountDownLatch count;
    private ClientProxyImpl client;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private volatile int responseCount;
    private Lwm2mResponse response;
    private Lwm2mRequest request;
    private ObservationListener<?> listenter; // todo delete???
    private boolean broadcast = false;

    public static PendingRequest broadcast(Lwm2mMqttRequest request, Server server) {
        return new PendingRequest(request, null, server.getClients().size());
    }


    public PendingRequest(Lwm2mMqttRequest request, Server server, int count) {
        this.request = request;
        this.responseCount = 0;
        this.count = new CountDownLatch(count);
        this.broadcast = true;
    }

    public PendingRequest(Lwm2mRequest request, ObservationListener<?> listener) {
        this(request);
        this.listenter = listener;
    }

    public PendingRequest(Lwm2mRequest request) {
        this.request = request;
        this.responseCount = 0;
    }

    public PendingRequest(Lwm2mRequest request, ClientProxyImpl client) {
        this(request);
        this.client = client;
    }

    public String getToken() {
        return request.getToken();
    }

    public ObservationListener<?> getListenter() {
        return listenter;
    }

    public Lwm2mRequest getRequest() {
        return request;
    }

    public Lwm2mResponse waitForCompletion() throws TimeoutException {
        return waitForCompletion(20, TimeUnit.SECONDS);
    }

    public Lwm2mResponse waitForCompletion(long timeout, TimeUnit unit) throws TimeoutException {
        if (!isBroadcast()) {
            boolean success = true;
            try {
                lock.lock();
                while (responseCount == 0) {
                    boolean awaitingSuccess = condition.await(timeout, unit);
                    success = awaitingSuccess;
                    if (!awaitingSuccess) {
                        break;
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                lock.unlock();
            }

            if (!success) {
                System.out.println("no success sending....");
                throw new TimeoutException();
            } else {
                return response;
            }
        } else {
            try {
                boolean awaitingSuccess = count.await(timeout, unit);
                if (!awaitingSuccess) {
                    System.out.println("no success sending....");
                    throw new TimeoutException();
                }
                return response;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void complete(Lwm2mResponse response) {
        this.responseCount++;
        this.response = response;

        if (count != null) {
            count.countDown();
        } else {
            lock.lock();
            condition.signal();
            lock.unlock();
        }
    }

    public boolean isCompleted() {
        return responseCount > 0;
    }

    public ClientProxyImpl getClient() {
        return client;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }
}
