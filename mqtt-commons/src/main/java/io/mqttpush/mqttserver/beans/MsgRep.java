package io.mqttpush.mqttserver.beans;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;

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
    private Integer messageId;
    /**
     * 消息主题
     */
    private String topName;
    
    /**
     * 消息的bytebuf 
     */
    private ByteBuf msgContent;



	public MsgRep(Integer messageId, String topName, ByteBuf msgContent) {
		super();
		this.messageId = messageId;
		this.topName = topName;
		this.msgContent = msgContent;
	}

	public Integer getMessageId() {
		return messageId;
	}

	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}

	public String getTopName() {
		return topName;
	}

	public void setTopName(String topName) {
		this.topName = topName;
	}


	public ByteBuf getMsgContent() {
		return msgContent;
	}


	public void setMsgContent(ByteBuf msgContent) {
		this.msgContent = msgContent;
	}

}