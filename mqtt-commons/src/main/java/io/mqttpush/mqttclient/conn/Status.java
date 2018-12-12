package io.mqttpush.mqttclient.conn;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author tianzhenjiu
 *
 */
public class Status {

	public static final AtomicBoolean hasConnect = new AtomicBoolean(false);
	public static final AtomicBoolean isLogin=new AtomicBoolean(false);
}
