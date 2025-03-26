package com.thinking.machines.nframework.common;
import com.google.gson.*;
import java.lang.reflect.*;
import com.google.gson.reflect.TypeToken;
import java.util.*;
 class RequestSerializer implements JsonSerializer<Request> {
    @Override
 public  JsonElement serialize(Request request, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("servicePath", request.getServicePath());
        
        JsonArray argumentsArray = new JsonArray();
        if (request.getArguments() != null) {
            for (Object arg : request.getArguments()) {
                JsonObject argObject = new JsonObject();
                if (arg == null) {
                    argObject.addProperty("type", "null");
                    argObject.add("value", JsonNull.INSTANCE);
                } else {
                    // Always include the fully qualified class name.
                    argObject.addProperty("type", arg.getClass().getName());
                    
                    // If the argument is a Collection, record the element type.
                    if (arg instanceof Collection) {
                        Collection<?> coll = (Collection<?>) arg;
                        String elementType = "java.lang.Object";
                        if (!coll.isEmpty()) {
                            Object first = coll.iterator().next();
                            if (first != null) {
                                elementType = first.getClass().getName();
                            }
                        }
                        argObject.addProperty("elementType", elementType);
                        argObject.add("value", context.serialize(arg, arg.getClass()));
                    } 
                    // If the argument is a Map, record key and value types.
                    else if (arg instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) arg;
                        String keyType = "java.lang.Object";
                        String valueType = "java.lang.Object";
                        if (!map.isEmpty()) {
                            Map.Entry<?, ?> entry = map.entrySet().iterator().next();
                            if (entry.getKey() != null) {
                                keyType = entry.getKey().getClass().getName();
                            }
                            if (entry.getValue() != null) {
                                valueType = entry.getValue().getClass().getName();
                            }
                        }
                        argObject.addProperty("mapKeyType", keyType);
                        argObject.addProperty("mapValueType", valueType);
                        argObject.add("value", context.serialize(arg, arg.getClass()));
                    } 
                    // Otherwise, serialize the object normally using its runtime type.
                    else {
                        argObject.add("value", context.serialize(arg, arg.getClass()));
                    }
                }
                argumentsArray.add(argObject);
            }
        }
        jsonObject.add("arguments", argumentsArray);
        return jsonObject;
    }
}

 class RequestDeserializer implements JsonDeserializer<Request> {
    @Override
   public  Request deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Request request = new Request();
        
        // Deserialize servicePath.
        request.setServicePath(jsonObject.get("servicePath").getAsString());
        
        // Deserialize each argument.
        JsonArray argumentsArray = jsonObject.getAsJsonArray("arguments");
        Object[] arguments = new Object[argumentsArray.size()];
        for (int i = 0; i < argumentsArray.size(); i++) {
            JsonObject argObject = argumentsArray.get(i).getAsJsonObject();
            String typeStr = argObject.get("type").getAsString();
            JsonElement valueElement = argObject.get("value");
            
            if ("null".equals(typeStr)) {
                arguments[i] = null;
            } 
            // Check if this argument is a Collection.
            else if (argObject.has("elementType")) {
                String elementTypeStr = argObject.get("elementType").getAsString();
                try {
                    Class<?> elementClass = Class.forName(elementTypeStr);
                    Class<?> collClass = Class.forName(typeStr);
                    Type collectionType = TypeToken.getParameterized(collClass, elementClass).getType();
                    arguments[i] = context.deserialize(valueElement, collectionType);
                } catch (ClassNotFoundException e) {
                    throw new JsonParseException("Cannot find class: " + elementTypeStr, e);
                }
            } 
            // Check if this argument is a Map.
            else if (argObject.has("mapKeyType") && argObject.has("mapValueType")) {
                String keyTypeStr = argObject.get("mapKeyType").getAsString();
                String valueTypeStr = argObject.get("mapValueType").getAsString();
                try {
                    Class<?> keyClass = Class.forName(keyTypeStr);
                    Class<?> valueClass = Class.forName(valueTypeStr);
                    Class<?> mapClass = Class.forName(typeStr);
                    Type mapType = TypeToken.getParameterized(mapClass, keyClass, valueClass).getType();
                    arguments[i] = context.deserialize(valueElement, mapType);
                } catch (ClassNotFoundException e) {
                    throw new JsonParseException("Cannot find class for map types: " + keyTypeStr + " or " + valueTypeStr, e);
                }
            } 
            // Otherwise, deserialize as a regular object.
            else {
                try {
                    Class<?> argClass = Class.forName(typeStr);
                    arguments[i] = context.deserialize(valueElement, argClass);
                } catch (ClassNotFoundException e) {
                    throw new JsonParseException("Cannot find class: " + typeStr, e);
                }
            }
        }
        request.setArguments(arguments);
        return request;
    }
}


 class ResponseSerializer implements JsonSerializer<Response> {
    @Override
    public JsonElement serialize(Response response, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("success", response.getSuccess());
        
        // Serialize the exception if one exists.
        if (response.getException() != null) {
            jsonObject.add("exception", context.serialize(response.getException(), response.getException().getClass()));
        } else {
            jsonObject.add("exception", JsonNull.INSTANCE);
        }
        
        // Serialize the result with type metadata.
        Object result = response.getResult();
        if (result != null) {
            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("type", result.getClass().getName());
            
            if (result instanceof Collection) {
                Collection<?> coll = (Collection<?>) result;
                String elementType = "java.lang.Object";
                if (!coll.isEmpty()) {
                    Object first = coll.iterator().next();
                    if (first != null) {
                        elementType = first.getClass().getName();
                    }
                }
                resultObject.addProperty("elementType", elementType);
                resultObject.add("value", context.serialize(result, result.getClass()));
            } 
            else if (result instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) result;
                String keyType = "java.lang.Object";
                String valueType = "java.lang.Object";
                if (!map.isEmpty()) {
                    Map.Entry<?, ?> entry = map.entrySet().iterator().next();
                    if (entry.getKey() != null) {
                        keyType = entry.getKey().getClass().getName();
                    }
                    if (entry.getValue() != null) {
                        valueType = entry.getValue().getClass().getName();
                    }
                }
                resultObject.addProperty("mapKeyType", keyType);
                resultObject.addProperty("mapValueType", valueType);
                resultObject.add("value", context.serialize(result, result.getClass()));
            } 
            else {
                resultObject.add("value", context.serialize(result, result.getClass()));
            }
            jsonObject.add("result", resultObject);
        } else {
            jsonObject.add("result", JsonNull.INSTANCE);
        }
        return jsonObject;
    }
}

 class ResponseDeserializer implements JsonDeserializer<Response> {
    @Override
    public Response deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Response response = new Response();
        
        response.setSuccess(jsonObject.get("success").getAsBoolean());
        
        // Deserialize exception if present.
        JsonElement exceptionElement = jsonObject.get("exception");
        if (exceptionElement != null && !exceptionElement.isJsonNull()) {
            response.setException(context.deserialize(exceptionElement, Throwable.class));
        }
        
        // Deserialize result.
        JsonElement resultElement = jsonObject.get("result");
        if (resultElement != null && !resultElement.isJsonNull()) {
            JsonObject resultObject = resultElement.getAsJsonObject();
            String typeStr = resultObject.get("type").getAsString();
            JsonElement valueElement = resultObject.get("value");
            
            if (resultObject.has("elementType")) {
                // The result is a Collection.
                String elementTypeStr = resultObject.get("elementType").getAsString();
                try {
                    Class<?> elementClass = Class.forName(elementTypeStr);
                    Class<?> collClass = Class.forName(typeStr);
                    Type collectionType = TypeToken.getParameterized(collClass, elementClass).getType();
                    response.setResult(context.deserialize(valueElement, collectionType));
                } catch (ClassNotFoundException e) {
                    throw new JsonParseException("Cannot find class: " + elementTypeStr, e);
                }
            } 
            else if (resultObject.has("mapKeyType") && resultObject.has("mapValueType")) {
                // The result is a Map.
                String keyTypeStr = resultObject.get("mapKeyType").getAsString();
                String valueTypeStr = resultObject.get("mapValueType").getAsString();
                try {
                    Class<?> keyClass = Class.forName(keyTypeStr);
                    Class<?> valueClass = Class.forName(valueTypeStr);
                    Class<?> mapClass = Class.forName(typeStr);
                    Type mapType = TypeToken.getParameterized(mapClass, keyClass, valueClass).getType();
                    response.setResult(context.deserialize(valueElement, mapType));
                } catch (ClassNotFoundException e) {
                    throw new JsonParseException("Cannot find map key/value classes: " + keyTypeStr + " or " + valueTypeStr, e);
                }
            } 
            else {
                // Otherwise, it is a regular object.
                try {
                    Class<?> resultClass = Class.forName(typeStr);
                    response.setResult(context.deserialize(valueElement, resultClass));
                } catch (ClassNotFoundException e) {
                    throw new JsonParseException("Cannot find class: " + typeStr, e);
                }
            }
        }
        return response;
    }
}


public class JSONUtil
{
private JSONUtil(){}
public static String toJSON(java.io.Serializable serializable)
{
try
{
Gson gson = new GsonBuilder()
                .registerTypeAdapter(Request.class, new RequestSerializer())
                .registerTypeAdapter(Response.class, new ResponseSerializer())
                .create();
String j=gson.toJson(serializable);
//System.out.println(j);
return j;
}catch(Exception e)
{
System.out.println("Exception : "+e);
return "{}";
}
}
public static <T> T fromJSON(String jsonString,Class<T> c)
{
try
{
Gson gson = new GsonBuilder()
                .registerTypeAdapter(Request.class, new RequestDeserializer())
                .registerTypeAdapter(Response.class, new ResponseDeserializer())
                .create();

return gson.fromJson(jsonString,c);
}catch(Exception exception)
{
System.out.println(exception);
return null;
}
}
}