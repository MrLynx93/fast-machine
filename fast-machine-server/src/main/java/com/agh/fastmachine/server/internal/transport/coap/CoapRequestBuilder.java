package com.agh.fastmachine.server.internal.transport.coap;

import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.core.internal.parser.WriteParser;
import com.agh.fastmachine.server.api.model.*;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.RequestBuilder;

// TODO finish
public class CoapRequestBuilder implements RequestBuilder<Lwm2mCoapRequest> {

    public Lwm2mCoapRequest buildCreateRequest(ObjectInstanceProxy instance) { // TODO insert ClientProxy (and id) into instance
        return Lwm2mCoapRequest.of(
                LWM2M.Path.fromString(instance.getPath()),
                LWM2M.Operation.M_CREATE,
                generateToken(),
                LWM2M.ContentType.TLV,
                WriteParser.serialize(instance)
        );
    }

    @Override
    public Lwm2mCoapRequest buildDeleteRequest(ObjectInstanceProxy instance) {
        return Lwm2mCoapRequest.of(
                LWM2M.Path.fromString(instance.getPath()),
                LWM2M.Operation.M_DELETE,
                generateToken()
        );
    }

    @Override
    public Lwm2mCoapRequest buildDiscoverRequest(ObjectNodeProxy node) {
        return Lwm2mCoapRequest.of(
                LWM2M.Path.fromString(node.getPath()),
                LWM2M.Operation.M_DISCOVER,
                generateToken(),
                LWM2M.ContentType.LINK_FORMAT
        );
    }

    @Override
    public Lwm2mCoapRequest buildExecuteRequest(ObjectResourceProxy<?> resource, byte[] arguments) {
        return Lwm2mCoapRequest.of(
                LWM2M.Path.fromString(resource.getPath()),
                LWM2M.Operation.M_EXECUTE,
                generateToken(),
                LWM2M.ContentType.OPAQUE, // TODO argument should be String!!!
                arguments
        );
    }

    @Override
    public Lwm2mCoapRequest buildObserveRequest(ObjectNodeProxy<?> node) {
        return Lwm2mCoapRequest.of(
                LWM2M.Path.fromString(node.getPath()),
                LWM2M.Operation.I_OBSERVE,
                generateToken(),
                LWM2M.ContentType.NO_FORMAT // TODO is that correct? Response for observe is empty?
        );
    }

    @Override
    public Lwm2mCoapRequest buildReadRequest(ObjectBaseProxy<?> object) {
        return Lwm2mCoapRequest.of(
                LWM2M.Path.fromString(object.getPath()),
                LWM2M.Operation.M_READ,
                generateToken(),
                LWM2M.ContentType.TLV
        );
    }

    @Override
    public Lwm2mCoapRequest buildReadRequest(ObjectInstanceProxy instance) {
        return Lwm2mCoapRequest.of(
                LWM2M.Path.fromString(instance.getPath()),
                LWM2M.Operation.M_READ,
                generateToken(),
                LWM2M.ContentType.TLV
        );
    }

    @Override
    public Lwm2mCoapRequest buildReadRequest(ObjectResourceProxy<?> resource) {
        return Lwm2mCoapRequest.of(
                LWM2M.Path.fromString(resource.getPath()),
                LWM2M.Operation.M_WRITE_ATTRIBUTE,
                generateToken(),
                getResourceContentType(resource)
        );
    }

    @Override
    public Lwm2mCoapRequest buildWriteAttributesRequest(ObjectNodeProxy<?> node) {
        return Lwm2mCoapRequest.of(
                LWM2M.Path.fromString(node.getPath()),
                LWM2M.Operation.M_WRITE_ATTRIBUTE,
                generateToken(),
                LWM2M.ContentType.LINK_FORMAT
        );
    }

    @Override
    public Lwm2mCoapRequest buildWriteRequest(ObjectInstanceProxy instance) {
        return Lwm2mCoapRequest.of(
                LWM2M.Path.fromString(instance.getPath()),
                LWM2M.Operation.M_WRITE,
                generateToken(),
                LWM2M.ContentType.TLV
        ); // TODO
    }


    private LWM2M.ContentType getResourceContentType(ObjectResourceProxy resource) {
        if (OpaqueResourceValue.class.equals(resource.getValueType())) {
            return LWM2M.ContentType.OPAQUE;
        }
        if (resource instanceof ObjectMultipleResourceProxy) {
            return LWM2M.ContentType.TLV;
        }
        return LWM2M.ContentType.PLAIN_TEXT;
    }

    public String generateToken() {
        return null; // TODO
    }
}
