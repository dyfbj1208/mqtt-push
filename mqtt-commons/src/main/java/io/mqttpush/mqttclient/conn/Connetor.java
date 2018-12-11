package io.mqttpush.mqttclient.conn;

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

public class Connetor {

	Logger logger = Logger.getLogger(Connetor.class);
	final EventLoopGroup eventLoopGroup;
	final Bootstrap bootstrap;

	final ConnectProperties properties;
	final ApiService apiService;
	final MessageListener defaultMessageListener;
	ChannelFuture nowCloseFuture;

	AtomicBoolean isValidate = new AtomicBoolean(true);

	ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

	ChannelFuture channelFuture;

	public Connetor(ConnectProperties properties, ApiService apiService, MessageListener defaultMessageListener) {
		super();
		this.properties = properties;
		this.apiService = apiService;
		this.defaultMessageListener = defaultMessageListener;
		bootstrap = new Bootstrap();
		eventLoopGroup = new NioEventLoopGroup(1);

		init();
	}

	/**
	 * 初始化
	 */
	public void init() {

		final Integer pingtime = properties.getPingtime();
		final String deviceId = properties.getDeviceId();
		final String username = properties.getUsername();
		final String password = properties.getPassword();
		final String subTopic = properties.getSubTopic();

		bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						p.addLast(new ReadTimeoutHandler(pingtime * 2), MqttEncoder.INSTANCE, new MqttDecoder(),
								new ConnectionHandle(Connetor.this,isValidate, apiService, deviceId, username, password, subTopic),
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

				PingRunnable runnable = new PingRunnable(future.channel(),
						isValidate, 
						Connetor.this,
						properties.getPingtime(),
						executorService);
				executorService.schedule(runnable, properties.getPingtime(), TimeUnit.SECONDS);
				isValidate.set(true);
				if (logger.isDebugEnabled()) {
					logger.debug("连接成功" + host + ":" + port);
				}
			} else {
				/**
				 * 连接失败，重新连接
				 */
				executorService.schedule( ()->{
					reconnection(future.channel());
				},3, TimeUnit.SECONDS);
				logger.warn("连接失败", future.cause());
			}
		});
		this.channelFuture = channelFuture;
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
		executorService.shutdownNow();
		executorService = Executors.newScheduledThreadPool(2);
		
		
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
