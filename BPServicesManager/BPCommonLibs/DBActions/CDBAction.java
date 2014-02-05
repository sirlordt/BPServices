package DBActions;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CDBAction {

	public String strID;
	public String strDescription;
	public String strCommand;
	
	ArrayList<String> KeywordsMarkedForMap;
	
	LinkedHashMap<String,String> InputParams;
	
	CDBAction() {

		strID = "";
		strDescription = "";
		strCommand = "";
		
		KeywordsMarkedForMap = new ArrayList<String>();
		
		InputParams = new LinkedHashMap<String,String>();
		
	}
	
	CDBAction( CDBAction DBActionToClone ) {
		
		strID = DBActionToClone.strID;
		strDescription = DBActionToClone.strDescription;
		strCommand = DBActionToClone.strCommand;
		
		KeywordsMarkedForMap = new ArrayList<String>( DBActionToClone.KeywordsMarkedForMap );
		
		InputParams = new LinkedHashMap<String,String>( DBActionToClone.InputParams );
		
	}
	
}
