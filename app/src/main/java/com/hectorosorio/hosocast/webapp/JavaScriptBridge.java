package com.hectorosorio.hosocast.webapp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.hectorosorio.hosocast.MainActivity;
import com.hectorosorio.hosocast.R;
import com.hectorosorio.hosocast.video.VideoProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by hector on 9/8/15.
 */
public class JavaScriptBridge {
    private Context mContext;
    private WebAppFragment fragment;

    private static final String TAG = JavaScriptBridge.class.getSimpleName();

    JavaScriptBridge(Context c){
        mContext = c;

        FragmentManager fragmentManager = ((AppCompatActivity) mContext).getSupportFragmentManager();
        fragment = (WebAppFragment) fragmentManager.findFragmentById(R.id.webapp);
    }

    @JavascriptInterface
    public void manageOrientation(String o){

        if(o.equals("landscape")) {
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else{
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @JavascriptInterface
    public void removeSplash(){
        Log.d(TAG, "removeSplash");
        fragment.hideLoadingView();
        Toast.makeText(mContext, "removeSplash", Toast.LENGTH_SHORT).show();
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Log.d(TAG, "showToast: " + toast);
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    /** Show a video in native player from the web page */
    @JavascriptInterface
    public void showVideo(String data) {
        Log.d(TAG, "showVideo: " + data);
        fragment.viewVideo(data);
    }

    /*
     * Chromecast
     */

    /** Show a cast message from the web page */
    @JavascriptInterface
    public void sendMessageToCast(String message) {
        Log.d(TAG, "sendMessageToCast: " + message);
        ((MainActivity) mContext).sendMessageToReceiver(message);
    }

    @JavascriptInterface
    public void castVideo(String metadata) {
        Log.d(TAG, "castVideo: " + metadata);
        try {
            JSONObject jsonObject = null;
            if (false) {
                jsonObject = new JSONObject(metadata); // VideoProvider.buildTestJSON();
            }
            else {
                jsonObject = VideoProvider.buildTestJSON();
            }
            List<MediaInfo> media = VideoProvider.buildMedia(jsonObject);

            fragment.playCastVideo(media.get(0));
        }
        catch(JSONException e) {
            Log.d(TAG, "castVideo Exception: ", e);
            //ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void playCastVideo() {
        Log.d(TAG, "playCastVideo");
    }

    @JavascriptInterface
    public void pauseCastVideo() {
        Log.d(TAG, "pauseCastVideo");
    }

    @JavascriptInterface
    public void seekCastVideo(int position) {
        Log.d(TAG, "seekCastVideo: " + position);
    }

    /** Show a cast message from the web page */
    @JavascriptInterface
    public void showNotification(String message) {
        Log.d(TAG, "showNotification: " + message);

        //Intent resultIntent = new Intent(getActivity(), MainActivity.class);
        //NotificationUtil.sendNotification(getActivity(), resultIntent, this.message);

        fragment.showNotification(message);
    }

    @JavascriptInterface
    public boolean isCastDeviceDetected() {
        boolean routeAvailable = fragment.getCastManager().isAnyRouteAvailable();
        //boolean castVar = ((MainActivity) mContext).isCastDeviceAvailable();
        Log.d(TAG, "isCastDeviceDetected: routeAvailable=" + routeAvailable); // + ", castVar=" + castVar
        return routeAvailable;
    }

    @JavascriptInterface
    public void openCastDeviceList() {
        Log.d(TAG, "openCastDeviceList");
        fragment.showCastDialog();
    }

    @JavascriptInterface
    public boolean isCastDeviceConnected() {
        boolean connected = fragment.getCastManager().isConnected();
        Log.d(TAG, "isCastDeviceConnected: " + connected);
        return connected;
    }
}
