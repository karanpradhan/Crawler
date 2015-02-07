package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.BerkleyDB;
@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet{
	
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws java.io.IOException,MalformedURLException
	{
		String path = getServletContext().getInitParameter("BDBstore");
		BerkleyDB users = new BerkleyDB("user-password",path);
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if(username.equals("") || username==null || password.equals("") || password==null)
		{
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html><body><h3>Invalid Username or Password!</h3><p><a href='./home.html'>Click here to try again</a></body></html>");
			
		}
		String actual_password = users.retrieve(username, "");
		if(actual_password != null)
		{
		if(!actual_password.equals(password))
		{
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html><body><h3>Invalid Username or Password!</h3><p><a href='./home.html'>Click here to try again</a></body></html>");
		}
		else
		{
			HttpSession user_session = request.getSession();
			
			if(user_session!=null)
			{
			user_session.setAttribute("username", username);
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html><body>");
			out.println("<h2>Welcome, "+username+" !!</h2>");
			out.println("<div style='margin:60 100; text-align:center'>");
			out.println("<form method='post' action='./channel'>");
			out.println("<p>URL  for style sheet: <input type='text' name='xsl'/> </p>");
			out.println("<p>Channel names: <input type='text' name='channel'/></p>");
			out.println("<p>Semicolon seperated XPATHS <input type='text' name='xpath'/></p>");
			out.println("<input type='submit'/>");
			out.println("</form>");
			out.println("<div style='margin:60 100; text-align:center'>");
			out.println("<form method='post' action='./logout'>");
			out.println("<input type='submit' value='logout'/>");
			out.println("</form>");
			out.println("<form method='post' action='./delete'>");
			out.println("<input type='text' name='delete' />");
			out.println("<input type='submit' value='delete'/>");
			out.println("</form>");
			out.println("</div>");
			out.println("</html></body>");
			}
			else
			{
				response.setContentType("text/html");
				PrintWriter out = response.getWriter();
				out.println("<html><body>");
				out.println("<h3>Please login!<a href='./home.html'> click here </a></h3>");
				out.println("</html></body>");
			}
			
		}
		}
		else
		{
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html><body><h3>Invalid Username or Password!</h3><p><a href='./home.html'>Click here to try again</a></body></html>");
		}
		users.close_all();
		
	}
}
