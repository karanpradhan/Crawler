package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.BerkleyDB;
@SuppressWarnings("serial")
public class RegisterServlet extends HttpServlet{

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
			out.println("<html><body><h3>Invalid Username or Password!</h3><p><a href='./register.html'>Click here to try again</a></body></html>");
			
		}
		else
		{
		if(users.check(username,""))
		{
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html><body><h3>Username already exists!</h3><p><a href='./register.html'>Click here to try again</a></body></html>");
			
		}
		else
		{
			users.insert(username, password);
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html><body><h3>Registered successfully!</h3><p><a href='./home.html'>Click here to login</a></body></html>");
			
		}
		}
		
		users.close_all();
	}
}
