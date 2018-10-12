package io.netty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestReg {

	public static void main(String[] args) {
		
		String url="http://www.xwmission.com:8081/#/experience-platform";
		Pattern pattern=Pattern.compile("http://(\\w+((\\.\\w+)+))(:(\\d+))?");
		
		Matcher matcher= pattern.matcher(url);
		
		if(matcher.find()) {
			System.out.println(matcher.group(1));
			System.out.println(matcher.group(5));
		}
	}

}
