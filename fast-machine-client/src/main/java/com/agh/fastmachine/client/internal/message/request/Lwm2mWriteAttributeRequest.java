package com.agh.fastmachine.client.internal.message.request;

import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import com.agh.fastmachine.client.internal.exception.UnsupportedWriteAttributeException;
import com.agh.fastmachine.core.api.model.Attributes;
import org.eclipse.californium.core.server.resources.CoapExchange;


public class Lwm2mWriteAttributeRequest extends Lwm2mRequest {

    private Integer minimumPeriod;
    private Integer maximumPeriod;
    private Boolean hasCancel = false;
    private Number greaterThan;
    private Number lessThan;
    private Integer dimension;
    private Number step;

    private boolean hasMalformedOptions;
    private boolean hasExcessiveOptions;

    public Lwm2mWriteAttributeRequest(CoapExchange exchange, ServerObjectInstance serverObjectInstance) {
        super(exchange, serverObjectInstance);
        parseQueryOptions();
    }

    private void parseQueryOptions() {
        for (String query : exchange.getRequestOptions().getUriQuery()) {
            String[] elems = query.split("=");
            String paramName = elems[0];
            String paramValue = elems[1];

            if (Attributes.GREATER_THAN.equals(paramName)) {
                try {
                    greaterThan = Long.parseLong(paramValue);
                } catch (NumberFormatException e1) {
                    try {
                        greaterThan = Float.parseFloat(paramValue);
                    } catch (NumberFormatException e2) {
                        greaterThan = Double.parseDouble(paramValue);
                    }
                }
            }
            if (Attributes.LESS_THAN.equals(paramName)) {
                try {
                    lessThan = Long.parseLong(paramValue);
                } catch (NumberFormatException e1) {
                    try {
                        lessThan = Float.parseFloat(paramValue);
                    } catch (NumberFormatException e2) {
                        lessThan = Double.parseDouble(paramValue);
                    }
                }
            }
            if (Attributes.STEP.equals(paramName)) {
                try {
                    step = Long.parseLong(paramValue);
                } catch (NumberFormatException e1) {
                    try {
                        step = Float.parseFloat(paramValue);
                    } catch (NumberFormatException e2) {
                        step = Double.parseDouble(paramValue);
                    }
                }
            }
            if (Attributes.DIMENSION.equals(paramName)) {
                dimension = Integer.parseInt(paramValue);
            }
            if (Attributes.MINIMUM_PERIOD.equals(paramName)) {
                minimumPeriod = Integer.parseInt(paramValue);
            }
            if (Attributes.MAXIMUM_PERIOD.equals(paramName)) {
                maximumPeriod = Integer.parseInt(paramValue);
            }
            if (Attributes.CANCEL.equals(paramName)) {
                hasCancel = true;
            }
        }
    }

    public Boolean hasCancel() {
        return hasCancel;
    }

    public Integer getMinimumPeriod() {
        return minimumPeriod;
    }

    public Integer getMaximumPeriod() {
        return maximumPeriod;
    }

    public Number getGreaterThan() {
        return greaterThan;
    }

    public Number getLessThan() {
        return lessThan;
    }

    public Number getStep() {
        return step;
    }

    public boolean getHasMalformedOptions() {
        return hasMalformedOptions;
    }

    public boolean hasExcessiveOptions() {
        return hasExcessiveOptions;
    }

    public Integer getDimension() {
        return dimension;
    }
}
