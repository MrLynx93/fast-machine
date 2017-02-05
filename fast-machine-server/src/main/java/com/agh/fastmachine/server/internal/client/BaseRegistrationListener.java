package com.agh.fastmachine.server.internal.client;

import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.listener.RegistrationListener;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

abstract class BaseRegistrationListener extends RegistrationListener {
    private RegistrationListener registrationListener;
    private Lock registrationLock = new ReentrantLock();
    private Condition isRegisteredCondition = registrationLock.newCondition();

    public abstract boolean isRegistered();

    protected abstract void clearObjectTree();

    public void setRegistrationListener(RegistrationListener listener) {
        this.registrationListener = listener;
    }

    public void waitForRegistration() {
        try {
            registrationLock.lock();
            while (!isRegistered()) {
                isRegisteredCondition.await();
            }
        } catch (InterruptedException e) {
        } finally {
            registrationLock.unlock();
        }
    }

    @Override
    public void onRegister(ClientProxy client) {
        try {
            registrationLock.lock();
            if (registrationListener != null) {
                registrationListener.onRegister(client);
            }
            isRegisteredCondition.signalAll();
        } finally {
            registrationLock.unlock();
        }

    }

    @Override
    public void onDeregister(ClientProxy client) {
        try {
            registrationLock.lock();
            if (registrationListener != null) {
                registrationListener.onDeregister(client);
            }
//            isRegisteredCondition.await();
        } finally {
            registrationLock.unlock();
        }
    }

    @Override
    public void onUpdate(ClientProxy client) {
        if (registrationListener != null) {
            registrationListener.onUpdate(client);
        }
    }

    @Override
    public void onExpire(ClientProxy client) {
        if (registrationListener != null) {
            registrationListener.onExpire(client);
            registrationListener = null;
        }
        clearObjectTree();
    }

}
