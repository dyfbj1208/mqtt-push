package io.mqttpush.mqttserver.util;

public class StringCasKey extends CasKey<String> {

	
	public StringCasKey(String t) {
		super(t);
	}

	@Override
	public String getParentKey() {
		
		
		String nowKey=super.t;
		int index=nowKey.lastIndexOf("/");
		
		if(index>0) {
			return nowKey.substring(0, index);
		}
		
		return null;
	}


}
