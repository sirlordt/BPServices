package BPCommonClasses;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import CommonClasses.CConfigProxy;
import CommonClasses.CLanguage;
import CommonClasses.CRegisteredManagerInfo;
import DBActions.CDBActionsManager;
import ExtendedLogger.CExtendedLogger;

public class CConfigServiceDBConnection {

	public final static String _Dynamic_Backend = "dynamic-backend";
	
	public String strName;
    public String strSelectedBackendURL;
    public ArrayList<String> strAvailableBackendsURL;
    public int intCurrentURLSelected;
    public String strProxyIP;
    public int intProxyPort;
    public String strProxyUser;
    public String strProxyPassword;
    public String strDatabase;
    public String strDateFormat;
    public String strTimeFormat;
    public String strDateTimeFormat;
    public String strUser;
    public String strPassword;
    public String strMapsFilePath;
    public String strDBActionsFilePath;
    public String strDBDefinitionsFilePath;

    public Properties KeywordsMaps;
    
    public CDBActionsManager DBActionsManager;
    
	public CConfigServiceDBConnection() {

		strName = "";
		strSelectedBackendURL = "";
		strAvailableBackendsURL = new ArrayList<String>();
		intCurrentURLSelected = 0;
		strProxyIP = "";
		intProxyPort = 0;
		strProxyUser = "";
		strProxyPassword = "";
		strDatabase = "";
		strDateFormat = "";
		strTimeFormat = "";
		strDateTimeFormat = "";
		strUser = "";
		strPassword = "";
		strMapsFilePath = "";
		strDBActionsFilePath = "";
		strDBDefinitionsFilePath = "";
		
		KeywordsMaps = null;
		
		DBActionsManager = null;
		
	}
	
	public CConfigServiceDBConnection( CConfigServiceDBConnection ConfigDBConnection ) {
	
        strName = ConfigDBConnection.strName;
        strSelectedBackendURL = ConfigDBConnection.strSelectedBackendURL;
        strAvailableBackendsURL = new ArrayList<String>( ConfigDBConnection.strAvailableBackendsURL );
        intCurrentURLSelected = ConfigDBConnection.intCurrentURLSelected;
        strProxyIP = ConfigDBConnection.strProxyIP;
		intProxyPort = ConfigDBConnection.intProxyPort;
		strProxyUser = ConfigDBConnection.strProxyUser;
		strProxyPassword = ConfigDBConnection.strProxyPassword;
        strDatabase = ConfigDBConnection.strDatabase;
		strDateFormat = ConfigDBConnection.strDateFormat;
		strTimeFormat = ConfigDBConnection.strTimeFormat;
		strDateTimeFormat = ConfigDBConnection.strDateTimeFormat;
        strUser = ConfigDBConnection.strUser;
        strPassword = ConfigDBConnection.strPassword;
        strMapsFilePath = ConfigDBConnection.strMapsFilePath;
        strDBActionsFilePath = ConfigDBConnection.strDBActionsFilePath;
		strDBDefinitionsFilePath = ConfigDBConnection.strDBDefinitionsFilePath;
        
        KeywordsMaps = new Properties( ConfigDBConnection.KeywordsMaps );
        
        DBActionsManager = new CDBActionsManager( ConfigDBConnection.DBActionsManager );
	
	}	

	public CConfigServiceDBConnection getCloneConfigDBConnection() {
	
		CConfigServiceDBConnection ConfigDBConnection = new CConfigServiceDBConnection( this );
		
		return ConfigDBConnection;
		
	}

	public boolean getDynamicBackendOnSelectMethod() {
		
		return strAvailableBackendsURL.contains( _Dynamic_Backend );
		
	} 
	
	public boolean selectURLBackend( int intSelectMethod, ArrayList<CRegisteredManagerInfo> ListOfBackendsNodes, CExtendedLogger Logger, CLanguage Lang ) { //0 = Random, 1 = Round robin, 2 = Dynamic from cluster
		
		boolean bResult = false;
		
		try {
			
			if ( intSelectMethod == 0 ) { //Random from list
				
			   if ( strAvailableBackendsURL.size() > 1 ) {
				   
				   Random rand = new Random();
				   
				   intCurrentURLSelected = rand.nextInt( strAvailableBackendsURL.size() - 1 );
				   
				   if ( intCurrentURLSelected < strAvailableBackendsURL.size() ) {

					   strSelectedBackendURL = strAvailableBackendsURL.get( intCurrentURLSelected );

					   bResult = true;
					   
				   }
				   
			   }
			   else if ( strAvailableBackendsURL.size() == 1 ) {
				   
				   strSelectedBackendURL = strAvailableBackendsURL.get( 0 );
				   
				   bResult = true;
				   
			   }
				
			}
			else if ( intSelectMethod == 1 || ( intSelectMethod == 2 && ( ListOfBackendsNodes == null || ListOfBackendsNodes.size() == 0 ) ) ) { //Round robin
				
				if ( strAvailableBackendsURL.size() > 1 ) {

					int intCountInitPass = 0;
					
					do {
						
						if ( intCurrentURLSelected + 1 > strAvailableBackendsURL.size() ) {

							intCurrentURLSelected = 0;
							
							intCountInitPass++;

						}
						else {

							intCurrentURLSelected += 1;

						}

						strSelectedBackendURL = strAvailableBackendsURL.get( intCurrentURLSelected );

					} while ( strSelectedBackendURL.equals( _Dynamic_Backend ) && intCountInitPass <= 1 );
					
					bResult = true;

				}
				else if ( strAvailableBackendsURL.size() == 1 ) {

					strSelectedBackendURL = strAvailableBackendsURL.get( 0 );

					bResult = true;

				}
				
			}
			else if ( intSelectMethod == 2 ) { //Dynamic back end from cluster 
				
				CRegisteredManagerInfo SelectedBackendNode = ListOfBackendsNodes.get( 0 );
				
				int intBestNodeDelta = 0;
				
				for ( int intIndex = 1; intIndex < ListOfBackendsNodes.size(); intIndex++ ) {
					
					CRegisteredManagerInfo CurrentBackendNode = ListOfBackendsNodes.get( intIndex );
					
					int intCurrentNodeDelta = 0;
					
					if ( CurrentBackendNode.intLoad >= 75 ) { //Too much load
						
						intCurrentNodeDelta = 100 - CurrentBackendNode.intLoad;
						
					}
					else {
						
						intCurrentNodeDelta = ( CurrentBackendNode.intStandardizedWeight + ( CurrentBackendNode.intLoad - 100 ) ) / 2;
						
					}
					
					if ( intCurrentNodeDelta > intBestNodeDelta ) {
						
						intBestNodeDelta = intCurrentNodeDelta;
						
						SelectedBackendNode = CurrentBackendNode;
						
					}
					
					/*if ( CurrentBackendNode.intLoad < SelectedBackendNode.intLoad && CurrentBackendNode.intStandardizedWeight >= SelectedBackendNode.intStandardizedWeight ) {
						
						SelectedBackendNode = CurrentBackendNode;
						
					}*/
					
				}

				if ( SelectedBackendNode != null ) {
					
					strSelectedBackendURL = SelectedBackendNode.strManagerURL;
					
				}
				
			}
			
			
		}
		catch ( Exception Ex ) {
			
			Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}

		return bResult;
		
	}
	
	public String getURLBackend() {
		
		return strSelectedBackendURL;
		
	}
	
	public CConfigProxy getConfigProxy() {
		
		return new CConfigProxy( strProxyIP, intProxyPort, strProxyUser, strProxyPassword );
		
	}
	
}
