package com.sgoldm.plugin.printPDF;
 
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;

/**
 * This plug in brings up a native overlay to print pdf documents using
 * AirPrint for iOS and Google Cloud Print for Android.
 */
public class PrintPDF extends CordovaPlugin {

    private CallbackContext command;

    public static final String ACTION_PRINT_WITH_DATA = "printWithData";
	public static final String ACTION_IS_PRINT_AVAILABLE = "isPrintingAvailable";
	private static final String DEFAULT_DOC_NAME = "unknown";

    private String filePathString;

    /**
     * Executes the request.
     *
     * This method is called from the WebView thread.
     * To do a non-trivial amount of work, use:
     *     cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     *     cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action   The action to execute.
     * @param args     The exec() arguments in JSON form.
     * @param callback The callback context used when calling
     *                 back into JavaScript.
     * @return         Whether the action was valid.
     */
    @Override
    public boolean execute (String action, JSONArray args,
                            CallbackContext callback) throws JSONException {

        command = callback;

        if (action.equals(ACTION_PRINT_WITH_DATA)) {
			print(args);
	           return true;
	    } else if (action.equals(ACTION_IS_PRINT_AVAILABLE)) {
			isAvailable();
	           return true;
		}
	    return false;
	
    }

    /**
     * Informs if the device is able to print documents.
     * A Internet connection is required to load the cloud print dialog.
     */
    private void isAvailable () {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Boolean supported = isOnline();
                PluginResult result;

                result = new PluginResult(PluginResult.Status.OK, supported);

                command.sendPluginResult(result);
            }
        });
    }

    /**
     * Create an intent with the content to print out
     * and sends that to the cloud print activity.
     *
     * @param args
     *      The exec arguments as JSON
     */
    private void print (JSONArray args) {
        final String content = args.optString(0, "");
        final String title = args.optString(1, DEFAULT_DOC_NAME);

        byte[] pdfAsBytes = Base64.decode(content, 0);

        try {
            File outputDir = cordova.getActivity().getCacheDir();
            File filePath = File.createTempFile(title, null, outputDir);
            FileOutputStream os = new FileOutputStream(filePath, true);
            os.write(pdfAsBytes);
            os.close();
            filePathString = filePath.toString();
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printViaGoogleCloudPrintDialog(title, filePathString);
                }
            });
        } catch (Exception e) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "{\"success\": false, \"available\": true, \"error\": \""+e.getMessage()+"\"}");
            command.sendPluginResult(result);
            return;
        }

    }

    /**
     * Checks if the device is connected
     * to the Internet.
     *
     * @return
     *      true if online otherwise false
     */
    private Boolean isOnline () {
        Activity activity = cordova.getActivity();
        ConnectivityManager conMGr =
                (ConnectivityManager) activity.getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = conMGr.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }

    /**
     * Uses the cloud print web dialog to print the content.
     *
     * @param title
     *      The title for the print job
     */
    private void printViaGoogleCloudPrintDialog(String title, String filePath) {
        Intent intent = new Intent(
                cordova.getActivity(), CloudPrintDialog.class);

        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_TEXT, filePath);

        cordova.startActivityForResult(null, intent, 0);
        cordova.setActivityResultCallback(this);
    }

    /**
     * Called when an activity you launched exits, giving you the reqCode you
     * started it with, the resCode it returned, and any additional data from it.
     *
     * @param reqCode     The request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resCode     The integer result code returned by the child activity
     *                    through its setResult().
     * @param intent      An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        super.onActivityResult(reqCode, resCode, intent);
        File file = new File(filePathString);
        boolean deleted = file.delete();
        command.success();
        command = null;
    }


}