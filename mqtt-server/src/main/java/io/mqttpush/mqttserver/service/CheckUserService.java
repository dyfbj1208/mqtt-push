package io.mqttpush.mqttserver.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;

/**
 * 检查用户名密码的服务
 * 这里很简单的处理了
 * @author acer
 * 
 */
public class CheckUserService {

	Logger logger = Logger.getLogger(getClass());

	Map<String, String> userpwds = new ConcurrentHashMap<String, String>();

	boolean init;

	public void init() {

		if (!init) {
			userpwds.put("admin", "admin123456");
			userpwds.put("user", "user123456");

		}

	}
	
	public CheckUserService(){
		init();
	}

	

	/**
	 * 验证用户名密码 返回一个验证结果
	 * 
	 * @param validate
	 * @return
	 */
	public MqttConnectReturnCode checkUserReturnCode(String username,String password) {

		MqttConnectReturnCode returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED;

		String key = username;
		if (userpwds.containsKey(key)) {
			String pwd = userpwds.get(key);
			if (pwd.equals(password)) {
				returnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;
			} else {
				returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;

				if (logger.isInfoEnabled()) {
					logger.info(key + "用户名验证失败");
				}
			}

		} else {
			returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
		}

		return returnCode;

	}
}
