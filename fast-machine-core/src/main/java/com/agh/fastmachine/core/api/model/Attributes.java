package com.agh.fastmachine.core.api.model;

public class Attributes {
    ////////// NAMES ///////////
    public static final String MINIMUM_PERIOD = "pmin";
    public static final String MAXIMUM_PERIOD = "pmax";
    public static final String GREATER_THAN = "gt";
    public static final String LESS_THAN = "lt";
    public static final String STEP = "stp";
    public static final String DIMENSION = "dim";
    public static final String CANCEL = "cancel";

    ///////// VALUES ///////////
    public Integer minimumPeriod = null;
    public Integer maximumPeriod = null;
    public Double lessThan = null;
    public Double greaterThan = null;
    public Double step = null;
    public Integer dimension = null;
    public boolean cancel = false;

    public boolean isNumeric = false;

    public Attributes() {

    }

    public Attributes(boolean isNumeric) {
        this.isNumeric = isNumeric;
    }

    public String toDiscoverString() {
        StringBuilder builder = buildStringWithDelimiter(";");
        return builder.toString();
    }

    public String toRestString() {
        StringBuilder builder = buildStringWithDelimiter("&");
        if (builder.length() > 0 && builder.charAt(0) == '&') {
            builder.deleteCharAt(0);
            builder.insert(0, '?');
        }
        return builder.toString();
    }

    public StringBuilder buildStringWithDelimiter(String delimiter) {
        StringBuilder builder = serializeBasicAttributes(delimiter);
        if (isNumeric) {
            appendNumericAttributes(builder, delimiter);
        }
        return builder;
    }

    private void appendNumericAttributes(StringBuilder builder, String delimiter) {
        if (greaterThan != null) {
            builder.append(delimiter).append(GREATER_THAN).append('=').append(greaterThan);
        }
        if (lessThan != null) {
            builder.append(delimiter).append(LESS_THAN).append('=').append(lessThan);
        }
        if (step != null) {
            builder.append(delimiter).append(STEP).append('=').append(step);
        }
    }

    private StringBuilder serializeBasicAttributes(String delimiter) {
        StringBuilder b = new StringBuilder();

        if (minimumPeriod != null) {
            b.append(delimiter).append(MINIMUM_PERIOD).append('=').append(minimumPeriod);
        }
        if (maximumPeriod != null) {
            b.append(delimiter).append(MAXIMUM_PERIOD).append('=').append(maximumPeriod);
        }
        if (dimension != null) {
            b.append(delimiter).append(DIMENSION).append('=').append(dimension);
        }
        if (cancel) {
            b.append(delimiter).append(CANCEL).append('=').append(1);
        }
        //todo dimension attribute
        return b;
    }

}

