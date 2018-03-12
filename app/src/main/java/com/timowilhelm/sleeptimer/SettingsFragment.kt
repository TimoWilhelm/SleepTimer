package com.timowilhelm.sleeptimer

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v14.preference.SwitchPreference
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.preference.Preference
import android.support.v7.preference.Preference.OnPreferenceClickListener
import android.util.Log
import android.view.LayoutInflater
import android.webkit.WebView




class SettingsFragment : PreferenceFragmentCompat() {
    lateinit var turnOffScreenPreference: SwitchPreference
    lateinit var licencePreference: Preference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)

        turnOffScreenPreference = findPreference("turn_off_screen") as SwitchPreference
        turnOffScreenPreference.onPreferenceChangeListener = Preference
                .OnPreferenceChangeListener { _, newValue ->
                    askForDeviceAdmin(newValue as Boolean)
                }

        licencePreference = findPreference("openSourceLicenses") as Preference
        licencePreference.onPreferenceClickListener = OnPreferenceClickListener { _ ->
            run {
                displayLicensesDialogFragment()
                true
            }
        }
    }


    private fun askForDeviceAdmin(newValue: Boolean): Boolean {
        if (!newValue) return true
        val policyManager = activity.getSystemService(Context.DEVICE_POLICY_SERVICE)
                as DevicePolicyManager
        val adminReceiver = ComponentName(activity.applicationContext,
                SleepTimerAdminReceiver::class.java)
        if (!policyManager.isAdminActive(adminReceiver)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Need device admin rights to turn off the screen")
            Log.i("asd", "starting now")
            startActivityForResult(intent, 0)
        }
        return policyManager.isAdminActive(adminReceiver)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is NumberPickerPreference) {
            dialogFragment = NumberPickerPreferenceDialogFragmentCompat
                    .newInstance(preference.key)
        }
        if (dialogFragment != null) {
            dialogFragment!!.setTargetFragment(this, 0)
            dialogFragment!!.show(this.fragmentManager,
                    "android.support.v7.preference" + ".PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun displayLicensesDialogFragment() {
        val dialog = LicensesDialogFragment.newInstance()
        dialog.show(fragmentManager, "LicensesDialog")
    }
}
