package io.mqttpush.mqttserver.service;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.beans.ConstantBean;
import io.mqttpush.mqttserver.beans.ServiceBeans;
import io.mqttpush.mqttserver.util.ByteBufEncodingUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;

/**
 * 管理者登录信息的设备号以及channel
 * 
 * @author tzj
 *
 */
public class ChannelUserService {

	Logger logger = Logger.getLogger(getClass());


	/**
	 * 用于根据登录的客户端标识找channel
	 * 
	 * 这里不需要线程安全的Map
	 */
	ConcurrentHashMap<String, Channel> str2channel = new ConcurrentHashMap<>(64);
	
	
	MessagePushService messagePushService;
	/**
	 * 退出
	 * 
	 * @param handlerContext
	 */
	public void loginout(Channel channel) {
	
		String deviceId = deviceId(channel);
			if(deviceId==null) {
				return;
			}
			
			synchronized (deviceId) {
				
				
				if (str2channel.containsKey(deviceId)){
					if(str2channel.get(deviceId)==channel){
						str2channel.remove(deviceId);
					}
				}else {
					logger.info(deviceId+"已经关闭，难道是触发了两次关闭?");
					return;
				}
	
				 String lastDeviceId=null;
				 if(channel.hasAttr(ConstantBean.LASTSENT_DEVICEID)) {
					 lastDeviceId=channel.attr(ConstantBean.LASTSENT_DEVICEID).get();
				 }
				ByteBufEncodingUtil bufEncodingUtil=ByteBufEncodingUtil.getInatance();
				getmessagePushService().send2Admin(bufEncodingUtil.offlineBytebuf(channel.alloc(), deviceId,lastDeviceId));
				if(logger.isDebugEnabled()) {
					logger.debug(deviceId + "退出,在线人数\t" + str2channel.size());
				}
			}
			

	}
	
	


	/**
	 * 成功登录
	 * 剔除以前的连接
	 * 
	 * @param ident
	 * @param channel
	 */
	public void processLoginSuccess(String deviceId, Channel channel) {

		
		synchronized (deviceId) {
			
			final Channel channelOld =  str2channel.put(deviceId, channel);
			ChannelFuture channelFuture=null;
			if (channelOld != null) {
				
				/**
				 * 
				 * 算了，直接关闭了拉到
				 * 
				 */
				channelFuture=channelOld.close();
				channelFuture.addListener((ChannelFuture closeFuture)->{
					
					 if(closeFuture.isSuccess()) {
						 registerAndNotice(channelOld, deviceId);
					 }else {
						 logger.error("旧的关闭失败，新的也不能上线");
					 }
					
				});
				
				
			}else {
				registerAndNotice(channel, deviceId);
			}
			
		
			
		}
		

	}
	
	/**
	 * 注册并且发送通知
	 * @param channel
	 * @param deviceId
	 */
	private void registerAndNotice(Channel channel,String deviceId) {
		
		channel.attr(ConstantBean.deviceKey).set(deviceId);
		channel.attr(ConstantBean.loginKey).set(Boolean.TRUE);
		
		ByteBufEncodingUtil bufEncodingUtil=ByteBufEncodingUtil.getInatance();
		if(isAdmin(deviceId)) {
			if(logger.isDebugEnabled()) {
				logger.debug("admin上线"+channel.remoteAddress());
			}
		}else {
			getmessagePushService().send2Admin(bufEncodingUtil.onlineBytebuf(channel.alloc(), deviceId));
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug(deviceId + "登录成功,在线人数\t" + str2channel.size());
		}
	}

	/**
	 * 是否登录
	 * 
	 * @param deviceId
	 * @return
	 */
	public boolean isLogin(String deviceId) {
		return str2channel.containsKey(deviceId);
	}

	/**
	 * 是否登录
	 * 
	 * @param deviceId
	 * @return
	 */
	public boolean isLogin(Channel channel) {

		return channel != null && channel.hasAttr(ConstantBean.loginKey);
	}

	/**
	 * 根据信道返回设备id
	 * 
	 * @param channel
	 * @return
	 */
	public String deviceId(Channel channel) {

		if (isLogin(channel)) {

			return channel.attr(ConstantBean.deviceKey).get();
		}

		return null;
	}

	/**
	 * 根据设备号获取channel
	 * @param deviceId
	 * @return
	 */
	public Channel channel(String deviceId) {
		return str2channel.get(deviceId);
	}
	
	
	/**
	 * 判断这个设备是不是admin
	 * @param deviceId
	 * @return
	 */
	public boolean isAdmin(String deviceId) {
		
		 return deviceId.startsWith("admin");
	}
	
	MessagePushService getmessagePushService() {
		
		if(messagePushService==null) {
			messagePushService=ServiceBeans.getInstance().getMessagePushService();
		}
		return  messagePushService;
	}

}
