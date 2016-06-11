package com.agh.fastmachine.client.internal.message.response;

import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.internal.model.ObjectNodeModel;
import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.core.internal.visitor.writer.PlainTextWriterVisitor;
import com.agh.fastmachine.core.internal.visitor.writer.TLVWriterVisitor;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class Lwm2mNotifyResponse extends Lwm2mResponse {
    private static final PlainTextWriterVisitor plainTextParser = new PlainTextWriterVisitor();
    private static final TLVWriterVisitor tlvParser = new TLVWriterVisitor();

    public Lwm2mNotifyResponse(ObjectNodeModel node) {
        super(CoAP.ResponseCode.CHANGED);
        payload = parseNode(node);
    }

    @Override
    public void respond(CoapExchange exchange) {
        Response response = new Response(code);
        response.setPayload(payload);
        response.setType(CoAP.Type.CON);
        exchange.respond(response);
    }

    private byte[] parseNode(ObjectNodeModel node) {
        if (node instanceof ObjectInstance || node instanceof ObjectBase) {
            return tlvParser.parseNodeModel(node);
        }
        if (node instanceof ObjectResource) {
            if (OpaqueResourceValue.class.equals(((ObjectResource) node).getValueType())) {
                return (byte[]) ((ObjectResource) node).getValue().value;
            } else {
                return plainTextParser.parseNodeModel(node);
            }
        }
        return null;
    }
}
