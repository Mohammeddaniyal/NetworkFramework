package com.thinking.machines.nframework.common;
import com.google.gson.*;
import java.lang.reflect.*;
import com.google.gson.reflect.TypeToken;
import java.util.*;
class RequestSerializer implements JsonSerializer<Request> {
    @Override
    public JsonElement serialize(Request request, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("servicePath", request.getServicePath());

        JsonArray argumentsArray = new JsonArray();
        for (Object arg : request.getArguments()) {
            JsonObject argObject = new JsonObject();
            argObject.addProperty("type", arg.getClass().getName());

            if (arg instanceof List) {
                // Handle list serialization
                List<?> list = (List<?>) arg;
                argObject.addProperty("elementType", list.isEmpty() ? "java.lang.Object" : list.get(0).getClass().getName());
                argObject.add("value", context.serialize(list));
            } else {
                // Handle regular object serialization
                argObject.add("value", context.serialize(arg));
            }

            argumentsArray.add(argObject);
        }

        jsonObject.add("arguments", argumentsArray);
        return jsonObject;
    }
}
class RequestDeserializer implements JsonDeserializer<Request> {
    @Override
    public Request deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Request request = new Request();

        // Deserialize servicePath
        request.setServicePath(jsonObject.get("servicePath").getAsString());

        // Deserialize arguments
        JsonArray argumentsArray = jsonObject.getAsJsonArray("arguments");
        if (argumentsArray != null) {
            Object[] arguments = new Object[argumentsArray.size()];
            for (int i = 0; i < argumentsArray.size(); i++) {
                JsonObject argumentElement = argumentsArray.get(i).getAsJsonObject();
                String type = argumentElement.get("type").getAsString();
                String elementType = argumentElement.has("elementType") ? argumentElement.get("elementType").getAsString() : null;

                JsonElement value = argumentElement.get("value");
                
                // Handle List with custom elements
                if ((type.equals("java.util.LinkedList") || type.equals("java.util.ArrayList") )&& elementType != null) {
                    try {
                        Class<?> elementClass = Class.forName(elementType);
                        List<?> list = context.deserialize(value, TypeToken.getParameterized(List.class, elementClass).getType());
                        arguments[i] = list;
                    } catch (ClassNotFoundException e) {
                        throw new JsonParseException("Cannot find class for type: " + elementType, e);
                    }
                } 
                // Handle double-to-int conversion
                else if (type.equals("java.lang.Integer")) {
                    arguments[i] = (int)value.getAsJsonPrimitive().getAsDouble();
                } 
                // Handle other custom classes
                else {
                    try {
                        Class<?> argumentClass = Class.forName(type);
                        arguments[i] = context.deserialize(value, argumentClass);
                    } catch (ClassNotFoundException e) {
                        throw new JsonParseException("Cannot find class for type: " + type, e);
                    }
                }
            }
            request.setArguments(arguments);
        }

        return request;
    }
}

class ResponseSerializer implements JsonSerializer<Response> {
    @Override
    public JsonElement serialize(Response response, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        // Serialize success
        jsonObject.addProperty("success", response.getSuccess());

        // Serialize exception
        if (response.getException() != null) {
            jsonObject.add("exception", context.serialize(response.getException()));
        } else {
            jsonObject.add("exception", null);
        }
        // Serialize result
        Object result = response.getResult();
        if (result != null) {
            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("type", result.getClass().getName());
            if (result instanceof Collection) {
                // Handle List, ArrayList, LinkedList
                Collection<?> collection = (Collection<?>) result;
if(collection.isEmpty()==false)
{

                resultObject.addProperty("elementType", collection.iterator().next().getClass().getName());

} else {
                resultObject.addProperty("elementType", "java.lang.Object"); // Fallback type for empty list
            }
              resultObject.add("value", context.serialize(collection.isEmpty() ? new ArrayList<>() : collection));

            } else {
                // Handle custom class
                resultObject.add("value", context.serialize(result));
            }

            jsonObject.add("result", resultObject);
        } else {
            jsonObject.add("result", null);
        }

        return jsonObject;
    }
}
class ResponseDeserializer implements JsonDeserializer<Response> {
    @Override
    public Response deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Response response = new Response();

        // Deserialize success
        JsonElement successElement = jsonObject.get("success");

        if (successElement != null && !successElement.isJsonNull()) {
            response.setSuccess(successElement.getAsBoolean());
        } else {
            response.setSuccess(false); // or any default value you want
        }


        // Deserialize exception
        JsonElement exceptionElement = jsonObject.get("exception");
        if (exceptionElement != null && !exceptionElement.isJsonNull()) {
            response.setException(context.deserialize(exceptionElement, Throwable.class));
        }


        // Deserialize result
        JsonElement resultElement = jsonObject.get("result");
        if (resultElement != null && !resultElement.isJsonNull()) {
            JsonObject resultObject = resultElement.getAsJsonObject();
            String type = resultObject.get("type").getAsString();
            String elementType = resultObject.has("elementType") ? resultObject.get("elementType").getAsString() : null;


            JsonElement value = resultObject.get("value");

            try {
                if (type.equals("java.util.LinkedList") || type.equals("java.util.ArrayList")) {
                    // Handle List with custom elements
                    Class<?> elementClass = Class.forName(elementType);                   
 List<?> list = context.deserialize(value, TypeToken.getParameterized(List.class, elementClass).getType());
                    if (list == null ) {
                if (type.equals("java.util.ArrayList")) 
                        list = new ArrayList<>(); // Fallback to an empty list if null
                if (type.equals("java.util.LinkedList")) 
                        list = new LinkedList<>(); // Fallback to an empty list if null                    
	}else  if (list.size()==0  ) {
                if (type.equals("java.util.ArrayList")) 
                        list = new ArrayList<>(); // Fallback to an empty list if null
                if (type.equals("java.util.LinkedList")) 
                        list = new LinkedList<>(); // Fallback to an empty list if null                    
	}
                    response.setResult(list);
                } else if (type.equals("java.lang.Integer")) {
                    // Handle double-to-int conversion
                    response.setResult((int)value.getAsJsonPrimitive().getAsDouble());
                } else {
                    // Handle custom class
                    Class<?> resultClass = Class.forName(type);
                    response.setResult(context.deserialize(value, resultClass));
                }
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Cannot find class for type: " + type, e);
            }
        } else {
            response.setResult(new ArrayList<>()); // Fallback to an empty list if result is null
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