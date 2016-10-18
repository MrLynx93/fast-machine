package com.agh.fastmachine.server.internal.transport;

import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.parser.ReadParser;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectNodeProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.parser.ServerReadParser;

public class ObserveHandler {
    private final ReadParser readParser = new ServerReadParser();
    private final ObjectNodeProxy node;
    private final ObservationListener listener;

    public ObserveHandler(ObjectNodeProxy node, ObservationListener listener) {
        this.node = node;
        this.listener = listener;
    }

    @SuppressWarnings("unchecked")
    public void onNotify(Lwm2mResponse response) {
        if (node instanceof ObjectResourceProxy) {
            ObjectResourceProxy<?> resource = (ObjectResourceProxy) node;
            if (OpaqueResourceValue.class.equals(resource.getValueType())) {
                OpaqueResourceValue newValue = new OpaqueResourceValue(response.getPayload());
                resource.internal().update(newValue);
            } else {
                ResourceValue<?> newValue = readParser.deserialize(resource.getValue(), response.getPayload());
                resource.internal().update(newValue);
            }
        }
        if (node instanceof ObjectInstanceProxy) {
            ObjectInstanceProxy instance = (ObjectInstanceProxy) node;
            ObjectInstanceProxy newValue = readParser.deserialize(instance, response.getPayload());
            instance.internal().update(newValue);
            listener.onNotify(node);
            return;
        }
        if (node instanceof ObjectBaseProxy) {
            ObjectBaseProxy<?> object = (ObjectBaseProxy) node;
            ObjectBaseProxy newValue = readParser.deserialize(object, response.getPayload());
            object.internal().update(newValue);
        }
        listener.onNotify(node);
    }
}
