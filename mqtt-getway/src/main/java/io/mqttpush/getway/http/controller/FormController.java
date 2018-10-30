package io.mqttpush.getway.http.controller;

import io.mqttpush.mqttserver.beans.HttpPushVo;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;

/**
 * 处理表单的controller
 * 
 * @author tianzhenjiu
 *
 */
public class FormController extends Controller {

	FormController() {
		initBcChannel();
	}

	@Override
	public void service(Channel requestChannel, HttpRequest request, HttpResponse response) {

		if (!(request instanceof FullHttpRequest)) {
			response.setStatus(HttpResponseStatus.BAD_REQUEST);
			return;
		}

		HttpPushVo httpPushVo = new HttpPushVo();
		try {
			httpPushVo = seriaForVo(httpPushVo, request);
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatus(HttpResponseStatus.BAD_REQUEST);
			return;
		}

		super.routeData(requestChannel, httpPushVo);

	}

	/**
	 * 解析request 得到vo对象
	 * 
	 * @param httpPushVo
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private HttpPushVo seriaForVo(HttpPushVo httpPushVo, HttpRequest request) throws IOException {

		HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
		InterfaceHttpData callbackInterface = decoder.getBodyHttpData("callback");
		InterfaceHttpData fromIdentifyInterface = decoder.getBodyHttpData("fromIdentify");
		InterfaceHttpData toIdentifyInterface = decoder.getBodyHttpData("toIdentify");
		InterfaceHttpData byteContentInterface = decoder.getBodyHttpData("byteContent");

		if (callbackInterface instanceof Attribute) {
			httpPushVo.setCallback(((Attribute) callbackInterface).getValue());
		}

		if (fromIdentifyInterface instanceof Attribute) {
			httpPushVo.setFromIdentify(((Attribute) fromIdentifyInterface).getValue());
		}

		if (toIdentifyInterface instanceof Attribute) {
			httpPushVo.setToIdentify(((Attribute) toIdentifyInterface).getValue());
		}
		if (byteContentInterface instanceof Attribute) {
			httpPushVo.setByteContent(((Attribute) byteContentInterface).getByteBuf());
		} else if (byteContentInterface instanceof FileUpload) {
			httpPushVo.setByteContent(((FileUpload) byteContentInterface).getByteBuf());
		}

		return httpPushVo;
	}

}
