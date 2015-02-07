package edu.upenn.cis455.xpathengine;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tokenizer {
	List<String> token_queue;
	//private static String regex = "\\/|\\[|[A-Z::a-z0-9-=()_,.\\s@]+|\".*\"|\\]";
	//private static String regex = "\\/|\\[|[A-Z::a-z0-9-=()_,.\\s@]+(\"([^\"]*)\")*\\)?|\\]";
	private static String regex = "\\/|\\[|[A-Z::a-z0-9-=()_,.\\s@]+(\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\")*\\)?|\\]";
	Tokenizer()
	{
		//token_queue=new LinkedList<String>();
	}
	void parse(String input) throws Exception
	{
		token_queue=new LinkedList<String>();
		Stack<String> s = new Stack<String>();
		input = input.trim();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		while(m.find())
		{
			if(m.group(0).equals(")") || m.group(0).equals("("))
			{	
				throw new Exception(" () exception");
				
			}
			if(!m.group(0).matches("[\\s]+")){
				String x = m.group(0).trim();
			token_queue.add(x);
			if(m.group(0).equals("["))
			{
				s.push("{");
			}
			if(m.group(0).equals("]"))
			{
				s.pop();
			}
			}
		}
		if(!s.isEmpty())
		{
			throw new Exception("Parsing error");
		}
		
	}
	String nextToken()
	{
		String tok = token_queue.remove(0);
		return tok;
	}
	void pushBack(String token)
	{	
		token_queue.add(0,token);
		
	}
	public static void main(String arg[]) throws Exception
	{
		Tokenizer t = new Tokenizer();
		t.parse("/a/b[foo[text()=\"#$(/][]\"]][bar]/hi[@asdf=\"#$(&[]\"][this][is][crazy]");
		String x = t.nextToken();
		t.pushBack(x);
		System.out.println();
	}
	
}
