package io.mqttpush.mqttclient.conn;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import io.mqttpush.mqttclient.handle.ConnectionHandle;
import io.mqttpush.mqttclient.handle.PingRunnable;
import io.mqttpush.mqttclient.handle.PubHandle;
import io.mqttpush.mqttclient.handle.SubHandle;
import io.mqttpush.mqttclient.service.ApiService;
import io.mqttpush.mqttclient.service.MessageListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.GenericFutureListener;

public class Connetor {
	
	
	Logger logger=Logger.getLogger(Connetor.class);
	final EventLoopGroup group = new NioEventLoopGroup();
	final Bootstrap bootstrap;

	final Properties properties;
	final ApiService apiService;
	final MessageListener defaultMessageListener;
	ChannelFuture nowCloseFuture;
	
	AtomicBoolean isValidate=new AtomicBoolean(false);
	
	final String host;
	final Integer port;
	
	ChannelFuture channelFuture;
	
	public Connetor(Properties properties, ApiService apiService, MessageListener defaultMessageListener) {
		super();
		this.properties = properties;
		this.apiService = apiService;
		this.defaultMessageListener = defaultMessageListener;
		bootstrap=new Bootstrap();
		
		host = (String) properties.getOrDefault("host", "127.0.0.1");
		port = (Integer) properties.getOrDefault("port", 1000);
		
		init();
	}
	
	
	
	/**
	 * 初始化
	 */
	public void  init() {
	
		final Integer pingtime = (Integer) properties.getOrDefault("pingtime", 60);
		final String deviceId = (String) properties.getOrDefault("deviceId", "0000");
		final String username = (String) properties.getOrDefault("username", "user");
		final String password = (String) properties.getOrDefault("password", "user123456");
		final String subTopic = (String) properties.getOrDefault("subTopic", null);
		

		bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
		.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				p.addLast(
						new ReadTimeoutHandler(pingtime*2),
						MqttEncoder.INSTANCE, new MqttDecoder(),
						new ConnectionHandle(isValidate,apiService,deviceId, username, password,subTopic),
						new PubHandle(defaultMessageListener), new SubHandle());
			}
		});
		
		
	}
	
	
	/**
	 * 连接
	 * @return
	 */
	public ChannelFuture connection() {

			ChannelFuture channelFuture= bootstrap.connect(host, port);
			
			
			final ScheduledExecutorService executorService= Executors.newScheduledThreadPool(2);
			
			channelFuture.addListener(new GenericFutureListener<ChannelFuture>() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if(future.isSuccess()) {
						
						PingRunnable runnable=new PingRunnable(future.channel(), isValidate, Connetor.this,executorService);
						executorService.schedule(runnable, 1, TimeUnit.MINUTES);
						isValidate.set(true);
						if(logger.isDebugEnabled()) {
							logger.debug("连接成功"+host+":"+port);
					
						}
					}
					else {
						executorService.schedule(new Runnable() {
							@Override
							public void run() {
								reconnection();
							}
						}, 3, TimeUnit.SECONDS);
						logger.warn("连接失败",future.cause());
					}
				}
				
			});
			this.channelFuture=channelFuture;
			return channelFuture;
			
			 
		
	}
	
	
	/**
	 * 重连接
	 * @return
	 */
	public ChannelFuture  reconnection() {
	
		ChannelFuture channelFuture=this.channelFuture;
		if(channelFuture==null) {
			return null;
		}
		
		Channel channel=channelFuture.channel();
		if(!channel.isActive()) {
			return connection();
		}
		
		channelFuture=channel.close();
		
		channelFuture.addListener(new GenericFutureListener<ChannelFuture>() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				 
				if(future.isSuccess()) {
					connection();
				}else {
					logger.warn("关闭失败",future.cause());
				}
			}
			
		});
		
		return channelFuture;
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		logger.info("销毁");
	}
	
	
}
