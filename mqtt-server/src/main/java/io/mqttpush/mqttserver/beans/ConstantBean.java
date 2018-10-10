package io.mqttpush.mqttserver.beans;

import io.netty.util.AttributeKey;

/**
 * 常量
 * @author tianzhenjiu
 *
 */
public class ConstantBean {

	
	/**
	 * channel 获取最后一个发送的消息对象
	 */
	public static final AttributeKey<SendableMsg> LASTSENT_KEY = AttributeKey.valueOf("lastsent");
	
	
	public static final AttributeKey<String> LASTSENT_DEVICEID = AttributeKey.valueOf("lastSendDeviceId");
	/**
	 * channnel 获取deviceid的key
	 */
	public static   final AttributeKey<String> deviceKey = AttributeKey.valueOf("deviceId");
	
	/**
	 * 指示是否登录
	 */
	public static final AttributeKey<Boolean> loginKey = AttributeKey.valueOf("login");
	
	
	public static final int MAX_ERROR_SENT=3;
	
	
	/**
	 * 管理员接收主题
	 * 这个主题可以收到发给管理员的消息
	 */
	public static final String  adminRecivTopic="/root/admin/reciv";
	
	/**
	 * 管理员发送给中间件的主题
	 * 这个主题可以收到管理员发的消息
	 */
	public static final String  adminSendTopic="/root/admin/send";
	
	/**
	 *点对点通信
	 */
	public static final String ONE2ONE_CHAT_PREFIX="/root/chat/one2one/";
	
}
