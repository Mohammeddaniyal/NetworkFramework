package com.thinking.machines.nframework.common;
import java.lang.reflect.*;
public class ConvertArgument
{
public static Object convert(Object argument, String argumentType) {
    if (argument == null || argumentType == null) return null;

    try {
        Class<?> targetClass = Class.forName(argumentType);

        // Handle basic types
        if (targetClass == Integer.class && argument instanceof Double) {
            return ((Double) argument).intValue();
        }

        // Handle custom objects (e.g., LinkedTreeMap to CustomClass)
        if (argument instanceof com.google.gson.internal.LinkedTreeMap) {
            String json = JSONUtil.toJSON((java.io.Serializable)argument);
            return JSONUtil.fromJSON(json, targetClass);
        }

        // Handle other edge cases as needed...

        // If the argument already matches the type, return as-is
        if (targetClass.isInstance(argument)) {
            return argument;
        }

    } catch (ClassNotFoundException e) {
        System.err.println("Class not found: " + argumentType);
    } catch (Exception e) {
        System.err.println("Error converting argument: " + e.getMessage());
    }

    return argument; // Return original argument if no conversion applied
}
}