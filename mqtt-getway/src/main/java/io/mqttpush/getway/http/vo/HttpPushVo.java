package io.mqttpush.getway.http.vo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.nio.charset.Charset;

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
	private String textcontent;

	private ByteBuf byteContent;
	
	public HttpPushVo() {}

	public HttpPushVo(String callback, String fromIdentify, String toIdentify, String textcontent) {
		super();
		this.callback = callback;
		this.fromIdentify = fromIdentify;
		this.toIdentify = toIdentify;
		this.textcontent = textcontent;
	}

	public HttpPushVo(String callback, String fromIdentify, String toIdentify, ByteBuf byteContent) {
		super();
		this.callback = callback;
		this.fromIdentify = fromIdentify;
		this.toIdentify = toIdentify;
		this.byteContent = byteContent;
	}

	/**
	 * @return the callback
	 */
	public String getCallback() {
		return callback;
	}

	/**
	 * @return the fromIdentify
	 */
	public String getFromIdentify() {
		return fromIdentify;
	}

	/**
	 * @return the toIdentify
	 */
	public String getToIdentify() {
		return toIdentify;
	}

	/**
	 * @return the textcontent
	 */
	public String getTextcontent() {
		return textcontent;
	}

	/**
	 * @return the byteContent
	 */
	public ByteBuf getByteContent() {
		
		if(byteContent==null) {

			ByteBuf byteBuf=PooledByteBufAllocator.DEFAULT.buffer();

			if(textcontent!=null){
				byteBuf.writeCharSequence(textcontent, Charset.forName("utf-8"));
			}else{
				byteBuf.writeByte(0);
			}
			this.byteContent=byteBuf;
		}


		return byteContent;
	}

	/**
	 * @param callback the callback to set
	 */
	public void setCallback(String callback) {
		this.callback = callback;
	}

	/**
	 * @param fromIdentify the fromIdentify to set
	 */
	public void setFromIdentify(String fromIdentify) {
		this.fromIdentify = fromIdentify;
	}

	/**
	 * @param toIdentify the toIdentify to set
	 */
	public void setToIdentify(String toIdentify) {
		this.toIdentify = toIdentify;
	}

	/**
	 * @param textcontent the textcontent to set
	 */
	public void setTextcontent(String textcontent) {
		this.textcontent = textcontent;
	}

	/**
	 * @param byteContent the byteContent to set
	 */
	public void setByteContent(ByteBuf byteContent) {
		this.byteContent = byteContent;
	}
	
	
	
}
