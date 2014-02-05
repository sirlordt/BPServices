package DBActions;

import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import CommonClasses.CLanguage;
import CommonClasses.NamesSQLTypes;
import ExtendedLogger.CExtendedLogger;

public class CDBActionsManager {
	
	public ArrayList<CDBAction> DBActions; 
	
	public String strDBActionsFilePath;
	
	public CDBActionsManager() {
		
		strDBActionsFilePath = "";
		
		//Don't Load in memory wait for manual call to method LoadDBActionsFromFile
		
		DBActions = new ArrayList<CDBAction>(); 
		
	}
	
	public CDBActionsManager( CDBActionsManager DBActionsManagerToClone ) {

		this.strDBActionsFilePath = DBActionsManagerToClone.strDBActionsFilePath;
		
		this.DBActions = new ArrayList<CDBAction>( DBActionsManagerToClone.DBActions ); 
		
	}

	public CDBActionsManager( String strDBActionsFilePath, CExtendedLogger Logger, CLanguage Lang ) {
		
		DBActions = new ArrayList<CDBAction>(); 
		
		this.loadDBActionsFromFile( strDBActionsFilePath, Logger, Lang );
		
	}
	
	public boolean loadDBActionsFromFile( String strDBActionsFilePath, final CExtendedLogger Logger, final CLanguage Lang ) {
		
		boolean bResult = false;
		
		this.strDBActionsFilePath = strDBActionsFilePath;

		DBActions.clear();
		
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler XMLParserHandler = new DefaultHandler() {

				String strNodeName = "";
				int intActionDBSectionNode = 0;
				int intCommandNode = 0;
				int intMapNode = 0;
				int intInputParamNode = 0;
				String strDataType = "";
				CDBAction DBAction = null;
				
				public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {

					strNodeName = qName;
					
					if ( qName.equals( ConfigXMLTagsDBActions._ActionDB ) ) {
						
						if ( intActionDBSectionNode == 0 ) {
						
							if ( attributes.getLength() == 2 ) {

								DBAction = new CDBAction(); 

								for ( int intIndexAttrib = 0; intIndexAttrib < attributes.getLength(); intIndexAttrib++ ) {

									String strAttribName = attributes.getQName( intIndexAttrib );

									if ( strAttribName.equals( ConfigXMLTagsDBActions._ID ) ) {

										DBAction.strID = attributes.getValue( intIndexAttrib ).trim();
										
										if ( DBAction.strID.isEmpty() == true ) {

											Logger.logError( "-1001", Lang.translate( "The [%s] attribute is empty for the section [%s]", ConfigXMLTagsDBActions._ID, qName ) );
											
											DBAction = null;
											
											break;
											
										}

									}
									else if ( strAttribName.equals( ConfigXMLTagsDBActions._Description ) ) {

										DBAction.strDescription = attributes.getValue( intIndexAttrib );

									}
									else {

										Logger.logWarning( "-1", Lang.translate( "The [%s] attribute is unknown for the node [%s]", strAttribName, qName ) );

										DBAction = null;
										
										break;
										
									}

								}
								
								if ( DBAction == null ) {

									Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );

								}

							}
							else {
								
								Logger.logWarning( "-1", Lang.translate( "Wrong count of attributes for the section [%s], found [%s] must be [%s]", qName, Integer.toString( attributes.getLength() ), "2" ) );
								Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
								
							}
						
						}
						else {
							
							Logger.logWarning( "-1", Lang.translate( "Ignoring the section [%s]", qName ) );
							
						}
							
						intActionDBSectionNode++;
						
					}
					else if ( DBAction != null && intActionDBSectionNode == 1 ) {
						
						if ( qName.equals( ConfigXMLTagsDBActions._Command ) ) {

							intCommandNode++;
							
						}
						else if ( qName.equals( ConfigXMLTagsDBActions._Map ) ) {
							
							intMapNode++;
							
						}
						else if ( qName.equals( ConfigXMLTagsDBActions._InputParam ) ) {
							
							if ( attributes.getLength() == 1 ) {
							
								String strAttribName = attributes.getQName( 0 );
								
								if ( strAttribName.equals( ConfigXMLTagsDBActions._DataType ) ) {
									
									strDataType = attributes.getValue( 0 );
									
									if ( NamesSQLTypes.CheckJavaSQLType( strDataType ) ) {
									    
										intInputParamNode++;
										
									}
									else {
										
										Logger.logWarning( "-1", Lang.translate( "The [%s] attribute value [%s] is invalid for the node [%s]", strAttribName, strDataType, qName ) );
										
									}
								
								}
								
							}

						}
						
					}
					
				}
				
				public void endElement( String uri, String localName, String qName ) throws SAXException {

					if ( qName.equals( ConfigXMLTagsDBActions._ActionDB ) ) {
						
						if ( DBAction != null && intActionDBSectionNode == 1 ) {
						
							if ( DBAction.strID.isEmpty() == false && DBAction.strCommand.isEmpty() == false ) {

								if ( checkDBActionForId( DBAction.strID, Logger, Lang ) == false ) {

									DBActions.add( DBAction );

									Logger.logMessage( "1", Lang.translate( "Added the DBAction to list ID = [%s]", DBAction.strID ) );

								}
								else {

									Logger.logError( "-1001", Lang.translate( "The DBAction ID [%s] already exists in the list from DB actions. This ID must be unique for config file!", DBAction.strID ) );

								}

							}
							else {

								Logger.logWarning( "-1", Lang.translate( "The DBAction in URI [%s] contains invalid or empty values, ID = [%s] and Command = [%s]", uri, DBAction.strID, DBAction.strCommand ) );

							}
						
						    DBAction = null;
							
						}
						
						intActionDBSectionNode--;
						
					}
					else if ( DBAction != null && intActionDBSectionNode == 1 ) {
					
						if ( qName.equals( ConfigXMLTagsDBActions._Command ) ) {

							intCommandNode--;

						}
						else if ( qName.equals( ConfigXMLTagsDBActions._Map ) ) {

							intMapNode--;

						}
						else if ( qName.equals( ConfigXMLTagsDBActions._InputParam ) ) {

							intInputParamNode--;

						}
					
					}

				}

				public void characters( char ch[], int start, int length ) throws SAXException {

					if ( DBAction != null && intActionDBSectionNode == 1 ) {
					
						String strValue = new String( ch, start, length );
						
						if ( strValue.isEmpty() == false ) {
							
							if ( intCommandNode == 1 ) {

								DBAction.strCommand = strValue;

							}
							else if ( intMapNode == 1 ) {

								if ( existsKeywordsForMapOrInputParams( DBAction, strValue, Logger, Lang ) == false ) {

									DBAction.KeywordsMarkedForMap.add( strValue );

								}

							}
							else if ( intInputParamNode == 1 ) {

								if ( existsKeywordsForMapOrInputParams( DBAction, strValue, Logger, Lang ) == false ) {

									DBAction.InputParams.put( strValue, strDataType );

								}

							}
					
						}
						else {
							
							Logger.logError( "-1001", Lang.translate( "The [%s] node value is empty string, ignoring the node", strNodeName ) );
							
						}
						
					}

				}
				
				
			};

