package io.mqttpush.getway.http.vo;

/**
 * http 请求接口的格式
 * @author tianzhenjiu
 *
 */
public class HttpPushVo {

	/**
	 * 回调
	 */
	private  String callback;
	
	/**
	 * 标志是谁发的
	 */
	private String  fromIdentify;
	
	/**
	 * 标志发给谁
	 */
	private String  toIdentify;
	
	/**
	 * 内容
	 */
	private String content;

	
	public HttpPushVo() {}
	
	public HttpPushVo(String callback, String fromIdentify, String toIdentify, String content) {
		super();
		this.callback = callback;
		this.fromIdentify = fromIdentify;
		this.toIdentify = toIdentify;
		this.content = content;
	}

	public String getCallback() {
		return callback;
	}

	public String getFromIdentify() {
		return fromIdentify;
	}

	public String getToIdentify() {
		return toIdentify;
	}

	public String getContent() {
		return content;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public void setFromIdentify(String fromIdentify) {
		this.fromIdentify = fromIdentify;
	}

	public void setToIdentify(String toIdentify) {
		this.toIdentify = toIdentify;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
	
	
	
}
