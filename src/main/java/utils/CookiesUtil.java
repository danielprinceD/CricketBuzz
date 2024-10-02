package utils;

import javax.servlet.http.Cookie;

public class CookiesUtil {
	
	public static Cookie getCookies(Cookie []cookies) {
		if(cookies == null)
			return null;
		
		for(Cookie cookie : cookies)
			if(cookie.getName().equals("token"))
				return cookie;
		
		return null;
	}
	
}
