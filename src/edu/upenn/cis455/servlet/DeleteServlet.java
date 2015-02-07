package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.BerkleyDB;
@SuppressWarnings("serial")
public class DeleteServlet extends HttpServlet{

	public void doPost(HttpServletRequest request,HttpServletResponse response) throws java.io.IOException,MalformedURLException
	{
		String path = getServletContext().getInitParameter("BDBstore");
		BerkleyDB user_channel = new BerkleyDB("user-channel",path);
		BerkleyDB channel_xpath = new BerkleyDB("channel-xpath",path);
		BerkleyDB channel_xsl = new BerkleyDB("channel-xsl",path);
		HttpSession session = request.getSession();
		String username = (String)session.getAttribute("username");
		
		if(username==null)
		{
			username="";
		}
		
		String delete = request.getParameter("delete");
		if(delete!=null && !delete.equals(""))
		{	
		String list_channel = user_channel.retrieve(username, "");
		if(list_channel==null)
		{
			list_channel="";
		}
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<HTML><BODY>");
		if(!list_channel.contains(delete))
		{
			out.println("<h3>Cannot delete<h3>");
		}
		else
		{
			if(list_channel.contains(";"))
			{
				String[] res = list_channel.split(";");
				String str ="";
				for(int i=0;i<res.length;i++)
				{
					if(!res[i].equals(delete))
					{
						str = str+res[i]+";"; 
					}
				}
				if(res.length == 1)
				{
					str = str.substring(0,str.length()-2);
				}
				channel_xpath.delete(delete);
				channel_xsl.delete(delete);
			}
			else
			{
				channel_xpath.delete(list_channel);
				channel_xsl.delete(list_channel);
				user_channel.delete(username);
			}
			out.println("<h3>Deleted successfully! <a href='./view'>Click here to view list</a></h3>");
			
		}
		
		out.println("</BODY></HTML>");
		}
		else
		{
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<HTML><BODY>");
			out.println("<h3>Invalid input<h3>");
			out.println("</BODY></HTML>");
			
		}
		user_channel.close_all();
		channel_xpath.close_all();
		channel_xsl.close_all();
	
	}
}