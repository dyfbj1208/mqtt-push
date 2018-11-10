package io.mqttpush.mqttserver.util;

import io.mqttpush.mqttserver.util.AdminMessage.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * 构建报文的工具类
 * @author tianzhenjiu
 *
 */
public class ByteBufEncodingUtil {

	
	/**
//	 *上线报文的前缀
 * 下线报文的前缀
 * 暂存消息的前缀
//	 */
	public static final  char[] prefixchars= {'+','-','&','*'};
	
	
	private static ByteBufEncodingUtil packatUtil;
	
	
	
	public static ByteBufEncodingUtil getInatance() {
		
		if(packatUtil==null) {
			packatUtil=new ByteBufEncodingUtil();
		}
		return  packatUtil;
	}
	/**
	 * 构建一个上线消息的报文
	 * @param allocator
	 * @param deviceId 上线的设备
	 * @return
	 */
	public ByteBuf onlineBytebuf(ByteBufAllocator allocator,String deviceId) {
		 ByteBuf buf=allocator.buffer();
		 buf.writeByte(prefixchars[0]);
		 buf.writeBytes(deviceId.getBytes());
		 
		 return buf;
		
	}
	
	
	/**
	 * 构建一个下线报文 用于内部传输
	 * @param allocator
	 * @param deviceId  下线的设备
	 * @param lastChatDeviceId 下线之前最后交谈的那个设备id
	 * @return
	 */
	public ByteBuf offlineBytebuf(ByteBufAllocator allocator,String deviceId,String lastChatDeviceId) {
		 
		 ByteBuf buf=allocator.buffer();
		 buf.writeByte(prefixchars[1]);
		
		 byte[] bs=deviceId.getBytes();
		 buf.writeInt(bs.length);
		 buf.writeBytes(bs);
		 
		 if(lastChatDeviceId==null) {
			 buf.writeInt(0);
		 }else {
			 bs=lastChatDeviceId.getBytes();
		 	buf.writeInt(bs.length);
		 	buf.writeBytes(bs);
		 }
		 return buf;
		
	}
	
	/**
	 * 构建一个暂存消息对象
	 * @param allocator
	 * @param timestamp
	 * @param deviceId
	 * @param content
	 * @return
	 */
	public ByteBuf stashMQByteBuf(ByteBufAllocator allocator,Long timestamp,String deviceId,ByteBuf content) {
		
		 ByteBuf buf=allocator.buffer();
		 buf.writeByte(prefixchars[2]);
		 buf.writeLong(timestamp);
		 
		 byte[] bs=deviceId.getBytes();
		 buf.writeInt(bs.length);
		 buf.writeBytes(bs);
		 buf.writeBytes(content);
		 
		 return buf;
	}
	
	/**
	 * 构建一个保存消息对象
	 * @param allocator
	 * @param timestamp
	 * @param deviceId
	 * @param content
	 * @return
	 */
	public ByteBuf saveMQByteBuf(ByteBufAllocator allocator,Long timestamp,String deviceId,ByteBuf content) {
		
		 ByteBuf buf=allocator.buffer();
		 buf.writeByte(prefixchars[3]);
		 buf.writeLong(timestamp);
		 
		 byte[] bs=deviceId.getBytes();
		 buf.writeInt(bs.length);
		 buf.writeBytes(bs);
		 buf.writeBytes(content);
		 
		 return buf;
	}
	

	/**
	 * 解码
	 * @param buf
	 * @return
	 */
	public AdminMessage  dencoding(ByteBuf buf) {
		
		int type=buf.readByte();
	
		
		AdminMessage adminMessage=null;
		long  timestamp=0;
		String deviceId=null;
		byte [] bs=null;
		switch(type) {
			case  '+':
				bs =new byte[buf.readableBytes()];
				buf.readBytes(bs);
				adminMessage=new AdminMessage(MessageType.ONLINE, new String(bs));
				break;
			case  '-':
				bs =new byte[buf.readInt()];
				buf.readBytes(bs);
				
				byte[] bslast =new byte[buf.readInt()];
				buf.readBytes(bslast);
				
				adminMessage=new OfflineMessage(MessageType.OFFLINE, new String(bs),new String(bslast));
				
				break;
			case '&':
				
				timestamp=buf.readLong();
	
				bs=new byte[buf.readInt()];
				buf.readBytes(bs);
				deviceId=new String(bs);
				
				bs=new byte[buf.readableBytes()];
				buf.readBytes(bs);
				adminMessage=new StashMessage(MessageType.STASH, deviceId, timestamp, bs);
				break;
			case '*':
				
				/**
				 * 保存消息解码
				 */
				timestamp=buf.readLong();
				
				bs=new byte[buf.readInt()];
				buf.readBytes(bs);
				deviceId=new String(bs);
				
				bs=new byte[buf.readableBytes()];
				buf.readBytes(bs);
				adminMessage=new StashMessage(MessageType.SAVEMSG, deviceId, timestamp, bs);
				break;
				
				
		}
		
		return  adminMessage;
	}
	
	public static void main(String[] args) {
		

		ByteBufEncodingUtil bufEncodingUtil=ByteBufEncodingUtil.getInatance();
		
		
		ByteBuf content=ByteBufAllocator.DEFAULT.buffer();
		content.writeBytes("nihao".getBytes());
		ByteBuf buf=	bufEncodingUtil.saveMQByteBuf(ByteBufAllocator.DEFAULT, System.currentTimeMillis(), "11111", content);
		
		
		Object o= bufEncodingUtil.dencoding(buf);
		
		System.out.println(o);
		
	}
	
}
