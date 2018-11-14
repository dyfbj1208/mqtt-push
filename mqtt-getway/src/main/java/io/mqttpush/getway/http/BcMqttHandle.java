package io.mqttpush.getway.http;

import io.mqttpush.getway.GetWayConstantBean;
import io.mqttpush.getway.common.Statistics;
import io.mqttpush.getway.http.controller.ControllBeans;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.stream.ChunkedWriteHandler;

import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理BC后端发过来的报文 接到报文并且丢给ab 端
 * 
 * @author tianzhenjiu
 *
 */
public class BcMqttHandle extends ChannelInboundHandlerAdapter {

	Logger logger = Logger.getLogger(getClass());

	/**
	 * 标志是否初始化了channnel
	 */
	static AtomicBoolean isInit = new AtomicBoolean(false);



	final GetWayConstantBean constantBean = GetWayConstantBean.instance();

	public BcMqttHandle() {

		if (!isInit.get()) {
			if (isInit.compareAndSet(false, true)) {
				initAbChannel();
			}

		}
	}
	
	

	/**
	 * 初始化连接的参数
	 */
	public void initAbChannel() {

		Bootstrap bootstrap = constantBean.httpCallbackStap;
		bootstrap.group(constantBean.httpCallbackgroup).channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch)  {
						ch.pipeline().addLast(new HttpClientCodec());
					}

				});
	}

	/**
	 * 只处理两种消息，即登录成功和收到发布消息
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof MqttConnAckMessage) {

			MqttConnAckMessage ackMessage = (MqttConnAckMessage) msg;

			switch (ackMessage.variableHeader().connectReturnCode()) {
			case CONNECTION_ACCEPTED:
				processLoginSuccess(ctx.channel());
				break;
			default:
				break;
			}
		} else if (msg instanceof MqttPublishMessage) {
			
			MqttPublishMessage publishMessage = (MqttPublishMessage) msg;
			replay(publishMessage,ctx);

			callbackMessage(ctx.channel(), publishMessage);

		} else {
			ctx.fireChannelRead(msg);
		}

	}

	
	/**
	 * 回复
	 * @param publishMessage
	 * @param ctx
	 */
	void replay(MqttPublishMessage publishMessage,ChannelHandlerContext ctx) {
		
	
		
		try {
			MqttPubAckMessage replMessage=null;
			MqttQoS mqttQoS=publishMessage.fixedHeader().qosLevel();
			
			if(mqttQoS.equals(MqttQoS.EXACTLY_ONCE)) {
				
				replMessage= new MqttPubAckMessage(
						new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_LEAST_ONCE, false, 0),
						MqttMessageIdVariableHeader.from(publishMessage.variableHeader().packetId()));
			
				
			}else if(mqttQoS.equals(MqttQoS.AT_LEAST_ONCE)) {
				
				replMessage= new MqttPubAckMessage(
						new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_LEAST_ONCE, false, 0),
						MqttMessageIdVariableHeader.from(publishMessage.variableHeader().packetId()));
			}
			
			
			if(replMessage!=null) {
				ctx.writeAndFlush(replMessage);
			}
		} catch (Exception e) {
				logger.warn("回复异常",e);
		}
	}
	
	/**
	 * 回调http 接口消息
	 * 
	 * @param bcChannel
	 * @param publishMessage
	 */
	public void callbackMessage(Channel bcChannel, MqttPublishMessage publishMessage) {

		String callback = bcChannel.attr(constantBean.bcHttpCallBackAttr).get();

		if (callback != null) {

			/**
			 * 匹配出host 和端口
			 */
			Pattern pattern = Pattern.compile("http://(\\w+((\\.\\w+)+))(:(\\d+))?");

			Matcher matcher = pattern.matcher(callback);
			String host = null;
			int port = 80;
			if (matcher.find()) {

				String gt = matcher.group(1);
				if (gt != null) {
					host = gt;
				}

				if (host != null && (gt = matcher.group(5)) != null) {
					port = Integer.parseInt(gt);
				}

			}

			if (host != null) {

				/**
				 * 连接回调服务器 并在连接成功之后 发消息
				 */
				ChannelFuture channelFuture = constantBean.httpCallbackStap.connect(host, port);
				final ByteBuf httpContent = publishMessage.content();
				channelFuture.addListener((ChannelFuture future) -> {
					if (future.isSuccess()) {
						
				
						
						httpContent.retain();
						
						DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
								HttpMethod.POST, callback, httpContent);
						defaultFullHttpRequest.headers().set(AbHttpHandle.CONTENT_TYPE, "application/json");
						defaultFullHttpRequest.headers().set(AbHttpHandle.CONTENT_LENGTH, httpContent.readableBytes());
						future.channel().writeAndFlush(defaultFullHttpRequest).addListener(ChannelFutureListener.CLOSE);
						/**
						 * http回调数+1
						 */
						Statistics.httpResCount.incrementAndGet();
						
						if(logger.isDebugEnabled()) {
							
							byte[] bs=new byte[httpContent.readableBytes()];
							httpContent.readBytes(bs);
							logger.debug("调用callback"+callback);
							
							logger.debug("content:"+new String(bs));
						}
					}
				});
			}else{
				logger.warn("回调需要找到http主机，现在没有找到host"+callback);
			}
		}else{

			logger.warn("为什么call地址为空?");
		}

	}

	/**
	 * 处理登录成功，发送缓存的消息
	 * 
	 * @param channel
	 */
	public void processLoginSuccess(Channel channel) {

		channel.attr(ControllBeans.loginKey).set(true);

		MqttPublishMessage mqttPublishMessages = null;

		while ((mqttPublishMessages = ControllBeans.mqttPublishMessages.poll()) != null) {
			channel.write(mqttPublishMessages);
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("登录成功,http写入MQTT");
		}

		channel.flush();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		/**
		 * http后端连接数+1
		 */
		Statistics.httpBcCount.incrementAndGet();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);

		/**
		 * http后端连接数-1
		 */
		Statistics.httpBcCount.decrementAndGet();
	}
}
