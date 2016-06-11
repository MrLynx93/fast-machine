package com.agh.fastmachine.client.internal.attribute;

import com.agh.fastmachine.client.internal.exception.UnsupportedWriteAttributeException;
import com.agh.fastmachine.client.internal.message.request.Lwm2mWriteAttributeRequest;
import com.agh.fastmachine.client.api.model.AbstractLwm2mNode;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.api.model.Attributes;

import java.lang.reflect.ParameterizedType;

// TODO check this
public class AttributesFactory {

    public static Attributes create(ObjectResource<?> resource) {
        Class type = (Class) ((ParameterizedType) resource.getValueType().getGenericSuperclass()).getActualTypeArguments()[0];
        return new Attributes(Number.class.isAssignableFrom(type));
    }

    public static Attributes merge(ObjectResource<?> resource, Lwm2mWriteAttributeRequest request) {
        Class type = (Class) ((ParameterizedType) resource.getValueType().getGenericSuperclass()).getActualTypeArguments()[0];

        if (Number.class.isAssignableFrom(type)) {
            Attributes writeAttributes = new Attributes(true);

            mergeBasicAttributes(request, writeAttributes);
            if (!equals(writeAttributes.greaterThan, request.getGreaterThan())) {
                writeAttributes.greaterThan = request.getGreaterThan().doubleValue();
            }
            if (!equals(writeAttributes.lessThan, request.getLessThan())) {
                writeAttributes.lessThan = request.getLessThan().doubleValue();
            }
            if (!equals(writeAttributes.step, request.getStep())) {
                writeAttributes.step = request.getStep().doubleValue();
            }
            return writeAttributes;
        }


        Attributes attributes = new Attributes(false);
        mergeBasicAttributes(request, attributes);
        return attributes;
    }

    private static void mergeBasicAttributes(Lwm2mWriteAttributeRequest request, Attributes attributes) {
        if (!equals(attributes.minimumPeriod, request.getMinimumPeriod())) {
            attributes.minimumPeriod = request.getMinimumPeriod();
        }
        if (!equals(attributes.maximumPeriod, request.getMaximumPeriod())) {
            attributes.maximumPeriod = request.getMaximumPeriod();
        }
    }

    public static Attributes create(AbstractLwm2mNode node) {
        return new Attributes(false);
    }

    public static Attributes merge(AbstractLwm2mNode node, Lwm2mWriteAttributeRequest request) throws UnsupportedWriteAttributeException {
        if (request.getDimension() != null) {
            throw new UnsupportedWriteAttributeException(Attributes.DIMENSION);
        }
        Attributes attributes = new Attributes(false);
        mergeBasicAttributes(request, attributes);
        return attributes;
    }

    private static boolean equals(Object a, Object b) {
        return (a == null && b == null) || (a != null && b != null && a.equals(b));
    }
}
