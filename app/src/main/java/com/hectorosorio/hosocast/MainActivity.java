package com.hectorosorio.hosocast;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;

import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;
import com.hectorosorio.hosocast.utils.NotificationUtil;
import com.hectorosorio.hosocast.webapp.WebAppFragment;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private VideoCastManager mCastManager;
    private VideoCastConsumer mCastConsumer;
    private MiniController mMini;
    private MenuItem mediaRouteMenuItem;
    private MediaRouteActionProvider mMediaRouteActionProvider;

    /**
     * Web App CONNECTION_STATE options:
     *    active = 3
     *    loading = 2
     *    inactive = 1
     *    unavailable = 0
     *    error = -1
     */
    public static final int APPLICATION_CONNECTED = 3;
    public static final int APPLICATION_CONNECT_FAILED = -1;
    public static final int APPLICATION_DISCONNECTED = 1;
    public static final int CONNECTED = 3;
    public static final int DISCONNECTED = 1;
    public static final int CONNECTION_FAILED = -1;
    public static final int CAST_DEVICE_DETECTED = 1;
    public static final int CONNECTION_SUSPENDED = 0;
    public static final int CONNECTION_RECOVERED = 1;
    public static final int CAST_DEVICE_SELECTED = 2;
    public static final int CAST_DEVICE_AVAILABLE = 1;
    public static final int CAST_DEVICE_UNAVAILABLE = 0;
    public static final int FAILED = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VideoCastManager.checkGooglePlayServices(this);

        mCastManager = VideoCastManager.getInstance();

        // -- Adding MiniController
        mMini = (MiniController) findViewById(R.id.miniController1);
        mCastManager.addMiniController(mMini);

        mCastConsumer = new VideoCastConsumerImpl() {

            /**
             * {@link com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumer}
             */

            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId,
                                               boolean wasLaunched) {
                Log.d(TAG, "onApplicationConnected() sessionId: " + sessionId + ", wasLaunched: " + wasLaunched
                        + ", appMetadata: " + (null != appMetadata ? appMetadata.getName() : null));
                invalidateOptionsMenu();
                // 8 !!! true
                notifyWedApp(APPLICATION_CONNECTED);
            }

            @Override
            public void onApplicationConnectionFailed(int errorCode){
                Log.d(TAG, "onApplicationConnectionFailed() errorCode: " + errorCode);
                notifyWedApp(APPLICATION_CONNECT_FAILED);
            }

            @Override
            public void onApplicationStatusChanged(String appStatus) {
                Log.d(TAG, "onApplicationStatusChanged() appStatus: " + appStatus);
                // 7 !!! "", null, "Ready to play" - called multiple times
            }

            @Override
            public void onApplicationDisconnected(int errorCode) {
                Log.d(TAG, "onApplicationDisconnected() errorCode: " + errorCode);
                notifyWedApp(APPLICATION_DISCONNECTED);
            }

            @Override
            public void onRemoteMediaPlayerMetadataUpdated() {
                Log.d(TAG, "onRemoteMediaPlayerMetadataUpdated()");
                // 9 !!!
                try {
                    RemoteMediaPlayer remoteMediaPlayer = mCastManager.getRemoteMediaPlayer();
                    if (remoteMediaPlayer != null) {
                        MediaStatus mediaStatus= remoteMediaPlayer.getMediaStatus();
                        MediaInfo mediaInfo = remoteMediaPlayer.getMediaInfo();
                        MediaMetadata mediaMetadata = mediaInfo.getMetadata();
                        Log.d(TAG, "onRemoteMediaPlayerMetadataUpdated(): state=" + mediaStatus.getPlayerState() + ", title=" + mediaMetadata.getString(MediaMetadata.KEY_TITLE));
                    }
                } catch (Exception e) {
                    // silent
                }
            }

            @Override
            public void onRemoteMediaPlayerStatusUpdated() {
                Log.d(TAG, "onRemoteMediaPlayerStatusUpdated()");
                // 10
                try {
                    RemoteMediaPlayer remoteMediaPlayer = mCastManager.getRemoteMediaPlayer();
                    if (remoteMediaPlayer != null) {
                        MediaStatus mediaStatus= remoteMediaPlayer.getMediaStatus();
                        MediaInfo mediaInfo = remoteMediaPlayer.getMediaInfo();
                        MediaMetadata mediaMetadata = mediaInfo.getMetadata();
                        Log.d(TAG, "onRemoteMediaPlayerStatusUpdated(): state=" + mediaStatus.getPlayerState() + ", title=" + mediaMetadata.getString(MediaMetadata.KEY_TITLE));
                    }
                } catch (Exception e) {
                    // silent
                }
            }

            @Override
            public void onVolumeChanged(double value, boolean isMute) {
                Log.d(TAG, "onVolumeChanged() value: " + value + ", isMute: " + isMute);
                // 6 !!!
            }

            @Override
            public void onApplicationStopFailed(int errorCode) {
                Log.d(TAG, "onApplicationStopFailed() errorCode: " + errorCode);
            }

            @Override
            public void onDataMessageSendFailed(int errorCode) {
                Log.d(TAG, "onDataMessageSendFailed() errorCode: " + errorCode);
            }

            @Override
            public void onDataMessageReceived(String message) {
                Log.d(TAG, "onMessageReceived(): " + message);
                WebAppFragment webAppFragment = (WebAppFragment) getSupportFragmentManager().findFragmentById(R.id.webapp);
                webAppFragment.callWebAppFunction("showJavaScriptMessage('Called from Cast: " + message + "')");

                if ("notification".equals(message)) {
                    Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);
                    NotificationUtil.sendNotification(MainActivity.this, resultIntent, message);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onMediaLoadResult(int statusCode) {
                Log.d(TAG, "onMediaLoadResult() statusCode: " + statusCode);
            }

            /**
             * {@link com.google.android.libraries.cast.companionlibrary.cast.callbacks.BaseCastConsumer}
             */

            @Override
            public void onConnected() {
                Log.d(TAG, "onConnected()");
                notifyWedApp(CONNECTED);
                // 5
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "onDisconnected()");
                invalidateOptionsMenu();
                notifyWedApp(DISCONNECTED);
            }

            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Log.d(TAG, "onConnectionFailed() result=" + result);
                notifyWedApp(CONNECTION_FAILED);
            }

            @Override
            public void onCastDeviceDetected(final MediaRouter.RouteInfo info) {
                Log.d(TAG, "onCastDeviceDetected(): " + info);
                notifyWedApp(CAST_DEVICE_DETECTED);
                // 3 !!! gets a list of devices, webapp loads right after this
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
            public void onCastAvailabilityChanged(boolean castPresent) {
                Log.d(TAG, "onCastAvailabilityChanged() castPresent=" + castPresent);
                // 2 !!! if true are devices
                if (castPresent) {
                    notifyWedApp(CAST_DEVICE_AVAILABLE);
                }
                else {
                    notifyWedApp(CAST_DEVICE_UNAVAILABLE);
                }
            }

            @Override
            public void onConnectionSuspended(int cause) {
                Log.d(TAG, "onConnectionSuspended() cause: " + cause);
                com.hectorosorio.hosocast.utils.Utils.showToast(MainActivity.this, R.string.connection_temp_lost);
                notifyWedApp(CONNECTION_SUSPENDED);
            }

            @Override
            public void onConnectivityRecovered() {
                Log.d(TAG, "onConnectivityRecovered()");
                com.hectorosorio.hosocast.utils.Utils.showToast(MainActivity.this, R.string.connection_recovered);
                notifyWedApp(CONNECTION_RECOVERED);
            }

            @Override
            public void onUiVisibilityChanged(boolean visible) {
                Log.d(TAG, "onUiVisibilityChanged() visible=" + visible);
                // 1 !!! when app is in foreground = true
            }

            @Override
            public void onReconnectionStatusChanged(int status) {
                Log.d(TAG, "onReconnectionStatusChanged() status=" + status);
                //
            }

            @Override
            public void onDeviceSelected(CastDevice device) {
                Log.d(TAG, "onDeviceSelected() castPresent=" + (null != device ? device.getFriendlyName() : null));
                // 4 !!!
                notifyWedApp(CAST_DEVICE_SELECTED);
            }

            @Override
            public void onFailed(int resourceId, int statusCode) {
                String reason = "Not Available";
                if (resourceId > 0) {
                    reason = getString(resourceId);
                }
                Log.e(TAG, "onFailed() Action failed, reason:  " + reason + ", status code: " + statusCode);
                notifyWedApp(FAILED);
            }
        };
        mCastManager.reconnectSessionIfPossible();
    }

    private void notifyWedApp(int status) {
        WebAppFragment webAppFragment = (WebAppFragment) getSupportFragmentManager().findFragmentById(R.id.webapp);
        webAppFragment.notifyWebApp(status);
    }

    public void showCastDialog() {
        Log.d(TAG, "showCastDialog");
        Log.d(TAG, "showCastDialog visible=" + mMediaRouteActionProvider.isVisible());
        MediaRouteButton button = mMediaRouteActionProvider.getMediaRouteButton();
        if (button != null) {
            button.showDialog();
        }
        Log.d(TAG, "startCastActivity visible=" + mMediaRouteActionProvider.isVisible());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        mMediaRouteActionProvider = (MediaRouteActionProvider)
                MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        //mMediaRouteActionProvider.setRouteSelector(mCastManager.getMediaRouteSelector());
        //if (mCastManager.getMediaRouteDialogFactory() != null) {
        //    mediaRouteActionProvider.setDialogFactory(mCastManager.getMediaRouteDialogFactory());
        //}

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
                webAppFragment.callWebAppFunction("showJavaScriptMessage('Call from Android to JavaScript')");
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
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return mCastManager.onDispatchVolumeKeyEvent(event, CastApplication.VOLUME_INCREMENT)
                || super.dispatchKeyEvent(event);
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
            //mMini.setOnMiniControllerChangedListener(mCastManager);
            mCastManager.incrementUiCounter();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() was called");
        mCastManager.decrementUiCounter();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        //mMini.removeOnMiniControllerChangedListener(mCastManager);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy is called");

        if (null != mCastManager) {
            mMini.removeOnMiniControllerChangedListener(mCastManager);
            mCastManager.removeMiniController(mMini);
        }
        super.onDestroy();
    }

    /**
     * Send a text message to the receiver
     * @param message
     */
    public void sendMessageToReceiver(String message) {
        try {
            mCastManager.sendDataMessage(message);
        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
