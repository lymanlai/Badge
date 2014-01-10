/*
    Copyright 2013 appPlant UG

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
*/

package de.appplant.cordova.plugin.badge;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;


public class Badge extends CordovaPlugin {

    /**
     * Feste ID der Notification, damit sie wiedergefunden und gelöscht werden kann
     */
    static final int ID = -450793490;

    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equalsIgnoreCase("setBadge")) {
            int number = args.optInt(0);

            if (number == 0) {
                clearBadge();
            } else {
                setBadge(number);
            }

            return true;
        }

        // Returning false results in a "MethodNotFound" error.
        return false;
    }

    /**
     * Erstellt eine Notification mit der Badgezahl.
     *
     * @param badge Die anzuzeigende Zahl
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setBadge (int badge) {
        Context context             = cordova.getActivity().getApplicationContext();
        String title                = getTitle(badge);

        NotificationManager mgr     = getNotificationManager();

        String packageName          = context.getPackageName();
        Intent launchIntent         = context.getPackageManager().getLaunchIntentForPackage(packageName);
        PendingIntent contentIntent = PendingIntent.getActivity(context, ID, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Builder notification = new Notification.Builder(context)
        .setContentTitle(title)
        .setNumber(badge)
        .setTicker(title)
        .setAutoCancel(true)
        .setSmallIcon(android.R.drawable.ic_dialog_email)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), getDrawableIcon()))
        .setContentIntent(contentIntent);


        if (Build.VERSION.SDK_INT<16) {
            // build notification for HoneyComb to ICS
            mgr.notify(ID, notification.getNotification());
        } else if (Build.VERSION.SDK_INT>15) {
            // Notification for Jellybean and above
            mgr.notify(ID, notification.build());
        }
    }

    /**
     * Löscht die Nachricht mit der Badge Zahl.
     */
    private void clearBadge () {
        getNotificationManager().cancel(ID);
    }

    /**
     * @param badge
     * @return Der Text, welcher als Titel in der Notification angezeigt wird
     */
    private String getTitle (int badge) {
        String title   = "%d new messages";
        int titleResId = getTitleResId();

        if (titleResId > 0) {
            Context context  = cordova.getActivity().getApplicationContext();

            title = context.getString(titleResId);
        }

        return String.format(title, badge);
    }

    /**
     * @return Die ID der lokalisierten Title-Ressource
     */
    private int getTitleResId () {
        Context context  = cordova.getActivity().getApplicationContext();
        String className = context.getPackageName();
        int resId        = 0;

        try {
            Class<?> klass  = Class.forName(className + ".R$string");

            resId = (Integer) klass.getDeclaredField("notification_badge_title").get(Integer.class);
        } catch (Exception e) {}

        return resId;
    }

    /**
     * @return Den NotificationManager der App
     */
    private NotificationManager getNotificationManager () {
        Context context = cordova.getActivity().getApplicationContext();

        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * @return Der Zahlencode des App Icons
     */
    private int getDrawableIcon () {
        Context context  = cordova.getActivity().getApplicationContext();
        String className = context.getPackageName();
        int resId        = android.R.drawable.ic_menu_info_details;

        try {
            Class<?> klass  = Class.forName(className + ".R$drawable");

            resId = (Integer) klass.getDeclaredField("icon").get(Integer.class);
        } catch (Exception e) {}

        return resId;
    }
}
