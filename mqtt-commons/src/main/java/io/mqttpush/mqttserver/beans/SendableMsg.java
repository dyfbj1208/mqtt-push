package io.mqttpush.mqttserver.beans;

import io.netty.buffer.ByteBuf;

/**
 * 用于发送的消息对象
 * @author tzj
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
	 * 重发标记
	 */
	private boolean  isDup;
	/**
	 * 重发次数
	 */
	private int dupTimes;

	/**
	 * 消息内容的字节数组形式
	 * 凡是发送都是bytebu,凡是暂存都是字节数组
	 */
	byte[] bytesContent;
	
	public SendableMsg(String topname, String sendDeviceId, ByteBuf msgContent) {
		super(msgContent.hashCode(), topname, msgContent);
		this.setSendDeviceId(sendDeviceId);
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

	public boolean isDup() {
		return isDup;
	}

	public void setDup(boolean dup) {
		isDup = dup;
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
