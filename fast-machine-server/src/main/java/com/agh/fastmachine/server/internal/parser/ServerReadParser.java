package com.agh.fastmachine.server.internal.parser;

import com.agh.fastmachine.core.internal.parser.ReadParser;

public class ServerReadParser extends ReadParser {

    public ServerReadParser() {
        this.objectFactory = new ServerObjectFactory();
    }
}
