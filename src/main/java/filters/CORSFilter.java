package filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CORSFilter extends HttpFilter implements Filter {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response , FilterChain chain ) throws ServletException, IOException {
		
		HttpServletResponse res = (HttpServletResponse)response;
		HttpServletRequest req  = (HttpServletRequest)request;
		res.setHeader("Access-Control-Allow-Origin", "*"); 
	    res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
	    res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Content-Disposition");
	    res.setHeader("Access-Control-Max-Age", "3600");
	    if("OPTIONS".equalsIgnoreCase( req.getMethod() ))
	    {
	    	response.setStatus(HttpServletResponse.SC_OK);
	    	return;
	    }
	    chain.doFilter(request, response);
	    
	}


}
