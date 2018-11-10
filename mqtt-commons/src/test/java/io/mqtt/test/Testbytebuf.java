package io.mqtt.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class Testbytebuf {

	public static void main(String[] args) {
	
		
		
		
		ByteBuf byteBuf=ByteBufAllocator.DEFAULT.buffer();
		
		byteBuf.writeBytes("nihao".getBytes());
		

		ByteBuf byteBuf2=byteBuf.alloc().heapBuffer(byteBuf.readableBytes());
		
		byteBuf.resetReaderIndex();
		
		byteBuf2.writeBytes(byteBuf);
		
		System.out.println(byteBuf2);

	
		byte [] bs=byteBuf2.array();
		
		
		System.out.println(bs.length);
		
		//System.out.println(new String(byteBuf2.array()));
		
		System.out.println("end");
	}

}
