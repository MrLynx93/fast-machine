package com.agh.fastmachine.client.api.model;

import com.agh.fastmachine.client.internal.access.ServerAccessVerifier;
import com.agh.fastmachine.client.internal.message.request.Lwm2mContentRequest;
import com.agh.fastmachine.client.internal.message.request.Lwm2mRequest;
import com.agh.fastmachine.client.internal.message.response.Lwm2mCreateResponse;
import com.agh.fastmachine.client.internal.message.response.Lwm2mResponse;
import com.agh.fastmachine.client.internal.exception.MultipleInstanceNotAllowedException;
import com.agh.fastmachine.client.internal.exception.ResourcesAccessRightsException;
import com.agh.fastmachine.client.internal.exception.ServerAccessRightsException;
import com.agh.fastmachine.client.internal.exception.ServerIdNotFoundException;
import com.agh.fastmachine.client.api.model.builtin.AccessControlObjectInstance;
import com.agh.fastmachine.client.internal.visitor.merge.Lwm2mNodeMerger;
import com.agh.fastmachine.client.internal.service.observation.ObserveSession;
import com.agh.fastmachine.client.internal.access.CreateAccessVerifier;
import com.agh.fastmachine.client.internal.visitor.ObjectNodeVisitor;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mObject;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.annotation.SingleInstance;
import com.agh.fastmachine.core.internal.model.ObjectBaseModel;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.client.internal.parser.ClientReadParser;
import com.agh.fastmachine.core.internal.parser.ReadParser;
import com.agh.fastmachine.core.api.model.Operations;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;
import org.eclipse.californium.core.coap.CoAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public abstract class ObjectBase<T extends ObjectInstance> extends AbstractLwm2mNode implements ObjectBaseModel<T> {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    protected TreeMap<Integer, T> objectInstances = new TreeMap<>();
    private AccessControlObjectInstance accessControlObjectInstance;
    protected final boolean multipleInstancesAllowed;

    private final ReadParser readParser = new ClientReadParser();
    private Class<T> instanceClass;

    public ObjectBase(Class<T> instanceClass) {
        this.id = getClass().getAnnotation(Lwm2mObject.class).id();
        this.multipleInstancesAllowed = getClass().getAnnotation(SingleInstance.class) == null;
        this.instanceClass = instanceClass;
    }

    @Override
    public Map<Integer, T> getObjectInstances() {
        return Collections.unmodifiableMap(objectInstances);
    }

    public T getObjectInstance(int id) {
        return objectInstances.get(id);
    }

    @Override
    public T getInstance(int id) {
        return objectInstances.get(id);
    }

    @Override
    public Lwm2mResponse handleDelete(Lwm2mRequest request) {
        try {
            int shortServerId = getShortServerId(request);

            T instanceToDelete = getInstanceToDelete(request);
            ServerAccessVerifier.checkAccessRights(this, shortServerId, Operations.DELETE);
            client.getObjectInitializer().cleanupInstance(instanceToDelete);
            objectInstances.remove(instanceToDelete.getId());
            LOG.debug("Server {} deleted object {}", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.DELETED);

        } catch (ServerIdNotFoundException e) {
            LOG.error("Server {} is not set in LWM2M delete request", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.UNAUTHORIZED);

        } catch (ServerAccessRightsException e) {
            LOG.error("Server {} is not authorized to delete object {}", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.UNAUTHORIZED);
        }
    }

    @Override
    public Lwm2mCreateResponse<T> handleCreate(Lwm2mContentRequest request, Integer requestedId) {
        try {
            int shortServerId = getShortServerId(request);

            ServerAccessVerifier.checkAccessRights(this, shortServerId, Operations.CREATE);
            int requestedInstanceId = getRequestedInstanceId(request);
            T newInstance = createNewInstance(requestedInstanceId);
            newInstance.gatherResources();
            newInstance.setParent(this);

            ObjectInstanceModel parsedInstance = readParser.deserialize(newInstance, request.getByteContent());
            checkResourcesCreateAccessRights(newInstance, parsedInstance);
            Lwm2mNodeMerger.merge(newInstance, parsedInstance);
            client.getObjectInitializer().initializeInstance(newInstance, shortServerId);
            objectInstances.put(requestedInstanceId, newInstance);
            LOG.debug("Server {} created object {}", request.getServerUri(), coapResource.getPath());
            return new Lwm2mCreateResponse<>(CoAP.ResponseCode.CREATED, new byte[]{}, newInstance);//TODO response string

        } catch (ServerIdNotFoundException e) {
            LOG.error("Server {} is not set in LWM2M create request", request.getServerUri(), coapResource.getPath());
            return new Lwm2mCreateResponse<>(CoAP.ResponseCode.UNAUTHORIZED);

        } catch (ServerAccessRightsException e) {
            LOG.error("Server {} is not authorized to create node object {}", request.getServerUri(), coapResource.getPath());
            return new Lwm2mCreateResponse<>(CoAP.ResponseCode.UNAUTHORIZED);

        } catch (MultipleInstanceNotAllowedException e) {
            LOG.error("Server {} can't create new objects {}: Multiple instances not allowed!", request.getServerUri(), coapResource.getPath());
            return new Lwm2mCreateResponse<>(CoAP.ResponseCode.BAD_REQUEST);

        } catch (ResourcesAccessRightsException e) {
            LOG.error("Can't create node {}. Server don't have write access or some resource is mandatory. Request content: {}", coapResource.getPath(), request.getStringContent());
            return new Lwm2mCreateResponse<>(CoAP.ResponseCode.BAD_REQUEST);
        }
    }

    /// FOR INTERNAL USE ///

    public List<Integer> getSupportedResourceIds() {
        List<Integer> resourceIds = new ArrayList<>();
        for (Field field : instanceClass.getFields()) {
            Lwm2mResource annotation = field.getAnnotation(Lwm2mResource.class);
            if (annotation != null) {
                resourceIds.add(annotation.id());
            }
        }
        return resourceIds;
    }

    private T createNewInstance(int requestedInstanceId) {
        try {
            Constructor<T> constructor = instanceClass.getConstructor(int.class);
            return constructor.newInstance(requestedInstanceId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AccessControlObjectInstance getAccessControlObjectInstance() {
        return accessControlObjectInstance;
    }

    public T getNewInstance() {
        return getNewInstance(generateNextId());
    }

    public T getNewInstance(int id) {
        T newInstance = createNewInstance(id);
        addInstance(newInstance);
        return newInstance;
    }

    public void addInstance(T instance) {
        instance.setParent(this);
        objectInstances.put(instance.getId(), instance);
    }

    @Override
    public void notifyObservers(Integer shortServerId) {
        ObserveSession observeSession = observeSessions.get(shortServerId);
        if (observeSession != null) {
            client.getObservationInterface().executeNotify(observeSession);
        }
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(ObjectNodeVisitor visitor) {
        visitor.visit(this);
    }

    /// PRIVATE ///

    private void checkResourcesCreateAccessRights(T newInstance, ObjectInstanceModel parsedInstance) throws ResourcesAccessRightsException {
        if (!CreateAccessVerifier.verify(newInstance, parsedInstance)) {
            throw new ResourcesAccessRightsException();
        }
    }

    private T getInstanceToDelete(Lwm2mRequest request) {
        // todo should switch to split()
        return objectInstances.get(Integer.parseInt(request.getPath().remove(request.getPath().size() - 1)));
    }

    private int getRequestedInstanceId(Lwm2mContentRequest request) throws MultipleInstanceNotAllowedException {
        Integer requestedId;
        if (multipleInstancesAllowed) {
            if (request.getPath().size() == 2) {
                requestedId = Integer.parseInt(request.getPath().get(1));
            } else {
                requestedId = generateNextId();
            }
        } else {
            if (!objectInstances.isEmpty()) {
                throw new MultipleInstanceNotAllowedException();

            } else {
                requestedId = 0;
            }
        }
        return requestedId;
    }

    private int generateNextId() {
        Integer prevId = 66666;
        for (Integer id : objectInstances.navigableKeySet()) {
            if (id - prevId > 1)
                return id + 1;
            prevId = id;
        }
        if (prevId == 66666)
            return 0;
        return prevId + 1;
    }

    void setAccessControlObjectInstance(AccessControlObjectInstance accessControlObjectInstance) {
        this.accessControlObjectInstance = accessControlObjectInstance;
    }

    void delete(int id) {
        objectInstances.remove(id);
    }
}
