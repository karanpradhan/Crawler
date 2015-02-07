package edu.upenn.cis455.xpathengine;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

public class XPathEngineImpl implements XPathEngine {
	String[] xpaths;
	Tokenizer t;
	@SuppressWarnings("rawtypes")
	Queue evaluation_queue;
	Document d;
	@SuppressWarnings("rawtypes")
	Stack s;
	boolean changed;
	@SuppressWarnings("rawtypes")
	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
		this.t = new Tokenizer();

	}

	public void setXPaths(String[] s) {
		/* TODO: Store the XPath expressions that are given to this method */
		xpaths = s;
	}

	public boolean isValid(int i) {
		/* TODO: Check which of the XPath expressions are valid */
		//s= new Stack();
		boolean ans = false;
		try{
			t.parse(xpaths[i].trim());
			ans = xpath(t.nextToken());
		}
		catch(Exception e){
			return false;
		}
		return ans;
	}

	public boolean[] evaluate(Document d) { 
		/* TODO: Check whether the document matches the XPath expressions */
		// evaluation_queue = new ArrayDeque();
		s = new Stack();
		boolean result[] = new boolean[xpaths.length]; 
		this.d= d;
		this.d.getDocumentElement().normalize();
		changed = false;
		for(int i=0;i<xpaths.length;i++)
		{
			if(isValid(i))
			{
				try {
					t.parse(xpaths[i]);
					evaluation_queue = new ArrayDeque();
					result[i]=eval_xpath(t.nextToken());
					//System.out.println(evaluation_queue);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}

			}
			else
			{
				result[i] = false;
			}
		}
		return result;
	}

	public boolean xpath(String token) throws Exception
	{

		String token1 = "";
		// String token2 = null;
		if(axis(token))
		{
			token1 = t.nextToken();
		}
		else
			return false;
		if(step(token1))
		{
			//token2 = t.nextToken();
			return true;
		}
		else
		{
			//t.pushBack(token2);
			t.pushBack(token1);
			return false;
		}

	}
	public boolean axis(String token) throws Exception
	{
		return token.equals("/");
	}
	@SuppressWarnings("unchecked")
	public boolean step(String token) throws Exception
	{
		
		//System.out.println("dsfdfsdf");
		int[] counter = {0,0};
		String token2="";
		String token4 = "";
		String token3="";

		if(nodeName(token) )
		{
			if(!t.token_queue.isEmpty()){
				token2 = t.nextToken();

				boolean extra2 = true;
				boolean extra = false;
				if(token2.equals("[") || token2.equals("/"))
				{
				if(token2.equals("["))
				{
					while(token2.equals("["))
					{
						extra = false;
						token4=t.nextToken();

						if(!test(token4))
						{
							throw new Exception("predicate exception");
						}
						String token5 = t.nextToken();
						if(!token5.equals("]"))
						{
							throw new Exception("] ing exception");
						}
						if(!t.token_queue.isEmpty()){
							token2 = t.nextToken();
							extra = true;
						}
						else
							return true;
					}
				}
			
				extra2 = false;
				}
				if(extra2)
					t.pushBack(token2);
				if(axis(token2))
				{
					if(!t.token_queue.isEmpty()){
						token3 = t.nextToken();
						counter[0]++;
						if(step(token3))
						{
							counter[1]++;
						}
						else
						{
							t.pushBack(token3);
							return false;
						}
					}
					if(!(counter[0] == counter[1] && counter[0] <= 1))
					{
						return false;
					}
					extra = false;
				}
				if(extra)
				{
					t.pushBack(token2);
				}

				return true;
			}
			else
			{
				return true;
			}
		}
		else
		{
			return false;
		}
	}


	public boolean test(String token) throws Exception
	{
		if(step(token))
		{
			return true;
		}
		int position = token.indexOf("\"");
		String temp = token.substring(0,position);
		temp = temp.trim();
		temp = temp.replaceAll(" ", "");
		String temp2 = temp+token.substring(position);

		if(temp2.contains("text()="))
		{
			return true;
		}

		if(temp2.contains("contains(text(),"))
		{
			return true;
		}
		if(temp2.contains("@") && temp2.contains("="))
		{
			return true;
		}


		return false;
	}
	public boolean nodeName(String token) throws Exception
	{
		token = token.trim();
		return token.matches("[A-Za-z0-9._-]+");
	}

	// Function for evaluation

	public boolean eval_xpath(String token) throws Exception
	{
		//  Element e = d.getDocumentElement();
		
		String token1 = "";
		// String token2 = null;
		if(eval_axis(token))
		{
			token1 = t.nextToken();
		}
		else
			return false;
		if(eval_step(token1))
		{
			//token2 = t.nextToken();
			return true;
		}
		else
		{
			//t.pushBack(token2);
			t.pushBack(token1);
			return false;
		}

	}
	public boolean eval_axis(String token)
	{
		return token.equals("/");
	}
	@SuppressWarnings("unchecked")
	public boolean eval_step(String token) throws Exception
	{
		//System.out.println(token);
		int[] counter = {0,0};
		String token2="";
		String token4 = "";
		String token3="";

		if(eval_nodeName(token) )
		{
			if(!t.token_queue.isEmpty()){
				token2 = t.nextToken();

				boolean extra2 = true;
				boolean extra = false;
				if(token2.equals("[") || token2.equals("/"))
				{
				if(token2.equals("["))
				{
					while(token2.equals("["))
					{
						ArrayDeque org = new ArrayDeque();
						org.addAll(evaluation_queue);
						s.push(org);
						extra = false;
						token4=t.nextToken();

						if(!eval_test(token4))
						{
							throw new Exception("predicate exception");
						}
						String token5 = t.nextToken();
						if(!token5.equals("]"))
						{
							throw new Exception("] ing exception");
						}
						ArrayDeque now = (ArrayDeque)s.pop();
						ArrayDeque temp = new ArrayDeque();
						Iterator i1 = now.iterator();
						//Iterator i2 = evaluation_queue.iterator();
						while(i1.hasNext())
						{
							Node parent = (Node)i1.next();
							Iterator i2 = evaluation_queue.iterator();
							while(i2.hasNext())
							{
								Node child = (Node)i2.next();
								if(child.equals(parent))
								{
									temp.add(parent);
								}
								if(child.getParentNode().equals(parent))
								{
									temp.add(parent);
								}
							}
						}
						evaluation_queue.clear();
						evaluation_queue.addAll(temp);
						if(!t.token_queue.isEmpty()){
							token2 = t.nextToken();
							extra = true;
						}
						else
							return true;
					}
				}
			
				extra2 = false;
				}
				if(extra2)
					t.pushBack(token2);
				if(eval_axis(token2))
				{
					if(!t.token_queue.isEmpty()){
						token3 = t.nextToken();
						counter[0]++;
						if(eval_step(token3))
						{
							counter[1]++;
						}
						else
						{
							t.pushBack(token3);
							return false;
						}
					}
					if(!(counter[0] == counter[1] && counter[0] <= 1))
					{
						return false;
					}
					extra = false;
				}
				if(extra)
				{
					t.pushBack(token2);
				}

				return true;
			}
			else
			{
				return true;
			}
		}
		else
		{
			return false;
		}

	}
	public boolean eval_test(String token) throws Exception
	{
		/*ArrayDeque org = new ArrayDeque();
		if(!changed){
		
		org.addAll(evaluation_queue);
		}*/
		if(eval_step(token))
		{
			//changed = true;
			//System.out.println("original is "+org);
			return true;
		}
		int size = evaluation_queue.size();
		@SuppressWarnings("rawtypes")

		int position = token.indexOf("\"");
		String temp = token.substring(0,position);
		temp = temp.replaceAll(" ", "");
		String temp2 = temp+token.substring(position);

		if(temp2.contains("text()="))
		{
			String[] res = temp2.split("=");
			int first = res[1].indexOf("\"");
			int last = res[1].lastIndexOf("\"");
			String test = res[1].substring(first+1,last);
			Iterator text_check = evaluation_queue.iterator();
			boolean text_flag = false;
			while(text_check.hasNext())
			{
				Node ele = (Node)text_check.next();
				if(ele.getTextContent().equals(test))
				{
					text_flag = true;
				}
				else
				{
					evaluation_queue.remove(ele);
				}
			}
			if(changed)
			{
			//	evaluation_queue.clear();
			//	evaluation_queue.add(org);
			//	changed = false;
			}
			return text_flag;
		}

		if(temp2.contains("contains(text(),"))
		{
			int first = temp2.indexOf("\"");
			int last = temp2.lastIndexOf("\"");
			String test = temp2.substring(first+1,last);
			Iterator text_check = evaluation_queue.iterator();
			boolean text_flag = false;
			while(text_check.hasNext())
			{
				Node ele = (Node)text_check.next();
				if(ele.getTextContent().contains(test))
				{
					text_flag = true;
				}
				else
				{
					evaluation_queue.remove(ele);
				}
			}
			if(changed)
			{
		//		evaluation_queue.clear();
		//		evaluation_queue.add(org);
		//		changed = false;
			}
			return text_flag;

		}
		if(temp2.contains("@") && temp2.contains("="))
		{
			String[] res = temp2.split("=");
			String attr = res[0].substring(1);
			System.out.println(attr);
			int first = res[1].indexOf("\"");
			int last = res[1].lastIndexOf("\"");
			String test = res[1].substring(first+1,last);
			Iterator text_check = evaluation_queue.iterator();
			boolean text_flag = false;
			while(text_check.hasNext())
			{
				Node ele = (Node)text_check.next();
				NamedNodeMap attr_map= ele.getAttributes();
				if(attr_map.getLength()!=0 && attr_map.getNamedItem(attr)!=null)
				{
					//System.out.println("blah"+attr_map.getNamedItem(attr).getNodeValue());
					if(attr_map.getNamedItem(attr).getNodeValue().equals(test)){
						text_flag =true;
					}
					else
					{
						evaluation_queue.remove(ele);
					}
				}
				else
				{
					evaluation_queue.remove(ele);
				}
			}
			if(changed)
			{
		//		evaluation_queue.clear();
		//		evaluation_queue.add(org);
		//		changed = false;
			}
			return text_flag;
		}


		return false;
	}
	public boolean eval_nodeName(String token) throws Exception
	{
		if(token.matches("[A-Za-z0-9._-]+")){
			if(evaluation_queue.isEmpty())
			{
				NodeList n_list;
				try
				{
					n_list= this.d.getChildNodes();
				}
				catch(Exception e)
				{
					return false;
				}
				for(int i=0;i<n_list.getLength();i++)
				{
					//if(n_list.item(i).)
					int a = n_list.getLength();
					//System.out.println(a);
					String nodeName = n_list.item(i).getNodeName();
					String nodeValue = n_list.item(i).getNodeValue();
					if(nodeName.equals(token) && nodeName!="#text")
					{
						evaluation_queue.add(n_list.item(i));
						//return true;
					}

				}
				if(evaluation_queue.isEmpty())
					return false;
				else
					return true;
			}
			else
			{
				boolean flag =false;
				Iterator qi = evaluation_queue.iterator();
				int size= evaluation_queue.size();
				while(size>0)
				{
					//Node queue_element = (Node)qi.next();
					Node queue_element = (Node)evaluation_queue.remove();
					NodeList ch;
					try{
						ch = queue_element.getChildNodes();
					}
					catch(Exception e1)
					{
						continue;
					}
					for(int j = 0 ; j < ch.getLength(); j++)
					{
						if(ch.item(j).getNodeName().equals(token))
						{
							evaluation_queue.add(ch.item(j));
							flag = true;
							//changed=true;
						}
					}
					size--;
				}
				return flag;
			}

		}
		else
		{
			return false;
		}
	}

	public static void main(String arg[]) throws Exception
	{
		XPathEngineImpl x = new XPathEngineImpl();
	//String xp="/bookstore/book/author";
		String xpp = "/html/body[@bgcolor=\"#FFFFFF\"]/h1[contains(text(),\"Descent\")]";
		String xlldf = "/html/body/p/i[text()=\"grammar\"]";
		//System.out.println(xpp);
		String harry = "/bookstore/book[author[text()=\"J K. Rowling\"]]/title";
		String p = "/bookstore[book/author[text()=\"J K. Rowling\"]]";
		
		//String potter = "/bookstore[book[@category=\"web\"]/title[text()=\"Learning XML\"]]/book/title";
		String xp1="/bookstore/book/year[contains(text(),\"20\")]";
		String xp3 = "/note/hello1/to[text()=\"text2\"][@vp=\"text1\"]";
		String xp7 = "/aaa/bbb[ccc[dddd[eeee[ffff]]]]";
		//String xp4 = "/a/b[foo[text()=\"#$(/][]\"]][bar]/hi[@asdf=\"#$(&[]\"][this][is][crazy]";
		//String xp4 = "/test[ a/b1[ c1[p]/d[p] ] /n1[a]/n2 [c2/d[p]/e[text()=\"/asp[&123(123*/]\"]]]";
		//String xp=	"/foo/bar[@att=\"123\"]";
		//	String xp= "/xyz[contains(text(),\"someSubstring\")]";
		//	String xp= "/a/b/c[text()=\"theEntireText\"]" ;
		//	String xp="/blah[anotherElement]";
		//String xp="/this/that[something/else]";
		//String xp="//dhdh";
		
		//String xp=	" /d/e/f[foo[text()=\"something\"]][bar]";
		//String xp= "/a/b/c[text() =     \"whiteSpacesShouldNotMatter\"]";
		String xp = "/test [ a/b1[ c1[ p]/d[p ] ] / n1[a]/n2 [c2/d[p]/e[contains( text(), \"[contains ( text() , \\\"/ a  sp&123( 123*/\\\"] \\\"  \\\")]\")]]]";
		String[] s = {""};
		//  s[0] = xp;
		//  s[1] = xp1;
		s[0] = harry;
		x.setXPaths(s);
		System.out.println("blah "+x.isValid(0));
		InputStream is = new FileInputStream("testing.xml");
		
		
		
		
		
		
		Tidy tidy = new Tidy();
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
		
		//tidy.parseDOM(is, null);
		
		
	  Element ex = doc.getDocumentElement();
	  System.out.println(ex.getNodeName());
	  NodeList n = ex.getChildNodes();
	  for(int i=0;i<n.getLength();i++)
	  {
		  Node _xx = n.item(i);
		  System.out.println(_xx.getNodeName() + " sdf " );
	  }
		//System.out.println(n);
		System.out.println("Parsed");
		 boolean arr[] = x.evaluate(doc);
	   System.out.println(Arrays.toString(arr));
	}

}
