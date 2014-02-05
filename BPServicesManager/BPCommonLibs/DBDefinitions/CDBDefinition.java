package DBDefinitions;

import java.util.LinkedHashMap;

public class CDBDefinition {

	public String strName;
	public String strDescription;
	
	public String strDBMSName;
	public String strDBMSVersion;
	
	public String strCreateDefinition;
	
	public LinkedHashMap<String,String> Constants; 
	
	public LinkedHashMap<String,CDBTableDefinition> Tables;
	
	public CDBDefinition() {
		
		this.strName = "";
		this.strDescription = "";
		
		this.strDBMSName = "";
		this.strDBMSVersion = "";
		
		this.strCreateDefinition = "";
		
		this.Constants = new LinkedHashMap<String,String>();
		
		this.Tables = new LinkedHashMap<String,CDBTableDefinition>();
		
	}

	public CDBDefinition( CDBDefinition DBDefinitionToClone ) {
		
		this.strName = DBDefinitionToClone.strName;
		this.strDescription = DBDefinitionToClone.strDescription;
		
		this.strDBMSName = DBDefinitionToClone.strDBMSName;
		this.strDBMSVersion = DBDefinitionToClone.strDBMSVersion;
		
		this.strCreateDefinition = DBDefinitionToClone.strCreateDefinition;
		
		this.Constants = new LinkedHashMap<String,String>( DBDefinitionToClone.Constants );
		
		this.Tables = new LinkedHashMap<String,CDBTableDefinition>( DBDefinitionToClone.Tables );
		
	}
	
}
