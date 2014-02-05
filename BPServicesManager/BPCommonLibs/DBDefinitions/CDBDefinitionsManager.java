package DBDefinitions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.client.HttpClients;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import WebRowSet.CWebRowSetImpl;

import BPBackendServicesManager.CBPBackendServicesManager;
import BPCommonClasses.CConfigServiceDBConnection;
import CommonClasses.CConfigRegisterService;
import CommonClasses.CLanguage;
import CommonClasses.CRegisteredManagerInfo;
import CommonClasses.ConstantsCommonClasses;
import CommonClasses.NamesSQLTypes;
import ExtendedLogger.CExtendedLogger;

public class CDBDefinitionsManager {

	protected static final String _Backend_Context = "/DBServices";
	
	protected LinkedHashMap<String,CDBDefinition> DBDefinitions = null;
	
	protected String strDBDefinitionsFilePath = "";
	
	protected ArrayList<CConfigRegisterService> ConfiguredRegisterServices = null;
	
	protected String strRunningPath = "";
	
	protected int intRequestTimeout = 0;
	
	protected int intSocketTimeout = 0;
	
	public CDBDefinitionsManager( String strRunningPath, ArrayList<CConfigRegisterService> ConfiguredRegisterServices, int intRequestTimeout, int intSocketTimeout ) {
		
		this.strRunningPath = strRunningPath;
		
		DBDefinitions = new LinkedHashMap<String,CDBDefinition>(); 
		
        this.ConfiguredRegisterServices = ConfiguredRegisterServices;		

        this.intRequestTimeout = intRequestTimeout;
        
        this.intSocketTimeout = intSocketTimeout;
        
	}

