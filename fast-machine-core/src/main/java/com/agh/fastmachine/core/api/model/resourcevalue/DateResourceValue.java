package com.agh.fastmachine.core.api.model.resourcevalue;

import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateResourceValue extends ResourceValue<Date> {
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss");

    public DateResourceValue() {
    }

    public DateResourceValue(Date value, int id) {
        super(value, id);
    }

    public DateResourceValue(Date value) {
        super(value);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    @Override
    public void setValue(String value) {
        try {
            this.value = FORMAT.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
