package DBDefinitions;

import java.util.ArrayList;

public class CDBDataDefinition {

	public String strID; 
	public int intEvent; //0 = table_created, 1 = table_exists
	public int intType; // 0 = None, 1 = Where, 2 = Full
	public int intAction; //0 = Insert, 1 = Update
	public String strCondition;
	public ArrayList<String> strCommand = null;
	
	public CDBDataDefinition() {
		
		this.strID = "";
		this.intEvent = 0;
		this.intType = 0;
		this.intAction = 0;
		this.strCondition = "";
		
		strCommand = new ArrayList<String>();
		
	}

	public CDBDataDefinition( CDBDataDefinition DBDataDefinitionToClone ) {
		
		this.strID = DBDataDefinitionToClone.strID;
		this.intEvent = DBDataDefinitionToClone.intEvent;
		this.intType = DBDataDefinitionToClone.intType;
		this.intAction = DBDataDefinitionToClone.intAction;
		this.strCondition = DBDataDefinitionToClone.strCondition;
		
		strCommand = new ArrayList<String>( DBDataDefinitionToClone.strCommand );
		
	}
	
}
