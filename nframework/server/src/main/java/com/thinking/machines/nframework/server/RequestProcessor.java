package com.thinking.machines.nframework.server;
import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import com.thinking.machines.nframework.common.*;
import com.thinking.machines.nframework.common.exceptions.*;
import java.nio.charset.*;
class RequestProcessor extends Thread
{
private NFrameworkServer server;
private Socket socket;
RequestProcessor(NFrameworkServer server,Socket socket)
{
this.server=server;
this.socket=socket;
start();
}
public void run()
{
try
{
InputStream is=socket.getInputStream();
int bytesToReceive=1024;
byte tmp[]=new byte[1024];
byte header[]=new byte[1024];
int bytesReadCount;
int i,j,k;
j=0;
i=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
header[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}

int x=1023;
i=1;
int requestLength=0;
while(x>=0)
{
requestLength=requestLength+(header[x]*i);
i=i*10;
x--;
}

OutputStream os=socket.getOutputStream();
byte ack[]=new byte[1];
ack[0]=1;
os.write(ack,0,1);
os.flush();


byte request[]=new byte[requestLength];
int bytesToRecieve=requestLength;
j=0;
i=0;
while(j<bytesToRecieve)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
request[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}
String requestJSONString=new String(request,StandardCharsets.UTF_8);
Request requestObject=JSONUtil.fromJSON(requestJSONString,Request.class);
String servicePath=requestObject.getServicePath();
TCPService tcpService=this.server.getTCPService(servicePath);
Response responseObject=new Response();
if(tcpService==null)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(new RuntimeException("Invalid path : "+servicePath));
}
else
{
Class c=tcpService.c;
Method method=tcpService.method;
try
{
Object serviceObject=c.newInstance();
Object result=method.invoke(serviceObject,requestObject.getArguments());
responseObject.setSuccess(true);
responseObject.setResult(result);
responseObject.setException(null);
}catch(InstantiationException instantiationException)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(new RuntimeException("Unable to create object of service class associated with the path : "+servicePath));
}
catch(IllegalAccessException illegalAccessException)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(new RuntimeException("Unable to create object of service class associated with the path : "+servicePath));
}
catch(InvocationTargetException invocationTargetException)
{
Throwable t=invocationTargetException.getCause();
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(t);
}
}

String responseJSONString=JSONUtil.toJSON(responseObject);
byte objectBytes[]=responseJSONString.getBytes(StandardCharsets.UTF_8);
int responseLength=objectBytes.length;
header=new byte[1024];
x=responseLength;
i=1023;
while(x>0)
{
header[i]=(byte)(x%10);
i--;
x=x/10; 
}

os.write(header,0,1024);
os.flush();

while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}

int bytesToSend=responseLength;
j=0;
i=0;
int chunkSize=1024;

while(j<bytesToSend)
{
if((bytesToSend-j)<chunkSize) chunkSize=bytesToSend-j;
os.write(objectBytes,j,chunkSize);
os.flush();
j=j+chunkSize;
}

while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}
socket.close();

}catch(IOException ioException)
{
System.out.println(ioException);
}
}
}