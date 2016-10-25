package com.agh.fastmachine.server.internal.transport;

import com.agh.fastmachine.server.api.listener.ObservationListener;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PendingRequest {
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private volatile int responseCount;
    private Lwm2mResponse response;
    private Lwm2mRequest request;
    private ObservationListener<?> listenter; // todo delete???

    public PendingRequest(Lwm2mRequest request, ObservationListener<?> listener) {
        this(request);
        this.listenter = listener;
    }

    public PendingRequest(Lwm2mRequest request) {
        this.request = request;
        this.responseCount = 0;
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

    public Lwm2mResponse waitForCompletion() {
        try {
            lock.lock();
            while (responseCount == 0) {
                condition.await();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
        return response;
    }

    public void complete(Lwm2mResponse response) {
        lock.lock();
        this.responseCount++;
        this.response = response;
        condition.signal();
        lock.unlock();
    }

    public boolean isCompleted() {
        return responseCount > 0;
    }
}
