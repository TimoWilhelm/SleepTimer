package com.timowilhelm.sleeptimer

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.support.v7.preference.PreferenceManager
import android.widget.Toast

class SleepTimerAdminReceiver : DeviceAdminReceiver() {
    private fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onEnabled(context: Context, intent: Intent) {
        showToast(context,
                context.getString(R.string.admin_receiver_status_enabled))
    }

    override fun onDisabled(context: Context, intent: Intent) {
        showToast(context,
                context.getString(R.string.admin_receiver_status_disabled))
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean("turn_off_screen",false)
                .apply()
    }


}