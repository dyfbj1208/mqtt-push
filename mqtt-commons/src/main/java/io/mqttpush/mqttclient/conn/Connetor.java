package io.mqttpush.mqttclient.conn;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

public class Connetor {

	Logger logger = Logger.getLogger(Connetor.class);
	final EventLoopGroup eventLoopGroup;
	final Bootstrap bootstrap;

	final ConnectProperties properties;
	final ApiService apiService;
	final MessageListener defaultMessageListener;
	
	final ConnectionHandle connectionHandle;
	ChannelFuture nowCloseFuture;
	
	AtomicBoolean hasConnect = new AtomicBoolean(false);

	ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);


	public Connetor(ConnectProperties properties, ApiService apiService, MessageListener defaultMessageListener) {
		super();
		this.properties = properties;
		this.apiService = apiService;
		this.defaultMessageListener = defaultMessageListener;
		bootstrap = new Bootstrap();
		eventLoopGroup = new NioEventLoopGroup(1);
		
		final Integer pingtime = properties.getPingtime();
		final String deviceId = properties.getDeviceId();
		final String username = properties.getUsername();
		final String password = properties.getPassword();
		
		connectionHandle=new ConnectionHandle(Connetor.this, apiService, deviceId, username, password); 

	
		init(pingtime);
	}

	/**
	 * 初始化
	 */
	public void init(Integer pingtime) {

		

		bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						
						
						
						ChannelPipeline p = ch.pipeline();
	
						p.addLast(new ReadTimeoutHandler(pingtime * 2), MqttEncoder.INSTANCE, new MqttDecoder(),
								connectionHandle,
								new PubHandle(defaultMessageListener), new SubHandle());
					}
				});

	}

	/**
	 * 连接
	 * 
	 * @return
	 */
	public ChannelFuture connection() {

		String host = properties.getHost();
		Integer port = properties.getPort();

		ChannelFuture channelFuture = bootstrap.connect(host, port);

		channelFuture.addListener((ChannelFuture future) -> {

			if (future.isSuccess()) {
				hasConnect.set(true);
				
				PingRunnable pingRunnable = new PingRunnable(future.channel(),
						Connetor.this,
						properties.getPingtime(),
						executorService);
				
				pingRunnable.updatehasResp(true);
				connectionHandle.setPingRunnable(pingRunnable);
				executorService.schedule(pingRunnable, properties.getPingtime(), TimeUnit.SECONDS);
				
				
				if (logger.isDebugEnabled()) {
					logger.debug("连接成功" + host + ":" + port);
				}
			} else {
				/**
				 * 连接失败，重新连接
				 */
				if(hasConnect.get()) {
					logger.info("已经连接,无需重复连接");
					return;
				}
				executorService.schedule( ()->{
					reconnection(future.channel());
				},3, TimeUnit.SECONDS);
				logger.warn("连接失败"+future.cause());
			}
		});
		
		return channelFuture;
	}

	/**
	 * 重连接
	 * 
	 * @return
	 */
	public ChannelFuture reconnection(Channel channel) {

	
		
		/**
		 * 清理调调度线程池
		 */
		
		hasConnect.set(false);
		
		if (channel==null||!channel.isActive()) {
			return connection();
		}

		return  channel.close();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		logger.info("销毁");
	}

}
