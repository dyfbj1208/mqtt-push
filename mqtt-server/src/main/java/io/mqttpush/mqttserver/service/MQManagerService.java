package io.mqttpush.mqttserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import io.mqttpush.mqttserver.entity.DeviceMq;
import io.mqttpush.mqttserver.entity.MsgRep;
import io.mqttpush.mqttserver.entity.SendMsgStatus;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 管理消息队的服务 管理设备的消息队列
 * 
 * @author acer
 *
 */
public class MQManagerService {

	/**
	 * 消息池大容量 超过容量存redis
	 */
	private int msgrepsize = 0;

	/**
	 * 用户消息队列容量 超过了存redis
	 */
	private int usermsgsize = 1;

	private int CORE_SIZE = 4;

	Logger logger = Logger.getLogger(getClass());
	

	@Autowired
	AnsyncService ansyncService;
	/**
	 * 未成功发送的队列
	 */
	BlockingQueue<SendMsgStatus> unsucQue;

	
	/**
	 * 消息 队列
	 */
	ConcurrentHashMap<Integer, MsgRep> msgRepsque;

	ConcurrentHashMap<String, BlockingQueue<SendMsgStatus>> deviceSendableMQ;

	public MQManagerService() {

		unsucQue = new LinkedBlockingQueue<>(10);
		msgRepsque = new ConcurrentHashMap<>();
		deviceSendableMQ = new ConcurrentHashMap<>();
	}

	/**
	 * 将消息存储 如果消息缓存池已经满了就存储redis 存储redis的时候使用的
	 * 
	 * @param msgRep
	 * @return
	 */
	public boolean pushMsg(MsgRep msgRep) {

		logger.debug("添加信息" + msgRep);

		if (msgRepsque.size() >= msgrepsize) {
			synchronized (msgRepsque) {

				if (msgRepsque.size() > msgrepsize) {
					// ansyncService.runTask(() -> {
					msgRepsque.forEach((k, v) -> {
						//redisService.saveSerializable("MSGREP:", "" + k, v);
						//TODO  把不能发送的消息保存起来
					});
					msgRepsque.clear();
					// });

				}
			}
		}

		msgRepsque.put(msgRep.getMessageid(), msgRep);

		return true;
	}

	/**
	 * 存储用户消息队列
	 * 
	 * @param deviceId
	 * @param sendMsgStatus
	 * @return
	 */
	public boolean pushDeviceMq(String deviceId, final SendMsgStatus sendMsgStatus) {

		BlockingQueue<SendMsgStatus> blockingQueue = null;
		if (!deviceSendableMQ.containsKey(deviceId)) {
			blockingQueue = new LinkedBlockingQueue<>(usermsgsize);
			deviceSendableMQ.put(deviceId, blockingQueue);
		} else
			blockingQueue = deviceSendableMQ.get(deviceId);

		if (blockingQueue != null) {

			logger.debug(deviceId + "不在线：store" + sendMsgStatus);
			if (!blockingQueue.offer(sendMsgStatus)) {

				synchronized (blockingQueue) {

					if (!blockingQueue.offer(sendMsgStatus)) {

						final List<SendMsgStatus> list = new ArrayList<SendMsgStatus>();
						blockingQueue.drainTo(list);
						blockingQueue.offer(sendMsgStatus);
						//redisService.saveList("DEVICEMQ:" + deviceId, "" + sendMsgStatus.getMesssageid());
						// TODO redis save
						
					}

				}
			}
		}

		return true;
	}

	public void checkMsgAndPush(Integer msgid, ByteBuf byteBuf) {

		if (!msgRepsque.containsKey(msgid)) {
			pushMsg(new MsgRep());
		}
	}

	/**
	 * 获取消息 此方法应该在 用户线程执行
	 * 
	 * @param deviceId
	 * @return
	 */
	public DeviceMq getDeviceQueMsg(String deviceId) {

		SendMsgStatus msgStatus = null;
		BlockingQueue<SendMsgStatus> blockingQueue = null;
		MsgRep msgRep = null;
		if (deviceSendableMQ.containsKey(deviceId)) {
			blockingQueue = deviceSendableMQ.get(deviceId);
		}

		if (blockingQueue != null && !blockingQueue.isEmpty()) {
			msgStatus = blockingQueue.poll();
		} else {
//			String msgid = redisService.popList("DEVICEMQ:" + deviceId);
//			if (msgid != null) {
//				try {
//					msgStatus = new SendMsgStatus(Integer.parseInt(msgid), SendMsgStatus.MsgStatus.SEND_FAIL);
//				} catch (NumberFormatException e) {
//					logger.info("异常", e);
//				}
//			}
			// TODO  从缓存拿消息发送

		}

		if (msgStatus != null) {
			msgRep = getMsgRep(msgStatus.getMesssageid());
			return new DeviceMq(msgRep, true);
		}

		return new DeviceMq(false);
	}

	/**
	 * 获取消息
	 * 
	 * @param msgid
	 * @param sendRunable
	 * @return
	 */
	public MsgRep getMsgRep(final Integer msgid) {

		if (msgRepsque.containsKey(msgid))
			return msgRepsque.get(msgid);

		MsgRep msgRep = null;
//		try {
//			msgRep = (MsgRep) redisService.findObj(MsgRep.class, "MSGREP:" + msgid);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		if (msgRep != null) {
//			if (msgRep.getByteBuf() == null) {
//				byte[] bs = msgRep.getContent();
//				if (bs != null && bs.length > 0)
//					msgRep.setByteBuf(Unpooled.wrappedBuffer(bs));
//			}
//			pushMsg(msgRep);
//		}
		
		// TODO  从缓存获取消息

		return msgRep;
	}

	public boolean pushUnSuccessMQ(SendMsgStatus sendMsgStatus) {

		return false;
	}
}
