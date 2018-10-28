package io.mqttpush.getway.http;

import io.mqttpush.getway.GetWayConstantBean;
import io.mqttpush.getway.common.Statistics;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.TimeUnit;

public class HttpGetWayServer {

	public static void main(String[] args) {

		
		
		int proxyport=10000;
		String proxyhost="localhost";
		int httpport=8887;
		if(args!=null) {
			
			if(args.length>0) {
				try {
					httpport=Integer.parseInt(args[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if(args.length>1) {
				proxyhost=args[1];
			}
			if(args.length>2) {
				try {
					proxyport=Integer.parseInt(args[2]);
				} catch (Exception e) {
						e.printStackTrace();
				}
			}
			
		}
		
		
		GetWayConstantBean.instance(proxyhost, proxyport);
		Statistics statistics=Statistics.instance();
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup).
			channel(NioServerSocketChannel.class)
			.childHandler(new HttpChannelInitializer());

			ChannelFuture channelFuture;
			try {
				channelFuture = serverBootstrap.bind(httpport).sync();
				/**
				 * 定时器两分钟统计一下QPS
				 */
				workerGroup.scheduleWithFixedDelay(statistics,1,1, TimeUnit.MINUTES);
				System.out.println("http网关启动成功 " + channelFuture.channel().localAddress());
				channelFuture.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}
