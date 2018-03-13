package com.timowilhelm.sleeptimer

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.Preference.OnPreferenceClickListener
import com.timowilhelm.preferencecompatextensions.PreferenceFragmentCompat


class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var turnOffScreenPreference: SwitchPreference
    private lateinit var licencePreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        turnOffScreenPreference = findPreference("turn_off_screen") as SwitchPreference
        turnOffScreenPreference.onPreferenceChangeListener = Preference
                .OnPreferenceChangeListener(fun(_, newValue): Boolean {
                    if (!(newValue as Boolean)) return true

                    val policyManager = activity?.getSystemService(Context.DEVICE_POLICY_SERVICE)
                            as DevicePolicyManager

                    val adminReceiver = ComponentName(context, SleepTimerAdminReceiver::class.java)

                    if (!policyManager.isAdminActive(adminReceiver)) {
                        askForDeviceAdmin(adminReceiver)
                    }
                    return policyManager.isAdminActive(adminReceiver)
                })

        licencePreference = findPreference("openSourceLicenses") as Preference
        licencePreference.onPreferenceClickListener = OnPreferenceClickListener { _ ->
            run {
                displayLicensesDialogFragment()
                true
            }
        }
    }

    private fun askForDeviceAdmin(adminReceiver: ComponentName) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Need device admin permission to turn off the screen")
        startActivityForResult(intent, 0)
    }

    private fun displayLicensesDialogFragment() {
        val dialog = LicensesDialogFragment.newInstance()
        dialog.show(fragmentManager, "LicensesDialog")
    }
}
