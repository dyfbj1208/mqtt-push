package io.mqttpush.mqttserver.beans;

import java.io.Serializable;
import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 消息实体类
 * @author acer
 *
 */
public class MsgRep implements  Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5090868774308374442L;
/**
 * 消息id
 * 用消息体的hashcode获取
 */
    private Integer messageid;
    /**
     * 消息主题
     */
    private String topname;
    /**
     * 发送方
     */
    private String sendiden;
    /**
     * 消息体
     */
    private byte[] content;
    
    /**
     * 消息的bytebuf 
     */
    private ByteBuf byteBuf;
    
    public MsgRep(){
    	
    }

	public MsgRep(String topname, String sendiden, byte[] content) {
		super();
		
		this.messageid = content.hashCode();
		this.topname = topname;
		this.sendiden = sendiden;
		this.content = content;
		
		
		byteBuf=Unpooled.wrappedBuffer(content);
	}
	
	public MsgRep(String topname, String sendiden, ByteBuf byteBuf) {
		super();
		this.messageid =byteBuf.hashCode();
		this.topname = topname;
		this.sendiden = sendiden;
		this.byteBuf=byteBuf;
	}

	public Integer getMessageid() {
		return messageid;
	}

	public void setMessageid(Integer messageid) {
		this.messageid = messageid;
	}

	public String getTopname() {
		return topname;
	}

	public void setTopname(String topname) {
		this.topname = topname;
	}

	public String getSendiden() {
		return sendiden;
	}

	public void setSendiden(String sendiden) {
		this.sendiden = sendiden;
	}

	public byte[] getContent() {
		if(content==null){
			byte[] bs=new byte[byteBuf.readableBytes()];
			byteBuf.readBytes(bs);
			return bs;
		}
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public ByteBuf getByteBuf() {
		return byteBuf;
	}

	public void setByteBuf(ByteBuf byteBuf) {
		this.byteBuf = byteBuf;
	}

	@Override
	public String toString() {
		return "MsgRep [messageid=" + messageid + ", topname=" + topname + ", sendiden=" + sendiden + ", content="
				+ Arrays.toString(content) + ", byteBuf=" + byteBuf + "]";
	}
    
   
    
    
   
    
}