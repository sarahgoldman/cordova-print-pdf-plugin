package com.sgoldm.plugin;
 
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class PrintPDF extends CordovaPlugin {
	
	public static final String ACTION_PRINT_WITH_DATA = "printWithData";
	public static final String ACTION_PRINT_WITH_URL = "printWithURL";
	
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		try {
		    if (action.equals(ACTION_PRINT_WITH_DATA)) { 
		       callbackContext.success();
		       return true;
		    } else if (action.equals(ACTION_PRINT_WITH_URL)) {
				callbackContext.success();
			    return true;
			}
		    callbackContext.error("Invalid action");
		    return false;
		} catch(Exception e) {
		    System.err.println("Exception: " + e.getMessage());
		    callbackContext.error(e.getMessage());
		    return false;
		}
	}
}