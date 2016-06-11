package com.agh.fastmachine.client.internal.service.observation;

import com.agh.fastmachine.client.internal.ServerObjectResolver;
import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.client.internal.message.request.Lwm2mRequest;
import com.agh.fastmachine.client.internal.message.response.Lwm2mNotifyResponse;
import com.agh.fastmachine.client.api.model.AbstractLwm2mNode;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ObservationInterface {
    private static final Logger LOG = LoggerFactory.getLogger(ObservationInterface.class);
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(THREAD_POOL_SIZE);
    private final Map<ObserveSession, ScheduledFuture<?>> observeSessionTasks = new ConcurrentHashMap<>();
    private final ServerObjectResolver serverObjectResolver;

    public ObservationInterface(ServerObjectResolver serverObjectResolver) {
        this.serverObjectResolver = serverObjectResolver;
    }

    public ObserveSession startObserving(AbstractLwm2mNode node, Lwm2mRequest request) {
        ObserveSession observeSession = new ObserveSession(request.getRequestingServer(), node, request.getExchange());
        int maximumPeriod = getWriteAttributesForSession(observeSession).maximumPeriod;

        ObserveTask observeTask = new ObserveTask(observeSession);
        ScheduledFuture<?> taskFuture = executor.schedule(observeTask, maximumPeriod, TimeUnit.SECONDS);
        observeSessionTasks.put(observeSession, taskFuture);

        return observeSession;
    }

    public void stopObserving(ObserveSession observeSession) {
        observeSessionTasks.get(observeSession).cancel(false);
        observeSessionTasks.remove(observeSession);
    }

    public void executeNotify(ObserveSession observeSession) {
        long minimumPeriod = getWriteAttributesForSession(observeSession).minimumPeriod * 1000L;
        long maximumPeriod = getWriteAttributesForSession(observeSession).maximumPeriod * 1000L;
        long timeToSleepForMinimumPeriod = minimumPeriod - observeSession.getTimeSinceLastNotify();

        observeSessionTasks.get(observeSession).cancel(false);

        if (timeToSleepForMinimumPeriod > 0) {
            ObserveTask observeTask = new ObserveTask(observeSession);
            ScheduledFuture<?> taskFuture = executor.schedule(observeTask, timeToSleepForMinimumPeriod, TimeUnit.MILLISECONDS);
            observeSessionTasks.put(observeSession, taskFuture);
        } else {
            notify(observeSession);
            ObserveTask observeTask = new ObserveTask(observeSession);
            ScheduledFuture<?> taskFuture = executor.schedule(observeTask, maximumPeriod, TimeUnit.MILLISECONDS);
            observeSessionTasks.put(observeSession, taskFuture);
        }
    }

    public void notify(ObserveSession observeSession) { // TODO should use handleRead to refresh value
        Lwm2mNotifyResponse response = new Lwm2mNotifyResponse(observeSession.node);
        observeSession.lastNotifyTime = System.currentTimeMillis();
        observeSession.coapResource.changed(response);
        LOG.debug("Sent notify on node {} to server {}", observeSession.coapResource.getName(), observeSession.getServerUri());
    }


    private Attributes getWriteAttributesForSession(ObserveSession observeSession) {
        Attributes attributes = observeSession.getMergedWriteAttributes();
        if (attributes == null) { // TODO more sophisticated condition for presence of write attributes
            LOG.debug("Did not find Write Attributes for node {}. Using default.", observeSession.coapResource.getName());
            ServerObjectInstance serverObjectInstance = serverObjectResolver.resolveServerObject(observeSession.exchange);
            attributes = new Attributes(false);
            attributes.maximumPeriod = serverObjectInstance.defaultMaximumPeriod.getValue().value;
            attributes.minimumPeriod = serverObjectInstance.defaultMinimumPeriod.getValue().value;
        }
        return attributes;
    }

    private class ObserveTask implements Runnable {
        private ObserveSession observeSession;

        private ObserveTask(ObserveSession observeSession) {
            this.observeSession = observeSession;
        }

        @Override
        public void run() {
            ObservationInterface.this.executeNotify(observeSession);
        }

    }

}
