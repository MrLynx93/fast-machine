package com.agh.fastmachine.server.api.listener;

import com.agh.fastmachine.server.api.model.ObjectNodeProxy;

public interface ObservationListener<T extends ObjectNodeProxy> {
    void onNotify(T node);
}
