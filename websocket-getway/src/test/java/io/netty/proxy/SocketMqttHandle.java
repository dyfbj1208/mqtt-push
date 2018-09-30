package io.netty.proxy;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class SocketMqttHandle extends  MessageToMessageEncoder<ByteBuf> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		
		
		ByteBuf buf=ctx.alloc().buffer();
		
		buf.writeBytes(msg);
		
		out.add(new BinaryWebSocketFrame(buf));
	}


	

}
