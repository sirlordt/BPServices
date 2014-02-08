package BPServicesManager;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ServiceLoader;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import AbstractResponseFormat.CAbstractResponseFormat;
import AbstractResponseFormat.CCSVResponseFormat;
import AbstractResponseFormat.CJSONResponseFormat;
import AbstractResponseFormat.CJavaXMLWebRowSetResponseFormat;
import AbstractResponseFormat.CXMLDataPacketResponseFormat;
import AbstractService.CAbstractService;
import AbstractServicesManager.CAbstractServicesManager;
import BPCommonClasses.CBPAbstractService;
import BPCommonClasses.CConfigServiceDBConnection;
import CommonClasses.CClassPathLoader;
import CommonClasses.CLanguage;
import CommonClasses.CConfigServicesDaemon;
import CommonClasses.CRegisterManagerTask;
import CommonClasses.CSecurityTokensManager;
import CommonClasses.ConstantsCommonConfigXMLTags;
import CommonClasses.ConstantsCommonClasses;
import CommonClasses.InitArgsConstants;
import DBDefinitions.CDBDefinitionsManager;
import ExtendedLogger.CExtendedLogger;

public class CServicesManager extends CAbstractServicesManager {

	private static final long serialVersionUID = -1535559412203731810L;

    public static final String strVersion = "0.0.0.1";

	public static CConfigServicesManager ConfigServicesManager = null;;
    
	public static HashMap<String,CAbstractService> RegisteredServices = null;

    protected CSecurityTokensManager SecurityTokensManager;
	
    //protected Timer RegisterManagerTimer;
    
    protected CRegisterManagerTask RegisterManagerTask;
    
    static {
    	
    	RegisteredServices = new HashMap<String,CAbstractService>();
    	
    }
	
	public CServicesManager() {

		super();
		
        SecurityTokensManager = CSecurityTokensManager.getSecurityTokensManager( ConstantsServicesManager._Security_Manager_Name );
		
        this.strContextPath = "/BPServices";
        
    	intInitPriority = 10;
        
        //RegisterManagerTimer = null;
        
        RegisterManagerTask = null;
        
	}
	
	@Override
	public void finalize() {
		
		this.endManager( ServicesDaemonConfig );
		
	}
	
