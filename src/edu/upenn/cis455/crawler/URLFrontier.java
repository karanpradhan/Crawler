package edu.upenn.cis455.crawler;

import java.util.LinkedList;
import java.util.Queue;

public class URLFrontier {
	Queue<String> queue;
	
	URLFrontier()
	{
		queue= new LinkedList<String>();
	}
	void push(String url)
	{
		queue.add(url);
	}
	String pop()
	{
		return queue.poll();
	}
	boolean checkFrontier(String url)
	{
		return queue.contains(url);
	}
	boolean isEmpty()
	{
		return queue.isEmpty();
	}
}
