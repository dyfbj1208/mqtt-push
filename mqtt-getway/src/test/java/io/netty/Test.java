package io.netty;

import java.nio.Buffer;

import io.mqttpush.mqttserver.beans.HttpPushVo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class Test {

	public static void main(String[] args) {
		
//		
//		ByteBuf buffer=ByteBufAllocator.DEFAULT.buffer();
//		
//		buffer.writeBytes("nihaomacccd".getBytes());
//		
//	
//
//		System.out.println(buffer.readableBytes());
//		ByteBuf buffer2=ByteBufAllocator.DEFAULT.buffer();
//	
//		buffer2.writeBytes(buffer);
//		
//	
//		buffer2.resetReaderIndex();
//		byte[] bs=new byte[buffer2.readableBytes()];
//		buffer2.readBytes(bs);
//	
//		System.out.println(new String(bs));
//		System.out.println(buffer2.readableBytes());
		
		HttpPushVo httpPushVo=new HttpPushVo();
		
		httpPushVo.setTextcontent("你好吗");
		httpPushVo.setCallback("baidu.com");
		httpPushVo.setFromIdentify("11");
		httpPushVo.setToIdentify("3333");
		
		
		
		
		
		ByteBuf buf=httpPushVo.getByteContent();
		
		
		
		byte[] bs=new byte[buf.readableBytes()];
		buf.readBytes(bs);
		
		System.out.println(new String(bs));

	}

}
