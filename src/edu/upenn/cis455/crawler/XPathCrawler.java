package edu.upenn.cis455.crawler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import edu.upenn.cis455.storage.BerkleyDB;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

//import edu.upenn.cis455.storage.*;

public class XPathCrawler {
	
	
	
  public static void main(String args[]) throws UnknownHostException, IOException, ParseException, InterruptedException, URISyntaxException
  {
	  String path = args[1];
	  //System.out.println(path+"  fadsf ");
    /* TODO: Implement crawler */
	   
	   BerkleyDB url_channel = new BerkleyDB("url-channel",path);
	   BerkleyDB url_date = new BerkleyDB("url-date",path);
	   BerkleyDB channel_xpath = new BerkleyDB("channel-xpath",path);
	   BerkleyDB url_ext = new BerkleyDB("url-ext",path);
	   BerkleyDB channel_xml = new BerkleyDB("channel-xml",path);
	   BerkleyDB url_content = new BerkleyDB("url-content",path);
	   
	   
	 	String url_start = args[0];
	  	//String berkley_db = args[1];
	  	double size_in_mb = Double.parseDouble(args[2]);
	  	//int max_files = Integer.parseInt(args[3]);
	    URLFrontier url_queue= new URLFrontier();
	    //url_queue.push("http://www.spit.ac.in/");
	    url_queue.push(url_start);
    while(!url_queue.isEmpty())
	    {
    	try
    	{
    	HostNameResolver url1 = null;
    	
		url1 =new  HostNameResolver(url_queue.pop());
		HashMap<String,String> headers = null;
    	String date = null;
		if(url_date.check(url1.getURL(),""))
		{
			
			date = url_date.retrieve(url1.getURL(),"");
			
			Calendar now = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			String date_now = df.format(now.getTime());	
			save_timestamp(url_date,url1.getURL(),date_now);
			
			
		}
		headers= DocumentDownloader.getHeaders(url1.getURL(), url1.getHostName(),url1.getPort(),url1.getAddress(),date);
	    String location = "";
	    boolean to_download = true;
	    boolean modified = false;
	    String type =  null;
		if(headers.containsKey("status"))
	    {
	    	if(headers.get("status").equals("302") || headers.get("status").equals("301"))
	    	{
	    		location = headers.get("location");
	    		if(!location.contains("http://"))
	    			location = "http://" + new URL(location).getHost() + location;
	    		
	    		url_queue.push(location);
	    		to_download = false;
	    		//continue;
	    	}
	    	if(headers.get("status").equals("304"))
	    	{
	    		if(headers.containsKey("type"))
	    		{
	    			type = headers.get("type");
	    		}
	    		System.out.println(url1.getURL()+" ... Not Modified");
	    		to_download = false;
	    		modified = true;
	    	}
	    	if(headers.get("status").equals("error"))
	    	{
	    		to_download = false;
	    		//modified=true;
	    	}
	    }
		else
		{
			to_download = false;
		}
		
		if(headers.containsKey("type"))
		{
			type = headers.get("type");
		}
		else
		{
			to_download = false;
		}
		if(headers.containsKey("length"))
		{	
			int size = 0;
			try{
			size = Integer.parseInt(headers.get("length"));
			}
			catch(Exception e)
			{
				size = 0;
			}
		//	size = size * 1048576;
		//	if(size > max_files)
		//		to_download = false;
		//	if(size == 0)
		//		to_download = true;
			if(size!=0)
			{
				double temp = (double)size / 1048576;
				if(temp > size_in_mb)
					to_download = false;
			}
			
		}
		Date d = null;
		if(headers.containsKey("date-modified"))
		{
			SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			try{
			d = df.parse(headers.get("date-modified"));
			}
			catch(Exception e)
			{
				
			}
		}
		HashMap<String,String> robots = null;
		if(to_download)
			robots= DocumentDownloader.processRobotsTxt(url1.getURL(), url1.getHostName(),url1.getPort(),url1.getAddress());
		int delay = 0;
		if(robots!=null)
		{
			if(robots.containsKey("crawl-delay"))
			{
				delay = Integer.parseInt(robots.get("crawl-delay"));
			}
			String disallow[] = null;
			if(robots.containsKey("disallow"))
			{
				
				String temp = robots.get("disallow");
				if(temp.contains(";"))
				{
					disallow = temp.split(";");
				}
				else
				{
					disallow = new String[1];
					disallow[0] = temp;
				}
				
			}
			if(disallow!=null)
			{
				for(int i=0;i<disallow.length;i++)
				{
					String path_current = new URL(url1.getURL()).getPath();
					if(path_current.startsWith(disallow[i]))
						to_download = false;
				}
			}	
		}
		
		File f =null;
		if(to_download)
		{
		//	Thread.sleep(delay*1000);
			Calendar now = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			String date_now = df.format(now.getTime());
			
			
			f = DocumentDownloader.downloadFile(url1.getURL(), url1.getHostName(),url1.getPort(),url1.getAddress(),url_content);
			System.out.println(url1.getURL()+" ... Downloading");
			//save_in_DB(url_content,url1.getURL(),f);
			save_timestamp(url_date,url1.getURL(),date_now);
			if(type.equals("html")){
				//System.out.println(url1.u.toURI().toString());
				ArrayList<String> anchors = DocumentDownloader.getAnchors(f, "html",url1.u.toURI().toString());
				for(String i : anchors)
				{
					if(!url_queue.checkFrontier(i))
					{
						url_queue.push(i);
					}
				}
				url_ext.insert(url1.getURL(), "html");
				//System.out.println(anchors);
			}
			
			if(type.equals("xml"))
			{
				Document doc = getXMLDocument(f);
				ArrayList<String> channels = getChannels(channel_xpath);
				if(channels!=null)
				{
				for(String x:channels)
				{
					String[] xpaths = getXpaths(channel_xpath,x);	
					XPathEngineImpl x_impl = new XPathEngineImpl();
					x_impl.setXPaths(xpaths);
					boolean arr[] = x_impl.evaluate(doc);
					boolean arr_flag = cumm_boolean(arr);
					if(channel_xml.check(x, ""))
					{
						if(arr_flag)
						{
						String temp = channel_xml.retrieve(x, "");
						temp = temp + ";" +url1.getURL();
						channel_xml.insert(x, temp);
						}
					}
					else
					{
						if(arr_flag)
						{
							channel_xml.insert(x,url1.getURL());
						}
					}
					
				}
				}
			}
				
			
		}
		
		if(modified)
		{
			File temp = get_file(url_content,url1.getURL());
			String temp_type = url_ext.retrieve(url1.getURL(), "");
				//System.out.println(url1.u.toURI().toString());
			ArrayList<String> anchors = null;
			
			    if(temp_type!=null)
			    {
			    	Calendar now = Calendar.getInstance();
					SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
					df.setTimeZone(TimeZone.getTimeZone("GMT"));
					String date_now = df.format(now.getTime());	
					save_timestamp(url_date,url1.getURL(),date_now);
			    	
				if(temp_type.equals("html")){
				 anchors= DocumentDownloader.getAnchors(temp, "html",url1.u.toURI().toString());
				 
				 for(String i : anchors)
					{
						if(!url_queue.checkFrontier(i))
						{
							url_queue.push(i);
						}
					
				
					}
				 
				}
			    }
				
				temp.delete();
		}
		
		
		if(f!=null)
			f.delete();
		
	    
	    }
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		
    	}
	    }
    
