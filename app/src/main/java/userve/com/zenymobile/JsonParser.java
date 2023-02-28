package userve.com.zenymobile;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonParser {


	
	public String[] parsUpdateYN(StringBuilder html) {
		String[] returnData = new String[2];
		
		String htmlStr = html.toString();
		JSONObject json = null;
		
		try {
			json = new JSONObject(htmlStr);
			returnData[0] = json.getString("updateYN");
			returnData[1] = json.getString("fileName");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnData;
	}
}
