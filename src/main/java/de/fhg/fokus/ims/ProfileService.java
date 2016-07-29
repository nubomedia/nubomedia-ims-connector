package de.fhg.fokus.ims;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.ims.core.Utils;

public class ProfileService {
	private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	private UserProfile profile;
	
	
	
	public ProfileService() {
		loadProfiles();		
	}
	
	public UserProfile getProfile(){
		return profile;
	}
	
	private void loadProfiles()
	{
		String defaultFile = "profile.cfg";
		String temp = System.getProperty("profile.file", null);
		if (temp != null)
			defaultFile = temp;

		// Be compatible - support for profile.cfg (or profile.file parameter)
		Properties appProperties = new Properties();		
		boolean bOK = loadClientProfile(new File(System.getProperty("application.path", "."), defaultFile), appProperties);
		if (bOK){			
			profile = new UserProfile(appProperties);			
		}		
	}
	
	private boolean loadClientProfile(File file, Properties properties){
		if (!file.exists())
			return false;

		FileInputStream inputStream;
		try{
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e){
			LOGGER.error(e.getMessage(), e);
			return false;
		}

		try{
			properties.load(inputStream);
		} catch (IOException e){
			LOGGER.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

}
