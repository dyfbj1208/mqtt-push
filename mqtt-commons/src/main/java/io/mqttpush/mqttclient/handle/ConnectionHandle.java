package io.mqttpush.mqttclient.handle;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import io.mqttpush.mqttclient.conn.Connetor;
import io.mqttpush.mqttclient.conn.Status;
import io.mqttpush.mqttclient.service.ApiService;
import io.mqttpush.mqttclient.service.DefaultApiService;
import io.mqttpush.mqttserver.beans.SendableMsg;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * 
 * @author tianzhenjiu
 *
 */
@Sharable
public class ConnectionHandle extends ChannelInboundHandlerAdapter {

	Logger logger = Logger.getLogger(getClass());

	final String username;
	final String password;
	final String deviceId;
	final ApiService apiService;
	final Connetor connetor;
	PingRunnable pingRunnable;

	
	
	
	final BlockingQueue<TopicAndQos> topics=new LinkedBlockingQueue<>(4);

	public ConnectionHandle(Connetor connetor, ApiService apiService, String deviceId,
			String username, String password) {
		super();
		
		this.connetor = connetor;
		this.deviceId = deviceId;
		this.username = username;
		this.password = password;
		this.apiService = (apiService == null) ? DefaultApiService.intance() : apiService;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		apiService.setChannel(ctx.channel());
		apiService.login(deviceId, username, password);
		
		ctx.channel().closeFuture().addListener((ChannelFuture future)->{
			connetor.reconnection(ctx.channel());
		});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.warn("链路异常"+ cause);
		ctx.fireExceptionCaught(cause);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

		logger.warn("链路关闭,将会重新连接");
		AtomicBoolean isLogin=Status.isLogin;
		isLogin.set(false);
		super.channelInactive(ctx);

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof MqttMessage) {

			MqttMessage message = (MqttMessage) msg;
			MqttFixedHeader fixedHeader = message.fixedHeader();
			MqttMessageType messageType = fixedHeader.messageType();

			switch (messageType) {
			
			case DISCONNECT:
				ctx.close();
				break;
				
				
			case CONNACK:
				ack(ctx, (MqttConnAckMessage) message);
		
			default:// 如果有消息来了就置为可用,因为这里的default匹配的一定是其publish 或sub消息类型
				whenPongResp();
				ctx.fireChannelRead(msg);
				break;
			}
		} else
			ctx.close();

	}

	public void ack(ChannelHandlerContext ctx, MqttConnAckMessage ackMessage) {

		switch (ackMessage.variableHeader().connectReturnCode()) {

		case CONNECTION_ACCEPTED:
			AtomicBoolean isLogin=Status.isLogin;
			isLogin.set(true);
			List<TopicAndQos> andQos=new LinkedList<>();
			topics.drainTo(andQos);
			andQos.forEach((t)->{
				subTopic(t.topicName, t.mqttQoS);
			});
			logger.info("登录成功");
			break;
		default:
			if (logger.isDebugEnabled()) {
				// 登录失败
				logger.warn("登录失败" + ackMessage.variableHeader().connectReturnCode());
			}
			break;
		}
	}
	
	

	/**
	 * 订阅
	 * @param topicName
	 * @param mqttQoS
	 */
	public void subTopic(String topicName) {
		
		subTopic(topicName, MqttQoS.AT_MOST_ONCE);
	}
	/**
	 * 订阅
	 * @param topicName
	 * @param mqttQoS
	 */
	public void subTopic(String topicName,MqttQoS mqttQoS) {
		
		AtomicBoolean isLogin=Status.isLogin;
		if(!isLogin.get()) {
			topics.offer(new TopicAndQos(topicName, mqttQoS));
		}else {			
			apiService.subscribe(topicName, mqttQoS);
		}
	}
	/**
	 * 当收到pong报文的时候
	 */
	public void whenPongResp() {
		if(pingRunnable!=null) {			
			pingRunnable.updatehasResp(true);
		}
		else {
			logger.warn("为什么pingrunnable还是空的?");
		}
	}

	/**
	 * @param pingRunnable the pingRunnable to set
	 */
	public void setPingRunnable(PingRunnable pingRunnable) {
		this.pingRunnable = pingRunnable;
	}
	

	static class TopicAndQos{
		
		String topicName;
		MqttQoS mqttQoS;
		public TopicAndQos(String topicName, MqttQoS mqttQoS) {
			super();
			this.topicName = topicName;
			this.mqttQoS = mqttQoS;
		}
		
		
		
	}

	
	
}
