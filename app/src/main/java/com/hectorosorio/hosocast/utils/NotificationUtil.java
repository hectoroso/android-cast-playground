package com.hectorosorio.hosocast.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.hectorosorio.hosocast.MainActivity;
import com.hectorosorio.hosocast.R;

/**
 * Created by hector on 9/1/15.
 */
public class NotificationUtil {

    private Activity activity = null;

    public NotificationUtil(Activity activity) {
        this.activity = activity;
    }

    private static int numClicks = 0;
    // Sets an ID for the notification
    private static int mNotificationId = 001;
    public static void sendNotification(Activity activity, Intent intent, String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity);

        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setCategory(NotificationCompat.CATEGORY_ALARM); // CATEGORY_PROGRESS?

        //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //mBuilder.setSound(alarmSound);

        mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);

        mBuilder.setContentTitle("Notifications Sample");
        mBuilder.setContentText(message);
        //mBuilder.setSubText("Tap to view documentation about notifications.");

        mBuilder.setNumber(++numClicks);

        /** Set the icon that will appear in the notification bar. This icon also appears
         * in the lower right hand corner of the notification itself.
         *
         * Important note: although you can use any drawable as the small icon, Android
         * design guidelines state that the icon should be simple and monochrome. Full-color
         * bitmaps or busy images don't render well on smaller screens and can end up
         * confusing the user.
         */
        mBuilder.setSmallIcon(R.drawable.ic_stat_notification);

        // Set the notification to auto-cancel. This means that the notification will disappear
        // after the user taps it, rather than remaining until it's explicitly dismissed.
        mBuilder.setAutoCancel(true);

        /**
         * Build the notification's appearance.
         * Set the large icon, which appears on the left of the notification. In this
         * sample we'll set the large icon to be the same as our app icon. The app icon is a
         * reasonable default if you don't have anything more compelling to use as an icon.
         */
        //mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));


        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String[] events = new String[2];
        events[0] = "Zero";
        events[1] = "One";
        // Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle("Event tracker details:");
        // Moves events into the expanded layout
        for (int i=0; i < events.length; i++) {

            inboxStyle.addLine(events[i]);
        }
        // Moves the expanded layout object into the notification object.
        mBuilder.setStyle(inboxStyle);



        // Create an intent that will be fired when the user clicks the notification.
        //Intent intent = new Intent(Intent.ACTION_VIEW,
        //        Uri.parse("http://developer.android.com/reference/android/app/Notification.html"));
        //Intent resultIntent = new Intent(getActivity(), MainActivity.class);

        /*
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ResultActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        */
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        activity,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Set the intent that will fire when the user taps the notification.
        mBuilder.setContentIntent(resultPendingIntent);

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = getNotificationManager(activity);

        /**
         * Builds the notification and issues it. This will immediately display the notification icon in the
         * notification bar.
         */
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    public void cancelNotification(Activity activity) {
        NotificationManager mNotifyMgr = getNotificationManager(activity);
        mNotifyMgr.cancel(mNotificationId);
    }

    private static NotificationManager getNotificationManager(Activity activity) {
        return (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);
    }
}
