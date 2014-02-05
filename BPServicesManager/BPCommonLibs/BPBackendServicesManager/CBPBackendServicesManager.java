package BPBackendServicesManager;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.UUID;

import BPCommonClasses.CConfigServiceDBConnection;
import CommonClasses.CBackendServicesManager;
import CommonClasses.CConfigProxy;
import CommonClasses.CLanguage;
import CommonClasses.ConstantsCommonConfigXMLTags;
import CommonClasses.ConstantsCommonClasses;
import ExtendedLogger.CExtendedLogger;

public class CBPBackendServicesManager extends CBackendServicesManager {
	
	protected class CSessionInfo {
		
		public String strSecurityTokenID;
		public String strTransactionID;
		public boolean bTransactionInUse;
		
	}
	
	public LinkedList<CSessionInfo> TransactionsPool;
	
    public CBPBackendServicesManager( String strPathToTempDir ) {
		
    	super(strPathToTempDir);
    	
    	TransactionsPool = new LinkedList<CSessionInfo>();
    	
	}

	public ArrayList<LinkedHashMap<String,String>> callServiceSystemStartSession( CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, CExtendedLogger Logger, CLanguage Lang ) {
    	
    	ArrayList<LinkedHashMap<String,String>> Result = null;
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( ConfigDBConnection.strSelectedBackendURL );
    		
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigDBConnection.getConfigProxy(), Logger, Lang );
    		
    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.Start.Session" ) );
    		urlParameters.add( new BasicNameValuePair( "DBConnection", ConfigDBConnection.strDatabase ) );
    		urlParameters.add( new BasicNameValuePair( "username", ConfigDBConnection.strUser ) );
    		urlParameters.add( new BasicNameValuePair( "password", net.maindataservices.Utilities.uncryptString( ConstantsCommonConfigXMLTags._Password_Crypted, ConstantsCommonConfigXMLTags._Password_Crypted_Sep, ConstantsCommonClasses._Crypt_Algorithm, ConfigDBConnection.strPassword, Logger, Lang ) ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
    		
			String strResponseFileName = "System.Start.Session.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), this.strPathToTempDir, strResponseFileName, Logger, Lang ) ) {

				Response.close();
				
				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFileName, Logger, Lang );
				
			}
			
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }
    
    public ArrayList<LinkedHashMap<String,String>> callServiceSystemEndSession( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, String strSecurityTokenID, CExtendedLogger Logger, CLanguage Lang ) {

    	ArrayList<LinkedHashMap<String,String>> Result = null;
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    		 
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );
    		
    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.End.Session" ) );
    		urlParameters.add( new BasicNameValuePair( "SecurityTokenID", strSecurityTokenID ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFileName = "System.End.Session.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFileName, Logger, Lang ) ) { 

				Response.close();
				
				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFileName, Logger, Lang );
				
			}
			
			removeTransactionsFromPool( strSecurityTokenID, Logger, Lang );
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }

    public boolean addTransactionToPool( String strSecurityTokenID, String strTransactionID, boolean bMarkInUse, CExtendedLogger Logger, CLanguage Lang ) {
    	
    	boolean bResult = false;
    	
    	try {

    		if ( findTransactionOnPool( strSecurityTokenID, strTransactionID, bMarkInUse, Logger, Lang) == false ) {

    			CSessionInfo SessionInfo = new CSessionInfo();

    			SessionInfo.strSecurityTokenID = strSecurityTokenID;
    			SessionInfo.strTransactionID = strTransactionID;
    			SessionInfo.bTransactionInUse = bMarkInUse;

    			synchronized ( TransactionsPool ) {

    				TransactionsPool.add( SessionInfo );

    			}

    			bResult = true;

    		}
			
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return bResult;
    	
    }
    
    public boolean removeTransactionsFromPool( String strSecurityTokenID, CExtendedLogger Logger, CLanguage Lang ) {
    	
    	boolean bResult = false;
    	
    	try {

			for ( int intIndex= 0; intIndex < TransactionsPool.size(); intIndex++ ) {

				if ( strSecurityTokenID.equalsIgnoreCase( TransactionsPool.get( intIndex ).strSecurityTokenID ) ) {

	    			synchronized ( TransactionsPool ) {
					
	    				TransactionsPool.remove( intIndex );

	    			}
	    			
	    			bResult = true;
	    			
				}

			}

			
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return bResult;
    	
    }

    public boolean removeTransactionFromPool( String strSecurityTokenID, String strTransactionID, CExtendedLogger Logger, CLanguage Lang ) {
    	
    	boolean bResult = false;
    	
    	try {

			for ( int intIndex= 0; intIndex < TransactionsPool.size(); intIndex++ ) {

				if ( strSecurityTokenID.equalsIgnoreCase( TransactionsPool.get( intIndex ).strSecurityTokenID ) && strTransactionID.equalsIgnoreCase( TransactionsPool.get( intIndex ).strTransactionID ) ) {

	    			synchronized ( TransactionsPool ) {
						
	    				TransactionsPool.remove( intIndex );

	    			}

	    			bResult = true;

				}

			}

			
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return bResult;
    	
    }
    
    public String getTransactionFromPool( String strSecurityTokenID, boolean bMarkInUse, CExtendedLogger Logger, CLanguage Lang ) {
    	
    	String strResult = "";
    	
    	try {

			synchronized ( TransactionsPool ) {
    		
				for ( CSessionInfo SessionInfo: TransactionsPool ) {

					if ( strSecurityTokenID.equalsIgnoreCase( SessionInfo.strSecurityTokenID ) && SessionInfo.bTransactionInUse == false ) {

						synchronized ( SessionInfo ) {

							SessionInfo.bTransactionInUse = bMarkInUse;

						}

						strResult = SessionInfo.strTransactionID;

					}

				}
    		
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return strResult;
    	
    }

    public boolean findTransactionOnPool( String strSecurityTokenID, String strTransactionID, boolean bMarkInUse, CExtendedLogger Logger, CLanguage Lang ) {
    	
    	boolean bResult = false;
    	
    	try {

			synchronized ( TransactionsPool ) {
    		
				for ( CSessionInfo SessionInfo: TransactionsPool ) {

					if ( strSecurityTokenID.equalsIgnoreCase( SessionInfo.strSecurityTokenID ) && strTransactionID.equalsIgnoreCase( strTransactionID ) ) {

						synchronized ( SessionInfo ) {

							SessionInfo.bTransactionInUse = bMarkInUse;

						}

						bResult = true;

						break;
						
					}

				}
    		
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return bResult;
    	
    }
    
    public boolean markTransactionOnPool( String strSecurityTokenID, String strTransactionID, boolean bMarkInUse, CExtendedLogger Logger, CLanguage Lang ) {
    	
    	return findTransactionOnPool( strSecurityTokenID, strTransactionID, bMarkInUse, Logger, Lang);
    	
    }
    
    public ArrayList<LinkedHashMap<String,String>> callServiceSystemStartTransaction( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, String strSecurityTokenID, CExtendedLogger Logger, CLanguage Lang ) {

    	ArrayList<LinkedHashMap<String,String>> Result = null;
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    	
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );
    		
    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.Start.Transaction" ) );
    		urlParameters.add( new BasicNameValuePair( "SecurityTokenID", strSecurityTokenID ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFileName = "System.Start.Transaction.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFileName, Logger, Lang ) ) { 
				
				Response.close();

				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFileName, Logger, Lang );
			
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }

    public ArrayList<LinkedHashMap<String,String>> callServiceSystemCommitTransaction( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, String strSecurityTokenID, String strTransactionID, CExtendedLogger Logger, CLanguage Lang ) {

    	ArrayList<LinkedHashMap<String,String>> Result = null;
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    		 
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );
    		
    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.Commit.Transaction" ) );
    		urlParameters.add( new BasicNameValuePair( "SecurityTokenID", strSecurityTokenID ) );
    		urlParameters.add( new BasicNameValuePair( "TransactionID", strTransactionID ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFileName = "System.Commit.Transaction.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFileName, Logger, Lang ) ) { 
				
				Response.close();

				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFileName, Logger, Lang );
			
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }

    public ArrayList<LinkedHashMap<String,String>> callServiceSystemRollbackTransaction( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, String strSecurityTokenID, String strTransactionID, CExtendedLogger Logger, CLanguage Lang ) {

    	ArrayList<LinkedHashMap<String,String>> Result = null;
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    		 
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );

    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.Rollback.Transaction" ) );
    		urlParameters.add( new BasicNameValuePair( "SecurityTokenID", strSecurityTokenID ) );
    		urlParameters.add( new BasicNameValuePair( "TransactionID", strTransactionID ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFileName = "System.Rollback.Transaction.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFileName, Logger, Lang ) ) { 
				
				Response.close();

				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFileName, Logger, Lang );
			
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }

    public ArrayList<LinkedHashMap<String,String>> callServiceSystemEndTransaction( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, String strSecurityTokenID, String strTransactionID, CExtendedLogger Logger, CLanguage Lang ) {

    	ArrayList<LinkedHashMap<String,String>> Result = null;
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    		 
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );
    		
    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.End.Transaction" ) );
    		urlParameters.add( new BasicNameValuePair( "SecurityTokenID", strSecurityTokenID ) );
    		urlParameters.add( new BasicNameValuePair( "TransactionID", strTransactionID ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFileName = "System.End.Transaction.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFileName, Logger, Lang ) ) { 

				Response.close();
				
				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFileName, Logger, Lang );
				
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }

    public ArrayList<LinkedHashMap<String,String>> callServiceSystemPing( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, int intPing, CExtendedLogger Logger, CLanguage Lang ) {

    	ArrayList<LinkedHashMap<String,String>> Result = null;
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    		 
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );

    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.Ping" ) );
    		urlParameters.add( new BasicNameValuePair( "Ping", Integer.toString( intPing ) ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFileName = "System.Ping.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFileName, Logger, Lang ) ) { 

				Response.close();
				
				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFileName, Logger, Lang );
				
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }
 
    public ArrayList<LinkedHashMap<String,String>> callServiceSystemObjectExists( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, String strSecurityTokenID, String strObjectType, String strObjectName, CExtendedLogger Logger, CLanguage Lang ) {

    	ArrayList<LinkedHashMap<String,String>> Result = null;
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    		 
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );
    		
    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.Object.Exists" ) );
    		urlParameters.add( new BasicNameValuePair( "SecurityTokenID", strSecurityTokenID ) );
    		urlParameters.add( new BasicNameValuePair( "ObjectType", strObjectType ) );
    		urlParameters.add( new BasicNameValuePair( "ObjectName", strObjectName ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFileName = "System.Object.Exists.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFileName, Logger, Lang ) ) { 

				Response.close();

				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFileName, Logger, Lang );
			
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }
    
    public ArrayList<LinkedHashMap<String,String>> callServiceSystemListObjects( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, String strSecurityTokenID, String strObjectType, CExtendedLogger Logger, CLanguage Lang ) {

    	ArrayList<LinkedHashMap<String,String>> Result = null;
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    		 
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );

    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.List.Objects" ) );
    		urlParameters.add( new BasicNameValuePair( "SecurityTokenID", strSecurityTokenID ) );
    		urlParameters.add( new BasicNameValuePair( "ObjectType", strObjectType ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFileName = "System.List.Objects.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFileName, Logger, Lang ) ) { 

				Response.close();
				
				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFileName, Logger, Lang );
				
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }
    
    public ArrayList<LinkedHashMap<String,String>> callServiceSystemDescribeObject( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, String strSecurityTokenID, String strObjectType, String strObjectName, CExtendedLogger Logger, CLanguage Lang ) {

    	ArrayList<LinkedHashMap<String,String>> Result = null;
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    		 
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );

    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.Describe.Object" ) );
    		urlParameters.add( new BasicNameValuePair( "SecurityTokenID", strSecurityTokenID ) );
    		urlParameters.add( new BasicNameValuePair( "ObjectType", strObjectType ) );
    		urlParameters.add( new BasicNameValuePair( "ObjectName", strObjectName ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFileName = "System.Describe.Object.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFileName, Logger, Lang ) ) { 

				Response.close();
				
				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFileName, Logger, Lang );
				
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }
    
    public ArrayList<LinkedHashMap<String,String>> callServiceSystemDatabaseInfo( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, String strSecurityTokenID, CExtendedLogger Logger, CLanguage Lang ) {

    	ArrayList<LinkedHashMap<String,String>> Result = null;
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    		 
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );
    		
    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.Database.Info" ) );
    		urlParameters.add( new BasicNameValuePair( "SecurityTokenID", strSecurityTokenID ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFileName = "System.Database.Info.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFileName, Logger, Lang ) ) { 

				Response.close();
				
				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFileName, Logger, Lang );
				
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }
    
    public String callServiceSystemExecuteSQLResponseToFile( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, String strSecurityTokenID, String strTransactionID, String strSQL, LinkedHashMap<String,String> Params, CExtendedLogger Logger, CLanguage Lang ) {

    	String strResult = "";
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    		 
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );

    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.Execute.SQL" ) );
    		urlParameters.add( new BasicNameValuePair( "SecurityTokenID", strSecurityTokenID ) );
    		urlParameters.add( new BasicNameValuePair( "TransactionID", strTransactionID ) );
    		urlParameters.add( new BasicNameValuePair( "SQL", strSQL ) );
    		
    		if ( Params != null ) {
    		
    			for ( Entry<String,String> Param: Params.entrySet() ) {

    				urlParameters.add( new BasicNameValuePair( Param.getKey(), Param.getValue() ) );

    			}
    		
    		}
    		
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFileName = "System.Execute.SQL.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFileName, Logger, Lang ) ) { 

				Response.close();
				
				strResult = strPathToTempDir + strResponseFileName;
				
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return strResult;
    	
    }
    
    public ArrayList<LinkedHashMap<String,String>> callServiceSystemExecuteSQLParsed( CloseableHttpClient HTTPClient, String strURL, CConfigProxy ConfigProxy, String strSecurityTokenID, String strTransactionID, String strSQL, LinkedHashMap<String,String> Params, CExtendedLogger Logger, CLanguage Lang ) {

    	ArrayList<LinkedHashMap<String,String>> Result = new ArrayList<LinkedHashMap<String,String>>();
    	
    	try {
    		
        	CloseableHttpResponse Response = null;
        	
    		HttpPost PostData = new HttpPost( strURL );
    		 
    		//Set the proxy
    		HttpClientContext Context = setConfigProxy( PostData, ConfigProxy, Logger, Lang );

    		// add header
    		PostData.setHeader( "User-Agent", "/BPServices" );
    	 
    		ArrayList<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    		urlParameters.add( new BasicNameValuePair( "ServiceName", "System.Execute.SQL" ) );
    		urlParameters.add( new BasicNameValuePair( "SecurityTokenID", strSecurityTokenID ) );
    		urlParameters.add( new BasicNameValuePair( "TransactionID", strTransactionID ) );
    		urlParameters.add( new BasicNameValuePair( "SQL", strSQL ) );
    		
    		if ( Params != null ) {
    		
    			for ( Entry<String,String> Param: Params.entrySet() ) {

    				urlParameters.add( new BasicNameValuePair( Param.getKey(), Param.getValue() ) );

    			}
    		
    		}
    		
    		urlParameters.add( new BasicNameValuePair( "ResponseFormat", _ResponseFormat ) );
    		urlParameters.add( new BasicNameValuePair( "ResponseFormatVersion", _ResponseFormatVersion ) );
    		
			PostData.setEntity( new UrlEncodedFormEntity( urlParameters ) );
			
			if ( Context == null )
				Response = HTTPClient.execute( PostData );
			else
				Response = HTTPClient.execute( PostData, Context );
			
			String strResponseFile = "System.Execute.SQL.Response." + this.strGUID + "." + UUID.randomUUID().toString();
			
			if ( Response != null && this.saveResponseToFile( Response.getEntity().getContent(), strPathToTempDir, strResponseFile, Logger, Lang ) ) { 

				Response.close();
				
				Result = this.parseResponseMessage( this.strPathToTempDir, strResponseFile, Logger, Lang );
				
			}
    		
    	}
    	catch ( Exception Ex ) {
    		
			Logger.logException( "-1015", Ex.getMessage(), Ex );
    		
    	}
    	
    	return Result;
    	
    }
    
}
