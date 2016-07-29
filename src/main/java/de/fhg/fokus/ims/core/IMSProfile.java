package de.fhg.fokus.ims.core;

import de.fhg.fokus.ims.UserProfile;

public class IMSProfile
{
	
	//private UserProfile userProfile;
	/*
	 * 
	 * #NUBOMEDIA IMS Connector profile
#Thu Apr 14 10:53:19 CEST 2016
callOfferMedia=audio
gstAudioCodecs=PCMU,PCMA,MPA
imsPrack=false
xdmsPassword=
imsSubscribeToReg=false
proxySystemProxy=false
imsRegister=true
presenceMood=false
callAccept=ask
sessionExpireTimer=3600
presenceOmaV1=false
soundOutgoingCall=
conferenceSubscribe=always
imsDomain=open-ims.test
xdmsAddress=open-ims.test
imsLocalInterface=10.147.66.14
conferenceAccept=ask
imsLocalPort=5060
imsSecretKey=bob
authIsim=false
imsPCSCFPort=4060
imsDisplayName=Bob
presenceSimple=true
imsRegisterTime=3600
imsEnableLog=true
id=3ac2d709-c4b5-4625-bd97-d5bdd08cedfb
imsPrivateId=bob@open-ims.test
imsPCSCFAddress=192.168.149.68
presencePublish=false
presenceSubcribeAll=false
imsPublicId=sip\:bob@open-ims.test
xdmsUser=
xdmsXcapRoot=/xdms/xcap
imsPCSCFDiscoverFunction=Fix IP
proxyUserProxy=false
gstAudioInputDevice=dshowaudiosrc
proxyNoProxy=true
presenceFixedIDs=true
xdmsEnable=false
xdmsPort=8080*/

	private UserProfile userProfile;
	
	public IMSProfile(UserProfile properties)
	{
		this.userProfile = properties;
	}

	public String getID()
	{
		return userProfile.getProperty("id");
	}
	
	public String getDomain()
	{		
		return  userProfile.getProperty("imsDomain");
	}

	public String getDisplayName()
	{
		return  userProfile.getProperty("imsDisplayName");
	}

	public String getPublicId()
	{
		return  userProfile.getProperty("imsPublicId");
	}

	public String getPrivateId()
	{
		return  userProfile.getProperty("imsPrivateId");
	}

	public String getSecretKey()
	{
		return  userProfile.getProperty("imsSecretKey");
	}

	public String getPCSCFAddress()
	{
		return  userProfile.getProperty("imsPCSCFAddress");
	}

	public String getPCSCFPort()
	{
		return  userProfile.getProperty("imsPCSCFPort");
	}

	public String getLocalInterface()
	{
		return  userProfile.getProperty("imsLocalInterface");
	}

	public String getLocalPort()
	{
		return  userProfile.getProperty("imsLocalPort");
	}

	public boolean getRegister()
	{
		return Boolean.valueOf(userProfile.getProperty("imsRegister"));
	}

	public String getRegisterTime()
	{
		return  userProfile.getProperty("imsRegisterTime");
	}

	public boolean getEnableLog()
	{
		return Boolean.valueOf(userProfile.getProperty("imsEnableLog"));
	}

	public boolean isSessionTimerEnabled()
	{
		return Boolean.valueOf(userProfile.getProperty("enableSessionTimer"));
	}
	
	public boolean isPrackSupportEnabled()
	{
		return Boolean.valueOf(userProfile.getProperty("imsPrack"));
	}
	

	public String getSessionExpireTimer()
	{
		return  userProfile.getProperty("sessionExpireTimer");
	}

	public String getPCSCFDiscovery()
	{
		return  userProfile.getProperty("imsPCSCFDiscoverFunction");
	}

	public boolean getSubscribeToReg()
	{
		return Boolean.valueOf(userProfile.getProperty("imsSubscribeToReg"));
	}
}