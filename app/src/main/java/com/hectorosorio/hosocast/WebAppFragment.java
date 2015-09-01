package com.hectorosorio.hosocast;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hectorosorio.hosocast.utils.NotificationUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hector on 9/1/15.
 */
public class WebAppFragment extends Fragment {

    private static final String TAG = WebAppFragment.class.getSimpleName();

    public final static String hostname = "hectorpilotlytest.meteor.com";
    public final static String port = "80";

    WebView webView = null;
    View rootView = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.web_app_fragment, container, false);

        webView = (WebView) rootView.findViewById(R.id.webview);
        //webView.setPadding(0, 0, 0, 0);

        // Custom AppWebViewClient to handle when web content loads
        webView.setWebViewClient(new WebViewClient());

        // Provide JavaScript to Android interface
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");

        // Added to eliminate white flash before webview load
        // Based on http://stackoverflow.com/a/16217522:
        webView.setBackgroundColor(Color.argb(1, 0, 0, 0));

        webView.loadUrl("http://" + hostname + ":" + port);

        Button button = (Button) rootView.findViewById(R.id.send_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEnteredText(v);
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /** Called when the user clicks the Send button */
    public void sendEnteredText(View view) {
        EditText editText = (EditText) rootView.findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        MainActivity activity = (MainActivity) getActivity();
        activity.sendMessageToReceiver(message);
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a cast message from the web page */
        @JavascriptInterface
        public void showCast(String message) {
            Log.d(TAG, "showCast: " + message);
            MainActivity activity = (MainActivity) getActivity();
            activity.sendMessageToReceiver(message);
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Log.d(TAG, "showToast: " + toast);
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        /** Show a cast message from the web page */
        @JavascriptInterface
        public void showNotification(String message) {
            Log.d(TAG, "showNotification: " + message);

            Timer timer = new Timer();
            timer.schedule(new MyTimerTask(message) {
                /*
                @Override
                public void run() {
                    sendNotification(message);
                }
                */
            }, 15 * 1000);
        }
    }

    class MyTimerTask extends TimerTask {
        String message;

        public MyTimerTask(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            Intent resultIntent = new Intent(getActivity(), MainActivity.class);
            NotificationUtil.sendNotification(getActivity(), resultIntent, this.message);
        }
    }
}