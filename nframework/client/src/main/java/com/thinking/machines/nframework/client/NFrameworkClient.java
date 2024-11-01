package com.thinking.machines.nframework.client;
import com.thinking.machines.nframework.common.*;
import com.thinking.machines.nframework.common.exceptions.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
public class NFrameworkClient
{
public Object execute(String servicePath,Object ...arguments) throws Throwable
{
try
{
Request requestObject=new Request();
requestObject.setServicePath(servicePath);
requestObject.setArguments(arguments);
String requestJSONString=JSONUtil.toJSON(requestObject);
byte objectBytes[]=requestJSONString.getBytes(StandardCharsets.UTF_8);
int requestLength=objectBytes.length;
byte header[]=new byte[1024];
int x=requestLength;
int i=1023;
while(x>0)
{
header[i]=(byte)(x%10);
x=x/10;
i--;
}

Socket socket=new Socket("localhost",5500);
OutputStream os=socket.getOutputStream();
os.write(header,0,1024);
os.flush();



InputStream is;
is=socket.getInputStream();
byte ack[];
ack=new byte[1];
int bytesReadCount;
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1)continue;
break;
}

int bytesToSend=requestLength;
int j=0;
int chunkSize=1024;
while(j<bytesToSend)
{
if((bytesToSend-j)<chunkSize) chunkSize=bytesToSend-j;
os.write(objectBytes,j,chunkSize);
os.flush();
j=j+chunkSize;
}

int bytesToReceive=1024;
byte tmp[]=new byte[1024];
int k;
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


x=1023;
i=1;
int responseLength=0;
while(x>=0)
{
responseLength=responseLength+(header[x]*i);
i=i*10;
x--;
}

ack[0]=1;
os.write(ack,0,1);
os.flush();

byte response[]=new byte[responseLength];
int bytesToRecieve=responseLength;
j=0;
i=0;
while(j<bytesToRecieve)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
response[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}

os.write(ack,0,1);
os.flush();
socket.close();

String responseJSONString=new String(response,StandardCharsets.UTF_8);
Response responseObject=JSONUtil.fromJSON(responseJSONString,Response.class);
if(responseObject.getSuccess())
{
return responseObject.getResult();
}
else
{
throw responseObject.getException();
}
}catch(Exception e)
{
System.out.println(e.getMessage());
}
return null;
}
}