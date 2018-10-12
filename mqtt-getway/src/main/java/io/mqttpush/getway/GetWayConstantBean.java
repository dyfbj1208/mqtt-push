package io.mqttpush.getway;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.AttributeKey;

public class GetWayConstantBean {

	public final EventLoopGroup wsgroup = new NioEventLoopGroup();

	public final EventLoopGroup httpgroup = new NioEventLoopGroup();
	
	public final EventLoopGroup httpCallbackgroup = new NioEventLoopGroup();

	public final Bootstrap wsbootstrap = new Bootstrap();
	
	public final Bootstrap httpbootstrap = new Bootstrap();
	
	public  final  Bootstrap  httpCallbackStart=new Bootstrap(); 
	
	
	
	
	public final String mqttserver="localhost";
	
	public final int mqttport=10000;


	/**
	 *点对点通信
	 */
	public  final String ONE2ONE_CHAT_PREFIX="/root/chat/one2one/";

	public final Map<String, Channel> bcHttpChannels=new ConcurrentHashMap<>(100);
	
	/**
	 * AB->BC 模型中用于指定后端bcchannnel
	 */
	public final AttributeKey<Channel>  bcChannelAttr=AttributeKey.valueOf("bcChannel");
	
	/**
	 * AB->BC 模型中用于指定后端abchannnel
	 */
	public final AttributeKey<Channel>  abChannelAttr=AttributeKey.valueOf("abChannel");
	
	
	/**
	 * http 回调的属性标志
	 */
	public final AttributeKey<String>  bcHttpCallBackAttr=AttributeKey.valueOf("bcHttpCallBack");
	
	static GetWayConstantBean constantBean=null;
	
	private  GetWayConstantBean() {}
	
	public static GetWayConstantBean instance() {
		
		if(constantBean==null) {
			constantBean=new GetWayConstantBean();
		}
		return  constantBean;
	}
}
