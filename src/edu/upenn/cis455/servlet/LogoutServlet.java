package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet{

	public void doPost(HttpServletRequest request,HttpServletResponse response) throws java.io.IOException,MalformedURLException
	{
	HttpSession session = request.getSession(false);
	//session.invalidate();
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	out.println("<HTML><BODY>");
	if (session == null) {
		out.println("<P>Logged Out Successfully!</P>");
	} 
	else {
		session.invalidate();
		out.println("<P>Logged Out Successfully!</P>");
		//out.println("<P>Uh-oh, session still exists!</P>");
	}
	out.println("</BODY></HTML>");
	
	
	
	}
}
