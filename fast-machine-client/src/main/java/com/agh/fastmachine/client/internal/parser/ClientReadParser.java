package com.agh.fastmachine.client.internal.parser;

import com.agh.fastmachine.core.internal.parser.ReadParser;

public class ClientReadParser extends ReadParser {

    public ClientReadParser() {
        this.objectFactory = new ClientObjectFactory();
    }
}
