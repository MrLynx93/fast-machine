package com.agh.fastmachine.core.internal;

public enum MediaTypeRegistry {

    PLAIN_TEXT(1540, "application/vnd.oma.lwm2m+text"),
    TLV(1541, "application/vnd.oma.lwm2m+tlv"),
    JSON(1542, "application/vnd.oma.lwm2m+json"),
    OPAQUE(1543, "application/vnd.oma.lwm2m+opaque"),
    UNDEFINED(1544, "unknown");

    private int key;
    private String name;

    MediaTypeRegistry(int key, String name) {
        this.key = key;
        this.name = name;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static int getKey(String name) {
        for (MediaTypeRegistry registry : MediaTypeRegistry.values()) {
            if (registry.getName().equals(name)) {
                return registry.getKey();
            }
        }
        throw new IllegalStateException("Media type with name: " + name + " does not exist");
    }

    public static String getName(int key) {
        for (MediaTypeRegistry registry : MediaTypeRegistry.values()) {
            if (registry.getKey() == key) {
                return registry.getName();
            }
        }
        throw new IllegalStateException("Media type with key: " + key + " does not exist");
    }

    public static MediaTypeRegistry getEnum(int key) {
        for (MediaTypeRegistry registry : MediaTypeRegistry.values()) {
            if (registry.getKey() == key) {
                return registry;
            }
        }
        throw new IllegalStateException("Media type with key: " + key + " does not exist");
    }
}
