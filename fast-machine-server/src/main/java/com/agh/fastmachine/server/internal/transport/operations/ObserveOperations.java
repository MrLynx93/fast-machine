package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

public interface ObserveOperations extends Operations {
    <T extends ObjectInstanceProxy> void observe(ObjectBaseProxy<T> object, ObservationListener<ObjectBaseProxy<T>> listener);
    void observe(ObjectInstanceProxy instance, ObservationListener<ObjectInstanceProxy> listener);
    <T extends ResourceValue<?>> void observe(ObjectResourceProxy<T> resource, ObservationListener<ObjectResourceProxy<T>> listener);
    void cancelObservation(ObjectBaseProxy object);
    void cancelObservation(ObjectInstanceProxy instance);
    void cancelObservation(ObjectResourceProxy resource);
}
