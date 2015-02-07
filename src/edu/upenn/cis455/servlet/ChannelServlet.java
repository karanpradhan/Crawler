package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.BerkleyDB;
@SuppressWarnings("serial")
public class ChannelServlet extends HttpServlet{

	public void doPost(HttpServletRequest request,HttpServletResponse response) throws java.io.IOException,MalformedURLException
	{
		String path = getServletContext().getInitParameter("BDBstore");
		BerkleyDB user_channel = new BerkleyDB("user-channel",path);
		BerkleyDB channel_xpath = new BerkleyDB("channel-xpath",path);
		BerkleyDB channel_xsl = new BerkleyDB("channel-xsl",path);
		String channel = request.getParameter("channel");
		String xsl = request.getParameter("xsl");
		String xpath = request.getParameter("xpath");
		HttpSession session = request.getSession();
		String username = (String) session.getAttribute("username");
		boolean flag = false;
		
		if(channel!=null && !channel.equals("") && xsl!=null && !xsl.equals("") && xpath!=null && !xpath.equals("") && username!=null && !username.equals(""))
		{
		
		if(channel_xpath.check(channel,""))
		{
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<HTML<BODY>");
			out.println("<h2>Already exists in database</h2>");
			out.println("</BODY></HTML>");
		}
		else	
		{	
		if(user_channel.check(username, ""))
		{
			String ret = user_channel.retrieve(username, "");
			ret = ret + ";"  + channel;
			user_channel.insert(username, ret);
		}
		else
		{
			user_channel.insert(username, channel);
		}
		
		channel_xpath.insert(channel, xpath);
		
			
		
		channel_xsl.insert(channel, xsl);
		
		
		
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<HTML<BODY>");
		out.println("<h2>Successfully inserted into database</h2>");
		out.println("<h3><a href='./view'>Click here to view all channels</a></h3>");
		out.println("</BODY></HTML>");
			
		}
		}
		else
		{
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<HTML<BODY>");
			out.println("<h2>Invalid Input</h2>");
			out.println("</BODY></HTML>");
		}
		
		user_channel.close_all();
		channel_xpath.close_all();
		channel_xsl.close_all();
		
		}
	}

