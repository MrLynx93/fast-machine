package com.agh.fastmachine.server.internal.transport.coap.message;

import com.agh.fastmachine.core.internal.parser.WriteParser;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectNodeProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.RequestBuilder;

// TODO finish
public class CoapRequestBuilder extends RequestBuilder<Lwm2mCoapRequest> {

    @Override
    public Lwm2mCoapRequest buildCreateRequest(ObjectInstanceProxy instance, int id) { // TODO insert ClientProxy (and id) into instance
        return new Lwm2mCoapRequest(
                LWM2M.Path.of(instance.getObjectId(), id),
                LWM2M.Operation.M_CREATE,
                generateToken(),
                LWM2M.ContentType.TLV,
                WriteParser.serialize(instance)
        );
    }

    @Override
    public Lwm2mCoapRequest buildCreateRequest(ObjectInstanceProxy instance) { // TODO insert ClientProxy (and id) into instance
        return new Lwm2mCoapRequest(
                LWM2M.Path.of(instance.getObjectId()),
                LWM2M.Operation.M_CREATE,
                generateToken(),
                LWM2M.ContentType.TLV,
                WriteParser.serialize(instance)
        );
    }

    @Override
    public Lwm2mCoapRequest buildDeleteRequest(ObjectInstanceProxy instance) {
        return new Lwm2mCoapRequest(
                instance.getPath(),
                LWM2M.Operation.M_DELETE,
                generateToken()
        );
    }

    @Override
    public Lwm2mCoapRequest buildDiscoverRequest(ObjectNodeProxy node) {
        return new Lwm2mCoapRequest(
                node.getPath(),
                LWM2M.Operation.M_DISCOVER,
                generateToken(),
                LWM2M.ContentType.LINK_FORMAT
        );
    }

    @Override
    public Lwm2mCoapRequest buildExecuteRequest(ObjectResourceProxy<?> resource, String arguments) {
        return new Lwm2mCoapRequest(
                resource.getPath(),
                LWM2M.Operation.M_EXECUTE,
                generateToken(),
                LWM2M.ContentType.PLAIN_TEXT, // TODO argument should be String!!!
                arguments.getBytes()
        );
    }

    @Override
    public Lwm2mCoapRequest buildObserveRequest(ObjectNodeProxy<?> node) {
        return new Lwm2mCoapRequest(
                node.getPath(),
                LWM2M.Operation.I_OBSERVE,
                generateToken(),
                LWM2M.ContentType.NO_FORMAT // TODO is that correct? Response for observe is empty?
        );
    }

    @Override
    public Lwm2mCoapRequest buildCancelObserveRequest(ObjectNodeProxy<?> node) {
        return new Lwm2mCoapRequest(
                node.getPath(),
                LWM2M.Operation.I_CANCEL_OBSERVATION,
                generateToken(),
                LWM2M.ContentType.NO_FORMAT
        );
    }

    @Override
    public Lwm2mCoapRequest buildReadRequest(ObjectBaseProxy<?> object) {
        return new Lwm2mCoapRequest(
                object.getPath(),
                LWM2M.Operation.M_READ,
                generateToken(),
                LWM2M.ContentType.TLV
        );
    }

    @Override
    public Lwm2mCoapRequest buildReadRequest(ObjectInstanceProxy instance) {
        return new Lwm2mCoapRequest(
                instance.getPath(),
                LWM2M.Operation.M_READ,
                generateToken(),
                LWM2M.ContentType.TLV
        );
    }

    @Override
    public Lwm2mCoapRequest buildReadRequest(ObjectResourceProxy<?> resource) {
        return new Lwm2mCoapRequest(
                resource.getPath(),
                LWM2M.Operation.M_READ,
                generateToken(),
                getResourceContentType(resource)
        );
    }

    @Override
    public Lwm2mCoapRequest buildWriteAttributesRequest(ObjectNodeProxy<?> node) {
        return new Lwm2mCoapWriteAttributesRequest(
                node.getPath(),
                LWM2M.Operation.M_WRITE_ATTRIBUTE,
                generateToken(),
                LWM2M.ContentType.LINK_FORMAT,
                node.getAttributes()
        );
    }

    @Override
    public Lwm2mCoapRequest buildWriteRequest(ObjectInstanceProxy instance) {
        return new Lwm2mCoapRequest(
                instance.getPath(),
                LWM2M.Operation.M_WRITE,
                generateToken(),
                LWM2M.ContentType.TLV,
                WriteParser.serialize(instance)
        );
    }

    @Override
    public Lwm2mCoapRequest buildWriteRequest(ObjectResourceProxy resource) {
        return new Lwm2mCoapRequest(
                resource.getPath(),
                LWM2M.Operation.M_WRITE,
                generateToken(),
                getResourceFormat(resource),
                WriteParser.serialize(resource)
        );
    }

}
