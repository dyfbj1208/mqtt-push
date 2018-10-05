package io.mqttpush.mqttserver.cache;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.redis.RedisArrayAggregator;
import io.netty.handler.codec.redis.RedisBulkStringAggregator;
import io.netty.handler.codec.redis.RedisDecoder;
import io.netty.handler.codec.redis.RedisEncoder;

public class RedisBeans {

	private String auth;
	int port = 6379;
	String host = "localhost";
	Channel redisChannel;

	public void init() {

		Bootstrap b = new Bootstrap();
		EventLoopGroup group = new NioEventLoopGroup();
		b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {

						ChannelPipeline p = ch.pipeline();
						p.addLast(new RedisDecoder());
						p.addLast(new RedisBulkStringAggregator());
						p.addLast(new RedisArrayAggregator());
						p.addLast(new RedisEncoder());

					}

				});

		redisChannel = b.connect(host, port).channel();

	}

	/**
	 * @return the redisChannel
	 */
	public Channel getRedisChannel() {
		
		if(redisChannel==null) {
			init();
		}
		return redisChannel;
	}
	
	
	
	
}
