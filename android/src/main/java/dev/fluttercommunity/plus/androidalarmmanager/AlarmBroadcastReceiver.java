// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package dev.fluttercommunity.plus.androidalarmmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import androidx.core.app.NotificationCompat;
import android.media.AudioAttributes;
import android.os.Build.VERSION_CODES;
import android.os.Build.VERSION;

import android.view.WindowManager;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
  /**
   * Invoked by the OS when a timer goes off.
   *
   * <p>
   * The associated timer was registered in {@link AlarmService}.
   *
   * <p>
   * In Android, timer notifications require a {@link BroadcastReceiver} as the
   * artifact that is
   * notified when the timer goes off. As a result, this method is kept simple,
   * immediately
   * offloading any work to
   * {@link AlarmService#enqueueAlarmProcessing(Context, Intent)}.
   *
   * <p>
   * This method is the beginning of an execution path that will eventually
   * execute a desired
   * Dart callback function, as registered by the Dart side of the
   * android_alarm_manager plugin.
   * However, there may be asynchronous gaps between {@code onReceive()} and the
   * eventual invocation
   * of the Dart callback because {@link AlarmService} may need to spin up a
   * Flutter execution
   * context before the callback can be invoked.
   */

  @Override
  public void onReceive(Context context, Intent intent) {
    // Log.d("flutter", "alarm-debugging:onReceive");
    AlarmFlagManager.set(context, intent);

    // PowerManager powerManager = (PowerManager)
    // context.getSystemService(Context.POWER_SERVICE);
    // PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
    // WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
    // PowerManager.ACQUIRE_CAUSES_WAKEUP |
    // PowerManager.ON_AFTER_RELEASE,
    // "AlarmBroadcastReceiver:My wakelock");

    runApp(context.getApplicationContext());

    // wakeLock.acquire();
    // try {
    // Log.d("flutter", "alarm-debugging:startActivity");
    // context.startActivity(startIntent);
    // } catch (Exception e) {
    // Log.d("flutter", e.toString());
    // }

    //알람 콜백에서 ForegroundTask 실행이나 알람 재등록 등의 처리를 위해선 이게 필요
    AlarmService.enqueueAlarmProcessing(context, intent);

    // wakeLock.release();
    // if (Build.VERSION.SDK_INT < 31) {
    // context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    // }
  }

  private void runApp(Context context) {
    Intent startIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
    startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    int flags = PendingIntent.FLAG_UPDATE_CURRENT;

    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      flags |= PendingIntent.FLAG_IMMUTABLE;
    }

    PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0, startIntent, flags);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      // Log.d("flutter", "alarm-debugging:notification in BroadCastReceiver");
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

      AudioAttributes attributes = new AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .setUsage(AudioAttributes.USAGE_ALARM)
          .build();

      assert notificationManager != null;

      String CHANNEL_ID = "NOTIFICATION_CHANNEL_ID";
      String CHANNEL_NAME = "Water Reminder Notification";

      NotificationChannel mChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
      if (mChannel == null) {
        mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(mChannel);
      }
      mChannel.setSound(null, null);

      NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

      builder
          .setSmallIcon(context.getResources().getIdentifier("ic_clock", "drawable", context.getPackageName()))
          .setColor(0XFF255AF5)
          .setContentTitle("Time to drink water!")
          .setContentText("Hydrate now for a healthier you!")
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setCategory(NotificationCompat.CATEGORY_CALL)
          .setVibrate(new long[] { 1000, 1000, 1000})
          .setContentIntent(fullScreenPendingIntent)
          .setFullScreenIntent(fullScreenPendingIntent, true)
          .setAutoCancel(true)
          .setOngoing(true)
          .setSound(null);
      
      Notification notification = builder.build();
      notificationManager.notify(0, notification);
    } else {

      // Log.d("flutter", "alarm-debugging:startActiviy");
      context.startActivity(startIntent);

    }
  }
}
