package com.thinking.machines.nframework.server;
import java.net.*;
import com.thinking.machines.nframework.server.annotations.*;
import java.util.*;
import java.lang.reflect.*;
public class NFrameworkServer
{
private ServerSocket serverSocket;
private Set<Class> tcpNetworkServiceClasses;
private Map<String,TCPService> tcpServices;
public NFrameworkServer()
{
this.tcpNetworkServiceClasses=new HashSet<>();
this.tcpServices=new HashMap<>();
}
public void registerClass(Class c)
{
this.tcpNetworkServiceClasses.add(c);
Path pathOnType;
Path pathOnMethod;
TCPService tcpService;
String servicePath;
pathOnType=(Path)c.getAnnotation(Path.class);
if(pathOnType==null)return;
Method methods[]=c.getMethods();
for(Method method:methods)
{
pathOnMethod=(Path)method.getAnnotation(Path.class);
if(pathOnMethod==null)continue;
servicePath=pathOnType.value()+pathOnMethod.value();
tcpService=new TCPService();
tcpService.c=c;
tcpService.method=method;
tcpService.path=servicePath;
tcpServices.put(servicePath,tcpService);
}
}
public TCPService getTCPService(String path)
{
return this.tcpServices.get(path);
/*
Path pathOnType;
Path pathOnMethod;
Method methods[];
String fullPath;
TCPService tcpService=null;
for(Class c:tcpNetworkServiceClasses)
{
pathOnType=(Path)c.getAnnotation(Path.class);
if(pathOnType==null) continue;
methods=c.getMethods();
for(Method method:methods)
{
pathOnMethod=(Path)method.getAnnotation(Path.class);
if(pathOnMethod==null) continue;
fullPath=pathOnType.value()+pathOnMethod.value();
if(path.equals(fullPath))
{
tcpService=new TCPService();
tcpService.c=c;
tcpService.method=method;
tcpService.path=path;
return tcpService;
}
}
}
return null;
*/
}
public void start()
{ 
try
{
this.serverSocket=new ServerSocket(5500);
Socket socket;
RequestProcessor requestProcessor;
while(true)
{
socket=serverSocket.accept();
requestProcessor=new RequestProcessor(this,socket);
}
}catch(Exception Exception)
{
}
}
}