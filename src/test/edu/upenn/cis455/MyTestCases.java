package test.edu.upenn.cis455;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.crawler.DocumentDownloader;
import edu.upenn.cis455.crawler.HostNameResolver;
import edu.upenn.cis455.storage.BerkleyDB;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class MyTestCases extends TestCase {

	public void testValidatorWrong() throws Exception
	{
		XPathEngineImpl x = new XPathEngineImpl();
		String xp = "/test[[text()=\"blah\"]";
		String arr[] = {""};
		arr[0] = xp;
		//System.out.println("blahh  "+arr);
		x.setXPaths(arr);
		assertEquals(false,x.isValid(0));
	}
	public void testValidatorCorrect() throws Exception
	{
		XPathEngineImpl x = new XPathEngineImpl();
		String xp = "/test [ a/b1[ c1[ p]/d[p ] ] / n1[a]/n2 [c2/d[p]/e[contains( text(), \"[contains ( text() , \\\"/ a  sp&123( 123*/\\\"] \\\"  \\\")]\")]]]";
		String arr[] = {""};
		arr[0] = xp;
		//System.out.println("blahh  "+arr);
		x.setXPaths(arr);
		assertEquals(true,x.isValid(0));
	}
	public void testEvaluteSuccess() throws Exception
	{
		XPathEngineImpl x = new XPathEngineImpl();
		String temp = "/html/body/p/i[text()=\"grammar\"]";
		String[] temp1 ={""};
		temp1[0] = temp;
		x.setXPaths(temp1);
		InputStream is = new FileInputStream("test.html");
		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
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
		System.out.println(writer.toString().getBytes());
		Document doc = db.parse(new ByteArrayInputStream(writer.toString().getBytes()));
		boolean arr[] = x.evaluate(doc);
		assertEquals(true,arr[0]);
	}
	
	public void testEvaluteFailure() throws Exception
	{
		XPathEngineImpl x = new XPathEngineImpl();
		String temp = "/html/p/i[text()=\"grammar\"]";
		String[] temp1 ={""};
		temp1[0] = temp;
		x.setXPaths(temp1);
		InputStream is = new FileInputStream("test.html");
		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
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
		System.out.println(writer.toString().getBytes());
		Document doc = db.parse(new ByteArrayInputStream(writer.toString().getBytes()));
		boolean arr[] = x.evaluate(doc);
		assertEquals(false,arr[0]);
	}
	public void testServlet() throws Exception
	{
		String xml_url = "http://www.w3schools.com/xml/cd_catalog.xml";
		//String xpath = request.getParameter("xpath");
		InetAddress  address = InetAddress.getByName(new URL(xml_url).getHost());
		int port = new URL(xml_url).getDefaultPort();
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
		boolean b = headers.contains("200 OK");
		
		
		assertEquals(true,b);
		
	}
	
	public void testStorage() throws UnsupportedEncodingException
	{
		File f = new File("./testdb");
		if (!f.exists()) {
		    f.mkdir(); }
		BerkleyDB b = new BerkleyDB("test","./testdb");
		b.insert("first", "hello");
		String test = b.retrieve("first", "");
		assertEquals(true,test.equals("hello"));
		b.close_all();
		
	}
	public void testStorage2() throws UnsupportedEncodingException
	{
		File f = new File("./testdb");
		if (!f.exists()) {
		    f.mkdir(); }
		BerkleyDB b = new BerkleyDB("test","./testdb");
		b.insert("first", "hello");
		boolean test = b.check("first", "");
		assertEquals(true,test);
		b.close_all();
	}
	public void testcrawler1() throws UnknownHostException, IOException
	{
		HostNameResolver url1 =new  HostNameResolver("http://crawltest.cis.upenn.edu/");
		HashMap<String,String> headers = DocumentDownloader.getHeaders(url1.getURL(), url1.getHostName(),url1.getPort(),url1.getAddress(),null);
		boolean test = headers.containsKey("status");
		assertEquals(true,test);
	}
	
	public void testcrawler2() throws UnknownHostException, IOException
	{
		HostNameResolver url1 =new  HostNameResolver("http://crawltest.cis.upenn.edu/robot.txt");
		HashMap<String,String> robots = DocumentDownloader.processRobotsTxt(url1.getURL(), url1.getHostName(),url1.getPort(),url1.getAddress());
		boolean test=robots.containsKey("disallow");
		assertEquals(true,test);
	}
	
}
