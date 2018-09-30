package io.mqttpush.mqttserver.util;

/**
 * 字符串的 父子级别key
 * 
 * @author acer
 *
 */
public class StringCasCadeKey extends CaseCadeKey<String> {

	public StringCasCadeKey(String k) {
	
		if (!k.startsWith("/")) {
			k="/"+k;
		}
		this.kvalue = k;
	}

	public StringCasCadeKey(String k, CaseCadeKey<String> parentKey) {
		super(k, parentKey);

		StringBuilder builder = new StringBuilder();
		String pname = null;
		if (parentKey != null) {
			pname = parentKey.kvalue;
		}

		if (!pname.startsWith("/")) {
			builder.append('/');
		}
		builder.append(pname);

		if (!k.startsWith("/")) {
			builder.append('/');
		}
		this.kvalue = builder.append(k).toString();
	}
}
