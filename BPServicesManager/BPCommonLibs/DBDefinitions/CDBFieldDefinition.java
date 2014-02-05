package DBDefinitions;


public class CDBFieldDefinition {
	
	public int intMatchCase; // 0 = Any, 1 = Exact
	public String strName;
	public String strType;
	public int intLength;
	public String strComment;
	public int intNotNull; //O = Allow Null, 1 = Not Null, 2 = Keep Field Config
	public boolean bAlter; 
	public boolean bAdd; 
	
	public CDBFieldDefinition() {
		
		this.intMatchCase = 0; //Any default
		this.strName = "";
		this.strType = "";
		this.intLength = 0;
		this.strComment = "";
		this.intNotNull = 0;
		this.bAlter = true;
		this.bAdd = true;
		
	}

	public CDBFieldDefinition( CDBFieldDefinition FieldDefinitionToClone ) {
		
		this.intMatchCase = FieldDefinitionToClone.intMatchCase;
		this.strName = FieldDefinitionToClone.strName;
		this.strType = FieldDefinitionToClone.strType;
		this.intLength = FieldDefinitionToClone.intLength;
		this.strComment = FieldDefinitionToClone.strComment;
		this.intNotNull = FieldDefinitionToClone.intNotNull;
		this.bAlter = FieldDefinitionToClone.bAlter;
		this.bAdd = FieldDefinitionToClone.bAdd;
		
	}
	
}
