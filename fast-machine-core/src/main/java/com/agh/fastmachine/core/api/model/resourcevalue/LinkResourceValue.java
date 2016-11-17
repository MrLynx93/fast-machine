package com.agh.fastmachine.core.api.model.resourcevalue;

import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

public class LinkResourceValue extends ResourceValue<Link> {

    public LinkResourceValue() {
    }

    public LinkResourceValue(Link value, int id) {
        super(value, id);
    }

    public LinkResourceValue(Link value) {
        super(value);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setValue(String value) {
        String[] elements = value.split(":");
        this.value = new Link(Integer.valueOf(elements[0]), Integer.valueOf(elements[1]));
    }
}
