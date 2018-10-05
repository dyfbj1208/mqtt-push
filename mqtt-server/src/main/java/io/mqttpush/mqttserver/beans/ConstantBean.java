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
	 * 管理员主题
	 */
	public static final String  adminTopic="/root/admin";
}
