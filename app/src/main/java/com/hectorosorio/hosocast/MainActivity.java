package com.hectorosorio.hosocast;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;

import com.hectorosorio.hosocast.utils.NotificationUtil;
import com.hectorosorio.hosocast.utils.Utils;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private VideoCastManager mCastManager;
    private VideoCastConsumer mCastConsumer;
    private MenuItem mediaRouteMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        VideoCastManager.checkGooglePlayServices(this);
        setContentView(R.layout.activity_main);

        mCastManager = VideoCastManager.getInstance();

        mCastConsumer = new VideoCastConsumerImpl() {

            @Override
            public void onFailed(int resourceId, int statusCode) {
                String reason = "Not Available";
                if (resourceId > 0) {
                    reason = getString(resourceId);
                }
                Log.e(TAG, "Action failed, reason:  " + reason + ", status code: " + statusCode);
            }

            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId,
                                               boolean wasLaunched) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDisconnected() {
                invalidateOptionsMenu();
            }

            @Override
            public void onConnectionSuspended(int cause) {
                Log.d(TAG, "onConnectionSuspended() was called with cause: " + cause);
                Utils.showToast(MainActivity.this, R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                Utils.showToast(MainActivity.this, R.string.connection_recovered);
            }

            @Override
            public void onCastDeviceDetected(final RouteInfo info) {
                Log.d(TAG, "onCastDeviceDetected: " + info);
                /*
                if (!CastPreference.isFtuShown(MainActivity.this) && mIsHoneyCombOrAbove) {
                    CastPreference.setFtuShown(MainActivity.this);

                    Log.d(TAG, "Route is visible: " + info);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mediaRouteMenuItem.isVisible()) {
                                Log.d(TAG, "Cast Icon is visible: " + info.getName());
                                showFtu();
                            }
                        }
                    }, 1000);
                }
                */
            }

            @Override
            public void onDataMessageReceived(String message) {
                Log.d(TAG, "onMessageReceived: " + message);
                WebAppFragment webAppFragment = (WebAppFragment) getSupportFragmentManager().findFragmentById(R.id.webapp);
                webAppFragment.webView.loadUrl("javascript:showJavaScriptMessage('Called from Cast: " + message + "')");

                if ("notification".equals(message)) {
                    Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);
                    NotificationUtil.sendNotification(MainActivity.this, resultIntent, message);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        };
        mCastManager.reconnectSessionIfPossible();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //menu.findItem(R.id.action_show_queue).setVisible(mCastManager.isConnected());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_call_javascript:
                // Call JavaScript function on web page
                WebAppFragment webAppFragment = (WebAppFragment) getSupportFragmentManager().findFragmentById(R.id.webapp);
                webAppFragment.webView.loadUrl("javascript:showJavaScriptMessage('Call from Android to JavaScript')");
                return true;
            case R.id.action_settings:
                //openSettings();
                /*
                i = new Intent(MainActivity.this, CastPreference.class);
                startActivity(i);
                break;
                */
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mCastManager.onDispatchVolumeKeyEvent(event, CastApplication.VOLUME_INCREMENT)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    // Trigger the discovery of devices by adding the MediaRouter.Callback to the MediaRouter instance
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        // Start media router discovery
        //mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    // Remove the MediaRouter.Callback when done
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        // End media router discovery
        //mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        mCastManager = VideoCastManager.getInstance();
        if (null != mCastManager) {
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() was called");
        mCastManager.decrementUiCounter();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy is called");

        if (null != mCastManager) {
            /*
            mMini.removeOnMiniControllerChangedListener(mCastManager);
            mCastManager.removeMiniController(mMini);
            */
        }
        super.onDestroy();
    }

    /**
     * Send a text message to the receiver
     * @param message
     */
    protected void sendMessageToReceiver(String message) {
        try {
            mCastManager.sendDataMessage(message);
        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

}
