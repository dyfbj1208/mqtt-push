package io.mqttpush.mqttserver.entity;

import java.io.Serializable;
import java.nio.channels.MulticastChannel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

/**
 * 
 * @author acer
 *
 */
public class CacheMsp implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8798384939363924701L;

	private Integer msgid;
	
	private String topname;
	
	ByteBuf byteBuf;
	
	byte[] content;
	
	public CacheMsp(){
		
	}

	public CacheMsp(ByteBuf byteBuf) {
		this.msgid = byteBuf.hashCode();
		this.byteBuf = byteBuf;
	}
	
	public CacheMsp(byte[] content){
		this.msgid=content.hashCode();
		this.byteBuf=Unpooled.wrappedBuffer(content);
		this.content=content;
	}

	public Integer getMsgid() {
		return msgid;
	}

	public void setMsgid(Integer msgid) {
		this.msgid = msgid;
	}

	public ByteBuf getByteBuf() {
		return byteBuf;
	}

	public void setByteBuf(ByteBuf byteBuf) {
		this.byteBuf = byteBuf;
	}

	public byte[] getContent() {
		if(content==null){
			byte[] bs=new byte[byteBuf.readableBytes()];
			byteBuf.readBytes(bs);
			 ReferenceCountUtil.release(byteBuf);
			return bs;
		}
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getTopname() {
		return topname;
	}

	public void setTopname(String topname) {
		this.topname = topname;
	}
	
	
}
