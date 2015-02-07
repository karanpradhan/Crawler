package edu.upenn.cis455.crawler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.storage.BerkleyDB;

public class DocumentDownloader {

	public static HashMap<String,String> getHeaders(String url,String host,int port,InetAddress ipaddress,String date) throws IOException
	{
		HashMap<String,String> headers_map = new HashMap<String,String>();
		Socket client = new Socket(ipaddress,port);
		PrintWriter out = new PrintWriter(client.getOutputStream(),true);
		//System.out.println("HEAD "+url+" HTTP/1.1\r\nHost: "+host+"\r\n"+"If-Modified-Since: "+date+"\r\nUser-Agent: cis455crawler\r\nConnection: close\r\n");
		if(date!=null)
			out.println("HEAD "+url+" HTTP/1.1\r\nHost: "+host+"\r\n"+"If-Modified-Since: "+date+"\r\nUser-Agent: cis455crawler\r\nConnection: close\r\n");
		else
			out.println("HEAD "+url+" HTTP/1.1\r\nHost: "+host+"\r\nUser-Agent: cis455crawler\r\nConnection: close\r\n");
		int br;
		BufferedInputStream in = new BufferedInputStream(client.getInputStream());
		String temp = "";
		byte data;
		String headers = "";
		while(!"\r\n\r\n".equals(temp)){
			data = (byte) in.read();
			headers += String.valueOf((char)data);
			if( ((char)data) == '\r' || (((char)data) == '\n'))
			{
				temp += String.valueOf((char)data);
			}
			else
			{
				temp = "";
			}
		}
		int first_stat = headers.indexOf("HTTP/1.1 ");
		//int first_stat_len = "HTTP/1.1 ".length();
		int last_stat = headers.indexOf("\r\n",first_stat);
		String status = headers.substring(first_stat,last_stat);
		boolean location_flag = false;
		if(status.contains("200"))
		{
			headers_map.put("status","200");

		}
		else if(status.contains("301")){
			headers_map.put("status", "301");
			location_flag =true;
		}

		else if(status.contains("302")){
			headers_map.put("status","302");
			location_flag = true;
		}
		else if(status.contains("304")){
			headers_map.put("status", "304");
			
		}
		
		else
			headers_map.put("status", "error");


		String MIMEtype = "";
		String _fileType = "";
		if(headers.contains("Content-Type: "))
		{
			int first = headers.indexOf("Content-Type: ");
			int last = headers.indexOf("\r\n", first);
			MIMEtype = headers.substring(first,last);
			int _slash = MIMEtype.indexOf("/");

			if(MIMEtype.contains(";")){
				int semicolon = MIMEtype.indexOf(";");
				_fileType = MIMEtype.substring(_slash+1,semicolon);
			}
			else
			{
				_fileType = MIMEtype.substring(_slash+1);
			}

			if(_fileType.equals("html")||_fileType.equals("xml"))
			{
				headers_map.put("type", _fileType);
			}

		}

		String content_length = "";
		if(headers.contains("Content-Length: "))
		{
			int len = "Content-Length: ".length();
			int first = headers.indexOf("Content-Length: ");
			int last = headers.indexOf("\r\n",first);
			content_length = headers.substring(first+len,last);
		}
		headers_map.put("length",content_length);
		

		if(location_flag)
		{
			String location = "";
			if(headers.contains("Location: "))
			{
				int len = "Location: ".length();
				int first = headers.indexOf("Location: ");
				int last = headers.indexOf("\r\n",first);
				location = headers.substring(first+len,last);
			}
			headers_map.put("location",location);
		}
		
			
		String date_modified = "";
		if(headers.contains("Last-Modified: "))
		{
			int len = "Last-Modified: ".length();
			int first = headers.indexOf("Last-Modified: ");
			int last = headers.indexOf("\r\n",first);
			date_modified = headers.substring(first+len,last);
		}
		headers_map.put("date-modified", date_modified);
		

		return headers_map;

	}

public static HashMap<String,String> processRobotsTxt(String url,String host,int port,InetAddress ipaddress) throws IOException
	{
		HashMap<String,String> robots = new HashMap<String,String>();
		//System.out.println(new URL(url).getHost());
		HashMap headers = getHeaders("http://"+new URL(url).getHost()+"/robots.txt",host,port,ipaddress,null);
		if(!headers.get("status").equals("200"))
			return null;
		Socket client = new Socket(ipaddress,port);
		PrintWriter out = new PrintWriter(client.getOutputStream(),true);
		out.println("GET "+"http://"+new URL(url).getHost()+"/robots.txt"+" HTTP/1.1\r\nHost: "+host+"\r\nUser-Agent: cis455crawler\r\nConnection: close\r\n");
		int br;
		BufferedInputStream in = new BufferedInputStream(client.getInputStream());
		//BufferedReader in2 = new BufferedReader(new InputStreamReader(client.getInputStream()));
		String temp = "";
		byte data;
		String headers1 = "";
		while(!"\r\n\r\n".equals(temp)){
			data = (byte) in.read();
			headers1 += String.valueOf((char)data);
			if( ((char)data) == '\r' || (((char)data) == '\n'))
			{
				temp += String.valueOf((char)data);
			}
			else
			{
				temp = "";
			}
		}
		//System.out.println(headers1);
		//in.close();
		//String url_hash = url.hashCode()+"."+headers.get("type");
		BufferedReader in2 = new BufferedReader(new InputStreamReader(in));
		/*StringBuilder robot_buffer = new StringBuilder();
		int line = 0;
		while((line = in.read())!=-1)
		{
			robot_buffer.append((char)line);
		}
		System.out.println(robot_buffer);

		 */
		String line = "";
		while((line = in2.readLine())!=null)
		{	
			line = line.trim();
			boolean override_flag = false;

			if(line.contains("User-agent: *") && !override_flag)
			{
				robots.put("user-agent", "*");
				String line_temp = "";
				while((line_temp = in2.readLine())!=null && line_temp.length()>0)
				{
					//System.out.println("aaa->"+line_temp);
					if(line_temp.contains("Disallow: "))
					{
						if(!robots.containsKey("disallow"))
						{
							int idx = line_temp.indexOf(":");
							robots.put("disallow", line_temp.substring(idx+2));
						}
						else
						{
							int idx = line_temp.indexOf(":");
							String x  = robots.get("disallow");
							robots.put("disallow", x+";"+line_temp.substring(idx+2));
						}
					}
					if(line_temp.contains("Crawl-delay: "))
					{
						int idx = line_temp.indexOf(":");
						robots.put("crawl-delay", line_temp.substring(idx+2));
					}
				}
			}

			if(line.contains("User-agent: cis455crawler"))
			{

				robots.clear();
				robots.put("user-agent", "cis455crawler");
				String line_temp2 = "";
				while((line_temp2 = in2.readLine())!=null && line_temp2.length()>0 )
				{

					if(line_temp2.contains("Disallow: "))
					{
						if(!robots.containsKey("disallow"))
						{
							int idx = line_temp2.indexOf(":");
							robots.put("disallow", line_temp2.substring(idx+2));
						}
						else
						{
							int idx = line_temp2.indexOf(":");
							String x  = robots.get("disallow");
							robots.put("disallow", x+";"+line_temp2.substring(idx+2));
						}
					}
					if(line_temp2.contains("Crawl-delay: "))
					{
						int idx = line_temp2.indexOf(":");
						robots.put("crawl-delay", line_temp2.substring(idx+2));
					}
				}
				override_flag = true;
			}

		}
		//System.out.println(line);

		//System.out.println("Returning");
		return robots;
	}

