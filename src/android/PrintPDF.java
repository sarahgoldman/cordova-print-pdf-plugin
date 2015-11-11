package com.sgoldm.plugin.printPDF;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    public static final String ACTION_PRINT_DOCUMENT = "printDocument";
    public static final String ACTION_IS_PRINT_AVAILABLE = "isPrintingAvailable";
    private static final String DEFAULT_DOC_NAME = "unknown";
    private static final String DEFAULT_DOC_TYPE = "Data";
    private static final String FILE_DOC_TYPE = "File";

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

        if (action.equals(ACTION_PRINT_DOCUMENT)) {

            final String content = args.optString(0, "");
            final String type = args.optString(1, DEFAULT_DOC_TYPE);
            final String title = args.optString(2, DEFAULT_DOC_NAME);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                try {
                    printViaCloud(content, type, title);
                } catch (IOException e) {
                    handlePrintError(e);
                }
            } else {
                printViaNative(content, type, title);
            }
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

    /**
     * Convert the content into an input stream
     *
     * @param content
     *      The encoded data string or file uri string
     *
     * @param type
     *      The content type as a string, either Data or File
     *
     * @return
     *      content as InputStream
     */
    private InputStream convertContentToInputStream(final String content, final String type) throws FileNotFoundException {
        InputStream input = null;
        if (type!= null && type.compareToIgnoreCase(FILE_DOC_TYPE) == 0){
            CordovaResourceApi resourceApi = webView.getResourceApi();
            Uri fileURL = resourceApi.remapUri(Uri.parse(content));
            File file = new File(fileURL.getPath());
            if (!file.exists()) {
                handlePrintError(new Exception("File does not exist"));
            } else {
                input = new FileInputStream(file);
            }
        } else {
            byte[] pdfAsBytes = Base64.decode(content, 0);
            input = new ByteArrayInputStream(pdfAsBytes);
        }
        return input;
    }

    private void writeInputStreamToOutput (InputStream input, FileOutputStream output) throws IOException {
        byte[] buf = new byte[1024];
        int bytesRead;

        while ((bytesRead = input.read(buf)) > 0) {
            output.write(buf, 0, bytesRead);
        }
        output.close();
    }

    private void handlePrintError (Exception e) {
        e.printStackTrace();
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "{\"success\": false, \"available\": true, \"error\": \"" + e.getMessage() + "\"}");
        command.sendPluginResult(result);
    }

    private void handlePrintDismissal () {
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "{\"success\": false, \"available\": true, \"dismissed\": true }");
        command.sendPluginResult(result);
    }

    private void handlePrintSuccess () {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "{\"success\": true, \"available\": true }");
        command.sendPluginResult(result);
        command = null;
    }

    /**
     * Print the document using the native print api
     *
     * @param content
     *      The encoded data string or file uri string
     *
     * @param type
     *      The content type as a string, either Data or File
     *
     * @param title
     *      The document title as a string
     *
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void printViaNative (final String content, final String type, final String title) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PrintManager printManager = (PrintManager) cordova.getActivity().getSystemService(Context.PRINT_SERVICE);

                PrintDocumentAdapter pda = new PrintDocumentAdapter() {

                    @Override
                    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {

                        if (cancellationSignal.isCanceled()) {
                            handlePrintDismissal();
                            return;
                        }

                        InputStream input = null;
                        try {
                            input = convertContentToInputStream(content, type);
                        } catch (FileNotFoundException e) {
                            handlePrintError(e);
                        }
                        FileOutputStream output = new FileOutputStream(destination.getFileDescriptor());

                        try {
                            writeInputStreamToOutput(input, output);
                        } catch (IOException e) {
                            handlePrintError(e);
                        }

                        callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                    }

                    @Override
                    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {

                        if (cancellationSignal.isCanceled()) {
                            handlePrintDismissal();
                            return;
                        }

                        PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(title).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();

                        callback.onLayoutFinished(pdi, true);
                    }

                    @Override
                    public void onFinish() {
                        handlePrintSuccess();
                    }
                };

                printManager.print(title, pda, null);

            }
        });
    }

    /**
     * Create an intent with the content to print out
     * and sends that to the cloud print activity.
     *
     * @param content
     *      The encoded data string or file uri string
     *
     * @param type
     *      the content type as a string, either Data or File
     *
     * @param title
     *      The document title as a string
     *
    */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
   private void printViaCloud (final String content, final String type, final String title) throws IOException {

        File outputDir = cordova.getActivity().getCacheDir();
        File tempFile = File.createTempFile(title, null, outputDir);
        InputStream input = convertContentToInputStream(content, type);
        FileOutputStream output = new FileOutputStream(tempFile, true);

        writeInputStreamToOutput(input, output);

        filePathString = tempFile.toString();
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                printViaGoogleCloudPrintDialog(title, filePathString);
            }
        });

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
        if (file.exists()) {
            boolean deleted = file.delete();
        }
        handlePrintSuccess();
    }


}
