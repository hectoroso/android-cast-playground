package com.hectorosorio.hosocast.webapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ImageButton;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.hectorosorio.hosocast.MainActivity;
import com.hectorosorio.hosocast.R;
import com.hectorosorio.hosocast.mediaplayer.LocalPlayerActivity;
import com.hectorosorio.hosocast.utils.NotificationUtil;
import com.hectorosorio.hosocast.video.VideoProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hector on 9/1/15.
 */
public class WebAppFragment extends Fragment {

    private static final String TAG = WebAppFragment.class.getSimpleName();

    public final static String hostname = "hectorpilotlytest.meteor.com";
    public final static String port = "80";

    protected WebView webView = null;
    private View rootView = null;
    private boolean webViewLoaded = false;

    private View mEmptyView;
    private View mLoadingView;
    private VideoCastManager mCastManager;
    private VideoCastConsumerImpl mCastConsumer;

    private List<MediaInfo> data;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.web_app_fragment, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView = (WebView) rootView.findViewById(R.id.webview);

        // TODO use this only for debugging
        webView.clearCache(true);

        //webView.setPadding(0, 0, 0, 0);

        // Custom AppWebViewClient to handle when web content loads
        webView.setWebViewClient(new WebViewClient());

        // Provide JavaScript to Android interface
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptBridge(getActivity()), "Android");

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

        mEmptyView = getView().findViewById(R.id.empty_view);
        //mLoadingView = getView().findViewById(R.id.progress_indicator);
        //layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //getLoaderManager().initLoader(0, null, this);

        mCastManager = VideoCastManager.getInstance();

        mCastConsumer = new VideoCastConsumerImpl() {

            // web view specific actions here

        };
        mCastManager.addVideoCastConsumer(mCastConsumer);
    }

    @Override
    public void onDetach() {
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        super.onDetach();
    }

    /** Called when the user clicks the Send button */
    public void sendEnteredText(View view) {
        EditText editText = (EditText) rootView.findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        MainActivity activity = (MainActivity) getActivity();
        activity.sendMessageToReceiver(message);
    }

    public void showCastDialog() {
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(getActivity().getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                ((MainActivity) getActivity()).showCastDialog();
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

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

    public void hideLoadingView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            // This code will always run on the UI thread, therefore is safe to modify UI elements.
            rootView.findViewById(R.id.progress_indicator).setVisibility(View.GONE);
            webViewLoaded = true;
            }
        });
    }

    public void viewVideo(String data) {
        Log.d(TAG, "viewVideo: " + data);
        try {
            JSONObject jsonObject = null;
            if (null != data) {
                jsonObject = new JSONObject(data);
            }
            else {
                jsonObject = VideoProvider.buildTestJSON();
            }
            List<MediaInfo> media = VideoProvider.buildMedia(jsonObject);

            Intent intent = new Intent(getActivity(), LocalPlayerActivity.class);
            intent.putExtra("media", Utils.mediaInfoToBundle(media.get(0)));
            intent.putExtra("shouldStart", false);
            ActivityCompat.startActivity(getActivity(), intent, null);
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void notifyWebApp(int status) {
        if (webViewLoaded) {
            webView.loadUrl("javascript:onCastEvent(" + status + ")");
        }
    }

    public void callWebAppFunction(String function) {
        if (webViewLoaded) {
            webView.loadUrl("javascript:" + function);
        }
    }

    private MediaInfo mSelectedMedia;
    private ImageButton mPlayCircle;

    public void playCastVideo(MediaInfo selectedMedia) {
        RemoteMediaPlayer remoteMediaPlayer = mCastManager.getRemoteMediaPlayer();

        mSelectedMedia = selectedMedia;
        /*
        mPlayCircle = (ImageButton) rootView.findViewById(R.id.play_circle);
        mPlayCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayback();
            }
        });
*/
        int position = 0;
        Log.d(TAG, "playCastVideo remoteMediaPlayer: " + (remoteMediaPlayer == null ? "NULL" : remoteMediaPlayer.getNamespace()));
        /*
        try {
            mCastManager.loadMedia(selectedMedia, true, position);
        } catch (Exception e) {
            Log.d(TAG, "playCastVideo loadMedia Exception: ", e);
        }
        */
        try {
            //mCastManager.play(position);
            com.hectorosorio.hosocast.utils.Utils.playNow(getActivity(), mSelectedMedia);
        } catch (Exception e) {
            Log.d(TAG, "playCastVideo Exception: ", e);
        }
    }

    public static enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }
    private PlaybackState mPlaybackState;

    private void togglePlayback() {
        Log.d(TAG, "togglePlayback");
        switch (mPlaybackState) {
            case PAUSED:
                Log.d(TAG, "togglePlayback PAUSED");
                try {
                    mCastManager.checkConnectivity();
                    mCastManager.play();
                    mPlaybackState = PlaybackState.PLAYING;
                } catch (Exception e) {
                    Log.d(TAG, "togglePlayback PAUSED Exception", e);
                    return;
                }
                break;

            case PLAYING:
                Log.d(TAG, "togglePlayback PLAYING");
                try {
                    mCastManager.checkConnectivity();
                    mCastManager.pause();
                    mPlaybackState = PlaybackState.PAUSED;
                } catch (Exception e) {
                    Log.d(TAG, "togglePlayback PLAYING Exception", e);
                    return;
                }
                break;

            case IDLE:
                Log.d(TAG, "togglePlayback IDLE Remote");
                try {
                    mCastManager.checkConnectivity();
                    mCastManager.play();
                    mPlaybackState = PlaybackState.PLAYING;
                } catch (Exception e) {
                    Log.d(TAG, "togglePlayback IDLE Exception", e);
                    return;
                }
                break;
        }
    }

    public VideoCastManager getCastManager() {
        return mCastManager;
    }
}
