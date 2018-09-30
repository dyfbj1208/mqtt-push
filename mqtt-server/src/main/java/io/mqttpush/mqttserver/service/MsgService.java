//package io.mqttpush.mqttserver.service;
//
//import io.mqttpush.mqttserver.entity.MsgRep;
//
///**
// * 消息保持到reids
// * @author acer
// *
// */
//public class MsgService extends RedisService{
//
//	/**
//	 * 保存MsgRep
//	 * @param msgRep
//	 */
//	public  void  saveMsgRep(MsgRep msgRep){
//		
//		super.saveSerializable("msgreps:", msgRep.getMessageid().toString(), msgRep);
//	}
//	
////	/**
////	 * 查询msgrep
////	 * @param ids
////	 * @return
////	 * @throws Exception
////	 */
////	public  List<MsgRep> findMsgs(Integer... ids) throws Exception{
////		
////		List<String> keys=new ArrayList<>();
////		for (Integer id:ids) {
////			keys.add("msgreps:"+id);
////		}
////		return  super.findObj(MsgRep.class, keys.toArray(new String[]{}));
////		
////		 
////	}
//	
////	/**
////	 * 保存成功发送的消息(只有消息id,接收方和发送方这些数据)
////	 * @param msgRep
////	 */
////	public  void  saveSendmsg(SendMsg sendMsg){
////		
////		StringBuilder builder=new StringBuilder("sucmessage:");
////		builder.append(sendMsg.getSendclientid()+":");
////		builder.append(sendMsg.getClientid()+":");
////		
////		super.saveList(builder.toString(), ""+sendMsg.getSendmsgid());
////	}
//	
//	
//}
