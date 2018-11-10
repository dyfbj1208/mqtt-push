package io.mqttpush.mqttserver.beans;

import io.netty.buffer.ByteBuf;

/**
 * 用于发送的消息对象
 * @author acer
 *
 */
public class SendableMsg extends MsgRep{

	private static final long serialVersionUID = -1043599050141437505L;
	
    /**
     * 发送方
     */
    private String sendDeviceId;
	
	 /**
	  * 保留标识
	  */
	private  boolean  retain;

	/**
	 * 重发次数
	 */
	private int dupTimes;
	
	
	byte[] bytesContent;
	
	public SendableMsg(String topname, String sendDeviceId, ByteBuf msgContent) {
		super(msgContent.hashCode(), topname, msgContent);
	}

	public boolean isRetain() {
		return retain;
	}


	public void setRetain(boolean retain) {
		this.retain = retain;
	}

	public String getSendDeviceId() {
		return sendDeviceId;
	}

	public void setSendDeviceId(String sendDeviceId) {
		this.sendDeviceId = sendDeviceId;
	}

	public int getDupTimes() {
		return dupTimes;
	}

	public void setDupTimes(int dupTimes) {
		this.dupTimes = dupTimes;
	}

	public byte[] getByteForContent() {
		
		
		if(bytesContent!=null) {
			return bytesContent;
		}
		ByteBuf msgContent=getMsgContent();
		if(msgContent==null||msgContent.refCnt()<=0) {
			return null;
		}
		
		msgContent.resetReaderIndex();
		bytesContent=new byte[msgContent.readableBytes()];
		msgContent.readBytes(bytesContent);
		msgContent.resetReaderIndex();
		return  bytesContent;
	}
}
