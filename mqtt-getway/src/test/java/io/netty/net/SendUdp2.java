package io.netty.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

//发送端程序
public class SendUdp2
{
  public static void main(String[] args) throws IOException, InterruptedException
  {
 
   
   
     final  DatagramSocket ms=new DatagramSocket(8888);
     
     final InetAddress address = InetAddress.getByName("106.14.191.21");   
       final  Scanner scanner=new Scanner(System.in);
       new Thread() {
    	   
    	   
    	   public void  run() {
    		   
    		   String line=null;
    		   while(!(line= scanner.nextLine()).equals("exit")) {
    			   DatagramPacket dataPacket = null; 
        		   byte[] data =line.getBytes();   
        	    
        	       dataPacket = new DatagramPacket(data, data.length, address,8899); 
        	       try {
					ms.send(dataPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}  
        	       System.out.println("发送"+line);
    		   }
    		   
    		   scanner.close();
    	   }
    	   
       }.start();
     
       
     new Thread() {
    	 
    	 public void run()
         {
              byte buf[] = new byte[1024];  
              DatagramPacket dp = new DatagramPacket(buf, 1024);  
              while (true) 
              {  
                     try
                     {  
                    	 ms.receive(dp);  
                         System.out.println("receive client message : "+new String(buf, 0, dp.getLength()));  
                     } 
                     catch (Exception e) 
                     {  
                         e.printStackTrace();  
                     }  
                 }  
             
         }
    	 
     }.start();
  }
}