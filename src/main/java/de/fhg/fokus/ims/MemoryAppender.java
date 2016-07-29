package de.fhg.fokus.ims;

import java.util.LinkedList;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class MemoryAppender extends AppenderSkeleton
{
	LinkedList logs = new LinkedList();

	protected void append(LoggingEvent arg0)
	{
		String s = this.getLayout().format(arg0);
		synchronized (logs)
		{
			if (logs.size() < 1000)
			{
				logs.add(s);
			} else
			{
				logs.removeFirst();
				logs.addLast(s);
			}
		}
	}

	public void close()
	{
		logs.clear();
	}

	public boolean requiresLayout()
	{
		return false;
	}

	public String[] getLogs()
	{
		synchronized (logs)
		{
			return (String[]) logs.toArray(new String[logs.size()]);
		}
	}
}
