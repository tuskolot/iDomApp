package com.example.jacekmichalik.idomapp;

public interface IDOMTaskNotyfikator {
    final public static String RUN_MACRO = "RUN_MACRO";
    final public static String SYS_INFO = "SYS_INFO";
    final public static String GET_MACROS= "GET_MACROS";

    public void handleUpdated(String updateTAG);
}
