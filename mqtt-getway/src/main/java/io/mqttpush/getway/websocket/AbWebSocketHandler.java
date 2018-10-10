package io.mqttpush.getway.websocket;

import java.time.LocalDateTime;

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
import io.netty.util.AttributeKey;


/**
 * 处理来自网页端的websocket 报文，并且建立与后端的连接，把报文丢给后端
 * @author tianzhenjiu
 *
 */
public class AbWebSocketHandler extends ChannelInboundHandlerAdapter {

	final EventLoopGroup group = new NioEventLoopGroup();

	final Bootstrap bootstrap = new Bootstrap();
	
	final String mqttserver="localhost";
	
	final int mqttport=10000;


	
	/**
	 * AB->BC 模型中用于指定后端bcchannnel
	 */
	public final AttributeKey<Channel>  bcChannelAttr=AttributeKey.valueOf("bcChannel");
	
	/**
	 * AB->BC 模型中用于指定后端abchannnel
	 */
	public final AttributeKey<Channel>  abChannelAttr=AttributeKey.valueOf("abChannel");

	
	
	Logger logger=Logger.getLogger(getClass());
	
	public AbWebSocketHandler() {
		initBcChannel();
	}
	
	
	/**
	 * 初始化后端连接channel
	 */
	public void initBcChannel() {

		Bootstrap bootstrap = this.bootstrap;
		bootstrap.group(group).
		channel(NioSocketChannel.class).
		option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(
						new BcMqttHandle(
								AbWebSocketHandler.this::getBcChannel,
								AbWebSocketHandler.this::getAbChannel
								));
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
		ChannelFuture channelFuture=bootstrap.connect(mqttserver, mqttport);
		Channel abchannel=ctx.channel();
		Channel bcChannel=channelFuture.channel();
	
		abchannel.attr(bcChannelAttr).set(bcChannel);
		bcChannel.attr(abChannelAttr).set(abchannel);
	
		if(logger.isDebugEnabled()) {			
			logger.debug("绑定成功AB-BC模型成功");
		}
		
	}



	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (!(msg instanceof BinaryWebSocketFrame)) {
			ctx.channel().writeAndFlush(new TextWebSocketFrame("来自服务端: 不支持" + LocalDateTime.now()));
			return;
		}

		BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) msg;
	
		Channel fromchannel=ctx.channel();
		Channel tochannel=getBcChannel(fromchannel);
		if(tochannel!=null&&tochannel.isActive()) {
			tochannel.writeAndFlush(binaryWebSocketFrame.content());
			
			if(logger.isDebugEnabled()) {			
				logger.debug("websocket写入到MQTT服务"+msg);
			}
			
		}
		

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		
		
		Channel abchannel=ctx.channel();
		Channel bcChannel=getBcChannel(abchannel);
		
		if(bcChannel!=null&&bcChannel.isActive()) {
			bcChannel.close();
		}
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		Channel abchannel= ctx.channel();
		abchannel.close();
		Channel bcChannel=getBcChannel(abchannel);
		
		if(bcChannel!=null&&bcChannel.isActive()) {
				bcChannel.close();
		}
		
		logger.warn("异常",cause);
	}
	
	
	
	
	/**
	 * 根据前端channel  得到绑定的后端channel
	 * @param fromchannel
	 * @return
	 */
	public Channel getBcChannel(Channel abChannel) {
		
		Channel bcChannel=null;
		
		if(abChannel==null) {
			return null;
		}
		if(abChannel.hasAttr(bcChannelAttr)) {
			bcChannel=abChannel.attr(bcChannelAttr).get();
		}
		
		return bcChannel;
		
	}
	
	
	/**
	 * 根据后端channel  得到绑定的前端channel
	 * @param fromchannel
	 * @return
	 */
	public Channel getAbChannel(Channel bcChannel) {
		
		Channel abChannel=null;
		
		if(bcChannel==null) {
			return null;
		}
		if(bcChannel.hasAttr(abChannelAttr)) {
			abChannel=bcChannel.attr(abChannelAttr).get();
		}
		
		return abChannel;
		
	}
	
	
}