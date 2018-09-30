package io.netty.proxy;

import java.nio.Buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class Test {

	public static void main(String[] args) {
		
		
		ByteBuf buffer=ByteBufAllocator.DEFAULT.buffer();
		
		buffer.writeBytes("nihaomacccd".getBytes());
		
	

		System.out.println(buffer.readableBytes());
		ByteBuf buffer2=ByteBufAllocator.DEFAULT.buffer();
	
		buffer2.writeBytes(buffer);
		
	
		buffer2.resetReaderIndex();
		byte[] bs=new byte[buffer2.readableBytes()];
		buffer2.readBytes(bs);
	
		System.out.println(new String(bs));
		System.out.println(buffer2.readableBytes());

	}

}
