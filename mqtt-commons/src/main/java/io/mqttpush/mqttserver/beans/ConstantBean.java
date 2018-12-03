package io.mqttpush.mqttserver.beans;

import java.util.Map;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * 常量
 * @author tianzhenjiu
 *
 */
public class ConstantBean {

	
	/**
	 * channel中的未确认报文 只会保存八个未确认报文
	 */
	public static final AttributeKey<SendableMsg> UnConfirmedKey = AttributeKey.valueOf("lastsent");
	
	
	public static final AttributeKey<String> LASTSENT_DEVICEID = AttributeKey.valueOf("lastSendDeviceId");
	/**
	 * channnel 获取deviceid的key
	 */
	public static   final AttributeKey<String> deviceKey = AttributeKey.valueOf("deviceId");
	
	/**
	 * 指示是否登录
	 */
	public static final AttributeKey<Boolean> loginKey = AttributeKey.valueOf("login");
	

	/**
	 * 当有旧的channel需要关闭的时候，旧的channel指示新的channel
	 */
	public static final AttributeKey<Channel> newChannel = AttributeKey.valueOf("newChannel");
	
	
	
	
	public static final int MAX_ERROR_SENT=3;
	
	
	
	/**
	 * 标识是系统发的
	 */
	public static final String  SYSTEM_IDENTIFY="/root/admin/reciv";
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
	
	
	public static final String ONE2ONE_CHAT_PREFIX="/root/chat/one2one/";
	
}
