package io.mqttpush.mqttserver.beans;

import io.netty.buffer.ByteBuf;

/**
 * 用于发送的消息对象
 * @author acer
 *
 */
public class SendableMsg extends MsgRep{

	
	 /**
	 * 
	 */
	private static final long serialVersionUID = -1043599050141437505L;
	 

	 
	 private Short shortmsgid;
	 
	 
	 /**
	  * 保留标识
	  */
	 boolean  retain;
	 
	 /**
	  * 重发标识
	  */
	 int  dupTimes;
	 
	 
	 public SendableMsg(){
		 
	 }
	 

	public SendableMsg(String topname, String sendclientid, ByteBuf msgContent) {
		super(topname,sendclientid,msgContent);
		shortmsgid=getMessageid().shortValue();
	}
	
	public SendableMsg(String topname, String sendclientid, byte[] bs) {
		super(topname,sendclientid,bs);
		shortmsgid=getMessageid().shortValue();
	}


	public boolean isRetain() {
		return retain;
	}


	public void setRetain(boolean retain) {
		this.retain = retain;
	}


	public int getDupTimes() {
		return dupTimes;
	}


	public void setDupTimes(int dupTimes) {
		this.dupTimes = dupTimes;
	}

	public Short getShortmsgid() {
		return shortmsgid;
	}

	public void setShortmsgid(Short shortmsgid) {
		this.shortmsgid = shortmsgid;
	}
}
