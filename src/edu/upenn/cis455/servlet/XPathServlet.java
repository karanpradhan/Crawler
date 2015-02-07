package edu.upenn.cis455.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

  /* TODO: Implement user interface for XPath engine here */
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws java.io.IOException,MalformedURLException
	{
		String xml_url = request.getParameter("xml");
		String xpath = request.getParameter("xpath");
		InetAddress  address = InetAddress.getByName(new URL(xml_url).getHost());
		int port = new URL(xml_url).getDefaultPort();
		String[] xpath_array;
		if(!xpath.contains(";")){
		xpath_array = new String[1];
		xpath_array[0] = xpath;}
		else
		{
			xpath_array = xpath.split(";");
		}
		System.out.println(Arrays.toString(xpath_array));
		XPathEngineImpl xpath_engine = new XPathEngineImpl();
		
		/*BufferedInputStream in = new BufferedInputStream(new URL(xml_url).openStream());
		FileOutputStream fout = null;
		File xml_file = new File("123");
		fout= new FileOutputStream(xml_file);
		int count;
		while((count = in.read())!=-1)
		{
			fout.write(count);
		}
		fout.close();
		in.close();
		*/
		Socket client = new Socket(address,port);
		PrintWriter out1 = new PrintWriter(client.getOutputStream(),true);
		out1.println("GET "+xml_url+" HTTP/1.1\r\nHost: "+new URL(xml_url).getHost()+"\r\nContent-Disposition: attachment;filename=a.xml\r\nContent-Type: application/octet-stream\r\nUser-Agent: cis455crawler\r\nConnection: close\r\n");
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
		String MIMEtype = "";
		String _fileType = "";
		if(headers.contains("Content-Type: "))
		{
			int first = headers.indexOf("Content-Type: ");
			int last = headers.indexOf("\r\n", first);
			MIMEtype = headers.substring(first,last);
			int _slash = MIMEtype.indexOf("/");
			_fileType = MIMEtype.substring(_slash+1);
			
		}
		System.out.println(_fileType);
		String url_hash = xml_url.hashCode()+"."+_fileType;
		File xml_file = new File(url_hash);
		FileOutputStream fout = new FileOutputStream(xml_file);
		int line;
		while((line = in.read())!=-1)
		{
			fout.write(line);
		}
		in.close();
		fout.close();
		
	/*	if(_fileType.equals("html"))
		{
			for(int p=0;p<xpath_array.length;p++)
			{
				xpath_array[p] = xpath_array[p].toLowerCase();
			}
		}
		*/
		
		xpath_engine.setXPaths(xpath_array);
		
		
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  dbf.setNamespaceAware(true);
	  dbf.setValidating(false);
	  DocumentBuilder db =null;
	  try {
		dbf.setFeature("http://xml.org/sax/features/namespaces", false);
		dbf.setFeature("http://xml.org/sax/features/validation", false);
		  dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		  dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		  db = dbf.newDocumentBuilder();
		  
	} catch (ParserConfigurationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  Tidy tidy = new Tidy();
	  if(_fileType.equals("html"))
	  {
		  tidy.setXHTML(true);
	  }
	  if(_fileType.equals("xml"))
	  {
		  tidy.setXmlTags(true);
	  }
	  InputStream is = new FileInputStream(xml_file);
	  StringWriter writer = new StringWriter();
		tidy.parse(is,writer);
	  
	  
		
		
		
		
		
	  Document doc = null;
	  try {
		 
			  doc = db.parse(new ByteArrayInputStream(writer.toString().getBytes()));
	} catch (SAXException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		boolean[] xpath_eval = xpath_engine.evaluate(doc);
		xml_file.delete();
		//new_html.delete();
		boolean answer = false;
		/*
		for(int i=0;i<xpath_eval.length;i++)
		{
			answer = answer | xpath_eval[i];
		}*/
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<body>");
		out.println("<h2>XPath Evaluations</h2>");
		for(int i=0;i<xpath_array.length;i++)
		{
			out.println("<h4>"+xpath_array[i]);
			if(xpath_eval[i])
			{
				out.println(": Success</h4>");
			}
			else
			{
				out.println(": Failed the test</h4>");
			}
		}
		out.println("</body>");
		out.println("</html>");
	}

}









