package io.netty.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

//发送端程序
public class SendUdp
{
  public static void main(String[] args) throws IOException
  {
 
      DatagramPacket dataPacket = null; 
      MulticastSocket ms = new MulticastSocket();
      ms.setTimeToLive(32);  
      //将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的  
       byte[] data = "组播 测试".getBytes();   
       InetAddress address = InetAddress.getByName("239.0.0.255");   
       dataPacket = new DatagramPacket(data, data.length, address,8899);  
       ms.send(dataPacket);  
       ms.close();   
  }
}