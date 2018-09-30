package io.mqttpush.mqttserver.entity;

import java.io.Serializable;

/**
 * 消息发送状态实体
 * @author tzj
 *
 */
public class SendMsgStatus implements Serializable{
	
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -4967255581521722941L;


	private Integer messsageid;


    private MsgStatus sendstate;
    
    public SendMsgStatus(){
    	
    }
    

	public SendMsgStatus(Integer messsageid, MsgStatus sendstate) {
		super();
		this.messsageid = messsageid;
		this.sendstate = sendstate;
	}


	public static  enum MsgStatus{
    	
    	READY_SEND(1),
    	SEND_SUCCESS(2),
    	SEND_FAIL(3);
 
    	
    	private final int value;

    	MsgStatus(int value){
    		this.value=value;
    	}
   

        public int value() {
            return value;
        }
    	
    	public static MsgStatus valueOf(int value) {
            for (MsgStatus q: values()) {
                if (q.value == value) {
                    return q;
                }
            }
            throw new IllegalArgumentException("invalid STARUS: " + value);
        }
    }

	public Integer getMesssageid() {
		return messsageid;
	}

	public void setMesssageid(Integer messsageid) {
		this.messsageid = messsageid;
	}


	public MsgStatus getSendstate() {
		return sendstate;
	}


	public void setSendstate(MsgStatus sendstate) {
		this.sendstate = sendstate;
	}


	@Override
	public String toString() {
		return "SendMsgStatus [messsageid=" + messsageid + ", sendstate=" + sendstate + "]";
	}

	

    
    
}