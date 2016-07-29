package de.fhg.fokus.ims.core;


import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Utils
{	
	private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	
	private static LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue();
	private static boolean running = false;

	public static void invokeLater(Runnable r)
	{
		if (!running)
		{
			Runner runner =new Runner();
			runner.setDaemon(true);
			runner.setName("InvokeLaterThread");
			runner.start();
			running = true;
		}
		
		try
		{
			queue.put(r);
		} catch (InterruptedException e)
		{
			
		}
	}

	private static class Runner extends Thread
	{
		public void run()
		{
			while (true)
			{
				Runnable r = null;
				try
				{
					r = (Runnable) queue.take();
				} catch (InterruptedException e) {}

				if (r == null)
					continue;
				
				LOGGER.debug("running task: {"+r.getClass()+"}");
				
				try
				{
					r.run();
				} catch (Throwable t)
				{
					LOGGER.error(t.getMessage());
				}

				LOGGER.error("running task finished");
			}
		}
	}
}
