package com.agh.fastmachine.client.internal.service.observation;

import com.agh.fastmachine.client.internal.attribute.AttributesResolver;
import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;

public class NumericObservationResolver<T extends ResourceValue<?>> {
    private final ObjectResource<T> node;
    private T previousValue;

    public NumericObservationResolver(ObjectResource<T> resource) {
        this.node = resource;
        this.previousValue = resource.getValue();
    }

    public boolean shouldNotify(Integer shortServerId) {
        Attributes attributes = AttributesResolver.getMergedAttributes(node, shortServerId);

        if (!attributes.isNumeric) {
            return false;
        }
        if (greaterThanThreshold(attributes)) {
            previousValue = node.getValue();
            return true;
        }
        if (lessThanThreshold(attributes)) {
            previousValue = node.getValue();
            return true;
        }
        if (stepGreaterThanThreshold(attributes)) {
            previousValue = node.getValue();
            return true;
        }
        return false;
    }

    private boolean greaterThanThreshold(Attributes writeAttributes) {
        Number greaterThan = writeAttributes.greaterThan;
        return greaterThan != null && getResourceDoubleValue() > greaterThan.doubleValue();
    }

    private boolean lessThanThreshold(Attributes writeAttributes) {
        Number lessThan = writeAttributes.lessThan;
        return lessThan != null && getResourceDoubleValue() < lessThan.doubleValue();
    }

    private boolean stepGreaterThanThreshold(Attributes writeAttributes) {
        Number step = writeAttributes.step;
        return step != null && Math.abs(getResourceDoubleValue() - getResourcePreviousDoubleValue()) > step.doubleValue();
    }

    private double getResourceDoubleValue() {
        return ((Number) node.getValue().value).doubleValue();
    }

    private double getResourcePreviousDoubleValue() {
        return ((Number) previousValue.value).doubleValue();
    }

}
