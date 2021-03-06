package io.mqttpush.getway.http.controller;

import java.nio.charset.Charset;

import com.alibaba.fastjson.JSON;

import io.mqttpush.mqttserver.beans.HttpPushVo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 处理JSON的fulltext
 * 
 * @author tianzhenjiu
 *
 */
public class FullTextController extends Controller {

	protected FullTextController() {
		initBcChannel();
	}

	@Override
	public void service(Channel requestChannel, HttpRequest request, HttpResponse response) {

		if (!(request instanceof FullHttpRequest)) {
			response.setStatus(HttpResponseStatus.BAD_REQUEST);
			return;
		}

		FullHttpRequest fullHttpRequest = (FullHttpRequest) request;

		ByteBuf byteBuf = fullHttpRequest.content();

		byte[] bs = new byte[byteBuf.readableBytes()];
		byteBuf.readBytes(bs);

		HttpPushVo httpPushVo = JSON.parseObject(new String(bs,Charset.forName("utf-8")),
				HttpPushVo.class);

		if(logger.isDebugEnabled()&&httpPushVo!=null) {
			logger.debug("JSON序列化成功"+httpPushVo.toString());
		}
		super.routeData(requestChannel, httpPushVo);

	}

}