			saxParser.parse( strDBActionsFilePath, XMLParserHandler );

			Logger.logMessage( "1", Lang.translate( "Count of DBActions registered: [%s]", Integer.toString( DBActions.size() ) ) );
			
			bResult = true;
			
		} 
		catch ( Exception Ex ) {
			
			if ( Logger != null )
				Logger.logException( "-1010", Ex.getMessage(), Ex );
			
		}		
		
		return bResult;
		
	}

	boolean checkDBActionForId( String strID, CExtendedLogger Logger, CLanguage Lang ) {
		
		boolean bResult = false;
		
		try {
		
			Logger.logMessage( "1", Lang.translate( "Check for exists for DBAction ID = [%s]", strID ) );

			if ( DBActions != null ) {

				Logger.logMessage( "1", Lang.translate( "Count of DBActions registered: [%s]", Integer.toString( DBActions.size() ) ) );

				for ( int I =0; I < DBActions.size(); I++ ) {

					if ( DBActions.get( I ).strID.equals( strID ) ) {

						bResult = true;

						Logger.logMessage( "1", Lang.translate( "DBAction ID = [%s] exists", strID ) );

						break;

					}

				}

				if ( bResult == false )
					Logger.logWarning( "-1", Lang.translate( "DBAction ID = [%s] not exists", strID ) );

			}
			else {

				Logger.logWarning( "-1", Lang.translate( "DBActions list is null" ) );

			}
		
		} 
		catch ( Exception Ex ) {
			
			if ( Logger != null )
				Logger.logException( "-1010", Ex.getMessage(), Ex );
			
		}		

		return bResult;
		
	}
	
	boolean existsKeywordsForMapOrInputParams( CDBAction DBAction, String strValue, CExtendedLogger Logger, CLanguage Lang ) {
		
		boolean bResult = true;
		
		if ( DBAction.KeywordsMarkedForMap.contains( strValue ) == false ) {

			if ( DBAction.InputParams.get( strValue ) == null ) {
			
				bResult = false;
				
			}
			else {

				Logger.logWarning( "-1", Lang.translate( "The value [%s] is already in the list for input parameters", strValue ) );
				
			}
			
		}
		else {
			
			Logger.logWarning( "-1", Lang.translate( "The value [%s] is already in the list keywords marked for maps", strValue ) );
			
		}
		
		return bResult;
		
	}
	
	CDBAction getDBActionForId( String strID, CExtendedLogger Logger, CLanguage Lang ) {
		
		CDBAction DBAction = null;
		
		try {
		
			Logger.logMessage( "1", Lang.translate( "Get DBAction ID = [%s]", strID ) );

			if ( DBActions != null ) {

				Logger.logMessage( "1", Lang.translate( "Count of DBActions registered: [%s]", Integer.toString( DBActions.size() ) ) );

				for ( int I =0; I < DBActions.size(); I++ ) {

					if ( DBActions.get( I ).strID.equals( strID ) ) {

						DBAction = DBActions.get( I );

						Logger.logMessage( "1", Lang.translate( "DBAction ID = [%s] found and returned", strID ) );

						break;

					}

				}

				if ( DBAction == null )
					Logger.logWarning( "-1", Lang.translate( "DBAction ID = [%s] not found", strID ) );

			}
			else {

				Logger.logWarning( "-1", Lang.translate( "DBActions list is null" ) );

			}
		
		} 
		catch ( Exception Ex ) {
			
			if ( Logger != null )
				Logger.logException( "-1010", Ex.getMessage(), Ex );
			
		}		

		return DBAction;
		
	}

	CDBAction getDBActionCopyForId( String strID, CExtendedLogger Logger, CLanguage Lang ) {
		
		CDBAction DBAction = null;

		try {
		
			Logger.logMessage( "1", Lang.translate( "Get DBAction copy ID = [%s]", strID ) );

			if ( DBActions != null ) {

				Logger.logMessage( "1", Lang.translate( "Count of DBActions registered: [%s]", Integer.toString( DBActions.size() ) ) );

				for ( int I =0; I < DBActions.size(); I++ ) {

					if ( DBActions.get( I ).strID.equals( strID ) ) {

						DBAction = new CDBAction( DBActions.get( I ) ); //Create clone copy

						Logger.logMessage( "1", Lang.translate( "DBAction ID = [%s] found and copied", strID ) );

						break;

					}

				}

				if ( DBAction == null )
					Logger.logWarning( "-1", Lang.translate( "DBAction ID = [%s] not found", strID ) );

			}
			else {

				Logger.logWarning( "-1", Lang.translate( "DBActions list is null" ) );

			}
		
		} 
		catch ( Exception Ex ) {
			
			if ( Logger != null )
				Logger.logException( "-1010", Ex.getMessage(), Ex );
			
		}		
		
		return DBAction;
		
	}

}
