package edu.upenn.cis455.crawler;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class HostNameResolver {
	URL u;
	String url;
	public HostNameResolver(String url) throws MalformedURLException
	{
		this.url=url;
		u = new URL(url);
	}
	public String getHostName()
	{
		return u.getHost();
	}
	public int getPort()
	{
		return u.getDefaultPort();
	}
	public InetAddress getAddress() throws UnknownHostException
	{
		return InetAddress.getByName(u.getHost());
	}
	public String getURL()
	{
		return url;
	}
	
}
