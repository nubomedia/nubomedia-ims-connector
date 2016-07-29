package de.fhg.fokus.ims;

import java.util.Enumeration;
import java.util.Properties;


public class UserProfile{

	private Properties propertyDepot = new Properties();
		
	public UserProfile(Properties properties)
	{
		Enumeration en = properties.keys();
		while (en.hasMoreElements())
		{
			String key = (String) en.nextElement();
			propertyDepot.put(key, properties.get(key));
		}
	}	
	

	public String getProperty(String key){
		return getProperty(key, null);
	}
			

	public String getProperty(String key, String defaultValue)
	{
		return propertyDepot.getProperty(key, defaultValue);
	}
	

	public void setProperty(String key, String value, boolean trransient)
	{
			propertyDepot.setProperty(key, value);
	}
	
	public void setProperty(String key, String value)
	{
		setProperty(key, value, false);
	}
	
	public void removeProperty(String name)
	{
		propertyDepot.remove(name);
	}
	

	public String getId()
	{
		return propertyDepot.getProperty("id");
	}

}
