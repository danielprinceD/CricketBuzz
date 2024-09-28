package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathMatcherUtil {
	
	public static boolean matchesPattern(String url, String pattern) {
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        return matcher.matches();
    }
	
	
	
}
