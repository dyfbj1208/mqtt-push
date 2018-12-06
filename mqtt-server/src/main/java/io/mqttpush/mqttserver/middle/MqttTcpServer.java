package io.mqttpush.mqttserver.middle;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 
 * @author
 * 
 */
public class MqttTcpServer {

	private int sobacklog = 1024;

	private int port = 10000;

	private int bossthreads = Runtime.getRuntime().availableProcessors();

	private int workthreads = Runtime.getRuntime().availableProcessors() * 2;

	private int readtimeout = 10000;

	public void start(Integer port) throws InterruptedException {
		
		
		if(port==null) {
			port=this.port;
		}

		ServerBootstrap bootstrap = new ServerBootstrap();// 启动辅助类
		EventLoopGroup group = null;
		EventLoopGroup workGroup = null;

		if (Epoll.isAvailable()) {
			group = new EpollEventLoopGroup(bossthreads);
			workGroup = new EpollEventLoopGroup(workthreads);
			bootstrap.channel(EpollServerSocketChannel.class);//
		} else {
			group = new NioEventLoopGroup(bossthreads);
			workGroup = new NioEventLoopGroup(workthreads);
			bootstrap.channel(NioServerSocketChannel.class);//
		}

		bootstrap.group(group, workGroup);
		bootstrap.childOption(ChannelOption.WRITE_SPIN_COUNT, 256);
		
		bootstrap.localAddress(new InetSocketAddress(port));// 绑定端口
		bootstrap.option(ChannelOption.SO_BACKLOG, sobacklog);
		try {
			bootstrap.childHandler(new MyChannelInitializer());
			ChannelFuture f = bootstrap.bind().sync();// 实际绑定操作
			System.out.println(MqttTcpServer.class.getName() + "001-服务器启动成功 " + f.channel().localAddress());
			f.channel().closeFuture().sync();// ;// 等待 服务器关闭
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully().sync();// 优雅关闭
			workGroup.shutdownGracefully().sync();
		}
	}

	
	

	public int getSobacklog() {
		return sobacklog;
	}

	public void setSobacklog(int sobacklog) {
		this.sobacklog = sobacklog;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getBossthreads() {
		return bossthreads;
	}

	public void setBossthreads(int bossthreads) {
		this.bossthreads = bossthreads;
	}

	public int getWorkthreads() {
		return workthreads;
	}

	public void setWorkthreads(int workthreads) {
		this.workthreads = workthreads;
	}

	public int getReadtimeout() {
		return readtimeout;
	}

	public void setReadtimeout(int readtimeout) {
		this.readtimeout = readtimeout;
	}

}