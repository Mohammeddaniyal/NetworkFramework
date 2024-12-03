package com.thinking.machines.nframework.common;
public class Request implements java.io.Serializable
{
private String servicePath;
private Object []arguments;
private String []argumentTypes;
//private Class[] typeOfArguments;
public void setServicePath(String servicePath)
{
this.servicePath=servicePath;
}
public String getServicePath()
{
return this.servicePath;
}
public void setArguments(Object ...arguments)
{
this.arguments=arguments;
}
public Object[] getArguments()
{
return this.arguments;
}

public void setArgumentTypes(String[] argumentTypes)
{
this.argumentTypes=argumentTypes;
}
public String[] getArgumentTypes()
{
return this.argumentTypes;
}
/*
public void setTypeOfArguments(Class[] typeOfArguments)
{
this.typeOfArguments=typeOfArguments;
}
public Class[] getTypeOfArguments()
{
return this.typeOfArguments;
}
*/
}