	static File downloadFile(String url,String host,int port,InetAddress ipaddress,BerkleyDB database) throws IOException
	{
		File f = new File(url.hashCode()+"");
		Socket client = new Socket(ipaddress,port);
		PrintWriter out = new PrintWriter(client.getOutputStream(),true);
		out.println("GET "+url+" HTTP/1.1\r\nHost: "+host+"\r\nUser-Agent: cis455crawler\r\nConnection: close\r\n");
		BufferedInputStream in = new BufferedInputStream(client.getInputStream());
		String temp = "";
		byte data;
		String headers = "";
		
		while(!"\r\n\r\n".equals(temp)){
			data = (byte) in.read();
			headers += String.valueOf((char)data);
			if( ((char)data) == '\r' || (((char)data) == '\n'))
			{
				temp += String.valueOf((char)data);
			}
			else
			{
				temp = "";
			}
		}
		FileOutputStream fout = new FileOutputStream(f);
		int line;
		StringBuffer str = new StringBuffer();
		while((line = in.read())!=-1)
		{
			str.append((char)line);
			fout.write(line);
		}
		
		fout.close();
		in.close();
		
		
		database.insert(url, str.toString());
		return f;
	}

	static ArrayList<String> getAnchors(File f,String type,String url) throws FileNotFoundException, MalformedURLException, UnsupportedEncodingException
	{
		
		String p_url = "";
		int pos = url.lastIndexOf("/");
		p_url = url.substring(0,pos+1);
		//System.out.println(p_url);
		ArrayList<String> anchor_list = new ArrayList<String>();
		Tidy tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		tidy.setShowErrors(0);
		if(type.equals("html"))
		{
			tidy.setXHTML(true);
		}
		if(type.equals("xml"))
		{
			tidy.setXmlTags(true);
		}
		InputStream is = new FileInputStream(f);
		OutputStream o = null;
		Document d = null;
		d = tidy.parseDOM(is, o);
		NodeList nl = d.getElementsByTagName("a");
		if(nl.getLength()>0)
		{
			for(int i = 0;i<nl.getLength();i++)
			{
				try
				{
				URL base_url = new URL(url);
				
				NamedNodeMap temp = nl.item(i).getAttributes();
				if(temp!=null && temp.getLength() != 0)
				{
					String value = "";
					Node temp2 =  temp.getNamedItem("href");
					if(temp2!=null){
						value=  temp2.getNodeValue();
						/* if(value.contains("http"))
						  anchor_list.add(value);
					  else
					  {
						  if(value.startsWith("/"))
							  anchor_list.add(p_url+value.substring(1));
						  else
							  anchor_list.add(p_url+value);*/
						URL now = new URL(base_url,value);
						if(!now.toString().contains("https")){
							anchor_list.add(URLDecoder.decode(now.toString(),"UTF-8"));}
						

					}
				}
				}
				catch(Exception e)
				{
					continue;
				}
			}
		}


		return anchor_list;

	}


	public static void main(String arg[])throws Exception
	{
		HostNameResolver url1 =new  HostNameResolver("http://crawltest.cis.upenn.edu/");
		//System.out.println(DocumentDownloader.getHeaders(url1.getURL(), url1.getHostName(),url1.getPort(),url1.getAddress(),"Mon, 01 Mar 2014 22:06:57 GMT"));
		//System.out.println(DocumentDownloader.processRobotsTxt(url1.getURL(), url1.getHostName(),url1.getPort(),url1.getAddress()));
		//File f =DocumentDownloader.downloadFile(url1.getURL(), url1.getHostName(),url1.getPort(),url1.getAddress());
		//System.out.println(DocumentDownloader.getAnchors(f, "html",url1.u.toURI().toString()));
		System.out.println(DocumentDownloader.getHeaders(url1.getURL(), url1.getHostName(),url1.getPort(),url1.getAddress(),null));
	}
}
