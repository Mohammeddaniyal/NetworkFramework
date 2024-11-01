package com.thinking.machines.nframework.common.exceptions;
public class NetworkException extends Exception
{
public NetworkException()
{
//do nothing
//the purpose of this constructor,for deserializing 
}
public NetworkException(String message)
{
super(message);
}
}