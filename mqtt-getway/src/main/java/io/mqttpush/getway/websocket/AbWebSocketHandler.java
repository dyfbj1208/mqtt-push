package io.mqttpush.getway.websocket;

import io.mqttpush.getway.GetWayConstantBean;
import io.mqttpush.getway.common.Statistics;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 处理来自网页端的websocket 报文，并且建立与后端的连接，把报文丢给后端
 * @author tianzhenjiu
 *
 */
public class AbWebSocketHandler extends ChannelInboundHandlerAdapter {

	
	GetWayConstantBean constantBean=GetWayConstantBean.instance();
	
	
	Logger logger=Logger.getLogger(getClass());
	
	/**
	 * 标志是否初始化了channnel
	 */
	static AtomicBoolean isInit=new AtomicBoolean(false);
	
	
	public AbWebSocketHandler() {
		
		if(!isInit.get()) {
			
			if(isInit.compareAndSet(false, true)) {
				initBcChannel();
			}
			
		}
		
	}
	
	
	/**
	 * 初始化后端连接channel
	 */
	public void initBcChannel() {

		
		Bootstrap bootstrap = constantBean.wsbootstrap;
		bootstrap.group(constantBean.wsgroup).
		channel(NioSocketChannel.class).
		option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(
						new BcMqttHandle(
								AbWebSocketHandler.this::getBcChannel,
								AbWebSocketHandler.this::getAbChannel,
								AbWebSocketHandler.this::aBChannelClose
								));
			}
			
			
		});
		

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);

		Statistics.aBconnCount.incrementAndGet();

		if(logger.isDebugEnabled()) {
			logger.debug(("用户上线: " + ctx.channel().id().asLongText()));
		}
		
		/**
		 * 连接后端MQTT 服务器，绑定 前后连接
		 */
		ChannelFuture channelFuture=constantBean.wsbootstrap.connect(
				constantBean.mqttserver,constantBean.mqttport);

		final Channel abchannel=ctx.channel();
		final Channel bcChannel=channelFuture.channel();


/**
 * 一定要等链接成功了才算绑定成功
 */
		channelFuture.addListener((c)->{
			if(c.isSuccess()){

				abchannel.attr(constantBean.bcChannelAttr).set(bcChannel);
				bcChannel.attr(constantBean.abChannelAttr).set(abchannel);

				Statistics.bCconnCount.incrementAndGet();
				if(logger.isDebugEnabled()) {
					logger.debug("绑定成功AB-BC模型成功");
				}
			}else{
				logger.warn("连接失败",c.cause());
			}
		});

		
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
			Statistics.requestCount.incrementAndGet();
			if(logger.isDebugEnabled()) {			
				logger.debug("websocket写入到MQTT服务"+msg);
			}
			
		}
		

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

		Channel abchannel=ctx.channel();


		//如果连接以及无效了直接-1
		if(!abchannel.isActive()) {			
			Statistics.aBconnCount.decrementAndGet();
			if(logger.isDebugEnabled()){
				logger.debug("前端连接无效"+abchannel.toString());
			}
		}
		
		aBChannelClose(abchannel);
		super.channelInactive(ctx);
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		Channel abchannel= ctx.channel();

		//如果连接以及无效了直接-1
		if(!abchannel.isActive()) {			
			Statistics.aBconnCount.decrementAndGet();
		}
		
		aBChannelClose(abchannel);
		logger.warn("异常",cause);
	}


	/**
	 * 关闭前端连接并且关闭关联的后端连接
	 * 所有的关闭必须根据前端连接关闭后端连接
	 * @param abchannel  前端连接
	 * @return
	 */
	public  ChannelFuture  aBChannelClose(Channel abchannel){



		ChannelFuture channelFuture=null;
		Channel bcChannel=getBcChannel(abchannel);


		/**
		 * 无论关闭前端还是关闭后端都需要判断这个channel是否可用
		 * 如果以及不可用了就说明可能以及被关闭了
		 */
		if(bcChannel!=null&&bcChannel.isActive()) {
			if(logger.isDebugEnabled()){
				logger.debug("关闭后端连接"+bcChannel.toString());
			}
			bcChannel.close();
			Statistics.bCconnCount.decrementAndGet();
		}
		
		if(abchannel!=null&&abchannel.isActive()) {
			//要最后关闭前端的，否则可能getBcChannel 返回空的后端连接
			//导致后端连接泄漏
			if(logger.isDebugEnabled()){
				logger.debug("关闭前端连接"+abchannel.toString());
			}

			channelFuture=abchannel.close();
			Statistics.aBconnCount.decrementAndGet();
		}
		return channelFuture;
	}
	
	/**
	 * 根据前端channel  得到绑定的后端channel
	 * @param abChannel
	 * @return
	 */
	public Channel getBcChannel(Channel abChannel) {
		
		Channel bcChannel=null;
		
		if(abChannel==null) {
			return null;
		}
		if(abChannel.hasAttr(constantBean.bcChannelAttr)) {
			bcChannel=abChannel.attr(constantBean.bcChannelAttr).get();
		}
		
		return bcChannel;
		
	}
	
	
	/**
	 * 根据后端channel  得到绑定的前端channel
	 * @param bcChannel
	 * @return
	 */
	public Channel getAbChannel(Channel bcChannel) {
		
		Channel abChannel=null;
		
		if(bcChannel==null) {
			return null;
		}
		if(bcChannel.hasAttr(constantBean.abChannelAttr)) {
			abChannel=bcChannel.attr(constantBean.abChannelAttr).get();
		}
		
		return abChannel;
		
	}
	
	
}