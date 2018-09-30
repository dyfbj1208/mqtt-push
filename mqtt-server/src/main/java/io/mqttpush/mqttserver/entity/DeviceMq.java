package io.mqttpush.mqttserver.entity;

/**
 * 
 * @author acer
 *
 */
public class DeviceMq {

	MsgRep msgRep;
	
	boolean next;

	public DeviceMq(boolean next){
		this.next=next;
	}
	
	public DeviceMq(MsgRep msgRep, boolean next) {
		super();
		this.msgRep = msgRep;
		this.next = next;
	}

	public MsgRep getMsgRep() {
		return msgRep;
	}

	public void setMsgRep(MsgRep msgRep) {
		this.msgRep = msgRep;
	}

	public boolean isNext() {
		return next;
	}

	public void setNext(boolean next) {
		this.next = next;
	}

	@Override
	public String toString() {
		return "DeviceMq [msgRep=" + msgRep + ", next=" + next + "]";
	}
	
	
}
