package io.netty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.mqttpush.getway.GetWayConstantBean;
import io.mqttpush.getway.http.BcMqttHandle;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.util.AttributeKey;

public class TestAttributre {
	
	public static class TestBeean{

		/* (non-Javadoc)
		 * @see java.lang.Object#finalize()
		 */
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			
			System.out.println(this+"被回收");
		}
		
		
		
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		

		AttributeKey<TestBeean> attributeKey=AttributeKey.valueOf("keytest");
		final GetWayConstantBean constantBean = GetWayConstantBean.instance();
		
		Bootstrap bootstrap = constantBean.httpbootstrap;
		bootstrap.group(constantBean.httpgroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(MqttEncoder.INSTANCE, new MqttDecoder());
						ch.pipeline().addLast(new BcMqttHandle());
					}

				});
		
		
	
		List<Channel> channels=new LinkedList<>();
		for (int i = 0; i < 1000; i++) {
			
			ChannelFuture channelFuture=bootstrap.connect("106.14.191.21", 22);
			channels.add(channelFuture.channel());
			channelFuture.channel().attr(attributeKey).set(new TestBeean());
		}

		System.out.println("连接完成");
		
		channels.forEach((c)->{
			c.close();
		});
		
		
		channels.clear();
		System.out.println("关闭");
		System.gc();
		TimeUnit.SECONDS.sleep(20);
		
	}

}
