package com.sgoldm.plugin.printPDF;
 
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.util.Base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This plug in brings up a native overlay to print pdf documents.
 */
public class PrintPDF extends CordovaPlugin {

    private CallbackContext command;

    public static final String ACTION_PRINT_WITH_DATA = "printWithData";
    public static final String ACTION_IS_PRINT_AVAILABLE = "isPrintingAvailable";
    private static final String DEFAULT_DOC_NAME = "unknown";
    private static final String DEFAULT_DOC_TYPE = "Data";
    private static final String FILE_DOC_TYPE = "File";

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
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void print (final JSONArray args) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                final String content = args.optString(0, "");
                final String type = args.optString(1, DEFAULT_DOC_TYPE);
                final String title = args.optString(2, DEFAULT_DOC_NAME);


                PrintManager printManager = (PrintManager) cordova.getActivity().getSystemService(Context.PRINT_SERVICE);

                PrintDocumentAdapter pda = new PrintDocumentAdapter() {

                    @Override
                    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {

                        if (cancellationSignal.isCanceled()) {
                            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "{\"success\": false, \"available\": true, \"dismissed\": true }");
                            command.sendPluginResult(result);
                            return;
                        }

                        InputStream input = null;
                        OutputStream output = null;

                        try {
                            if (type!= null && type.compareToIgnoreCase(FILE_DOC_TYPE) == 0){
                                CordovaResourceApi resourceApi= webView.getResourceApi();
                                Uri fileURL = resourceApi.remapUri(Uri.parse(content));
                                File file = new File(fileURL.getPath());
                                if (!file.exists()) {
                                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, "{\"success\": false, \"available\": true, \"error\": \"" + "File not Exist" + "\"}");
                                    command.sendPluginResult(result);
                                }
                                input = new FileInputStream(file);
                            } else {
                                byte[] pdfAsBytes = Base64.decode(content, 0);
                                input = new ByteArrayInputStream(pdfAsBytes);
                            }
                            output = new FileOutputStream(destination.getFileDescriptor());

                            byte[] buf = new byte[1024];
                            int bytesRead;

                            while ((bytesRead = input.read(buf)) > 0) {
                                output.write(buf, 0, bytesRead);
                            }

                            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

                        } catch (FileNotFoundException ee) {
                            //Catch exception
                            ee.printStackTrace();
                            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "{\"success\": false, \"available\": true, \"error\": \"" + ee.getMessage() + "\"}");
                            command.sendPluginResult(result);
                            return;
                        } catch (Exception e) {
                            //Catch exception
                            e.printStackTrace();
                            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "{\"success\": false, \"available\": true, \"error\": \"" + e.getMessage() + "\"}");
                            command.sendPluginResult(result);
                            return;
                        } finally {
                            try {
                                input.close();
                                output.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                PluginResult result = new PluginResult(PluginResult.Status.ERROR, "{\"success\": false, \"available\": true, \"error\": \"" + e.getMessage() + "\"}");
                                command.sendPluginResult(result);
                                return;
                            }
                        }
                    }

                    @Override
                    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {

                        if (cancellationSignal.isCanceled()) {
                            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "{\"success\": false, \"available\": true, \"dismissed\": true }");
                            command.sendPluginResult(result);
                            return;
                        }

                        PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(title).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();

                        callback.onLayoutFinished(pdi, true);
                    }

                    @Override
                    public void onFinish() {
                        PluginResult result = new PluginResult(PluginResult.Status.OK, "{\"success\": true, \"available\": true }");
                        command.sendPluginResult(result);
                    }
                };

                printManager.print(title, pda, null);

            }
        });
    }

    /**
     * Checks if the device is connected
     * to the Internet.
     *
     * @return
     *      true if online otherwise false
     */
    private Boolean isOnline() {
        Activity activity = cordova.getActivity();
        ConnectivityManager conMGr =
                (ConnectivityManager) activity.getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = conMGr.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }

}