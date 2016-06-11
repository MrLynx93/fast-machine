package com.agh.fastmachine.client.internal.visitor;

import com.agh.fastmachine.client.api.model.*;
import com.agh.fastmachine.client.internal.attribute.AttributesResolver;
import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;

import java.util.Map;

public class DiscoverVisitor implements ObjectNodeVisitor {
    private String response;

    public String getResponse() {
        return response;
    }

    @Override
    public <T extends ObjectInstance> void visit(ObjectBase<T> object) {
        StringBuilder b = new StringBuilder();

        for (Integer shortServerId : object.getWriteAttributes().keySet()) {
            appendObject(object, b);
            if (object.getNumberOfServers() > 1) {
                b.append(";ep=").append(shortServerId);
            }
            b.append(object.getWriteAttributes(shortServerId).toDiscoverString());
            b.append(',');
            appendSupportedResourcesList(object, b);
            b.append('\n');
        }
        b.deleteCharAt(b.length() - 1); // delete last ,
        b.deleteCharAt(b.length() - 1); // delete last \n
        response = b.toString();
    }

    @Override
    public void visit(ObjectInstance instance) {
        StringBuilder b = new StringBuilder();

        for (Integer shortServerId : instance.getWriteAttributes().keySet()) {
            appendInstance(instance, b);
            if (instance.getNumberOfServers() > 1) {
                b.append(";ep=").append(shortServerId);
            }
            b.append(instance.getWriteAttributes(shortServerId).toDiscoverString());
            b.append(',');
            appendSupportedResourcesWithAttributesList(instance, shortServerId, b);
            b.append('\n');
        }
        b.deleteCharAt(b.length() - 1); // delete last,
        b.deleteCharAt(b.length() - 1); // delete last\n
        response = b.toString();
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectResource<T> objectResource) {
        response = serializeResource(objectResource);
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectMultipleResource<T> objectMultipleResource) {
        response = serializeResource(objectMultipleResource);
    }

    private String serializeResource(ObjectResource<?> resource) {
        StringBuilder b = new StringBuilder();

        Map<Integer, Attributes> mergedAttributes = AttributesResolver.getMergedAttributes(resource);
        for (Integer shortServerId : mergedAttributes.keySet()) {
            appendResource(resource, b);
            b.append(mergedAttributes.get(shortServerId).toDiscoverString());
            b.append(",");
            b.append('\n');
        }
        b.deleteCharAt(b.length() - 1); // delete last ,
        b.deleteCharAt(b.length() - 1); // delete last \n
        return b.toString();
    }

    private void appendSupportedResourcesWithAttributesList(ObjectInstance instance, int shortServerId, StringBuilder b) {
        for (Integer resourceId : instance.getResources().keySet()) {
            b.append("</");
            b.append(instance.parent().getId());
            b.append("/0/");
            b.append(resourceId);
            b.append(">");
            b.append(instance.getWriteAttributes(shortServerId).toDiscoverString());
            b.append(',');
        }
    }

    private <T extends ObjectInstance> void appendSupportedResourcesList(ObjectBase<T> object, StringBuilder b) {
        for (Integer resourceId : object.getSupportedResourceIds()) {
            b.append("</");
            b.append(object.getId());
            b.append("/0/");
            b.append(resourceId);
            b.append(">,");
        }
    }

    private void appendObject(ObjectBase object, StringBuilder b) {
        b.append("</");
        b.append(object.getId());
        b.append(">");
    }

    private void appendInstance(ObjectInstance instance, StringBuilder b) {
        b.append("</");
        b.append(instance.parent().getId());
        b.append("/");
        b.append(instance.getId());
        b.append(">");
    }

    private void appendResource(ObjectResource resource, StringBuilder b) {
        b.append("</");
        b.append(resource.parent().parent().getId());
        b.append("/");
        b.append(resource.parent().getId());
        b.append("/");
        b.append(resource.getId());
        b.append(">");
    }
}
