///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2016. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;

class OmmIProviderActiveConfig extends ActiveServerConfig
{

	static final int DEFAULT_SERVICE_STATE								=	1;
	static final int DEFAULT_ACCEPTING_REQUESTS							=	1;
	static final boolean DEFAULT_IS_STATUS_CONFIGURED					=	false;
	static final int DEFAULT_SERVICE_ID									=	1;
	static final int DEFAULT_SERVICE_IS_SOURCE							=	1;
	static final int DEFAULT_SERVICE_SUPPORTS_QOS_RANGE					=	1;
	static final int DEFAULT_SERVICE_SUPPORTS_OUT_OF_BAND_SNAPSHATS		=  	1;
    static final int DEFAULT_SERVICE_ACCEPTING_CONSUMER_SERVICE			=	1;
	static final boolean DEFAULT_REFRESH_FIRST_REQUIRED					=	true;
	static final int DEFAULT_DIRECTORY_ADMIN_CONTROL					=	OmmIProviderConfig.AdminControl.API_CONTROL;
	static final int DEFAULT_DICTIONARY_ADMIN_CONTROL					=	OmmIProviderConfig.AdminControl.API_CONTROL;
	static final boolean DEFAULT_RECOVER_USER_SUBMIT_SOURCEDIRECTORY	=	true;
	static final String DEFAULT_IPROVIDER_SERVICE_NAME 					= 	"14002";
	static final String DEFAULT_SERVICE_NAME							=   "DIRECT_FEED";
	static final int DEFAULT_FIELD_DICT_FRAGMENT_SIZE         = 8192;
	static final int DEFAULT_ENUM_TYPE_FRAGMENT_SIZE        = 128000;
	    
	
	int 						directoryAdminControl;
	int                         dictionaryAdminControl;
	boolean						refreshFirstRequired;
	int                        maxFieldDictFragmentSize = DEFAULT_FIELD_DICT_FRAGMENT_SIZE;
	int                        maxEnumTypeFragmentSize = DEFAULT_ENUM_TYPE_FRAGMENT_SIZE;

	OmmIProviderActiveConfig()
	{
		super(DEFAULT_IPROVIDER_SERVICE_NAME);
		operationModel = DEFAULT_USER_DISPATCH;
		directoryAdminControl = DEFAULT_DIRECTORY_ADMIN_CONTROL;
		dictionaryAdminControl = DEFAULT_DICTIONARY_ADMIN_CONTROL;
		refreshFirstRequired = DEFAULT_REFRESH_FIRST_REQUIRED;
	}

	@Override
	int dictionaryAdminControl()
	{
		return dictionaryAdminControl;
	}

	@Override
	int directoryAdminControl()
	{
		return directoryAdminControl;
	}
}

