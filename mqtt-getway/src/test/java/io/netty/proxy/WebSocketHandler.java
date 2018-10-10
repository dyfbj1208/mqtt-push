package io.netty.proxy;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketHandler extends ChannelInboundHandlerAdapter {

	final EventLoopGroup group = new NioEventLoopGroup();

	final Bootstrap b = new Bootstrap();
	
	
	final String mqttserver="localhost";
	
	final int mqttport=10000;

	ChannelFuture channelFuture;
	/**
	 * websocket->MQTT
	 */
	static Map<Channel, Channel>  ab2bcChannels=new ConcurrentHashMap<>();
	
	
	/**
	 * MQTT->websocket
	 */
	public static  Map<Channel, Channel>  bc2abChannels=new ConcurrentHashMap<>();
	
	
	Logger logger=Logger.getLogger(getClass());
	
	public WebSocketHandler() {
		initBcChannel();
	}
	public void initBcChannel() {

		Bootstrap b = this.b;
		b.group(group).
		channel(NioSocketChannel.class).
		option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				
				ch.pipeline().addLast(new ClientHandle());
			}
			
			
		});
		

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		System.out.println("用户上线: " + ctx.channel().id().asLongText());
		
		/**
		 * 连接后端MQTT 服务器，绑定 前后连接
		 */
		channelFuture=b.connect(mqttserver, mqttport).sync();
		ab2bcChannels.put(ctx.channel(), channelFuture.channel());
		bc2abChannels.put(channelFuture.channel(), ctx.channel());
		
		logger.info("绑定成功");
		
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
		System.out.println("用户下线: " + ctx.channel().id().asLongText());
		super.channelInactive(ctx);
		
		if(channelFuture!=null) {
			channelFuture.channel().close();
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (!(msg instanceof BinaryWebSocketFrame)) {
			ctx.channel().writeAndFlush(new TextWebSocketFrame("来自服务端: 不支持" + LocalDateTime.now()));
			return;
		}

		BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) msg;
		//ctx.fireChannelRead(binaryWebSocketFrame.content());

		Channel fromchannel=ctx.channel();
		if(ab2bcChannels.containsKey(fromchannel)) {
			
			Channel tochannel=ab2bcChannels.get(fromchannel);
			tochannel.writeAndFlush(binaryWebSocketFrame.content());
			logger.info("websocket写入到MQTT服务");
		}
		

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.channel().close();
	}
}