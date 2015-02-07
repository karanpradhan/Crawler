package edu.upenn.cis455.storage;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;


public class BerkleyDB {
	Environment myDbEnvironment;
	Database myDatabase;
	public BerkleyDB(String database,String path)
	{
		Environment myDbEnvironment = null;
		try {
			 EnvironmentConfig envConfig = new EnvironmentConfig();
			 envConfig.setAllowCreate(true);
			 envConfig.setTransactional(true);
			 //System.out.println(path); 
			 myDbEnvironment = new Environment(new File(path), 
			 envConfig);
			} catch (DatabaseException dbe) {
			 //System.out.println("asdf");
				dbe.printStackTrace();
			}
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setTransactional(true);
		myDatabase = myDbEnvironment.openDatabase(null, database, dbConfig);
	}
	
	public void insert(String key,String value) throws UnsupportedEncodingException
	{
		DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
		DatabaseEntry theData = new DatabaseEntry(value.getBytes("UTF-8"));

		myDatabase.put(null, theKey, theData);
	}
	public boolean check(String key,String value) throws UnsupportedEncodingException
	{
		DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
		DatabaseEntry theData = new DatabaseEntry(value.getBytes("UTF-8"));
		
		if (myDatabase.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS)
			return true;
		else
			return false;
	}
	public String retrieve(String key,String value) throws UnsupportedEncodingException
	{
		DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
		DatabaseEntry theData = new DatabaseEntry(value.getBytes("UTF-8"));
		if (myDatabase.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS)
		{
			byte[] byteData = theData.getData();
			String Data = new String(byteData, "UTF-8");
			return Data;
		}
		else
		{
			return null;
		}
	}
	public boolean delete(String key)
	{
		try{
		DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
		myDatabase.delete(null, theKey); 
		return true;
		}
		catch(Exception e)
		{
			return false;
		}

	}
	
	public Cursor get_cursor()
	{
		return myDatabase.openCursor(null, null);
	}
	
	public void close_all()
	{
		myDatabase.close();
		try{
		myDbEnvironment.close();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
	}
	
	protected void finalize() throws Throwable
	{
		myDatabase.close();
		try{
		myDbEnvironment.close();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
		super.finalize();
	}
	
	
	
	
	
	public static void main(String arg[]) throws UnsupportedEncodingException, ParseException
	{
		//String path = "./export/dbEnv";
		//BerkleyDB test5 = new BerkleyDB("channel-xpath",path);
		//test5.insert("a","/rss/channel/description");
		/*
		BerkleyDB test5 = new BerkleyDB("channel-xpath");
		test5.insert("a","/rss/channel/description[text()=\"New York Times > Breaking News, World News & Multimedia\"]");
		test5.insert("b","/rss/channel/title;/rss/channel/title[text()=\"NYT > Africa\"]");*/
		//BerkleyDB test = new BerkleyDB("url-date");
		//BerkleyDB test2 = new BerkleyDB("url-content");
		//BerkleyDB test3 = new BerkleyDB("url-ext");
		//BerkleyDB test4 = new BerkleyDB("kaka");
		//test4.insert("1", "2");
		//test.insert("a", "blah");
		//test.insert("b", "bar");
		//test.insert("c", "baz");
		//System.out.println(test.check("a", ""));
		//System.out.println(test.delete("http://crawltest.cis.upenn.edu/"));
		//System.out.println(test2.delete("http://crawltest.cis.upenn.edu/"));
		//System.out.println(test3.delete("http://crawltest.cis.upenn.edu/"));
		//System.out.println(test.retrieve("http://crawltest.cis.upenn.edu/nytimes/",""));
		//System.out.println(test.retrieve("a",""));
		//System.out.println(test.delete("c"));
		//System.out.println(test.check("c",""));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.US);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		SimpleDateFormat df2 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
		df2.setTimeZone(TimeZone.getTimeZone("GMT"));
		String path = "./export/dbEnv";
		
		BerkleyDB test5 = new BerkleyDB("channel-xpath",path);
		BerkleyDB test6 = new BerkleyDB("channel-xml",path);
		BerkleyDB test7 = new BerkleyDB("url-content",path);
		BerkleyDB test8 = new BerkleyDB("url-date",path);
		test5.insert("a","/rss/channel/description");
		String urls = test6.retrieve("a", "");
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
		
		/*
		Cursor c = test6.get_cursor();
		DatabaseEntry key = new DatabaseEntry("".getBytes());
		DatabaseEntry data = new DatabaseEntry("".getBytes());
		if(!(c.getFirst(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS))
		{
			System.out.println("No data");
		}
		System.out.println(new String(key.getData())+" : "+new String(data.getData()));
		while((c.getNext(key, data, LockMode.DEFAULT)==OperationStatus.SUCCESS))
		{
			//System.out.println("asd");
			System.out.println(new String(key.getData()));
		}
		*/
		PrintStream out = System.out;
		out.println("<documentcollection>");
		for(int i=0;i<2;i++)
		{
		String xc = df.format(df2.parse(test8.retrieve(list[i],"")));	
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
	
}
