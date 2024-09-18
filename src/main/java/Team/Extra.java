package Team;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

public class Extra {
	public static void sendError(HttpServletResponse response , PrintWriter out , String message) {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		JsonObject result = new JsonObject();
		result.addProperty("error", message);
        out.print(result.toString());
        return;
	}
	public static void sendSuccess(HttpServletResponse response , PrintWriter out , String message) {
		response.setStatus(HttpServletResponse.SC_OK);
		JsonObject result = new JsonObject();
		result.addProperty("success", message);
        out.print(result.toString());
        return;
	}
}
