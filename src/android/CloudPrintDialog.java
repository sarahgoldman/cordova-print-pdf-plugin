package com.sgoldm.plugin.printPDF;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Custom activity to open the web based
 * Google Cloud Print dialog.
 */
public class CloudPrintDialog extends Activity {

    /**
     * URL to the web based Google Cloud Print dialog
     */
    private static final String PRINT_DIALOG_URL =
            "https://www.google.com/cloudprint/dialog.html";

    /**
     * Javascript interface prefix
     */
    private static final String JS_INTERFACE =
            "Printer";

    /**
     * Post message that is sent by Print Dialog web page
     * when the printing dialog needs to be closed.
     */
    private static final String CLOSE_POST_MESSAGE_NAME =
            "cp-dialog-on-close";

    /**
     * Intent that started the action.
     */
    Intent intent;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        init();
    }

    /**
     * Initializes the activity.
     * Creates the web view and its client + JS interface.
     */
    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    private void init() {
        WebView webView = new WebView(this);
        WebSettings settings = webView.getSettings();

        intent = this.getIntent();

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setJavaScriptEnabled(true);

        addContentView(webView, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );

        webView.setWebViewClient(new PrintDialogWebClient());

        webView.addJavascriptInterface(
                new PrintDialogJavaScriptInterface(), JS_INTERFACE);

        webView.loadUrl(PRINT_DIALOG_URL);
    }

    /**
     * Client to load the google cloud print page
     * once the page is ready.
     */
    private final class PrintDialogWebClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {

            if (!PRINT_DIALOG_URL.equals(url))
                return;

            // Submit print document.
            view.loadUrl("javascript:printDialog.setPrintDocument(" +
                "printDialog.createPrintDocument(" +
                "window." + JS_INTERFACE + ".getType()," +
                "window." + JS_INTERFACE + ".getTitle()," +
                "window." + JS_INTERFACE + ".getContent()," +
                "window." + JS_INTERFACE + ".getEncoding()))");

            // Add post messages listener.
            view.loadUrl("javascript:window.addEventListener('message'," +
                "function(evt){window." + JS_INTERFACE + ".onPostMessage(evt.data)}, false)");
        }
    }

    /**
     * Interface allows the Java object's methods
     * to be accessible from JavaScript.
     */
    final class PrintDialogJavaScriptInterface {

        @JavascriptInterface
        @SuppressWarnings("UnusedDeclaration")
        public String getType() {
            return intent.getType();
        }

        @JavascriptInterface
        @SuppressWarnings("UnusedDeclaration")
        public String getEncoding() {
            return "base64";
        }

        @JavascriptInterface
        @SuppressWarnings("UnusedDeclaration")
        public String getTitle() {
            return intent.getStringExtra(Intent.EXTRA_TITLE);
        }

        /**
         * @return
         *      Converted byte stream as base64 encoded string.
         */
        @JavascriptInterface
        @SuppressWarnings("UnusedDeclaration")
        public String getContent() {

            try {

                File file = new File(intent.getStringExtra(Intent.EXTRA_TEXT));
                int size = (int) file.length();
                byte[] bytes = new byte[size];
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();

                String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);

                return encoded;
            } catch (Exception e) {
                return "";
            }
        }

        /**
         * JS interface to get informed when the job is finished
         * and the view should be closed.
         *
         * @param message
         *      The fired event name
         */
        @JavascriptInterface
        @SuppressWarnings("UnusedDeclaration")
        public void onPostMessage(String message) {
            if (message.startsWith(CLOSE_POST_MESSAGE_NAME)) {
                finish();
            }
        }
    }
}