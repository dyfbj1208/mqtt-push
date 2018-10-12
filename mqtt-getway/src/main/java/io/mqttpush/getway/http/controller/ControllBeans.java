package io.mqttpush.getway.http.controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.util.AttributeKey;

/**
 * 注册和管理所有的controll  对象
 * @author tianzhenjiu
 *
 */
public class ControllBeans {

	
	public static AttributeKey<Boolean> loginKey = AttributeKey.valueOf("login");
	
	public static AttributeKey<String> requestIdentifyKey = AttributeKey.valueOf("requestIdentify");
	
	
	public static BlockingQueue<MqttPublishMessage> mqttPublishMessages=new LinkedBlockingQueue<>(1024);
	
	private ControllBeans() {}
	
	private static ControllBeans controllBeans;
	
	private  FormController formController;
	
	private  FullTextController fullTextController;
	
	public static  ControllBeans getInstance() {
		
		if(controllBeans==null) {
			controllBeans=new ControllBeans();
		}
		
		return controllBeans;
	}  
	
	public FormController formController() {
		
		if(formController==null) {
			formController=new FormController();
		}
		return formController;
		
	}
	
	
	public FullTextController fullTextController() {
		
		if(fullTextController==null) {
			fullTextController=new FullTextController();
		}
		return fullTextController;
		
	}
}
