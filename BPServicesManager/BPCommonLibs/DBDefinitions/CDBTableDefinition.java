package DBDefinitions;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CDBTableDefinition {

	public String strName;
	public String strDescription;
	public boolean bCreate;
	public boolean bCheckFields;
	public boolean bAddFields;
	public boolean bAlterFields;
	public boolean bCheckData;
	
	public ArrayList<String> CreationDefinitions;
	public String strAddFieldDefinition;
	public String strAlterFieldDefinition;
	
	public LinkedHashMap<String,CDBFieldDefinition> CheckFields;

	public LinkedHashMap<String,CDBDataDefinition> CheckData;
	
	public ArrayList<String> MapsNames;
	
	public CDBTableDefinition() {
		
		this.strName = "";
		
		this.strDescription = "";
		
		this.CreationDefinitions = new ArrayList<String>();
		
		this.strAddFieldDefinition = "";
		
		this.strAlterFieldDefinition = "";
		
		this.CheckFields = new LinkedHashMap<String,CDBFieldDefinition>();
		
		this.CheckData = new LinkedHashMap<String,CDBDataDefinition>();
		
		this.MapsNames = new ArrayList<String>();
		
		this.bCreate = true;
		
		this.bCheckFields = true;
		
		this.bAddFields = true;
		
		this.bAlterFields = true;
		
		this.bCheckData = true;
		
	}
	
	public CDBTableDefinition( CDBTableDefinition TableDefinitionToClone ) {
		
		this.strName = TableDefinitionToClone.strName;
		
		this.strDescription = TableDefinitionToClone.strDescription;
		
		this.CreationDefinitions = new ArrayList<String>( TableDefinitionToClone.CreationDefinitions );
		
		this.strAddFieldDefinition = TableDefinitionToClone.strAddFieldDefinition;
		
		this.strAlterFieldDefinition = TableDefinitionToClone.strAlterFieldDefinition;
		
		this.CheckFields = new LinkedHashMap<String,CDBFieldDefinition>( TableDefinitionToClone.CheckFields );
		
		this.CheckData = new LinkedHashMap<String,CDBDataDefinition>( TableDefinitionToClone.CheckData );
		
		this.MapsNames = new ArrayList<String>( TableDefinitionToClone.MapsNames );
		
		this.bCreate = TableDefinitionToClone.bCreate; 
				
		this.bCheckFields = TableDefinitionToClone.bCheckFields;
		
		this.bAddFields = TableDefinitionToClone.bAddFields;
		
		this.bAlterFields = TableDefinitionToClone.bAlterFields;
		
		this.bCheckData = TableDefinitionToClone.bCheckData;
		
	}
	
}