    public boolean loadAndRegisterResponsesFormats( CConfigServicesDaemon ServicesDaemonConfig ) {
    	
		boolean bResult = false;

		try {
			
			ServiceLoader<CAbstractResponseFormat> sl = ServiceLoader.load( CAbstractResponseFormat.class );
			sl.reload();

			CAbstractResponseFormat.clearRegisteredResponseFormat();

			Iterator<CAbstractResponseFormat> it = sl.iterator();

			while ( it.hasNext() ) {

				try {
					
					CAbstractResponseFormat ResponseFormatInstance = it.next();

					if ( ResponseFormatInstance.initResponseFormat( ServicesDaemonConfig, ConfigServicesManager ) == true ) {
					   
						CAbstractResponseFormat.registerResponseFormat( ResponseFormatInstance );

						ConfigServicesManager.Logger.logMessage( "1", ConfigServicesManager.Lang.translate( "Registered response format: [%s] min version: [%s] max version: [%s]", ResponseFormatInstance.getName(), ResponseFormatInstance.getMinVersion(), ResponseFormatInstance.getMaxVersion() ) );        
					
					}
					
				} 
				catch ( Error Err ) {

					if ( ConfigServicesManager.Logger != null )
						ConfigServicesManager.Logger.logError( "-1020", Err.getMessage(), Err );
					
				}
				catch ( Exception Ex ) {

					if ( ConfigServicesManager.Logger != null )
						ConfigServicesManager.Logger.logException( "-1011", Ex.getMessage(), Ex );

				}

			}

		    //Add to the end of list the built response formats 
			ConfigServicesManager.Logger.logMessage( "1", ConfigServicesManager.Lang.translate( "Adding to end of list the built in responses formats" ) );        

			CXMLDataPacketResponseFormat XMLDataPacketResponseFormat = new CXMLDataPacketResponseFormat();
			
			if ( XMLDataPacketResponseFormat.initResponseFormat( ServicesDaemonConfig, ConfigServicesManager ) == true ) {
				   
				CAbstractResponseFormat.registerResponseFormat( XMLDataPacketResponseFormat );
				ConfigServicesManager.Logger.logMessage( "1", ConfigServicesManager.Lang.translate( "Added built in response format [%s] min version: [%s] max version: [%s]", XMLDataPacketResponseFormat.getName(), XMLDataPacketResponseFormat.getMinVersion(), XMLDataPacketResponseFormat.getMaxVersion() ) );        
			
			}

			CJavaXMLWebRowSetResponseFormat JavaXMLWebRowSetResponseFormat = new CJavaXMLWebRowSetResponseFormat();
			
			if ( JavaXMLWebRowSetResponseFormat.initResponseFormat( ServicesDaemonConfig, ConfigServicesManager ) == true ) {
				   
				CAbstractResponseFormat.registerResponseFormat( JavaXMLWebRowSetResponseFormat );
				ConfigServicesManager.Logger.logMessage( "1", ConfigServicesManager.Lang.translate( "Added built in response format [%s] min version: [%s] max version: [%s]", JavaXMLWebRowSetResponseFormat.getName(), JavaXMLWebRowSetResponseFormat.getMinVersion(), JavaXMLWebRowSetResponseFormat.getMaxVersion() ) );        
			
			}
			
			CJSONResponseFormat JSONResponseFormat = new CJSONResponseFormat(); //JSON
			
			if ( JSONResponseFormat.initResponseFormat( ServicesDaemonConfig, ConfigServicesManager ) == true ) {
				   
				CAbstractResponseFormat.registerResponseFormat( JSONResponseFormat );
				ConfigServicesManager.Logger.logMessage( "1", ConfigServicesManager.Lang.translate( "Added built in response format [%s] min version: [%s] max version: [%s]", JSONResponseFormat.getName(), JSONResponseFormat.getMinVersion(), JSONResponseFormat.getMaxVersion() ) );        
			
			}
			
			CCSVResponseFormat CSVResponseFormat = new CCSVResponseFormat(); //CSV
			
			if ( CSVResponseFormat.initResponseFormat( ServicesDaemonConfig, ConfigServicesManager ) == true ) {
				   
				CAbstractResponseFormat.registerResponseFormat( CSVResponseFormat );
				ConfigServicesManager.Logger.logMessage( "1", ConfigServicesManager.Lang.translate( "Added built in response format [%s] min version: [%s] max version: [%s]", CSVResponseFormat.getName(), CSVResponseFormat.getMinVersion(), CSVResponseFormat.getMaxVersion() ) );        
			 
			}
			
			CAbstractResponseFormat DefaultResponseFormat = CAbstractResponseFormat.getResponseFomat( ConfigServicesManager.strDefaultResponseFormat, ConfigServicesManager.strDefaultResponseFormatVersion );

			if ( DefaultResponseFormat == null ) {
				
				if ( CAbstractResponseFormat.getReponseFormatSearchCodeResult() == -2 ) { //Response format name not found 
				
					ConfigServicesManager.Logger.logWarning( "1", ConfigServicesManager.Lang.translate( "The default response format [%s] version [%s] not found", ConfigServicesManager.strDefaultResponseFormat, ConfigServicesManager.strDefaultResponseFormatVersion ) );
					
					ConfigServicesManager.strDefaultResponseFormat = ConstantsServicesManager._Response_Format;
					ConfigServicesManager.strDefaultResponseFormatVersion = ConstantsServicesManager._Response_Format_Version;
				
				}
				else { //Response format name found but the version not match, use the response format with the min version available
					
					ConfigServicesManager.Logger.logWarning( "1", ConfigServicesManager.Lang.translate( "The default response format [%s] found, but the version [%s] not found, using the min version [%s] available", ConfigServicesManager.strDefaultResponseFormat, ConfigServicesManager.strDefaultResponseFormatVersion, CAbstractResponseFormat.getReponseFormatVersionSearchResult() ) );
					
					ConfigServicesManager.strDefaultResponseFormatVersion = CAbstractResponseFormat.getReponseFormatVersionSearchResult();
					
				}
				
			}
			
			ConfigServicesManager.Logger.logMessage( "1", ConfigServicesManager.Lang.translate( "Using default response format: [%s] min version: [%s]", ConfigServicesManager.strDefaultResponseFormat, ConfigServicesManager.strDefaultResponseFormatVersion ) );

			ConfigServicesManager.Logger.logMessage( "1", ConfigServicesManager.Lang.translate( "Count of responses formats registered: [%s]", Integer.toString( CAbstractResponseFormat.getCountRegisteredResponsesFormats() ) ) );        

			bResult = CAbstractResponseFormat.getCountRegisteredResponsesFormats() > 0;
			
		} 
		catch ( Exception Ex ) {

			if ( ConfigServicesManager.Logger != null )
				ConfigServicesManager.Logger.logException( "-1012", Ex.getMessage(), Ex );

		}

		return bResult;
    	
    }
    
