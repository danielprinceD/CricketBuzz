package controller;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.JsonObject;

public class Extra {
	public static void sendError(HttpServletResponse response , PrintWriter out , String message) {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		JsonObject result = new JsonObject();
		result.addProperty("code",400);
		result.addProperty("error", message);
        out.print(result.toString());
        return;
	}
	public static void sendSuccess(HttpServletResponse response , PrintWriter out , String message) {
		response.setStatus(HttpServletResponse.SC_OK);
		JsonObject result = new JsonObject();
		result.addProperty("code",200);
		result.addProperty("message", message);
        out.print(result.toString());
        return;
	}
	
	public static String convertToJson(HttpServletRequest request) throws Exception
	{
		StringBuilder json = new StringBuilder();
		BufferedReader reader = request.getReader();
		String line;
		
		while((line = reader.readLine()) != null)
			json.append(line);
		
		return json.toString();
	}
	
	public static String ForeignKeyError(String errorMessage) {
		
		if (errorMessage.contains("foreign key constraint fails")) {
	        String columnName = getForeignKeyColumnName(errorMessage);
	        return  "Foreign key violation in column: " + columnName ;
	    }
		return errorMessage;
	}
	
	private static String getForeignKeyColumnName(String errorMessage) {
        Pattern pattern = Pattern.compile("FOREIGN KEY \\(`(.*?)`\\) REFERENCES");
        Matcher matcher = pattern.matcher(errorMessage);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "Unknown column";
        }
	}
	
}
