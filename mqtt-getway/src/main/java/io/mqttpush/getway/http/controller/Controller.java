package io.mqttpush.getway.http.controller;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * 抽象的controller
 * @author tianzhenjiu
 *
 */
public  abstract class Controller {
	
	
	/**
	 *服务
	 * @param request
	 * @param response
	 */
	public abstract void service(HttpRequest request,HttpResponse response);
}
