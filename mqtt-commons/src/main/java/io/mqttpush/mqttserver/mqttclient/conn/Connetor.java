package io.mqttpush.mqttserver.mqttclient.conn;

import java.util.Properties;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.mqttclient.handle.ConnectionHandle;
import io.mqttpush.mqttserver.mqttclient.handle.Iden2PingHandle;
import io.mqttpush.mqttserver.mqttclient.handle.PubHandle;
import io.mqttpush.mqttserver.mqttclient.handle.SubHandle;
import io.mqttpush.mqttserver.mqttclient.service.ApiService;
import io.mqttpush.mqttserver.mqttclient.service.DefaultMessageListener;
import io.mqttpush.mqttserver.mqttclient.service.MessageListener;
import io.netty.bootstrap.Bootstrap;
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
	
	
	static Logger logger=Logger.getLogger(Connetor.class);
	final EventLoopGroup group = new NioEventLoopGroup();


	
	public ChannelFuture connection(Properties properties,final ApiService apiService,final MessageListener defaultMessageListener) {


		String host = (String) properties.getOrDefault("host", "127.0.0.1");
		Integer remotePort = (Integer) properties.getOrDefault("port", 1000);
		final Integer pingtime = (Integer) properties.getOrDefault("pingtime", 60);
		final String deviceId = (String) properties.getOrDefault("deviceId", "0000");
		final String username = (String) properties.getOrDefault("username", "user");
		final String password = (String) properties.getOrDefault("password", "user123456");
		final String subTopic = (String) properties.getOrDefault("subTopic", null);
		
		ChannelFuture f=null;

		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(
									new ReadTimeoutHandler(pingtime*2),
									MqttEncoder.INSTANCE, new MqttDecoder(),
									new Iden2PingHandle(pingtime),
									new ConnectionHandle(apiService,deviceId, username, password,subTopic),
									new PubHandle(defaultMessageListener), new SubHandle());
						}
					});

			return	 b.connect(host, remotePort).sync();
			
			
		} catch (InterruptedException e) {
			logger.warn("异常",e);
			
		} 
		
		return f;
	}
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		logger.info("销毁");
	}
	
	
}
