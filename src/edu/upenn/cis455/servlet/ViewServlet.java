package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import edu.upenn.cis455.storage.BerkleyDB;
@SuppressWarnings("serial")
public class ViewServlet extends HttpServlet{

	public void doGet(HttpServletRequest request,HttpServletResponse response) throws java.io.IOException,MalformedURLException
	{
		String path = getServletContext().getInitParameter("BDBstore");
		BerkleyDB user_channel = new BerkleyDB("channel-xpath",path);
		Cursor cr = user_channel.get_cursor();
		DatabaseEntry key = new DatabaseEntry("".getBytes());
		DatabaseEntry data = new DatabaseEntry("".getBytes());
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><body>");
		if(!(cr.getFirst(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS))
		{
			//System.out.println("No data");
			out.println("<h3>"+"No channels!"+"</h3>");
		}
		else
		{
			out.println("<h3>"+"Channel List!"+"</h3>");
			out.println("<h3>"+new String(key.getData()) +"</h3>");
			while((cr.getNext(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS))
			{
				out.println("<h3>"+new String(key.getData())+"</h3>");
			}
		}
		out.println("</body></html>");
		cr.close();
		user_channel.close_all();
		
	}
	
}
