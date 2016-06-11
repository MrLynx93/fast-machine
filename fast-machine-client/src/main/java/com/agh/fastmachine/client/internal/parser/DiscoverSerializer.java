package com.agh.fastmachine.client.internal.parser;

import com.agh.fastmachine.client.api.model.AbstractLwm2mNode;
import com.agh.fastmachine.client.internal.visitor.DiscoverVisitor;

public class DiscoverSerializer {

    public static String createDiscoverResponse(AbstractLwm2mNode node) {
        DiscoverVisitor discoverVisitor = new DiscoverVisitor();
        node.accept(discoverVisitor);
        return discoverVisitor.getResponse();
    }

}