package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.BerkleyDB;
@SuppressWarnings("serial")
public class DisplayServlet extends HttpServlet{

	public void doPost(HttpServletRequest request,HttpServletResponse response) throws java.io.IOException,MalformedURLException
	{
		String path = getServletContext().getInitParameter("BDBstore");
		String channel = request.getParameter("channel");
		//System.out.println(channel);
		if(channel!=null && !channel.equals(""))
		{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.US);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		SimpleDateFormat df2 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
		df2.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		
		BerkleyDB test6 = new BerkleyDB("channel-xml",path);
		BerkleyDB test7 = new BerkleyDB("url-content",path);
		BerkleyDB test8 = new BerkleyDB("url-date",path);
		BerkleyDB test9 = new BerkleyDB("channel-xsl",path);
		
		
		
		String urls = test6.retrieve(channel, "");
		if(urls!=null)
		{	
		String[] list;
		if(urls.contains(";"))
		{
			list = urls.split(";");
		}
		else
		{
			list = new String[1];
			list[0] = urls;
		}
		PrintWriter out = response.getWriter();
		response.setContentType("text/xml");
		out.println("<?xml-stylesheet type=\"text/xsl\" href=\""+ test9.retrieve(channel, "") +"\" encoding=\"UTF-8\"?> ");
		out.println("<documentcollection>");
		for(int i=0;i<list.length;i++)
		{
		String xc="";
		try {
			xc = df.format(df2.parse(test8.retrieve(list[i],"")));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		out.println("<document crawled=\""+xc +"\" location=\""+list[i]+"\">");
		String temp = test7.retrieve(list[i],"");
		if(temp!=null){
			int p = temp.indexOf("?>");
			out.println(temp.substring(p+2).trim());
		}
		out.println("</document>");
		}
		out.println("</documentcollection>");
		}
		else
		{
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.println("<html><body><h3>No match <a href='./home.html'>Click here to try again</a> </h3></body></html>");
		}
		
		test6.close_all();
		test7.close_all();
		test8.close_all();
		test9.close_all();
		}
		else
		{
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.println("<html><body><h3>Invalid channel name <a href='./home.html'>Click here to try again</a> </h3></body></html>");
		}
		
		
	}
}

