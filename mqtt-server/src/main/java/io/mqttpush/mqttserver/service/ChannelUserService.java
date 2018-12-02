package io.mqttpush.mqttserver.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.beans.ConstantBean;
import io.mqttpush.mqttserver.beans.ServiceBeans;
import io.mqttpush.mqttserver.util.ByteBufEncodingUtil;
import io.mqttpush.mqttserver.util.thread.MyHashRunnable;
import io.mqttpush.mqttserver.util.thread.SignelThreadPoll;
import io.netty.channel.Channel;

/**
 * 管理者登录信息的设备号以及channel
 * 
 * @author tzj
 *
 */
public class ChannelUserService {

	Logger logger = Logger.getLogger(getClass());

	/**
	 * thread的map 无需锁 无需线程切换
	 */
	ThreadLocal<Map<String, Channel>> threadstr2channel = new ThreadLocal<Map<String, Channel>>() {
		@Override
		protected Map<String, Channel> initialValue() {
			return new HashMap<>();
		}

	};

	MessagePushService messagePushService;
	
	SignelThreadPoll signelThreadPoll;

	/**
	 * 退出
	 * 
	 * @param handlerContext
	 */
	public void loginout(Channel channel) {

	

		String deviceId = deviceId(channel);
		Channel newChannel=channel.attr(ConstantBean.newChannel).get();
		if (deviceId == null) {
			logger.warn("为什么设备号为空?"+deviceId);
			return;
		}
		logger.info(deviceId + "关闭" + channel.remoteAddress());
		
		Runnable runnable=()->{
			/**
			 * 先关闭 然后再注册
			 */
			processCloseChannel(deviceId, channel);
			if(newChannel!=null) {
				registerAndNotice(deviceId, newChannel);
			}
		};
		
		getSignelThreadPoll().execute(new MyHashRunnable(deviceId, runnable, 0));
		

		
	}

	/**
	 * 清理channel和通知admin
	 * @param deviceId
	 * @param channel
	 */
	private void processCloseChannel(String deviceId,Channel channel) {
		
		Map<String, Channel> str2channel = threadstr2channel.get();
		if (str2channel.containsKey(deviceId)) {
			
			/**
			 * 如果当前设备号对应的channel和要关闭的channel相等 就说明是正常下线，就要移除
			 * 否则就是被挤下线，那么不能移除deviceID
			 */
			if (str2channel.get(deviceId) == channel) {
				str2channel.remove(deviceId);
			}
		} else {
			logger.info(deviceId + "已经关闭，难道是触发了两次关闭?");
			return;
		}

		String lastDeviceId = null;
		if (channel.hasAttr(ConstantBean.LASTSENT_DEVICEID)) {
			lastDeviceId = channel.attr(ConstantBean.LASTSENT_DEVICEID).get();
		}
		ByteBufEncodingUtil bufEncodingUtil = ByteBufEncodingUtil.getInatance();
		getmessagePushService().send2Admin(bufEncodingUtil.offlineBytebuf(channel.alloc(), deviceId, lastDeviceId));
	
		if (logger.isDebugEnabled()) {
			logger.debug(deviceId + "退出,在线人数\t" + str2channel.size());
		}
	}
	/**
	 * 成功登录 剔除以前的连接
	 * 
	 * @param ident
	 * @param channel
	 */
	public void processLoginSuccess(String deviceId, Channel channel) {

		Map<String, Channel> str2channel = threadstr2channel.get();
		final Channel channelOld = str2channel.put(deviceId, channel);
		if (channelOld != null) {
			/**
			 * 
			 * 算了，直接关闭了拉到
			 * 然后设置旧的channel对应的新channel
			 * 这个新的cahnnel只有在旧的channel logout的时候才会注册
			 * 
			 */
			logger.info(channelOld.remoteAddress()+"关闭设备旧的channel->"+deviceId);
			
			if(channelOld.isActive()) {
				channelOld.close();
				channelOld.attr(ConstantBean.newChannel).set(channel);
			}else {
				registerAndNotice(deviceId,channel);
			}
			

		} else {
			registerAndNotice(deviceId,channel);
		}

	}

	/**
	 * 注册并且发送通知
	 * 
	 * @param channel
	 * @param deviceId
	 */
	private void registerAndNotice(String deviceId,Channel channel) {

	
		channel.attr(ConstantBean.deviceKey).set(deviceId);
		channel.attr(ConstantBean.loginKey).set(Boolean.TRUE);

		ByteBufEncodingUtil bufEncodingUtil = ByteBufEncodingUtil.getInatance();
		if (isAdmin(deviceId)) {
			if (logger.isDebugEnabled()) {
				logger.debug("admin上线" + channel.remoteAddress());
			}
		} else {
			getmessagePushService().send2Admin(bufEncodingUtil.onlineBytebuf(channel.alloc(), deviceId));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("登录"+deviceId + "->" + channel.remoteAddress() );
		}
	}

	/**
	 * 是否登录
	 * 
	 * @param deviceId
	 * @return
	 */
	public boolean isLogin(String deviceId) {
		Map<String, Channel> str2channel = threadstr2channel.get();
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
	 * 无需当前线程也可以运行
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
	 * 必须保证运行在当前线程里面
	 * @param deviceId
	 * @return
	 */
	public Channel channel(String deviceId) {
		Map<String, Channel> str2channel = threadstr2channel.get();
		return str2channel.get(deviceId);
	}

	/**
	 * 判断这个设备是不是admin
	 * 
	 * @param deviceId
	 * @return
	 */
	public boolean isAdmin(String deviceId) {

		return deviceId.startsWith("admin");
	}

	MessagePushService getmessagePushService() {

		if (messagePushService == null) {
			messagePushService = ServiceBeans.getInstance().getMessagePushService();
		}
		return messagePushService;
	}

	public SignelThreadPoll getSignelThreadPoll() {
		
		if(signelThreadPoll==null) {
			signelThreadPoll=ServiceBeans.getInstance().getSignelThreadPoll();
		}
		return signelThreadPoll;
	}

	
	

}
