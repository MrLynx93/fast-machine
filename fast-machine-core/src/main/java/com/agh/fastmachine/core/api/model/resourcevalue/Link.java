package com.agh.fastmachine.core.api.model.resourcevalue;

public class Link {
    private Integer objectId;
    private Integer instanceId;

    public Link(Integer objectId, Integer instanceId) {
        this.objectId = objectId;
        this.instanceId = instanceId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public Integer getInstanceId() {
        return instanceId;
    }

    @Override
    public String toString() {
        return objectId + ":" + instanceId;
    }
}