    public boolean databaseInitiation( CConfigServiceDBConnection DBConnectionConfig, CExtendedLogger Logger, CLanguage Lang ) {
    	
		long lngStart = System.currentTimeMillis();
    	
    	boolean bResult = false;
    	
    	try {
    	
    		CDBDefinitionsManager DBDefinitionsManager = new CDBDefinitionsManager( strRunningPath, ConfigServicesManager.ConfiguredRegisterServices, ConfigServicesManager.intRequestTimeout, ConfigServicesManager.intSocketTimeout );
    		
    		DBDefinitionsManager.loadDBDefinitionsFromFile( DBConnectionConfig.strDBDefinitionsFilePath, Logger, Lang );

    		if ( DBDefinitionsManager.initiationObjectsDatabase( DBConnectionConfig, Logger, Lang ) ) {
    			
    			Logger.logMessage( "1", Lang.translate( "Successful initiation of database [%s] objects", DBConnectionConfig.strDatabase ) );
    			
    			bResult = true;
    			
    		}
    		else {
    			
    			Logger.logError( "-1001", Lang.translate( "Fail initiation of database [%s] objects", DBConnectionConfig.strDatabase ) );
    			
    		}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1026", Ex.getMessage(), Ex );
    		
    	}
    	
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "databaseInitiation", Long.toString( lngEnd - lngStart ) ) );
    	
    	return bResult;
    	
    }
    
    public boolean initiationOfAllDatabases( CExtendedLogger Logger, CLanguage Lang ) {
    	
		long lngStart = System.currentTimeMillis();
    	
    	boolean bResult = false;
    	
    	try {
    		
    		for ( int I = 0; I < ConfigServicesManager.ConfiguredDBConnections.size(); I ++ ) {
    			
    		 	CConfigServiceDBConnection DBConnectionConfig = ConfigServicesManager.ConfiguredDBConnections.get( I );
    			
    		 	if ( DBConnectionConfig.strDBDefinitionsFilePath != null && DBConnectionConfig.strDBDefinitionsFilePath.isEmpty() == false )
    		 		this.databaseInitiation( DBConnectionConfig, Logger, Lang );
    		 	
    		}
    		
    		bResult = true;
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1025", Ex.getMessage(), Ex );
    		
    	}
    	
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "initiationOfAllDatabases", Long.toString( lngEnd - lngStart ) ) );
    	
    	return bResult;
    	
    }
    
    public boolean loadAndRegisterBPServices( CConfigServicesDaemon ServicesDaemonConfig ) {
    	
    	boolean bResult = false;

    	try {

			ServiceLoader<CBPAbstractService> sl = ServiceLoader.load( CBPAbstractService.class );
			sl.reload();

			RegisteredServices.clear();

			Iterator<CBPAbstractService> it = sl.iterator();

			while ( it.hasNext() ) {

				try {
					
					CBPAbstractService ServiceInstance = it.next();

					if ( ServiceInstance.initializeService( ServicesDaemonConfig, ConfigServicesManager ) == true ) {

						RegisteredServices.put( ServiceInstance.getServiceName().toLowerCase(), ServiceInstance );

						ConfigServicesManager.Logger.logMessage( "1", ConfigServicesManager.Lang.translate( "Registered service: [%s] description: [%s] version: [%s]", ServiceInstance.getServiceName().toLowerCase(), ServiceInstance.getServiceDescription(), ServiceInstance.getServiceVersion() ) );        

					}
					else {

						ServiceInstance = null;							

					}
					
				} 
				catch ( Error Err ) {

					if ( ConfigServicesManager.Logger != null )
						ConfigServicesManager.Logger.logError( "-1020", Err.getMessage(), Err );
					
				}
				catch ( Exception Ex ) {

					if ( ConfigServicesManager.Logger != null )
						ConfigServicesManager.Logger.logException( "-1011", Ex.getMessage(), Ex );

				}

			}
    		
			ConfigServicesManager.Logger.logMessage( "1", ConfigServicesManager.Lang.translate( "Count of services registered: [%s]", Integer.toString( RegisteredServices.size() ) ) );        

			bResult = true; //RegisteredBPServices.size() > 0;

    	} 
		catch ( Exception Ex ) {
	
			if ( ConfigServicesManager.Logger != null )
				ConfigServicesManager.Logger.logException( "-1010", Ex.getMessage(), Ex );
	
		}
    	
    	return bResult;
    	
    }
    
    @Override
	public boolean initManager( CConfigServicesDaemon ServicesDaemonConfig ) {
    	
    	super.initManager( ServicesDaemonConfig );

    	this.strRunningPath = net.maindataservices.Utilities.getJarFolder( this.getClass() );
    	
        CExtendedLogger BPServicesManagerLogger = CExtendedLogger.getLogger( ConstantsServicesManager._Logger_Name );
        BPServicesManagerLogger.setupLogger( ServicesDaemonConfig.strInstanceID, ServicesDaemonConfig.InitArgs.contains( InitArgsConstants._LogToScreen ), this.strRunningPath + ConstantsCommonClasses._Logs_Dir, ConstantsServicesManager._Main_File_Log, ServicesDaemonConfig.strClassNameMethodName, ServicesDaemonConfig.bExactMatch, ServicesDaemonConfig.LoggingLevel.toString(), ServicesDaemonConfig.strLogIP, ServicesDaemonConfig.intLogPort, ServicesDaemonConfig.strHTTPLogURL, ServicesDaemonConfig.strHTTPLogUser, ServicesDaemonConfig.strHTTPLogPassword, ServicesDaemonConfig.strProxyIP, ServicesDaemonConfig.intProxyPort, ServicesDaemonConfig.strProxyUser, ServicesDaemonConfig.strProxyPassword );
		
		CLanguage BPServicesManagerLang = CLanguage.getLanguage( BPServicesManagerLogger, this.strRunningPath + ConstantsCommonClasses._Langs_Dir + ConstantsServicesManager._Main_File + "." + ConstantsCommonClasses._Lang_Ext );

		BPServicesManagerLogger.logMessage( "1", BPServicesManagerLang.translate( "Running dir: [%s]", this.strRunningPath ) );        
		BPServicesManagerLogger.logMessage( "1", BPServicesManagerLang.translate( "Version: [%s]", strVersion ) );        
    	
    	ConfigServicesManager = CConfigServicesManager.getConfigBPServicesManager( this.strRunningPath );
		
		boolean bResult = false;

    	try {

    		CClassPathLoader ClassPathLoader = new CClassPathLoader();

    		//Load important library class from /Libs folder
			ClassPathLoader.LoadClassFiles( this.strRunningPath + ConstantsCommonClasses._Libs_Dir, ConstantsCommonClasses._Lib_Ext, 2, BPServicesManagerLogger, BPServicesManagerLang  );

    		if ( ConfigServicesManager.loadConfig( this.strRunningPath + ConstantsServicesManager._Conf_File, BPServicesManagerLogger, BPServicesManagerLang ) == true ) {

    			try {

    				//Load responses formats class
    				ClassPathLoader.LoadClassFiles( ConfigServicesManager.strResponsesFormatsDir, ConstantsCommonClasses._Lib_Ext, 2, BPServicesManagerLogger, BPServicesManagerLang  );

    				if ( this.loadAndRegisterResponsesFormats( ServicesDaemonConfig ) == true ) {

    					//Load the business process services class
    					ClassPathLoader.LoadClassFiles( ConfigServicesManager.strServicesDir, ConstantsCommonClasses._Lib_Ext, 2, BPServicesManagerLogger, BPServicesManagerLang  );

    					if ( this.loadAndRegisterBPServices( ServicesDaemonConfig ) == true ) {

							net.maindataservices.Utilities.cleanupDirectory( new File( ConfigServicesManager.strTempDir ), new String[]{ ".txt" }, 0, BPServicesManagerLogger );
    						
    						bResult = true;

    					}	
    					else {

    						BPServicesManagerLogger.logError( "-1002", BPServicesManagerLang.translate( "No business process services found in path [%s]", ConfigServicesManager.strServicesDir ) );

    					}

    				}
    				else {

    					BPServicesManagerLogger.logError( "-1001", BPServicesManagerLang.translate( "No responses formats drivers found in path [%s]", ConfigServicesManager.strResponsesFormatsDir ) );

    				}

    			}
    			catch ( Exception Ex ) {

    				BPServicesManagerLogger.logException( "-1010", Ex.getMessage(), Ex );

    			}

    		}

    	}
    	catch ( Exception Ex ) {

    		BPServicesManagerLogger.logException( "-1011", Ex.getMessage(), Ex );

    	}
    	
    	return bResult;
    	
    }
    
    @Override
    public boolean postInitManager( CConfigServicesDaemon ServicesDaemonConfig, LinkedHashMap<String,Object> DataInfo ) {
    	
		this.initiationOfAllDatabases( ConfigServicesManager.Logger, ConfigServicesManager.Lang ); //Init or Check DB Struct
    	
		DataInfo.put( "RegisteredBPServices", RegisteredServices );
		
    	//Do call PostInitializeService of registered services
    	for ( Entry<String,CAbstractService> Entry : RegisteredServices.entrySet() ) {
    		
    		try {
    		
    			if ( Entry.getValue() != null ) {

    				ConfigServicesManager.Logger.logInfo( "1", ConfigServicesManager.Lang.translate( "Post initialize of service: [%s]", Entry.getKey() ) );
    				Entry.getValue().postInitializeService( ServicesDaemonConfig, ConfigServicesManager, DataInfo );
    			
    			}    
    		
    		}
			catch ( Error Err ) {

				if (  ConfigServicesManager.Logger != null )
					ConfigServicesManager.Logger.logError( "-1020", Err.getMessage(), Err );
				
			}
    		catch ( Exception Ex ) {
    			
				if ( ConfigServicesManager.Logger != null )
					ConfigServicesManager.Logger.logException( "-1021", Ex.getMessage(), Ex );
    			
    		}
    		
    	}
    	
    	if ( ConfigServicesManager.ConfiguredRegisterServices.size() > 0 ) {
    		
			RegisterManagerTask = new CRegisterManagerTask( "RegisterManagerTask - " + this.strContextPath, ConfigServicesManager.Logger, ConfigServicesManager.Lang, ConfigServicesManager.ConfiguredRegisterServices, ServicesDaemonConfig.ConfiguredNetworkInterfaces, this.strContextPath, ConfigServicesManager.strTempDir, ConstantsCommonClasses._Register_Manager_Frecuency, ConfigServicesManager.intRequestTimeout, ConfigServicesManager.intSocketTimeout );
			
			RegisterManagerTask.start();
			
    	}
    	
    	return true;
    	
    }
    
    @Override
    public boolean endManager( CConfigServicesDaemon ServicesDaemonConfig ) {
    	
    	if ( RegisteredServices != null ) {
    	
    		for ( Entry<String,CAbstractService> Entry : RegisteredServices.entrySet() ) {

    			try {

    				if ( Entry.getValue() != null ) {

    					ConfigServicesManager.Logger.logInfo( "1", ConfigServicesManager.Lang.translate( "Finalize of service: [%s]", Entry.getKey() ) );
    					Entry.getValue().finalizeService( ServicesDaemonConfig, ConfigServicesManager );

    				}    

    			}
				catch ( Error Err ) {

					if ( ConfigServicesManager.Logger != null )
						ConfigServicesManager.Logger.logError( "-1020", Err.getMessage(), Err );
					
				}
    			catch ( Exception Ex ) {

    				if ( ConfigServicesManager.Logger != null )
    					ConfigServicesManager.Logger.logException( "-1021", Ex.getMessage(), Ex );

    			}

    		}

    		RegisteredServices.clear();
    	
    	}
    	
    	if ( RegisterManagerTask != null ) {

    		try {

    			RegisterManagerTask.setStopNow();

    			RegisterManagerTask.join();

    		} 
    		catch ( Exception Ex ) {

    			if ( ConfigServicesManager.Logger != null )
    				ConfigServicesManager.Logger.logException( "-1022", Ex.getMessage(), Ex );

    		}

    		RegisterManagerTask.unregisterManager();

    	}
    	
    	if ( ConfigServicesManager.ConfiguredRegisterServices != null ) {

    		ConfigServicesManager.ConfiguredRegisterServices.clear();
    	
    	}
    	
    	return true;
    	
    }
    
    protected CAbstractResponseFormat getDefaultResponseFormat() {
    	
    	return CAbstractResponseFormat.getResponseFomat( ConfigServicesManager.strDefaultResponseFormat, ConfigServicesManager.strDefaultResponseFormatVersion );
    	
    }
    
    protected CAbstractResponseFormat getResponseFormat( String strResponseFormat, String strResponseFormatVersion ) {
    	
    	CAbstractResponseFormat ResponseFormat = null;
    	
        try {
    	
        	if (  strResponseFormat == null || strResponseFormat.isEmpty() ) {

        		ResponseFormat = this.getDefaultResponseFormat();    	

        	}   
        	else {

        		if ( strResponseFormatVersion == null || strResponseFormatVersion.isEmpty() )
        			strResponseFormatVersion = ConstantsCommonClasses._Version_Any;

        		ResponseFormat = CAbstractResponseFormat.getResponseFomat( strResponseFormat, strResponseFormatVersion );    	

        		if ( ResponseFormat == null ) {

        			ResponseFormat = this.getDefaultResponseFormat();    	

        		}

        	}
    	
        }
    	catch ( Exception Ex ) {
    		
    		ConfigServicesManager.Logger.logException( "-1010", Ex.getMessage(), Ex );

    	}
    	
    	return ResponseFormat;
    	
    }
    
    @Override
    protected void processRequest( HttpServletRequest Request, HttpServletResponse Response ) {

 	   try {
 	    	
		   /*response.setContentType("text/html");
		   response.setStatus(HttpServletResponse.SC_OK);
		   response.getWriter().println("<h1>" + DBServicesManagerConfig.DBServicesManagerLang.translate( "DB Services Manager" ) + "</h1>" );
	       response.getWriter().println("<h2>" + DBServicesManagerConfig.DBServicesManagerLang.translate( "Session" ) + "=" + request.getSession(true).getId() + "<h2>");
	       response.getWriter().println("<h3>IP=" + request.getRemoteAddr() + "<h3>" );*/
	   
 	       Response.setStatus( HttpServletResponse.SC_OK );

 	       String strServiceName = Request.getParameter( ConstantsCommonClasses._Request_ServiceName );

 	       //HttpSession ServiceSession = Request.getSession( true );

		   String strRequestSecurityTokenID  = ( Request.getParameter( ConstantsCommonClasses._Request_SecurityTokenID ) );

 	       //String strRequestTransactionID = ( String ) Request.getParameter( ConstantsCommonClasses._RequestTransactionID );

 	       String strRequestResponseFormat = Request.getParameter( ConstantsCommonClasses._Request_ResponseFormat );
 	       
 	       String strRequestResponseFormatVersion = Request.getParameter( ConstantsCommonClasses._Request_ResponseFormatVersion );

           CAbstractResponseFormat ResponseFormat = this.getResponseFormat( strRequestResponseFormat, strRequestResponseFormatVersion );
 	       
           if ( ResponseFormat != null ) {

        	   if ( strRequestResponseFormatVersion == null || strRequestResponseFormatVersion.isEmpty() ) {

        		   strRequestResponseFormatVersion = ConfigServicesManager.strDefaultResponseFormatVersion; //ResponseFormat.getMinVersion();

        	   }

        	   if ( strServiceName != null && strServiceName.isEmpty() == false ) {
        	   
        		   CAbstractService Service = RegisteredServices.get( strServiceName.toLowerCase() );

        		   if ( Service != null ) {

        			   if ( Service.getAuthRequired() == false ) { //Auth not required

        				   if ( strRequestSecurityTokenID == null )
        					   strRequestSecurityTokenID = "";
        				   
        				   Service.executeService( 1, Request, Response,  strRequestSecurityTokenID, RegisteredServices, ResponseFormat, strRequestResponseFormatVersion );

        			   }
        			  else if ( strRequestSecurityTokenID != null && strRequestSecurityTokenID.equals( "" ) == false ) {

						   //@SuppressWarnings("unchecked")
						   //ArrayList<String> strSessionSecurityTokens = ( ArrayList<String> ) ServiceSession.getAttribute( ConstantsCommonClasses._SessionSecurityTokens );

        				   if ( SecurityTokensManager.checkSecurityTokenID( strRequestSecurityTokenID, ConfigServicesManager.Logger, ConfigServicesManager.Lang ) == true ) {

        					   int intResultCode = Service.executeService( 1, Request, Response,  strRequestSecurityTokenID, RegisteredServices, ResponseFormat, strRequestResponseFormatVersion );
        					   
        					   if ( intResultCode < 0 ) {
        						   
        		        		   ConfigServicesManager.Logger.logWarning( "-1", ConfigServicesManager.Lang.translate( "The service name [%s] return negative value [%s]", strServiceName, Integer.toString( intResultCode ) ) );
        						   
        					   }

        				   }
        				   else {

        					   try {

        						   Response.setContentType( ResponseFormat.getContentType() );
        						   Response.setCharacterEncoding( ResponseFormat.getCharacterEncoding() );

        						   String strResponseBuffer = ResponseFormat.formatSimpleMessage( "", "", -3, ConfigServicesManager.Lang.translate( "The security token [%s] is incorrect. Possible attempted robbery in progress session", strRequestSecurityTokenID ), true, strRequestResponseFormatVersion, ConfigServicesManager.strGlobalDateTimeFormat, ConfigServicesManager.strGlobalDateFormat, ConfigServicesManager.strGlobalTimeFormat, ConfigServicesManager.Logger, ConfigServicesManager.Lang );
        						   Response.getWriter().print( strResponseBuffer );

        					   }
        					   catch ( Exception Ex ) {

        						   ConfigServicesManager.Logger.logException( "-1014", Ex.getMessage(), Ex );

        					   }

        				   }

        			   }
        			   else {

        				   try {

        					   Response.setContentType( ResponseFormat.getContentType() );
        					   Response.setCharacterEncoding( ResponseFormat.getCharacterEncoding() );

        					   String strResponseBuffer = ResponseFormat.formatSimpleMessage( "", "", -2, ConfigServicesManager.Lang.translate( "You must log in before using the service [%s]", strServiceName ), true, strRequestResponseFormatVersion, ConfigServicesManager.strGlobalDateTimeFormat, ConfigServicesManager.strGlobalDateFormat, ConfigServicesManager.strGlobalTimeFormat, ConfigServicesManager.Logger, ConfigServicesManager.Lang );
        					   Response.getWriter().print( strResponseBuffer );

        				   }
        				   catch ( Exception Ex ) {

        					   ConfigServicesManager.Logger.logException( "-1013", Ex.getMessage(), Ex );

        				   }

        			   }

        		   }
        		   else {

        			   try {

        				   Response.setContentType( ResponseFormat.getContentType() );
        				   Response.setCharacterEncoding( ResponseFormat.getCharacterEncoding() );

        				   String strResponseBuffer = ResponseFormat.formatSimpleMessage( "", "", -1, ConfigServicesManager.Lang.translate( "The service name [%s] not found", strServiceName ), true, strRequestResponseFormatVersion, ConfigServicesManager.strGlobalDateTimeFormat, ConfigServicesManager.strGlobalDateFormat, ConfigServicesManager.strGlobalTimeFormat, ConfigServicesManager.Logger, ConfigServicesManager.Lang );
        				   Response.getWriter().print( strResponseBuffer );

        			   }
        			   catch ( Exception Ex ) {

        				   ConfigServicesManager.Logger.logException( "-1012", Ex.getMessage(), Ex );

        			   }

        		   }

        	   }
        	   else {

        		   String strRequestIP = Request.getLocalAddr();
        		   String strForwardedIP = Request.getHeader( "X-Forwarded-For" );
        		   
        		   if ( strForwardedIP == null ) {
        			   
        			   strForwardedIP = "";
        			   
        		   }
        		   
        		   ConfigServicesManager.Logger.logWarning( "-1", ConfigServicesManager.Lang.translate( "The service name is empty, request from ip address [%s], forwarded from [%s]", strRequestIP, strForwardedIP ) );
        		   
        	   }
           
           }
           else  {

        	   String strMessage = ConfigServicesManager.Lang.translate( "Fatal error, no response format found" );
        	   
        	   Response.getWriter().print( strMessage );
        	   ConfigServicesManager.Logger.logError( "-1011", strMessage );
        	   
           }
           
	   }
	   catch ( Exception Ex ) {
  
		   ConfigServicesManager.Logger.logException( "-1010", Ex.getMessage(), Ex );
		   
	   }
 	   
    }
    
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) {
    	
    	if ( ConfigServicesManager.strResponseRequestMethod.equals( ConstantsCommonConfigXMLTags._Request_Method_ANY ) || ConfigServicesManager.strResponseRequestMethod.equals( ConstantsCommonConfigXMLTags._Request_Method_OnlyGET ) )
    		this.processRequest( request, response );
    	else
   	        response.setStatus( HttpServletResponse.SC_OK );

    }
    
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) {
    	
    	if ( ConfigServicesManager.strResponseRequestMethod.equals( ConstantsCommonConfigXMLTags._Request_Method_ANY ) || ConfigServicesManager.strResponseRequestMethod.equals( ConstantsCommonConfigXMLTags._Request_Method_OnlyPOST ) )
    		this.processRequest( request, response );
    	else
   	        response.setStatus( HttpServletResponse.SC_OK );
        
    }
    
}
