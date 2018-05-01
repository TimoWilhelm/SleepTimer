package com.timowilhelm.sleeptimer

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.support.v7.preference.PreferenceManager

class SleepTimerAdminReceiver : DeviceAdminReceiver() {

    override fun onDisabled(context: Context, intent: Intent) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.preference_turn_off_screen_key), false)
                .apply()
    }
}