    	url_content.close_all();
	    url_channel.close_all();
	    url_date.close_all();
	    channel_xpath.close_all();
	    url_ext.close_all();
	    channel_xml.close_all();
	   
    
  }
  
  
  static void save_in_DB(BerkleyDB database,String url,File f) throws UnsupportedEncodingException
  {
	  FileInputStream fis = null;
      String str = "";

      try {
          fis = new FileInputStream(f);
          int content;
          while ((content = fis.read()) != -1) {
              // convert to char and display it
              str += (char) content;
          }
      }
     catch(Exception e)
     {
    	 e.printStackTrace();
     }
      database.insert(url,str);
  
	
}
  static void save_timestamp(BerkleyDB database,String url,String date) throws UnsupportedEncodingException
  {
	  database.insert(url,date);
  }
  
  static File get_file(BerkleyDB database,String url) throws IOException
  {
	  String f_temp = database.retrieve(url,"");
	  FileOutputStream fop = null;
		File file= new File(url.hashCode()+"");
		fop=new FileOutputStream(file);
		byte[] content = f_temp.getBytes();
		fop.write(content);
		fop.flush();
		fop.close();
		return file;
	  
  }
  
  static ArrayList<String> getChannels(BerkleyDB database)
  {
	  	ArrayList<String> res = new ArrayList<String>();
	  	Cursor cr = database.get_cursor();
		DatabaseEntry key = new DatabaseEntry("".getBytes());
		DatabaseEntry data = new DatabaseEntry("".getBytes());
		if(!(cr.getFirst(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS))
		{
			cr.close();
			return null;
		}
		else
		{
			
			res.add(new String(key.getData())) ;
			while((cr.getNext(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS))
			{
				res.add(new String(key.getData())) ;
			}
			cr.close();
			return res;
		}
		
  }
  
  static String[] getXpaths(BerkleyDB database,String channel) throws UnsupportedEncodingException
  {
	 String s=  database.retrieve(channel,"");
	 if(s.contains(";"))
	 {
		 String res[] = s.split(";");
		 return res;
	 }
	 else
	 {
		 String res[] = new String[1];
		 res[0] = s;
		 return res;
	 }
  }
  
  static Document getXMLDocument(File f) throws ParserConfigurationException, SAXException, IOException
  {
	  	InputStream is = new FileInputStream(f);
		Tidy tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		tidy.setShowErrors(0);
		tidy.setXmlTags(true);
		StringWriter writer = new StringWriter();
		tidy.parse(is,writer);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);
		dbf.setFeature("http://xml.org/sax/features/namespaces", false);
		dbf.setFeature("http://xml.org/sax/features/validation", false);
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		//System.out.println(writer.toString().getBytes());
		Document doc = db.parse(new ByteArrayInputStream(writer.toString().getBytes()));
		return doc;
  }
  
  
  static boolean cumm_boolean(boolean[] x)
  {
	  boolean ret = false;
	  for(int i=0;i<x.length;i++)
	  {
		  ret = ret | x[i];
	  }
	  return ret;
  }
}
