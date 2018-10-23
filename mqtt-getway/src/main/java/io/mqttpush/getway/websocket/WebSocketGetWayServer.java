package io.mqttpush.getway.websocket;

import io.mqttpush.getway.GetWayConstantBean;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WebSocketGetWayServer {

	public static void main(String[] args) {

		int websocketport=9999;
		String proxyhost="localhost";
		int proxyport=8008;
		
		
		if(args!=null) {
			
			if(args.length>0){
				
				try {
					websocketport=Integer.parseInt(args[0]);
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
		
		
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup).
			channel(NioServerSocketChannel.class)
			.childHandler(new WebSocketChannelInitializer());

			ChannelFuture channelFuture;
			try {
				channelFuture = serverBootstrap.bind(websocketport).sync();
				System.out.println("websocker网关启动成功 " + channelFuture.channel().localAddress());
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