    public ArrayList<CRegisteredManagerInfo> getListOfBackendsNodes( CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CExtendedLogger Logger, CLanguage Lang ) {

		long lngStart = System.currentTimeMillis();
		
		ArrayList<CRegisteredManagerInfo> ListOfBackendNodes = null;
    	
    	for ( CConfigRegisterService ConfigRegisterService: ConfiguredRegisterServices ) {
    		
    		ListOfBackendNodes = BackendServicesManager.callServiceSystemListRegisteredManagers( HTTPClient, ConfigRegisterService.strPassword, ConfigRegisterService.strURL, ConfigRegisterService.ConfigProxy, _Backend_Context, Logger, Lang );
    		
    		if ( ListOfBackendNodes != null && ListOfBackendNodes.size() > 0 ) {
    			
    			break;
    			
    		}
    		
    	}
    	
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "getListOfBackendsNodes", Long.toString( lngEnd - lngStart ) ) );
    	
		return ListOfBackendNodes;
    	
    }	
	
	public boolean loadDBDefinitionsFromFile( String strDBDefinitionsFilePath, final CExtendedLogger Logger, final CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;
		
		this.strDBDefinitionsFilePath = strDBDefinitionsFilePath;

		DBDefinitions.clear();
		
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler XMLParserHandler = new DefaultHandler() {

				int intDatabaseSectionNode = 0;
				int intConstantsSectionNode = 0;
				String strConstantName = "";
				int intConstantNode = 0;
				int intTableSectionNode = 0;
				int intCreateDefinitionSectioNode = 0;
				int intCommandNode = 0;
				int intAddFieldDefinitionNode = 0;
				int intAlterFieldDefinitionNode = 0;
				int intCheckFieldsSectionNode = 0;
				int intFieldNode = 0;
				int intCheckDataSectionNode = 0;
				int intDataSectionNode = 0;
				int intConditionNode = 0;
				int intMapsNames = 0;
				int intMap = 0;
				
				CDBDefinition DBDefinition = null;
				CDBTableDefinition DBTableDefinition = null;
				CDBFieldDefinition DBFieldDefinition = null;
				CDBDataDefinition DBDataDefinition = null;
				
	            private Locator DocumentLocator;

	            @Override
	            public void setDocumentLocator( final Locator locator ) {
	                
	            	this.DocumentLocator = locator; // Save the locator, so that it can be used later for line tracking when traversing nodes.
	            	
	            }
	            
	            @Override
				public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {

	            	int intDocumentLine = DocumentLocator.getLineNumber();
	            	int intDocumentColumn = DocumentLocator.getColumnNumber();
	            	
					if ( qName.equals( ConfigXMLTagsDBDefinitions._Debug ) ) {
					
						Logger.logMessage( "1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn )  ) );
						Logger.logDebug( "1", Lang.translate( "Start Debug tag found on URI: [%s]", uri ) );

						if ( attributes.getLength() == ConfigXMLTagsDBDefinitions._DebugAttribCount ) {

							String strDebugID = attributes.getValue( 0 ).trim();
							
							if ( strDebugID.isEmpty() == false ) {

								Logger.logDebug( "1", Lang.translate( "Debug tag Id: [%s]", strDebugID ) );

							}
							
						}	
						
					}
					else if ( qName.equals( ConfigXMLTagsDBDefinitions._Database ) ) {
						
						if ( intDatabaseSectionNode == 0 ) {

							if ( attributes.getLength() == ConfigXMLTagsDBDefinitions._DatabaseAttribCount ) {

								DBDefinition = new CDBDefinition(); 

								for ( int intIndexAttrib = 0; intIndexAttrib < attributes.getLength(); intIndexAttrib++ ) {

									String strAttribName = attributes.getQName( intIndexAttrib );

									if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Name ) ) {

										DBDefinition.strName = attributes.getValue( intIndexAttrib ).trim();

										if ( DBDefinition.strName.isEmpty() == true ) {
										
											Logger.logError( "-1001", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
											Logger.logError( "-1001", Lang.translate( "The [%s] attribute is empty for the section [%s]", "Name", qName ) );
										
											DBDefinition = null;
											
											break;
											
										}	
										
									}
									else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Description ) ) {

										DBDefinition.strDescription = attributes.getValue( intIndexAttrib );

									}
									else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._DBMS_Name ) ) {

										DBDefinition.strDBMSName = attributes.getValue( intIndexAttrib ).trim();

										if ( DBDefinition.strDBMSName.isEmpty() == true ) {

											Logger.logError( "-1002", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
											Logger.logError( "-1002", Lang.translate( "The [%s] attribute is empty for the section [%s]", "DBMS_Name", qName ) );

											DBDefinition = null;
											
											break;
											
										}	
										
									}
									else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._DBMS_Version ) ) {

										DBDefinition.strDBMSVersion = attributes.getValue( intIndexAttrib ).trim();

										if ( DBDefinition.strDBMSName.isEmpty() == true ) {
											
											Logger.logError( "-1003", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
											Logger.logError( "-1003", Lang.translate( "The [%s] attribute is empty for the section [%s]", "DBMS_Version", qName ) );
											
											DBDefinition = null;
											
											break;
											
										}	

									}
									else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Create_Definition ) ) {

										DBDefinition.strCreateDefinition = attributes.getValue( intIndexAttrib ).trim();

										if ( DBDefinition.strCreateDefinition.isEmpty() == true ) {

											Logger.logError( "-1004", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
											Logger.logError( "-1004", Lang.translate( "The [%s] attribute is empty for the section [%s]", "Create_Definition", qName ) );
											
											DBDefinition = null;
											
											break;
											
										}	

									}
									else {

										Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
										Logger.logWarning( "-1", Lang.translate( "The [%s] attribute is unknown for the node [%s]", strAttribName, qName ) );

										DBDefinition = null;
										
										break;
										
									}

								}

								if (  DBDefinition != null && DBDefinitions.get( DBDefinition.strName.toLowerCase() + "@|@" + DBDefinition.strDBMSName.toLowerCase() + "@|@" + DBDefinition.strDBMSVersion.toLowerCase() ) != null ) {
								
									Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
									Logger.logWarning( "-1", Lang.translate( "Database definition with the next properties. Name: [%s], DBMS_Name: [%s] and DBMS_Version: [%s] already exists in the list of databases. This properties Name, DBMS_Name and DBMS_Version must be unique for config section", DBDefinition.strName, DBDefinition.strDBMSName, DBDefinition.strDBMSVersion ) );
									Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
									
									DBDefinition = null;
								
								}
								else if ( DBDefinition == null ) {
									
									Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
									Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
									
								}

							}
							else {
								
								Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
								Logger.logWarning( "-1", Lang.translate( "Wrong count of attributes for the section [%s], found [%s] must be [%s]", qName, Integer.toString( attributes.getLength() ), Integer.toString( ConfigXMLTagsDBDefinitions._DatabaseAttribCount ) ) );
								Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
								
							}
							
						}
						else {
							
							Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
							Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
							
						}

						intDatabaseSectionNode++;
						
					}
					else if ( DBDefinition != null && intDatabaseSectionNode == 1 ) {
					
						if ( qName.equals( ConfigXMLTagsDBDefinitions._Constants ) ) {

							intConstantsSectionNode++;

						}
						else if ( qName.equals( ConfigXMLTagsDBDefinitions._Constant ) ) {

							if ( intConstantsSectionNode == 1  ) {

								strConstantName = attributes.getValue( 0 );

							}

							intConstantNode++;

						}
						else if ( qName.equals( ConfigXMLTagsDBDefinitions._Table ) ) {

							if ( intTableSectionNode == 0 ) {

								if ( attributes.getLength() == ConfigXMLTagsDBDefinitions._TableAttribCount ) {

									DBTableDefinition = new CDBTableDefinition();

									for ( int intIndexAttrib = 0; intIndexAttrib < attributes.getLength(); intIndexAttrib++ ) {

										String strAttribName = attributes.getQName( intIndexAttrib );

										if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Name ) ) {

											DBTableDefinition.strName = attributes.getValue( intIndexAttrib ).trim();

											if ( DBTableDefinition.strName.isEmpty() == true ) {
												
												Logger.logError( "-1005", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
												Logger.logError( "-1005", Lang.translate( "The [%s] attribute is empty for the section [%s]", "Name", qName ) );
												
												DBTableDefinition = null;
												
											}
											
										}
										else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Description ) ) {

											DBTableDefinition.strDescription = attributes.getValue( intIndexAttrib );

										}
										else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Create ) ) {

											DBTableDefinition.bCreate = attributes.getValue( intIndexAttrib ).trim().toLowerCase().equals( ConfigXMLTagsDBDefinitions._True );

										}
										else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Check_Fields ) ) {

											DBTableDefinition.bCheckFields = attributes.getValue( intIndexAttrib ).trim().toLowerCase().equals( ConfigXMLTagsDBDefinitions._True );

										}
										else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Add_Fields ) ) {

											DBTableDefinition.bAddFields = attributes.getValue( intIndexAttrib ).trim().toLowerCase().equals( ConfigXMLTagsDBDefinitions._True );

										}
										else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Alter_Fields ) ) {

											DBTableDefinition.bAlterFields = attributes.getValue( intIndexAttrib ).trim().toLowerCase().equals( ConfigXMLTagsDBDefinitions._True );

										}
										else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Check_Data ) ) {

											DBTableDefinition.bCheckData = attributes.getValue( intIndexAttrib ).trim().toLowerCase().equals( ConfigXMLTagsDBDefinitions._True );

										}
										else {

											Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
											Logger.logWarning( "-1", Lang.translate( "The [%s] attribute is unknown for the node [%s]", strAttribName, qName ) );

											DBTableDefinition = null;
											
											break;

										}

									}

									if ( DBDefinition.Tables.get( DBTableDefinition.strName.toLowerCase() ) != null ) {

										Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
										Logger.logWarning( "-1", Lang.translate( "Table definition name [%s] already exists in the list of tables. This name must be unique for database definition section", DBTableDefinition.strName ) );
										Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
										
										DBTableDefinition = null;
										
									}	
									else if ( DBTableDefinition == null ) {

										Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
										Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );

									}

								}								
								else {
									
									Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
									Logger.logWarning( "-1", Lang.translate( "Wrong count of attributes for the section [%s], found [%s] must be [%s]", qName, Integer.toString( attributes.getLength() ), Integer.toString( ConfigXMLTagsDBDefinitions._TableAttribCount ) ) );
									Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
									
								}

							}
							else {
								
								Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
								Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
								
							}

							intTableSectionNode++;

						}
						else if ( DBTableDefinition != null && intTableSectionNode == 1 ) {
							
							if ( qName.equals( ConfigXMLTagsDBDefinitions._CreationDefinitions ) ) {
							
								intCreateDefinitionSectioNode++;
							
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._Command ) ) {

								intCommandNode++;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._AddFieldDefinition ) ) {
								
								intAddFieldDefinitionNode++;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._AlterFieldDefinition ) ) {
								
								intAlterFieldDefinitionNode++;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._CheckFields ) ) {
								
								intCheckFieldsSectionNode++;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._Field ) ) {
								
								if ( intCheckFieldsSectionNode == 1 && intFieldNode == 0 ) {
									
									if ( attributes.getLength() == ConfigXMLTagsDBDefinitions._FieldAttribCount ) {
										
										DBFieldDefinition = new CDBFieldDefinition(); 
										
										for ( int intIndexAttrib = 0; intIndexAttrib < attributes.getLength(); intIndexAttrib++ ) {

											String strAttribName = attributes.getQName( intIndexAttrib );

											if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._MatchCase ) ) {
												
												String strValue = attributes.getValue( intIndexAttrib ).trim().toLowerCase();
												
												if ( strValue.equals( ConfigXMLTagsDBDefinitions._Exact ) ) 
													DBFieldDefinition.intMatchCase = 1;
												else
													Logger.logMessage( "1", Lang.translate( "Using [any] for MatchCase attribute in Field node" ) );
												
											}
											else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Name ) ) {

												DBFieldDefinition.strName = attributes.getValue( intIndexAttrib ).trim();

												if ( DBFieldDefinition.strName.isEmpty() == true ) {
													
													Logger.logError( "-1006", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
													Logger.logError( "-1006", Lang.translate( "The [%s] attribute is empty for the section [%s]", "Name", qName ) );
													
													DBFieldDefinition = null;
													
													break;
													
												}
												else if ( DBTableDefinition.CheckFields.get( DBFieldDefinition.strName.toLowerCase() ) != null ) {

													Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
													Logger.logWarning( "-1", Lang.translate( "Field definition name [%s] already exists in the list of fields. This name must be unique for table definition section", DBFieldDefinition.strName ) );
													Logger.logWarning( "-1", Lang.translate( "Ignoring the node [%s]", qName ) );
													
													DBFieldDefinition = null;
													
													break;
													
												}
												
											}
											else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Type ) ) {

												DBFieldDefinition.strType = attributes.getValue( intIndexAttrib ).trim();
												
												if ( DBFieldDefinition.strType.isEmpty() == true ) {
													
													Logger.logError( "-1007", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
													Logger.logError( "-1007", Lang.translate( "The [%s] attribute is empty for the section [%s]", "Type", qName ) );

													DBFieldDefinition = null;
													
													break;
													
												}
												else if ( NamesSQLTypes.CheckJavaSQLType( DBFieldDefinition.strType ) == false ) {
													
													Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
													Logger.logWarning( "-1", Lang.translate( "The Type value: [%s] attribute is not java (JDBC) valid SQL type", DBFieldDefinition.strType ) );
													
												}

											}
											else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Length ) ) {

												DBFieldDefinition.intLength = net.maindataservices.Utilities.strToInteger( attributes.getValue( intIndexAttrib ).trim(), Logger );

												if ( DBFieldDefinition.intLength == 0 ) {
													
													Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
													Logger.logWarning( "-1", Lang.translate( "The length is zero, any size is valid for string field types" ) );
													
												}
												
											}
											else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Comment ) ) {

												DBFieldDefinition.strComment = attributes.getValue( intIndexAttrib ).trim();

											}
											else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._NotNull ) ) {

												String strNotNull = attributes.getValue( intIndexAttrib ).trim().toLowerCase();

												if ( strNotNull.equals( ConfigXMLTagsDBDefinitions._Keep ) ) {
													
													DBFieldDefinition.intNotNull = 2; //Keep table config
													
												}
												else if ( strNotNull.equals( ConfigXMLTagsDBDefinitions._True ) ) {
													
													DBFieldDefinition.intNotNull = 1; //Not Allow Null
													
												}
												else {
													
													DBFieldDefinition.intNotNull = 0; //Allow Null
													
												}
												
											}
											else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Alter ) ) {

												DBFieldDefinition.bAlter = attributes.getValue( intIndexAttrib ).trim().toLowerCase().equals( ConfigXMLTagsDBDefinitions._True );

											}
											else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Add ) ) {

												DBFieldDefinition.bAdd = attributes.getValue( intIndexAttrib ).trim().toLowerCase().equals( ConfigXMLTagsDBDefinitions._True );

											}
											else {

												Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
												Logger.logWarning( "-1", Lang.translate( "The [%s] attribute is unknown for the node [%s]", strAttribName, qName ) );

												DBFieldDefinition = null;
												
												break;

											}

										}
										
										if ( DBFieldDefinition == null ) {

											Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
											Logger.logWarning( "-1", Lang.translate( "Ignoring the node [%s]", qName ) );
											
										}
										
									}
									else {
										
										Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
										Logger.logWarning( "-1", Lang.translate( "Wrong count of attributes for the node [%s], found [%s] must be [%s]", qName, Integer.toString( attributes.getLength() ), Integer.toString( ConfigXMLTagsDBDefinitions._FieldAttribCount ) ) );
										Logger.logWarning( "-1", Lang.translate( "Ignoring the node [%s]", qName ) );
										
									}
									
								}
								else {
									
									Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
									Logger.logWarning( "-1", Lang.translate( "Ignoring the node [%s]", qName ) );
									
								}
								
								intFieldNode++;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._CheckData ) ) {
								
								intCheckDataSectionNode++;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._Data ) ) {
								
								if ( intCheckDataSectionNode == 1 && intDataSectionNode == 0 ) {
									
									if ( attributes.getLength() == ConfigXMLTagsDBDefinitions._DataAttribCount ) {
										
										DBDataDefinition = new CDBDataDefinition(); 
										
										for ( int intIndexAttrib = 0; intIndexAttrib < attributes.getLength(); intIndexAttrib++ ) {

											String strAttribName = attributes.getQName( intIndexAttrib );

											if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._ID ) ) {
												
												String strValue = attributes.getValue( intIndexAttrib ).trim();
												
												if ( strValue.isEmpty() == true ) {
													
													Logger.logError( "-1008", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
													Logger.logError( "-1008", Lang.translate( "The [%s] attribute is empty for the section [%s]", "ID", qName ) );
													
													DBDataDefinition = null;
													
													break;
													
												}
												else if ( DBTableDefinition.CheckData.get( strValue.toLowerCase() ) == null ) {
													
													DBDataDefinition.strID = strValue;
													
												}
												else {
													
													Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
													Logger.logWarning( "-1", Lang.translate( "Data definition ID [%s] already exists in the list of data. This ID must be unique for table definition section", DBDataDefinition.strID ) );
													Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
													
													DBDataDefinition = null;
													
													break;
													
												}
												
											}
											else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Event ) ) {
												
												String strValue = attributes.getValue( intIndexAttrib ).trim().toLowerCase();
												
												if ( strValue.equals( ConfigXMLTagsDBDefinitions._Table_Exists ) ) 
													DBDataDefinition.intEvent = 1;
												else
													Logger.logMessage( "1", Lang.translate( "Using [table_created] for Event attribute in Data section node" ) );
												
											}
											else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Active ) ) {

												String strValue = attributes.getValue( intIndexAttrib ).trim().toLowerCase();

												/*if ( strValue.isEmpty() == true ) {
													
													Logger.logError( "-1009", Lang.translate( "The [%s] attribute is empty for the section [%s]", "Active", qName ) );
													
													DBDataDefinition = null;
													
													break;
													
												}
												else*/ 
												if ( strValue.equals( ConfigXMLTagsDBDefinitions._True ) == false ) {
													
													Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
													Logger.logWarning( "-1", Lang.translate( "The [%s] attribute value disable the section [%s]", "Active", qName ) );
													
													DBDataDefinition = null;
													
													break;
													
												}
												
											}
											else {

												Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
												Logger.logWarning( "-1", Lang.translate( "The [%s] attribute is unknown for the node [%s]", strAttribName, qName ) );

												DBDataDefinition = null;
												
												break;

											}

										}
										
										if ( DBDataDefinition == null ) {

											Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
											Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
											
										}
										
									}
									else {
										
										Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
										Logger.logWarning( "-1", Lang.translate( "Wrong count of attributes for the section [%s], found [%s] must be [%s]", qName, Integer.toString( attributes.getLength() ), Integer.toString( ConfigXMLTagsDBDefinitions._DataAttribCount ) ) );
										Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
										
									}
									
								}
								else {
									
									Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
									Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
									
								}
								
								intDataSectionNode++;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._Condition ) ) {

								if ( DBDataDefinition != null && intCheckDataSectionNode == 1 && intDataSectionNode == 1 ) {

									if ( attributes.getLength() == ConfigXMLTagsDBDefinitions._ConditionAttribCount ) {

										for ( int intIndexAttrib = 0; intIndexAttrib < attributes.getLength(); intIndexAttrib++ ) {

											String strAttribName = attributes.getQName( intIndexAttrib );

											if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Type ) ) {

												String strValue = attributes.getValue( intIndexAttrib ).trim().toLowerCase();

												if ( strValue.equals( ConfigXMLTagsDBDefinitions._Where ) ) 
													DBDataDefinition.intType = 1;
												else if ( strValue.equals( ConfigXMLTagsDBDefinitions._Full ) )
													DBDataDefinition.intType = 2;
												else
													Logger.logMessage( "1", Lang.translate( "Using [none] for type attribute" ) );

											}
											else if ( strAttribName.equals( ConfigXMLTagsDBDefinitions._Action ) ) {

												String strValue = attributes.getValue( intIndexAttrib ).trim().toLowerCase();

												if ( strValue.equals( ConfigXMLTagsDBDefinitions._Update ) ) 
													DBDataDefinition.intAction = 1;
												else
													Logger.logMessage( "1", Lang.translate( "Using [insert] for action attribute" ) );

											}
											else {

												Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
												Logger.logWarning( "-1", Lang.translate( "The [%s] attribute is unknown for the node [%s]", strAttribName, qName ) );

												DBDataDefinition = null;

												break;

											}

										}

										if ( DBDataDefinition == null ) {

											Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
											Logger.logWarning( "-1", Lang.translate( "Ignoring the node [%s]", qName ) );

										}

									}
									else {

										Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
										Logger.logWarning( "-1", Lang.translate( "Wrong count of attributes for the section [%s], found [%s] must be [%s]", qName, Integer.toString( attributes.getLength() ), Integer.toString( ConfigXMLTagsDBDefinitions._ConditionAttribCount ) ) );
										Logger.logWarning( "-1", Lang.translate( "Ignoring the node [%s]", qName ) );

									}

								}
								else {

									Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
									Logger.logWarning( "-1", Lang.translate( "Ignoring the node [%s]", qName ) );

								}

								intConditionNode++;

							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._MapsNames ) ) {
							
								intMapsNames++;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._Map ) ) {
								
								intMap++;
								
							}

						}
						
					}
					
				}
				
	            @Override
				public void endElement( String uri, String localName, String qName ) throws SAXException {

	            	int intDocumentLine = DocumentLocator.getLineNumber();
	            	int intDocumentColumn = DocumentLocator.getColumnNumber();

	            	if ( qName.equals( ConfigXMLTagsDBDefinitions._Debug ) ) {
						
						Logger.logDebug( "1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
						Logger.logDebug( "1", Lang.translate( "End Debug tag found on URI: [%s]", uri ) );
						
					}
					else if ( qName.equals( ConfigXMLTagsDBDefinitions._Database ) ) {
						
						if ( DBDefinition != null && intDatabaseSectionNode == 1 ) {
							
							if ( DBDefinition.Tables.size() > 0 ) {

								//if ( DBDefinitions.get( DBDefinition.strName.toLowerCase() + "@|@" + DBDefinition.strDBMSName.toLowerCase() + "@|@" + DBDefinition.strDBMSVersion.toLowerCase() ) == null ) {
								
								DBDefinitions.put( DBDefinition.strName.toLowerCase() + "@|@" + DBDefinition.strDBMSName.toLowerCase() + "@|@" + DBDefinition.strDBMSVersion.toLowerCase(), DBDefinition );
								Logger.logMessage( "1", Lang.translate( "Added database definition with the next properties. Name: [%s], DBMS_Name: [%s], DBMS_Version: [%s], Tables_Defined: [%s]", DBDefinition.strName, DBDefinition.strDBMSName, DBDefinition.strDBMSVersion, Integer.toString( DBDefinition.Tables.size() ) ) );

								/*}
								else {
									
									Logger.logWarning( "-1", Lang.translate( "Database definition with the next properties. Name: [%s], DBMS_Name: [%s] and DBMS_Version: [%s] already exists in the list of databases. This properties Name, DBMS_Name and DBMS_Version must be unique for config section", DBDefinition.strName, DBDefinition.strDBMSName, DBDefinition.strDBMSVersion ) );
									Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
									
								}*/
								
							}
							else {
								
								Logger.logWarning( "-1", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
								Logger.logWarning( "-1", Lang.translate( "No tables definitions for the database. Name: [%s], DBMS_Name: [%s], DBMS_Version: [%s]. Ignoring section", DBDefinition.strName, DBDefinition.strDBMSName, DBDefinition.strDBMSVersion ) );
								Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
								
							}
							
							DBDefinition = null;
							
						}
						
						intDatabaseSectionNode--;
						
					}
					else if ( DBDefinition != null && intDatabaseSectionNode == 1 ) {
					
						if ( qName.equals( ConfigXMLTagsDBDefinitions._Constants ) ) {

							intConstantsSectionNode--;

						}
						else if ( qName.equals( ConfigXMLTagsDBDefinitions._Constant ) ) {

							intConstantNode--;
							
							if ( intConstantNode == 0 )
								strConstantName = "";

						}	
						else if ( qName.equals( ConfigXMLTagsDBDefinitions._Table ) ) {

							if ( DBTableDefinition != null && intTableSectionNode == 1 ) {

								if ( DBTableDefinition.CheckFields.size() > 0 ) {

									//if ( DBDefinition.Tables.get( DBTableDefinition.strName.toLowerCase() ) == null ) {

									DBDefinition.Tables.put( DBTableDefinition.strName.toLowerCase(), DBTableDefinition );

									Logger.logMessage( "1", Lang.translate( "Added table definition [%s] for the database name: [%s]", DBTableDefinition.strName, DBDefinition.strName ) );
										
									/*}
									else {

										Logger.logWarning( "-1", Lang.translate( "Table definition name [%s] already exists in the list of tables. This name must be unique for database definition section", DBTableDefinition.strName ) );
										Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );

									}*/

								}

								DBTableDefinition = null;

							}

							intTableSectionNode--;

						}
						else if ( DBTableDefinition != null && intTableSectionNode == 1 ) {

							if ( qName.equals( ConfigXMLTagsDBDefinitions._CreationDefinitions ) ) {
								
								intCreateDefinitionSectioNode--;
							
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._Command ) ) {

								intCommandNode--;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._AddFieldDefinition ) ) {

								intAddFieldDefinitionNode--;
								
							}	
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._AlterFieldDefinition ) ) {

								intAlterFieldDefinitionNode--;
								
							}	
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._CheckFields ) ) {
								
								intCheckFieldsSectionNode--;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._Field ) ) {
								
								if ( DBFieldDefinition != null && intFieldNode == 1 ) {
									
									//if ( DBTableDefinition.CheckFields.get( DBFieldDefinition.strName.toLowerCase() ) == null ) {

									DBTableDefinition.CheckFields.put( DBFieldDefinition.strName.toLowerCase(), DBFieldDefinition );

									Logger.logMessage( "1", Lang.translate( "Added field definition [%s] type [%s] for the table name: [%s]", DBFieldDefinition.strName, DBFieldDefinition.strType, DBTableDefinition.strName ) );
									
									/*	
									}
									else {

										Logger.logWarning( "-1", Lang.translate( "Field definition name [%s] already exists in the list of fields. This name must be unique for table definition section", DBFieldDefinition.strName ) );
										Logger.logWarning( "-1", Lang.translate( "Ignoring the node [%s]", qName ) );

									}
									*/
										
									DBFieldDefinition = null;
									
								}
								
								intFieldNode--;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._CheckData ) ) {
								
								intCheckDataSectionNode--;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._Data ) ) {
								
								if ( DBDataDefinition != null && intDataSectionNode == 1 ) {
									
									if ( DBDataDefinition.strCommand.isEmpty() == false  ) {
										
										//if ( DBTableDefinition.CheckData.get( DBDataDefinition.strID.toLowerCase() ) == null ) {
										
										if ( ( DBDataDefinition.strCondition.isEmpty() == true && DBDataDefinition.intType > 0 ) ) {

											//Error must be intType = 0 (None) for the condition must be empty
											Logger.logError( "-1001", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
											Logger.logError( "-1001", Lang.translate( "Data definition ID [%s] and Type [%s] must have a condition", DBDataDefinition.strID, DBDataDefinition.intType==1?"where":"full" ) );
											Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );

										}
										else {

											//add
											DBTableDefinition.CheckData.put( DBDataDefinition.strID.toLowerCase(), DBDataDefinition );
											Logger.logMessage( "1", Lang.translate( "Added data definition ID [%s] for the table name: [%s]", DBDataDefinition.strID, DBTableDefinition.strName ) );

										}

										/*
										}
										else {

											Logger.logWarning( "-1", Lang.translate( "Data definition ID [%s] already exists in the list of data. This ID must be unique for table definition section", DBDataDefinition.strID ) );
											Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );

										}
										*/
										
									}
									else {
									
										//Error must have one command
										Logger.logError( "-1002", Lang.translate( "Line: [%s], Column: [%s]", Integer.toString( intDocumentLine ), Integer.toString( intDocumentColumn ) ) );
										Logger.logError( "-1002", Lang.translate( "Data definition ID [%s] must have a command", DBDataDefinition.strID ) );
										Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
										
									}
									
									DBDataDefinition = null;
									
								}
								
								intDataSectionNode--;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._Condition ) ) {

								intConditionNode--;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._MapsNames ) ) {
								
								intMapsNames--;
								
							}
							else if ( qName.equals( ConfigXMLTagsDBDefinitions._Map ) ) {
								
								intMap--;
								
							}
							
						}	
					
					}
					
				}

	            @Override
				public void characters( char ch[], int start, int length ) throws SAXException {

					if ( DBDefinition != null && intDatabaseSectionNode == 1 ) { 
						
						String strValue = new String( ch, start, length );
						
						strValue = strValue.trim();

						if ( intConstantsSectionNode == 1 && intConstantNode == 1 && strConstantName.isEmpty() == false ) {

							DBDefinition.Constants.put( strConstantName, strValue );
							
						}
						else if ( DBTableDefinition != null && intTableSectionNode == 1 ) {
							
							if ( intCreateDefinitionSectioNode == 1 && intCommandNode == 1 ) {
								
								DBTableDefinition.CreationDefinitions.add( strValue );
								
							}
							else if ( intAddFieldDefinitionNode == 1 ) {
								
								DBTableDefinition.strAddFieldDefinition = strValue;
								
							}
							else if ( intAlterFieldDefinitionNode == 1 ) {
								
								DBTableDefinition.strAlterFieldDefinition = strValue;
								
							}
							else if ( DBDataDefinition != null && intCheckDataSectionNode == 1 && intDataSectionNode == 1 ) {
								
								if ( intConditionNode == 1 && DBDataDefinition.intType != 0 ) //No condition need
								    DBDataDefinition.strCondition = strValue;
								else if ( intCommandNode == 1 )
									DBDataDefinition.strCommand.add( strValue );
								
							}
							else if ( intMapsNames == 1 && intMap == 1 ) {
								
								DBTableDefinition.MapsNames.add( strValue );
								
							} 
							
						}
						
					}

				}
				
			};
			
			saxParser.parse( strDBDefinitionsFilePath, XMLParserHandler );
			
			Logger.logMessage( "1", Lang.translate( "Count of DBDefinitions registered: [%s]", Integer.toString( DBDefinitions.size() ) ) );
			
			bResult = true;
			
		} 
		catch ( Exception Ex ) {
			
			if ( Logger != null )
				Logger.logException( "-1010", Ex.getMessage(), Ex );
			
		}		
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "loadDBDefinitionsFromFile", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
	
	public LinkedHashMap<String,String> getExtraVars( String strTableNameValue, String strFieldNameValue, String strFieldTypeValue, String strFieldNotNullValue, String strFieldCommentValue, String strDateFormat, String strTimeFormat, String strDateTimeFormat, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();

		LinkedHashMap<String,String> Result = null;
		
		try {
		
			Result = new LinkedHashMap<String,String>();

			if ( strTableNameValue != null )
				Result.put( "@@table_name@@", strTableNameValue );

			if ( strFieldNameValue != null )
				Result.put( "@@field_name@@", strFieldNameValue );

			if ( strFieldTypeValue != null )
				Result.put( "@@field_type@@", strFieldTypeValue );

			if ( strFieldNotNullValue != null )
				Result.put( "@@field_not_null@@", strFieldNotNullValue );

			if ( strFieldCommentValue != null )
				Result.put( "@@field_comment@@", strFieldCommentValue );

			if ( strDateFormat == null || strDateFormat.isEmpty() )
				strDateFormat = "MM-dd-yyyy"; // Worried about this, every SMBD had own format and can get problems

			if ( strTimeFormat == null || strTimeFormat.isEmpty() )
				strTimeFormat = "HH:mm:ss"; 

			if ( strDateTimeFormat == null || strDateTimeFormat.isEmpty() )
				strDateTimeFormat = "MM-dd-yyyy HH:mm:ss"; 

			DateFormat DFormat = new SimpleDateFormat( strDateFormat ); 
			DateFormat TFormat = new SimpleDateFormat( strTimeFormat );
			DateFormat DTFormat = new SimpleDateFormat( strDateTimeFormat );

			Date CurrentDateTime = new Date();

			Result.put( "@@cur_date@@", DFormat.format( CurrentDateTime ) );
			Result.put( "@@cur_time@@", TFormat.format( CurrentDateTime ) );
			Result.put( "@@cur_date_time@@", DTFormat.format( CurrentDateTime ) );
		
		}
		catch ( Exception Ex ) {
			
			if ( Logger != null )
				Logger.logException( "-1010", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		if ( Logger != null )
			Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "loadDBDefinitionsFromFile", Long.toString( lngEnd - lngStart ) ) );
		
		return Result;
		
	}
	
	protected class CFunctionCall {
		
		public String strOriginalCall;
		public String strFunctionName;
		public ArrayList<String> strFunctionParams;
		public String strResultValue;
		
		public CFunctionCall( String strOriginalCall, String strFunctionName, ArrayList<String> strFunctionParams, String strDefaultResultValue )  {
			
			this.strOriginalCall = strOriginalCall;
			this.strFunctionName = strFunctionName;
			this.strFunctionParams = strFunctionParams;
			this.strResultValue = strDefaultResultValue;
			
		}
		
	}

	public ArrayList<CFunctionCall> getFunctionsCalls( ArrayList<String> strFunctionsNames, ArrayList<String> strCallsExpressions, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		ArrayList<CFunctionCall> Result = new ArrayList<CFunctionCall>();
		
		for ( String strCall : strCallsExpressions ) {
			
			for ( String strFunctionName: strFunctionsNames ) {
				
				if ( strCall.toLowerCase().startsWith( strFunctionName.toLowerCase() ) ) {
					
					ArrayList<String> strFunctionParams = net.maindataservices.Utilities.extractTokens( "(", ")", strCall );
					
					strFunctionParams = new ArrayList<String>( Arrays.asList( strFunctionParams.get( 0 ).split( "," ) ) );
					
					CFunctionCall FunctionCall = new CFunctionCall( strCall ,strFunctionName, strFunctionParams, "" );
					
					Result.add( FunctionCall );
					
				}				
				
			}
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		if ( Logger != null )
			Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "loadDBDefinitionsFromFile", Long.toString( lngEnd - lngStart ) ) );
		
		return Result;
		
	}
	
	public String replaceKeywords( String strKeywordsToReplace, LinkedHashMap<String,String> DBDefinitionConstants, ArrayList<String> DBTableMapsNames, Properties DBDefinitionKeywordsMaps, LinkedHashMap<String,String> ExtraVars, String strResultFilePathData, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		try {
		
			for ( Entry<String,String> DBConstant: DBDefinitionConstants.entrySet() ) {

				strKeywordsToReplace = strKeywordsToReplace.replace( DBConstant.getKey(), DBConstant.getValue() );			

			}

			for ( String strMapNameKey : DBTableMapsNames ) {

				String strMapValue = DBDefinitionKeywordsMaps.getProperty( strMapNameKey );

				if ( strMapValue != null )
					strKeywordsToReplace = strKeywordsToReplace.replace( strMapNameKey, strMapValue );			

			}
			
			if ( ExtraVars != null ) {
				
				for ( Entry<String,String> ExtraVar: ExtraVars.entrySet() ) {

					strKeywordsToReplace = strKeywordsToReplace.replace( ExtraVar.getKey(), ExtraVar.getValue() );			

				}
				
			}
			
			if ( strResultFilePathData != null && strResultFilePathData.isEmpty() == false ) {

				ArrayList<String> strCallsExpressions = net.maindataservices.Utilities.extractTokens( "${", "}$", strKeywordsToReplace );
				
				if ( strCallsExpressions.size() > 0 ) {
					
					ArrayList<String> strFunctionsNames = new ArrayList<String>();
					strFunctionsNames.add( "Condition.Result.Value" );
					
					ArrayList<CFunctionCall> FunctionsCallsList = this.getFunctionsCalls( strFunctionsNames, strCallsExpressions, Logger, Lang );
					
					CWebRowSetImpl WebRSRead = new CWebRowSetImpl();
					
	    			java.io.FileInputStream iStream = new java.io.FileInputStream( strResultFilePathData );
	    			BufferedReader br = new BufferedReader( new InputStreamReader( iStream ) );

	    			int intPageSize = 1000;
	    			int intPagePos = 1;
	    			
	    			WebRSRead.setPageSize( intPageSize );  //Set the number of rows to be loads into memory
	    			
	    			WebRSRead.initReadXML( br ); //Create the fields/columns metada only, no rows loaded!!! 
	    			
	    			WebRSRead.readXMLBody( br ); //Read the first page
					
					for ( CFunctionCall FunctionCall : FunctionsCallsList ) {
						
						if ( FunctionCall.strFunctionName.equals( "Condition.Result.Value" ) ) {
							
							int intRowNumber = net.maindataservices.Utilities.strToInteger( FunctionCall.strFunctionParams.get( 0 ), Logger );
							String strFieldName = FunctionCall.strFunctionParams.get( 1 );
							
							if ( intRowNumber > ( intPagePos * intPageSize ) ) {
								
								while ( WebRSRead.nextPage() ) {

									intPagePos += 1;
									
									if ( intRowNumber <= ( intPagePos * intPageSize ) ) break;

								}
								
							}
							else if ( intRowNumber < ( intPagePos * intPageSize ) - intPageSize && intRowNumber > 0 ) {
								
								while ( WebRSRead.previousPage() ) {

									intPagePos -= 1;
									
									if ( intRowNumber > ( intPagePos * intPageSize ) - intPageSize ) break;

								}
								
							}
							
			    			int intRowPos = intRowNumber - ( ( intPagePos * intPageSize ) - intPageSize );
							int intRowCount = 0;
			    			
							WebRSRead.beforeFirst();
			    			//WebRSRead.absolute( intRowPos );
			    			
			    			while ( WebRSRead.next() ) {
			    			
			    				intRowCount += 1;
			    				
			    				if ( intRowCount == intRowPos ) {
			    				
			    					FunctionCall.strResultValue = WebRSRead.getString( strFieldName );
			    				
			    					break;
			    					
			    				}	
							
			    			}
							
							strKeywordsToReplace = strKeywordsToReplace.replace( "${" + FunctionCall.strOriginalCall + "}$", FunctionCall.strResultValue );			
							
						}
						
					}
					
					WebRSRead.close();
					
					iStream.close();
					
				}
				
			}
		
		}
		catch ( Exception Ex ) {
			
			if ( Logger != null )
				Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		if ( Logger != null )
			Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "replaceKeywords", Long.toString( lngEnd - lngStart ) ) );
		
		return strKeywordsToReplace;
		
	}
	
	public String startSessionOnDatabase( CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		String strSecurityTokenID = "";

		try {
		
			Logger.logMessage( "1", Lang.translate( "Try to start session in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend() ) );

			ArrayList<LinkedHashMap<String,String>> Result = BackendServicesManager.callServiceSystemStartSession( HTTPClient, ConfigDBConnection, Logger, Lang );

			if ( Result != null && Result.size() > 0 && Result.get( 0 ) != null ) {

				strSecurityTokenID = Result.get( 0 ).get( "SecurityTokenID" );

				if ( strSecurityTokenID != null && strSecurityTokenID.isEmpty() == false ) {

					Logger.logMessage( "1", Lang.translate( "Start session successful in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );

				}
				else {

					Logger.logError( "-1002", Lang.translate( "Fail to start session in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]. SecurityTokenID is empty or null", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend() ) );

				}

			}
			else {

				Logger.logError( "-1001", Lang.translate( "Fail start session in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]. Result is empty or null", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend() ) );

			}
			
		}
		catch ( Exception Ex ) {
			
			Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "startSessionOnDatabase", Long.toString( lngEnd - lngStart ) ) );
		
		return strSecurityTokenID;
		
	}

	public boolean endSessionOnDatabase( CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;

		try {

			String strURL = ConfigDBConnection.getURLBackend();
			
			Logger.logMessage( "1", Lang.translate( "Try to end session in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, strURL ) );
			
			ArrayList<LinkedHashMap<String,String>> Result = BackendServicesManager.callServiceSystemEndSession( HTTPClient, strURL, ConfigDBConnection.getConfigProxy(), strSecurityTokenID, Logger, Lang );    				

			if ( Result != null && Result.size() > 0 && Result.get( 0 ) != null ) {

				String strCode = Result.get( 0 ).get( "Code" );

				Logger.logMessage( "1", Lang.translate( "Code response value is: [%s]", strCode ) );
				
				if ( net.maindataservices.Utilities.strToInteger( strCode, Logger ) > 0 ) {

					bResult = true;
					
					Logger.logMessage( "1", Lang.translate( "Successful end session in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.strSelectedBackendURL, strSecurityTokenID ) );

					BackendServicesManager.removeTransactionsFromPool( strSecurityTokenID, Logger, Lang ); //Remove all pool transactions for this Connection
					
				}
				else {
					
					Logger.logError( "-1002", Lang.translate( "Fail to end session in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.strSelectedBackendURL, strSecurityTokenID ) );
					
				}

			}
			else {
				
				Logger.logError( "-1001", Lang.translate( "Fail to end session in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.strSelectedBackendURL, strSecurityTokenID ) );
				
			}

		}
		catch ( Exception Ex ) {

			Logger.logException( "-1025", Ex.getMessage(), Ex );

		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "endSessionOnDatabase", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
	
	public String startTransactionOnDatabase( CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		String strTransactionID = "";
		
		try {

			strTransactionID = BackendServicesManager.getTransactionFromPool( strSecurityTokenID, true, Logger, Lang );
			
			if ( strTransactionID.isEmpty() ) {
			
				String strURL = ConfigDBConnection.getURLBackend();

				Logger.logMessage( "1", Lang.translate( "Trying to start transaction in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, strURL ) );

				ArrayList<LinkedHashMap<String,String>> Result = BackendServicesManager.callServiceSystemStartTransaction( HTTPClient, strURL, ConfigDBConnection.getConfigProxy(), strSecurityTokenID, Logger, Lang );    				

				if ( Result != null && Result.size() > 0 && Result.get( 0 ) != null ) {

					strTransactionID = Result.get( 0 ).get( "TransactionID" );

					if ( strTransactionID != null && strTransactionID.isEmpty() == false ) {

						BackendServicesManager.addTransactionToPool( strSecurityTokenID, strTransactionID, true, Logger, Lang ); //Add this transaction to pool associated to connection SecurityTokenID
						
						Logger.logMessage( "1", Lang.translate( "Successful start transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );

					}
					else {

						Logger.logError( "-1002", Lang.translate( "Fail to start transaction in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );

					}

				}
				else {

					Logger.logError( "-1001", Lang.translate( "Fail to start transaction in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );

				}

			}
			
		}
		catch ( Exception Ex ) {

			Logger.logException( "-1025", Ex.getMessage(), Ex );

		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "startTransactionOnDatabase", Long.toString( lngEnd - lngStart ) ) );
		
		return strTransactionID;
		
	}
	
	public boolean commitTransactionOnDatabase( CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, String strTransactionID, boolean bMarkInUse, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;

		try {

			String strURL = ConfigDBConnection.getURLBackend();
			
			Logger.logMessage( "1", Lang.translate( "Trying to commit transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, strURL ) );
			
			ArrayList<LinkedHashMap<String,String>> Result = BackendServicesManager.callServiceSystemCommitTransaction( HTTPClient, strURL, ConfigDBConnection.getConfigProxy(), strSecurityTokenID, strTransactionID, Logger, Lang );    				

			if ( Result != null && Result.size() > 0 && Result.get( 0 ) != null ) {

				String strCode = Result.get( 0 ).get( "Code" );

				Logger.logMessage( "1", Lang.translate( "Code response value is: [%s]", strCode ) );
				
				if ( net.maindataservices.Utilities.strToInteger( strCode, Logger ) > 0 ) {

					bResult = true;
					
					Logger.logMessage( "1", Lang.translate( "Successful commit transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );

					BackendServicesManager.markTransactionOnPool(strSecurityTokenID, strTransactionID, bMarkInUse, Logger, Lang ); //Mark on disuse on pool or continue using
					
				}
				else {
					
					Logger.logError( "-1002", Lang.translate( "Fail to commit transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );
					
				}

			}
			else {
				
				Logger.logError( "-1001", Lang.translate( "Fail to commit transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );
				
			}

		}
		catch ( Exception Ex ) {

			Logger.logException( "-1025", Ex.getMessage(), Ex );

		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "commitTransactionOnDatabase", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
	
	public boolean rollbackTransactionOnDatabase( CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, String strTransactionID, boolean bMarkInUse, CExtendedLogger Logger, CLanguage Lang ) {

		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;

		try {

			String strURL = ConfigDBConnection.getURLBackend();
			
			Logger.logMessage( "1", Lang.translate( "Trying to rollback transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, strURL ) );
			
			ArrayList<LinkedHashMap<String,String>> Result = BackendServicesManager.callServiceSystemRollbackTransaction( HTTPClient, strURL, ConfigDBConnection.getConfigProxy(), strSecurityTokenID, strTransactionID, Logger, Lang );    				

			if ( Result != null && Result.size() > 0 && Result.get( 0 ) != null ) {

				String strCode = Result.get( 0 ).get( "Code" );

				Logger.logMessage( "1", Lang.translate( "Code response value is: [%s]", strCode ) );
				
				if ( net.maindataservices.Utilities.strToInteger( strCode, Logger ) > 0 ) {

					bResult = true;
					
					Logger.logMessage( "1", Lang.translate( "Successful rollback transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );

					BackendServicesManager.markTransactionOnPool(strSecurityTokenID, strTransactionID, bMarkInUse, Logger, Lang ); //Mark on disuse on pool or continue using
					
				}
				else {
					
					Logger.logError( "-1002", Lang.translate( "Fail to rollback transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );
					
				}

			}
			else {
				
				Logger.logError( "-1001", Lang.translate( "Fail to rollback transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );
				
			}

		}
		catch ( Exception Ex ) {

			Logger.logException( "-1025", Ex.getMessage(), Ex );

		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "rollbackTransactionOnDatabase", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
	
	public boolean endTransactionOnDatabase( CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, String strTransactionID, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;

		try {

			String strURL = ConfigDBConnection.getURLBackend(); 
			
			Logger.logMessage( "1", Lang.translate( "Trying to end transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, strURL ) );
			
			ArrayList<LinkedHashMap<String,String>> Result = BackendServicesManager.callServiceSystemEndTransaction( HTTPClient, strURL, ConfigDBConnection.getConfigProxy(), strSecurityTokenID, strTransactionID, Logger, Lang );    				

			if ( Result != null && Result.size() > 0 && Result.get( 0 ) != null ) {

				String strCode = Result.get( 0 ).get( "Code" );

				Logger.logMessage( "1", Lang.translate( "Code response value is: [%s]", strCode ) );
				
				if ( net.maindataservices.Utilities.strToInteger( strCode, Logger ) > 0 ) {

					bResult = true;
					
					Logger.logMessage( "1", Lang.translate( "Successful end transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );
					Logger.logWarning( "-1", Lang.translate( "The transaction [%s] is not valid. Cannot used more", strTransactionID ) );

					BackendServicesManager.removeTransactionFromPool( strSecurityTokenID, strTransactionID, Logger, Lang ); //Remove
					
				}
				else {
					
					Logger.logError( "-1002", Lang.translate( "Fail to end transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );
					
				}

			}
			else {
				
				Logger.logError( "-1001", Lang.translate( "Fail to end transaction [%s] in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", strTransactionID, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );
				
			}

		}
		catch ( Exception Ex ) {

			Logger.logException( "-1025", Ex.getMessage(), Ex );

		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "endTransactionOnDatabase", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
	
	public LinkedHashMap<String,String> getDatabaseFeaturesInfo( CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, ArrayList<String> DBFeaturesInfoToGet, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		LinkedHashMap<String,String> Result = new LinkedHashMap<String,String>();
		
		try {
		
			String strURL = ConfigDBConnection.getURLBackend();
			
			Logger.logMessage( "1", Lang.translate( "Trying to get info from database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, strURL ) );
			
			ArrayList<LinkedHashMap<String,String>> DBFeaturesList = BackendServicesManager.callServiceSystemDatabaseInfo( HTTPClient, strURL, ConfigDBConnection.getConfigProxy(), strSecurityTokenID, Logger, Lang );

			if ( DBFeaturesList != null && DBFeaturesList.size() > 0 && DBFeaturesList.get( 0 ) != null ) {

                for ( LinkedHashMap<String,String> DBFeatureInfo: DBFeaturesList ) {
                	
                	String strFeatureName = DBFeatureInfo.get( "Feature" );
                	String strFeatureValue = DBFeatureInfo.get( "Value" );
                		
                	if ( DBFeaturesInfoToGet != null ) {

                		for ( String strDBFeaturesInfoToGet: DBFeaturesInfoToGet ) {

                			if ( strFeatureName.equals( strDBFeaturesInfoToGet )  ) {

                				Result.put( strFeatureName, strFeatureValue );

                				Logger.logMessage( "1", Lang.translate( "Database feature found and added. Feature: [%s], Value: [%s]", strFeatureName, strFeatureValue ) );

                			}

                		}

                	}
                	else if ( strFeatureName.isEmpty() == false && strFeatureValue.isEmpty() == false ) {

                		Result.put( strFeatureName, strFeatureValue );

                		Logger.logMessage( "1", Lang.translate( "Database feature added. Feature: [%s], Value: [%s]", strFeatureName, strFeatureValue ) );

                	}
                	
                }  

			}	
			else {

				Logger.logError( "-1001", Lang.translate( "Cannot get info from database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );

			}
		
		}
		catch ( Exception Ex ) {
			
			Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "getDatabaseFeaturesInfo", Long.toString( lngEnd - lngStart ) ) );
		
		return Result;
		
	}
	
	public boolean checkTableExists( String strTableName, CDBDefinition DBDefinition, CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, CExtendedLogger Logger, CLanguage Lang ) {

		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;
		
		try {
			
			String strURL = ConfigDBConnection.getURLBackend();
			
			Logger.logMessage( "1", Lang.translate( "Trying to check if table [%s] exists on database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", strTableName, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, strURL ) );
			
			ArrayList<LinkedHashMap<String,String>> Result = BackendServicesManager.callServiceSystemObjectExists( HTTPClient, strURL, ConfigDBConnection.getConfigProxy(), strSecurityTokenID, "1", strTableName, Logger, Lang );

			if ( Result != null && Result.size() > 0 && Result.get( 0 ) != null ) {

				String strCode = Result.get( 0 ).get( "Code" );

				Logger.logMessage( "1", Lang.translate( "Code response value is: [%s]", strCode ) );
				
				if ( net.maindataservices.Utilities.strToInteger( strCode, Logger ) == 1000 ) {

					bResult = true;
					Logger.logMessage( "1", Lang.translate( "Table [%s] exists in Database [%s] struct", strTableName, ConfigDBConnection.strDatabase ) );
					
				}
				else {
					
					Logger.logWarning( "-1", Lang.translate( "Table [%s] not exits in Database [%s] struct", strTableName, ConfigDBConnection.strDatabase ) );
					
				}
				
			}
			else {

				Logger.logError( "-1001", Lang.translate( "Fail to check table exists [%s] for the Database [%s]. Result is empty or null", strTableName, ConfigDBConnection.strDatabase ) );

			}
			
		}
		catch ( Exception Ex ) {
			
			Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "checkTableExists", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
	
	public boolean createTable( CDBDefinition DBDefinition, CDBTableDefinition DBTableDefinitionClone, CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;
		
		String strTransactionID = "";
		
		try {
			
			String strURL = ConfigDBConnection.getURLBackend();
			
			Logger.logMessage( "1", Lang.translate( "Trying to create table [%s] on database [%s] with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, strURL ) );
			
			strTransactionID = this.startTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, Logger, Lang );
			
			if ( strTransactionID.isEmpty() == false ) {
				
				boolean bCreationCommandsSuccessful = true;
				
				for ( String strCreateDefinitionCommand : DBTableDefinitionClone.CreationDefinitions ) {
					
					if ( strCreateDefinitionCommand.isEmpty() == false ) {
						
						strCreateDefinitionCommand = this.replaceKeywords( strCreateDefinitionCommand, DBDefinition.Constants, DBTableDefinitionClone.MapsNames, ConfigDBConnection.KeywordsMaps, null, null, Logger, Lang );

						Logger.logData( "1", Lang.translate( "Trying to execute the creation command on database [%s]", ConfigDBConnection.strDatabase ), strCreateDefinitionCommand );
						
						ArrayList<LinkedHashMap<String, String>> Result = BackendServicesManager.callServiceSystemExecuteSQLParsed( HTTPClient, strURL, ConfigDBConnection.getConfigProxy(), strSecurityTokenID, strTransactionID, strCreateDefinitionCommand, null, Logger, Lang );
						
						if ( Result != null && Result.size() > 0 && Result.get( 0 ) != null ) {
							
							String strCode = Result.get( 0 ).get( "Code" );

							Logger.logMessage( "1", Lang.translate( "Code response value is: [%s]", strCode ) );
							
							if ( net.maindataservices.Utilities.strToInteger( strCode, Logger ) > 0 ) {

								Logger.logMessage( "1", Lang.translate( "Successful to execute the creation command on database [%s]", ConfigDBConnection.strDatabase ) );
							
							}
							else {
								
								Logger.logError( "-1003", Lang.translate( "Fail to execute the creation command on database [%s], all creation commands canceled", ConfigDBConnection.strDatabase ) );
								
								bCreationCommandsSuccessful = false;
								break;
								
							}
							
						}
						else {
							
							Logger.logError( "-1002", Lang.translate( "Fail to execute the creation command on database [%s], all creation commands canceled", ConfigDBConnection.strDatabase ) );

							bCreationCommandsSuccessful = false;
							break;

						}
						
					}
					
				}
				
				if ( bCreationCommandsSuccessful ) {
					
					if ( this.commitTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, false, Logger, Lang ) ) {
					
						Logger.logMessage( "1", Lang.translate( "Successful to create table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

						bResult = true;

					}
					else {
						
						Logger.logError( "-1004", Lang.translate( "Fail to commit the transaction to create table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );
						
					}	
					
				}
				else {
					
					Logger.logError( "-1005", Lang.translate( "Fail to create table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

					this.rollbackTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, true, Logger, Lang );
					
				}
				
			}
			else {
				
				Logger.logError( "-1001", Lang.translate( "No valid TransactionID for create table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );
				
			}
			
		}
		catch ( Exception Ex ) {
			
			if ( strTransactionID.isEmpty() == false ) {
				
				this.rollbackTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, true, Logger, Lang );
				
			}
			
			Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "createTable", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}

	public boolean alterFieldTypeAndLengthFromTable( CDBDefinition DBDefinition, CDBTableDefinition DBTableDefinitionClone, CDBFieldDefinition DBFieldDefinitionClone, boolean bAllowNull, CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, String strTransactionID, CExtendedLogger Logger, CLanguage Lang ) {

		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;
		
		try {
			
			String strURL = ConfigDBConnection.getURLBackend();
			
			Logger.logMessage( "1", Lang.translate( "Trying to alter type and length of field [%s] in table [%s] on database [%s] with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, strURL ) );
			
			if ( DBTableDefinitionClone.strAlterFieldDefinition.isEmpty() == false ) {
				
				String strNotNull = "NULL";
				
				if ( bAllowNull == false )
					strNotNull = "NOT NULL";
				
				String strType = DBFieldDefinitionClone.strType;
				
				if ( DBFieldDefinitionClone.intLength > 0 )
					strType = strType + "(" + Integer.toString( DBFieldDefinitionClone.intLength ) + ")";
				
				LinkedHashMap<String,String> ExtraVars = this.getExtraVars( DBTableDefinitionClone.strName, DBFieldDefinitionClone.strName, strType, strNotNull, DBFieldDefinitionClone.strComment, ConfigDBConnection.strDateFormat, ConfigDBConnection.strTimeFormat, ConfigDBConnection.strDateTimeFormat, Logger, Lang );
				
				String strAlterFieldDefinition = this.replaceKeywords( DBTableDefinitionClone.strAlterFieldDefinition.trim(), DBDefinition.Constants, DBTableDefinitionClone.MapsNames, ConfigDBConnection.KeywordsMaps, ExtraVars, null, Logger, Lang );

				//Logger.logMessage( "1", Lang.translate( "Trying to execute alter field definition for field [%s] in table [%s] on database [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, DBConnectionConfig.strDatabase ), strAlterFieldCommand );
				
				ArrayList<LinkedHashMap<String, String>> Result = BackendServicesManager.callServiceSystemExecuteSQLParsed( HTTPClient, strURL, ConfigDBConnection.getConfigProxy(), strSecurityTokenID, strTransactionID, strAlterFieldDefinition, null, Logger, Lang );
				
				if ( Result != null && Result.size() > 0 && Result.get( 0 ) != null ) {
					
					String strCode = Result.get( 0 ).get( "Code" );

					Logger.logMessage( "1", Lang.translate( "Code response value is: [%s]", strCode ) );
					
					if ( net.maindataservices.Utilities.strToInteger( strCode, Logger ) > 0 ) {

						Logger.logMessage( "1", Lang.translate( "Successful to execute the alter field definition for field [%s] in table [%s] on database [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );
					
						bResult = true;
						
					}
					else {
						
						Logger.logError( "-1002", Lang.translate( "Fail to execute the alter field definition for field [%s] in table [%s] on database [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );
						
					}
					
				}
				else {
					
					Logger.logError( "-1001", Lang.translate( "Fail to execute the alter field definition for field [%s] in table [%s] on database [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );
					
				}
				
				
			}
			else {
				
				Logger.logWarning( "-1", Lang.translate( "No alter field definition found for table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );
				
			}
			
		}
		catch ( Exception Ex ) {
			
			Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "alterFieldTypeAndLengthFromTable", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
	
	public boolean addFieldToTable( CDBDefinition DBDefinition, CDBTableDefinition DBTableDefinitionClone, CDBFieldDefinition DBFieldDefinitionClone, CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, String strTransactionID, CExtendedLogger Logger, CLanguage Lang ) {

		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;
		
		try {

			String strURL = ConfigDBConnection.getURLBackend();
			
			Logger.logMessage( "1", Lang.translate( "Trying to add field [%s] type [%s] and length [%s] in table [%s] on database [%s] with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", DBFieldDefinitionClone.strName, DBFieldDefinitionClone.strType, Integer.toString( DBFieldDefinitionClone.intLength ), DBTableDefinitionClone.strName, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, strURL ) );
			
			if ( DBTableDefinitionClone.strAddFieldDefinition.isEmpty() == false ) {
				
				String strType = DBFieldDefinitionClone.strType;
				
				if ( DBFieldDefinitionClone.intLength > 0 )
					strType = strType + "(" + Integer.toString( DBFieldDefinitionClone.intLength ) + ")";
				
				LinkedHashMap<String,String> ExtraVars = this.getExtraVars( DBTableDefinitionClone.strName, DBFieldDefinitionClone.strName, strType, "NULL", DBFieldDefinitionClone.strComment, ConfigDBConnection.strDateFormat, ConfigDBConnection.strTimeFormat, ConfigDBConnection.strDateTimeFormat, Logger, Lang );
				
				String strAddFieldDefinition = this.replaceKeywords( DBTableDefinitionClone.strAddFieldDefinition.trim(), DBDefinition.Constants, DBTableDefinitionClone.MapsNames, ConfigDBConnection.KeywordsMaps, ExtraVars, null, Logger, Lang );

				//Logger.logMessage( "1", Lang.translate( "Trying to execute alter field definition for field [%s] in table [%s] on database [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, DBConnectionConfig.strDatabase ), strAlterFieldCommand );
				
				ArrayList<LinkedHashMap<String, String>> Result = BackendServicesManager.callServiceSystemExecuteSQLParsed( HTTPClient, strURL, ConfigDBConnection.getConfigProxy(), strSecurityTokenID, strTransactionID, strAddFieldDefinition, null, Logger, Lang );
				
				if ( Result != null && Result.size() > 0 && Result.get( 0 ) != null ) {
					
					String strCode = Result.get( 0 ).get( "Code" );

					Logger.logMessage( "1", Lang.translate( "Code response value is: [%s]", strCode ) );
					
					if ( net.maindataservices.Utilities.strToInteger( strCode, Logger ) > 0 ) {

						Logger.logMessage( "1", Lang.translate( "Successful to execute the add field definition for field [%s] in table [%s] on database [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );
					
						bResult = true;
						
					}
					else {
						
						Logger.logError( "-1002", Lang.translate( "Fail to execute the add field definition for field [%s] in table [%s] on database [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );
						
					}
					
				}
				else {
					
					Logger.logError( "-1001", Lang.translate( "Fail to execute the add field definition for field [%s] in table [%s] on database [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );
					
				}
				
				
			}
			else {
				
				Logger.logWarning( "-1", Lang.translate( "No add field definition found for table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );
				
			}
			
		}
		catch ( Exception Ex ) {
			
			Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "addFieldToTable", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
	
	public boolean checkTableFieldNamesAndTypes( CDBDefinition DBDefinition, CDBTableDefinition DBTableDefinitionClone, CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;
		
		String strTransactionID = "";
		
		try {

			if ( DBTableDefinitionClone.bCheckFields ) {

				String strURL = ConfigDBConnection.getURLBackend();

				Logger.logMessage( "1", Lang.translate( "Trying to check field name and type of table [%s] on database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, strURL ) );

				strTransactionID = this.startTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, Logger, Lang );

				if ( strTransactionID.isEmpty() == false ) {

					Logger.logMessage( "1", Lang.translate( "Trying to get info of table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

					ArrayList<LinkedHashMap<String,String>> fieldsInTable = BackendServicesManager.callServiceSystemDescribeObject( HTTPClient, strURL, ConfigDBConnection.getConfigProxy(), strSecurityTokenID, "1", DBTableDefinitionClone.strName, Logger, Lang );

					if ( fieldsInTable != null && fieldsInTable.size() > 0 && fieldsInTable.get( 0 ) != null ) {

						boolean bCheckTableSuccessful = true;

						for ( Entry<String,CDBFieldDefinition> FieldToCheck: DBTableDefinitionClone.CheckFields.entrySet() ) {

							CDBFieldDefinition DBFieldDefinitionClone = new CDBFieldDefinition( FieldToCheck.getValue() );

							LinkedHashMap<String,String> ExtraVars = this.getExtraVars( null, null, null, null, null, ConfigDBConnection.strDateFormat, ConfigDBConnection.strTimeFormat, ConfigDBConnection.strDateTimeFormat, Logger, Lang );

							String strFieldNameInDefinition = this.replaceKeywords( DBFieldDefinitionClone.strName, DBDefinition.Constants, DBTableDefinitionClone.MapsNames, ConfigDBConnection.KeywordsMaps, ExtraVars, null, Logger, Lang );

							DBFieldDefinitionClone.strName = strFieldNameInDefinition;

							//System.out.println( strFieldNameInDefinition );
							LinkedHashMap<String,String> FieldInfo = null;
							String strFieldNameInTable = "";

							Logger.logMessage( "1", Lang.translate( "Searching field name [%s] in table [%s] on database [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

							for ( LinkedHashMap<String,String> FieldTempInfo : fieldsInTable ) {

								strFieldNameInTable = FieldTempInfo.get( "COLUMN_NAME" );

								if ( strFieldNameInTable.toLowerCase().equals( strFieldNameInDefinition.toLowerCase() ) ) {

									Logger.logMessage( "1", Lang.translate( "Field name [%s] found in table [%s] on database [%s]", strFieldNameInTable, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

									FieldInfo = FieldTempInfo;
									break;

								}

							}

							if ( FieldInfo != null ) { //FieldName found in database table check the type

								boolean bAllowNull = FieldInfo.get( "IS_NULLABLE" ).toLowerCase().equals( "yes" );
								String strFieldTypeInTable = FieldInfo.get( "TYPE_NAME" );
								int intFieldTypeSizeInTable = net.maindataservices.Utilities.strToInteger( FieldInfo.get( "COLUMN_SIZE" ), Logger );

								Logger.logMessage( "1", Lang.translate( "Checking field [%s] definition type [%s] and length [%s]", strFieldNameInDefinition, DBFieldDefinitionClone.strType, Integer.toString( DBFieldDefinitionClone.intLength ) ) );
								Logger.logMessage( "1", Lang.translate( "Checking field [%s] in table [%s] type [%s] and length [%s] on database [%s]", strFieldNameInTable, DBTableDefinitionClone.strName, strFieldTypeInTable, Integer.toString( intFieldTypeSizeInTable ), ConfigDBConnection.strDatabase ) );

								if ( strFieldTypeInTable.toLowerCase().equals( DBFieldDefinitionClone.strType.toLowerCase() ) && ( intFieldTypeSizeInTable >= DBFieldDefinitionClone.intLength ) ) {

									Logger.logMessage( "1", Lang.translate( "Field [%s] definition type [%s] and length [%s] match", strFieldNameInDefinition, DBFieldDefinitionClone.strType, Integer.toString( DBFieldDefinitionClone.intLength ) ) );

								}
								else {

									Logger.logWarning( "-1", Lang.translate( "Field [%s] definition type [%s] and length [%s] not match", strFieldNameInDefinition, DBFieldDefinitionClone.strType, Integer.toString( DBFieldDefinitionClone.intLength ) ) );

									Logger.logMessage( "1", Lang.translate( "Trying to change the type [%s] and length [%s] to type [%s] and length [%s]", strFieldTypeInTable, Integer.toString( intFieldTypeSizeInTable ), DBFieldDefinitionClone.strType, Integer.toString( DBFieldDefinitionClone.intLength ) ) );

									if ( DBTableDefinitionClone.bAlterFields ) {

										if ( DBFieldDefinitionClone.bAlter ) {
											
											if ( this.alterFieldTypeAndLengthFromTable( DBDefinition, DBTableDefinitionClone, DBFieldDefinitionClone, bAllowNull, BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, Logger, Lang ) ) {

												Logger.logMessage( "1", Lang.translate( "Successful alter field [%s] in table [%s] on database [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

											}
											else {

												bCheckTableSuccessful = false;

											}
										
										}
										else {
											
											Logger.logWarning( "-1", Lang.translate( "The configuration had disabled alter field [%s] for table [%s], [Alter] attribute equal to false", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName ) );
											
										}

									}
									else {

										Logger.logWarning( "-1", Lang.translate( "The configuration had disabled alter fields for table [%s], [Alter_Fields] attribute equal to false", DBTableDefinitionClone.strName ) );

									}

								}

							}
							else { //FieldName not found in database table procedure to add to table

								if ( DBTableDefinitionClone.bAddFields ) {

									if ( DBFieldDefinitionClone.bAdd ) {
									
										if ( this.addFieldToTable( DBDefinition, DBTableDefinitionClone, DBFieldDefinitionClone, BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, Logger, Lang ) ) {

											Logger.logMessage( "1", Lang.translate( "Successful add field [%s] in table [%s] on database [%s]", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

										}
										else {

											bCheckTableSuccessful = false;

										}
									
									}
									else {
										
										Logger.logWarning( "-1", Lang.translate( "The configuration had disabled add field [%s] for table [%s], [Add] attribute equal to false", DBFieldDefinitionClone.strName, DBTableDefinitionClone.strName ) );
										
									}

								}
								else {
									
									Logger.logWarning( "-1", Lang.translate( "The configuration had disabled add fields for table [%s], [Add_Fields] attribute equal to false", DBTableDefinitionClone.strName ) );
									
								}
								
							}

						}

						if ( bCheckTableSuccessful ) {

							if ( this.commitTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, false, Logger, Lang ) ) {

								Logger.logMessage( "1", Lang.translate( "Successful check field names and types of table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

								bResult = true;

							}
							else {

								Logger.logError( "-1004", Lang.translate( "Fail to commit the transaction to check field name and type of table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

							}

						}
						else {

							Logger.logError( "-1003", Lang.translate( "Fail check field name and type of table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

							this.rollbackTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, true, Logger, Lang );

						}

					}
					else {

						Logger.logError( "-1002", Lang.translate( "Fail get info of table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

						this.rollbackTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, true, Logger, Lang );

					}

				}
				else {

					Logger.logError( "-1001", Lang.translate( "No valid TransactionID for create table [%s] on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

				}

			}
			else {
				
				Logger.logWarning( "-1", Lang.translate( "The configuration had disabled the verification of field names and types for table [%s], [Check_Fields] attribute equal to false", DBTableDefinitionClone.strName ) );
				
				bResult = true;
				
			}
			
		}
		catch ( Exception Ex ) {
			
			if ( strTransactionID.isEmpty() == false ) {
				
				this.rollbackTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, true, Logger, Lang );
				
			}
			
			Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "checkTableFieldNamesAndTypes", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
	
	public boolean checkTableData( int intEvent, CDBDefinition DBDefinition, CDBTableDefinition DBTableDefinitionClone, CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;
		
		String strTransactionID = "";
		
		try {
			
			if ( DBTableDefinitionClone.bCheckData ) {
			
				Logger.logMessage( "1", Lang.translate( "Trying to initiate table [%s] data on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

				strTransactionID = this.startTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, Logger, Lang );

				if ( strTransactionID.isEmpty() == false ) {

					//boolean bInitializationTableDataSuccessful = true;

					//if ( bInitializationTableDataSuccessful ) {

					for ( Entry<String,CDBDataDefinition> DataToCheck: DBTableDefinitionClone.CheckData.entrySet() ) {

						CDBDataDefinition DBDataDefinition = DataToCheck.getValue();

						if ( DBDataDefinition.intEvent == intEvent ) {

							if ( DBDataDefinition.intType == 0 ) { //None

								LinkedHashMap<String,String> ExtraVars = this.getExtraVars( DBTableDefinitionClone.strName, null, null, null, null, ConfigDBConnection.strDateFormat, ConfigDBConnection.strTimeFormat, ConfigDBConnection.strDateTimeFormat, Logger, Lang );

								for ( String strCommand : DBDataDefinition.strCommand ) {

									if ( strCommand.trim().isEmpty() == false ) {

										strCommand = this.replaceKeywords( strCommand.trim(), DBDefinition.Constants, DBTableDefinitionClone.MapsNames, ConfigDBConnection.KeywordsMaps, ExtraVars, null, Logger, Lang );

										Logger.logMessage( "1", Lang.translate( "Trying to execute the command [%s] in table [%s] on database [%s]", strCommand, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

										BackendServicesManager.callServiceSystemExecuteSQLResponseToFile( HTTPClient, ConfigDBConnection.getURLBackend(), ConfigDBConnection.getConfigProxy(), strSecurityTokenID, strTransactionID, strCommand, null, Logger, Lang );

									}

								}

							}
							else if ( DBDataDefinition.intType == 1  || DBDataDefinition.intType == 2 ) { //Where Or Full

								LinkedHashMap<String,String> ExtraVars = this.getExtraVars( DBTableDefinitionClone.strName, null, null, null, null, ConfigDBConnection.strDateFormat, ConfigDBConnection.strTimeFormat, ConfigDBConnection.strDateTimeFormat, Logger, Lang );

								String strCondition = this.replaceKeywords( DBDataDefinition.strCondition.trim(), DBDefinition.Constants, DBTableDefinitionClone.MapsNames, ConfigDBConnection.KeywordsMaps, ExtraVars, null, Logger, Lang );

								String strResponseFilePathName = "";

								if ( DBDataDefinition.intType == 1 ) //Where
									strResponseFilePathName = BackendServicesManager.callServiceSystemExecuteSQLResponseToFile( HTTPClient, ConfigDBConnection.getURLBackend(), ConfigDBConnection.getConfigProxy(), strSecurityTokenID, strTransactionID, "Select * From " + DBTableDefinitionClone.strName + " Where " + strCondition, null, Logger, Lang );
								else if ( DBDataDefinition.intType == 2 ) //Full
									strResponseFilePathName = BackendServicesManager.callServiceSystemExecuteSQLResponseToFile( HTTPClient, ConfigDBConnection.getURLBackend(), ConfigDBConnection.getConfigProxy(), strSecurityTokenID, strTransactionID, strCondition, null, Logger, Lang );

								boolean bIsResponseWithRows = BackendServicesManager.isResponseWithRows( strResponseFilePathName, Logger, Lang ); 

								if ( ( bIsResponseWithRows == true && DBDataDefinition.intAction == 1 ) || ( bIsResponseWithRows == false && DBDataDefinition.intAction == 0 ) ) {

									for ( String strCommand : DBDataDefinition.strCommand ) {

										if ( strCommand.trim().isEmpty() == false ) {

											strCommand = this.replaceKeywords( strCommand.trim(), DBDefinition.Constants, DBTableDefinitionClone.MapsNames, ConfigDBConnection.KeywordsMaps, ExtraVars, bIsResponseWithRows==true?strResponseFilePathName:null, Logger, Lang );

											Logger.logMessage( "1", Lang.translate( "Trying to execute the command [%s] in table [%s] on database [%s]", strCommand, DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

											BackendServicesManager.callServiceSystemExecuteSQLResponseToFile( HTTPClient, ConfigDBConnection.getURLBackend(), ConfigDBConnection.getConfigProxy(), strSecurityTokenID, strTransactionID, strCommand, null, Logger, Lang );

										}

									}

								}

							}

						}

					}

					if ( this.commitTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, false, Logger, Lang ) ) {

						Logger.logMessage( "1", Lang.translate( "Successful initialization table [%s] data on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

						bResult = true;

					}
					else {

						Logger.logError( "-1003", Lang.translate( "Fail to commit the transaction to initialize table [%s] data on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

					}

					/*}
					else {

						Logger.logError( "-1002", Lang.translate( "Fail to initialize table [%s] data on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

						this.rollbackTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, true, Logger, Lang );

					}*/

				}
				else {

					Logger.logError( "-1001", Lang.translate( "No valid TransactionID for initialize table [%s] data on database [%s]", DBTableDefinitionClone.strName, ConfigDBConnection.strDatabase ) );

				}
			
			}
			else {
				
				Logger.logWarning( "-1", Lang.translate( "The configuration had disabled the verification of data for table [%s], [Check_Data] attribute equal to false", DBTableDefinitionClone.strName ) );
				
				bResult = true;
				
			}
			
		}
		catch ( Exception Ex ) {
			
			if ( strTransactionID.isEmpty() == false ) {
				
				this.rollbackTransactionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, strTransactionID, false, Logger, Lang );
				
			}
			
			Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "checkTableData", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
		
	public boolean initiationOnDatabaseTables( CDBDefinition DBDefinition, CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, String strSecurityTokenID, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();

		boolean bResult = false;

		try {

			Logger.logMessage( "1", Lang.translate( "Start of initiation tables on database [%s]", ConfigDBConnection.strDatabase ) );

			for ( Entry<String,CDBTableDefinition> Table: DBDefinition.Tables.entrySet() ) {

				CDBTableDefinition DBTableDefinition = Table.getValue();
				String strTableName = this.replaceKeywords( DBTableDefinition.strName, DBDefinition.Constants, DBTableDefinition.MapsNames, ConfigDBConnection.KeywordsMaps, null, null, Logger, Lang );

				Logger.logMessage( "1", Lang.translate( "Checking table [%s] exists in database [%s] struct", strTableName, ConfigDBConnection.strDatabase ) );

				boolean bTableExists = this.checkTableExists( strTableName, DBDefinition, BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, Logger, Lang );

				CDBTableDefinition DBTableDefinitionClone = new CDBTableDefinition( DBTableDefinition );
				DBTableDefinitionClone.strName = strTableName;
				
				if ( bTableExists == true ) {

					Logger.logMessage( "1", Lang.translate( "Table [%s] exists in database [%s] struct", strTableName, ConfigDBConnection.strDatabase ) );

					Logger.logMessage( "1", Lang.translate( "Checking table [%s] field names and types", strTableName ) );

					if ( this.checkTableFieldNamesAndTypes( DBDefinition, DBTableDefinitionClone, BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, Logger, Lang ) == true ) {
						
						Logger.logMessage( "1", Lang.translate( "Checking of table [%s] data on database [%s]", strTableName, ConfigDBConnection.strDatabase ) );

						if ( this.checkTableData( 1, DBDefinition, DBTableDefinitionClone, BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, Logger, Lang ) ) { //table_exists event

							Logger.logMessage( "1", Lang.translate( "Successful check table [%s] data on database [%s]", strTableName, ConfigDBConnection.strDatabase ) );

							bResult = true;

						}
						else {
							
							Logger.logError( "-1002", Lang.translate( "Fail to check table [%s] data on database [%s]", strTableName, ConfigDBConnection.strDatabase ) );
							
						}
						
					}
					else {

						Logger.logError( "-1001", Lang.translate( "Fail to checking of table [%s] data on database [%s]", strTableName, ConfigDBConnection.strDatabase ) );
						
					}


				}
				else {

					Logger.logWarning( "-1", Lang.translate( "Table [%s] not exists in database [%s] struct", strTableName, ConfigDBConnection.strDatabase ) );

					if ( DBTableDefinition.bCreate == true ) {
					
						if ( DBTableDefinition.CreationDefinitions.size() > 0 ) {

							Logger.logMessage( "1", Lang.translate( "Trying to create the table [%s], using create definitions for this table", strTableName ) );

							if ( this.createTable( DBDefinition, DBTableDefinitionClone, BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, Logger, Lang ) == true ) {

								if ( this.checkTableFieldNamesAndTypes( DBDefinition, DBTableDefinitionClone, BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, Logger, Lang ) == true ) {

									Logger.logMessage( "1", Lang.translate( "Checking of table [%s] data", strTableName ) );

									if ( this.checkTableData( 0, DBDefinition, DBTableDefinitionClone, BackendServicesManager, HTTPClient, ConfigDBConnection, strSecurityTokenID, Logger, Lang ) ) { //table_exists event

										bResult = true;

										Logger.logMessage( "1", Lang.translate( "Successful checking table [%s] data on database [%s]", strTableName, ConfigDBConnection.strDatabase ) );

									}
									else {

										Logger.logError( "-1004", Lang.translate( "Fail to checking table [%s] data on database [%s]", strTableName, ConfigDBConnection.strDatabase ) );

									}

								}
								else {

									Logger.logError( "-1003", Lang.translate( "Fail to checking of table [%s] data on database [%s]", strTableName, ConfigDBConnection.strDatabase ) );

								}

							}

						}
						else {

							Logger.logWarning( "-1", Lang.translate( "Cannot create the table [%s], no create definitions for this table", strTableName ) );

						}
					
					}
					else {
						
						Logger.logWarning( "-1", Lang.translate( "The configuration is disabled creating table [%s], [Create] attribute equal to false", strTableName ) );
						
					}

				}

				bResult = true;

			}
			
			Logger.logMessage( "1", Lang.translate( "End of initiation tables on database [%s]", ConfigDBConnection.strDatabase ) );
			
		}
		catch ( Exception Ex ) {
			
			Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "initiationOnDatabaseTables", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
	
	public String tryInitSessionOnDatabase( CBPBackendServicesManager BackendServicesManager, CloseableHttpClient HTTPClient, CConfigServiceDBConnection ConfigDBConnection, CExtendedLogger Logger, CLanguage Lang ) {

		long lngStart = System.currentTimeMillis();
		
		String strSecurityTokenID = "";
		
		try {
		
			int intTryCounts = 1;
			
			String strOldBackendURLSelected = "";
			
			while ( strSecurityTokenID.isEmpty() && intTryCounts <= 4 ) {

				ArrayList<CRegisteredManagerInfo> ListOfBackendsNodes = null;

				if ( ConfigDBConnection.getDynamicBackendOnSelectMethod() && intTryCounts <= 3 ) {

					ListOfBackendsNodes = this.getListOfBackendsNodes( BackendServicesManager, HTTPClient, Logger, Lang ); //BackendServicesManager.callServiceSystemListRegisteredManagers( HTTPClient, strSecurityTokenID, strURL, ConfigProxy, strContext, Logger, Lang)  

				}

				if ( ListOfBackendsNodes != null && ListOfBackendsNodes.size() > 0 ) {

					ConfigDBConnection.selectURLBackend( 2, ListOfBackendsNodes, Logger, Lang ); //Dynamic backend

				}
				else {

					ConfigDBConnection.selectURLBackend( 1, ListOfBackendsNodes, Logger, Lang ); //Round robind

				}

				if ( strOldBackendURLSelected != ConfigDBConnection.getURLBackend() )
					strSecurityTokenID = this.startSessionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnection, Logger, Lang ); //Result.get( 0 ).get( "SecurityTokenID" );

				strOldBackendURLSelected = ConfigDBConnection.getURLBackend();
				
				intTryCounts += 1;

			}
		
		}
		catch ( Error Err ) {

			Logger.logError( "-1025", Err.getMessage(), Err );
			
		}
		catch ( Exception Ex ) {
			
			Logger.logException( "-1026", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "tryInitSessionOnDatabase", Long.toString( lngEnd - lngStart ) ) );
		
		return strSecurityTokenID;
		
	}
	
	public boolean initiationObjectsDatabase( CConfigServiceDBConnection ConfigDBConnection, CExtendedLogger Logger, CLanguage Lang ) {
		
		long lngStart = System.currentTimeMillis();
		
		boolean bResult = false;
		
		try {

			Logger.logMessage( "1", Lang.translate( "Init database objects with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend() ) );

			CConfigServiceDBConnection ConfigDBConnectionClone = new CConfigServiceDBConnection( ConfigDBConnection  );
			
			CBPBackendServicesManager BackendServicesManager = new CBPBackendServicesManager( this.strRunningPath + ConstantsCommonClasses._Temp_Dir );

			RequestConfig clientConfig = RequestConfig.custom().setConnectTimeout( intRequestTimeout ).setConnectionRequestTimeout( intRequestTimeout ).setSocketTimeout( intSocketTimeout ).build();
			
			CloseableHttpClient HTTPClient = HttpClientBuilder.create().setDefaultRequestConfig( clientConfig ).build(); // HttpClients.createDefault();
			
			/*ArrayList<CRegisteredManagerInfo> ListOfBackendsNodes = null;
			
			if ( ConfigDBConnectionClone.getDynamicBackendOnSelectMethod() ) {
			
				ListOfBackendsNodes = this.getListOfBackendsNodes( BackendServicesManager, HTTPClient, Logger, Lang ); //BackendServicesManager.callServiceSystemListRegisteredManagers( HTTPClient, strSecurityTokenID, strURL, ConfigProxy, strContext, Logger, Lang)  
			
			}
			
			if ( ListOfBackendsNodes != null && ListOfBackendsNodes.size() > 0 ) {
				
				ConfigDBConnectionClone.selectURLBackend( 2, ListOfBackendsNodes, Logger, Lang ); //Dynamic backend
				
			}
			else {
			
				ConfigDBConnectionClone.selectURLBackend( 1, ListOfBackendsNodes, Logger, Lang ); //Round robind
			
			}*/

			String strSecurityTokenID = this.tryInitSessionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnectionClone, Logger, Lang ); //Result.get( 0 ).get( "SecurityTokenID" );

			if ( strSecurityTokenID != null && strSecurityTokenID.isEmpty() == false ) {

				Logger.logMessage( "1", Lang.translate( "Start session successful in database with the next attributes Name: [%s], Database: [%s], User: [%s], URL: [%s], SecurityTokenID: [%s]", ConfigDBConnection.strName, ConfigDBConnection.strDatabase, ConfigDBConnection.strUser, ConfigDBConnection.getURLBackend(), strSecurityTokenID ) );

				/*ArrayList<String> DBFeaturesInfoToGet = new ArrayList<String>();
				DBFeaturesInfoToGet.add( "configuredEngineName" );
				DBFeaturesInfoToGet.add( "configuredEngineVersion" );*/

				LinkedHashMap<String,String> DatatabaseFeatureInfo = this.getDatabaseFeaturesInfo( BackendServicesManager, HTTPClient, ConfigDBConnectionClone, strSecurityTokenID, null, Logger, Lang );

				String strDBMSName = DatatabaseFeatureInfo.get( "configuredEngineName" );
				String strDBMSVersion = DatatabaseFeatureInfo.get( "configuredEngineVersion" );

				CDBDefinition DBDefinition = DBDefinitions.get( ConfigDBConnection.strDatabase.toLowerCase() + "@|@" + strDBMSName.toLowerCase() + "@|@" + strDBMSVersion.toLowerCase() );

				if ( DBDefinition != null ) {

					if ( this.initiationOnDatabaseTables( DBDefinition, BackendServicesManager, HTTPClient, ConfigDBConnectionClone, strSecurityTokenID, Logger, Lang ) ) {
						
						Logger.logMessage( "1", Lang.translate( "Successful initiate the tables on Database: [%s], DBMSName: [%s], DBMSVersion: [%s]", ConfigDBConnectionClone.strDatabase, strDBMSName, strDBMSVersion ) );
						
					}
					else {
						
						Logger.logError( "-1002", Lang.translate( "Cannot initiate the tables on Database: [%s], DBMSName: [%s], DBMSVersion: [%s]", ConfigDBConnectionClone.strDatabase, strDBMSName, strDBMSVersion ) );
						
					}

				}
				else {

					Logger.logError( "-1001", Lang.translate( "Cannot locate the database definition for next attributes Database: [%s], DBMSName: [%s], DBMSVersion: [%s]", ConfigDBConnection.strDatabase, strDBMSName, strDBMSVersion ) );

				}

				this.endSessionOnDatabase( BackendServicesManager, HTTPClient, ConfigDBConnectionClone, strSecurityTokenID, Logger, Lang );

			}
			
			BackendServicesManager.deleteTempResponsesFiles( Logger, Lang );
			
			HTTPClient.close();
			
		}
		catch ( Exception Ex ) {
			
			Logger.logException( "-1025", Ex.getMessage(), Ex );
			
		}
		
		long lngEnd = System.currentTimeMillis();
		
		Logger.logDebug( "0xExecutedON", Lang.translate( "[%s] executed on: [%s] ms", "initiationObjectsDatabase", Long.toString( lngEnd - lngStart ) ) );
		
		return bResult;
		
	}
	
}
