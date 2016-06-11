package com.agh.fastmachine.core.api.model;

public class Operations {
    public static final int READ = 1;
    public static final int WRITE = 2;
    public static final int EXECUTE = 4;
    public static final int DELETE = 8;
    public static final int CREATE = 16;
    public static final int ALL = 1 | 2 | 4 | 8 | 16;
    public static final int OTHER = 256;
}
