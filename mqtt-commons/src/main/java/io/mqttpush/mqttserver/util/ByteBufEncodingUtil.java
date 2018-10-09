package io.mqttpush.mqttserver.util;

import io.mqttpush.mqttserver.util.ChatMessage.MessageType;
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
	public static final  char[] prefixchars= {'+','-','&'};
	
	
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
	 * @param deviceId
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
	 * @param deviceId
	 * @return
	 */
	public ByteBuf offlineBytebuf(ByteBufAllocator allocator,String deviceId) {
		 ByteBuf buf=allocator.buffer();
		 buf.writeByte(prefixchars[1]);
		 buf.writeBytes(deviceId.getBytes());
		 
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
	public ByteBuf stashMQByteBuf(ByteBufAllocator allocator,Long timestamp,String deviceId,byte[] content) {
		
		 ByteBuf buf=allocator.buffer();
		 buf.writeByte(prefixchars[2]);
		 buf.writeLong(timestamp);
		 buf.writeInt(deviceId.length());
		 buf.writeBytes(deviceId.getBytes());
		 buf.writeBytes(content);
		 
		 return buf;
	}
	

	/**
	 * 解码
	 * @param buf
	 * @return
	 */
	public ChatMessage  dencoding(ByteBuf buf) {
		
		int type=buf.readByte();
	
		
		ChatMessage adminMessage=null;
		byte [] bs=null;
		switch(type) {
			case  '+':
				bs =new byte[buf.readableBytes()];
				buf.readBytes(bs);
				adminMessage=new ChatMessage(MessageType.ONLINE, new String(bs));
				break;
			case  '-':
				bs =new byte[buf.readableBytes()];
				buf.readBytes(bs);
				adminMessage=new ChatMessage(MessageType.OFFLINE, new String(bs));
				break;
			case '&':
				
				long  timestamp=buf.readLong();
	
				bs=new byte[buf.readInt()];
				buf.readBytes(bs);
				String deviceId=new String(bs);
				
				bs=new byte[buf.readableBytes()];
				buf.readBytes(bs);
				adminMessage=new StashMessage(MessageType.STASH, deviceId, timestamp, bs);
				break;
		}
		
		return  adminMessage;
	}
	
	public static void main(String[] args) {
		
		
//		ByteBuf buf=PackatUtil.getInatance().offlineBytebuf(ByteBufAllocator.DEFAULT, "1111");
//		buf.readableBytes();
//		System.out.println((char)buf.readByte());
//		
//		
//		byte []bs =new byte[buf.readableBytes()];
//		buf.readBytes(bs);
//		System.out.println(new String(bs));
		
		
//		ByteBuf buf=ByteBufEncodingUtil.getInatance().
//				stashMQByteBuf(ByteBufAllocator.DEFAULT, System.currentTimeMillis(), "111", new byte[] {1,2,34,56});
//		
//		
//		
//		System.out.println((char)buf.readByte());
//		
//		System.out.println(buf.readLong());
//		
//		int strlen=buf.readInt();
//		
//		byte[] bs=new byte[strlen];
//		buf.readBytes(bs);
//		System.out.println(new String(bs));
//		
//		bs=new byte[buf.readableBytes()];
//		
//		System.out.println(bs.length);
		
		ByteBufAllocator allocator= ByteBufAllocator.DEFAULT;
		
		ByteBuf buf=ByteBufEncodingUtil.getInatance().
				stashMQByteBuf(allocator, System.currentTimeMillis(), "111", new byte[] {1,2,34,56});
		
		buf.resetReaderIndex();
		System.out.println( ByteBufEncodingUtil.getInatance().dencoding(buf));
		
	}
	
}
