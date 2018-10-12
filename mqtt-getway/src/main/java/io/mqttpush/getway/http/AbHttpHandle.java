package io.mqttpush.getway.http;

import org.apache.log4j.Logger;

import io.mqttpush.getway.GetWayConstantBean;
import io.mqttpush.getway.http.controller.ControllBeans;
import io.mqttpush.getway.http.controller.Controller;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;

public class AbHttpHandle extends ChannelInboundHandlerAdapter {

	Logger logger = Logger.getLogger(getClass());

	private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };

	private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
	private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
	private static final AsciiString CONNECTION = AsciiString.cached("Connection");
	private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");
	
	
	final String FORM_URL_ENCODING="application/x-www-form-urlencoded";
	 
	final  ControllBeans controllbean=ControllBeans.getInstance();
	
	final GetWayConstantBean constantBean = GetWayConstantBean.instance();
	

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		 FullHttpResponse response = new DefaultFullHttpResponse(
         		HttpVersion.HTTP_1_1, 
         		HttpResponseStatus.INTERNAL_SERVER_ERROR);
		 
		 ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		 cause.printStackTrace();
	}

	
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		
		Channel requestChannel=ctx.channel();
		String closeIdentify=null;
		if(requestChannel.hasAttr(ControllBeans.requestIdentifyKey)) {
			closeIdentify=requestChannel.attr(ControllBeans.requestIdentifyKey).get();
		}
		if(closeIdentify!=null&&
				constantBean.bcHttpChannels.containsKey(closeIdentify)) {
			Channel bcChannel=constantBean.bcHttpChannels.get(closeIdentify);
			if(bcChannel.isActive()) {
				bcChannel.flush();
			}
		}
		
	}



	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof HttpRequest) {

			HttpRequest request = (HttpRequest) msg;
		
	            boolean keepAlive = HttpUtil.isKeepAlive(request);
	            FullHttpResponse response = new DefaultFullHttpResponse(
	            		HttpVersion.HTTP_1_1, 
	            		HttpResponseStatus.OK, Unpooled.wrappedBuffer(CONTENT));
	            
	            response.headers().set(CONTENT_TYPE, "text/plain");
	            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
	            
	            
	            applyController(ctx.channel(),request, response);
	            
	            
	            if (!keepAlive) {
	                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
	            } else {
	                response.headers().set(CONNECTION, KEEP_ALIVE);
	                ctx.write(response);
	            }
	 
		} else {
			ctx.close();
		}
	}
	
	/**
	 * 根据请求头决定应有的controller
	 * @param req
	 * @param response
	 */
	public void applyController(Channel channel,HttpRequest request,HttpResponse response) {
		
		if(request==null) {
			return;
		}
		
		String contentType=request.headers().get(CONTENT_TYPE);
		Controller controller=null;
		
		if(contentType!=null&&contentType.equalsIgnoreCase(FORM_URL_ENCODING)) {			
			controller=controllbean.formController();
		}else {			
			controller=controllbean.fullTextController();
		}
		
		controller.service(channel,request,response);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
		ctx.flush();
	}

	
}
