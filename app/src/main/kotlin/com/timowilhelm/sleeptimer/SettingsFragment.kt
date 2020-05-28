package com.timowilhelm.sleeptimer

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.SwitchPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceManager
import com.timowilhelm.preferencecompatextensions.PreferenceFragmentCompat


class SettingsFragment : PreferenceFragmentCompat() {

    companion object RequestCodes {
        const val REQUEST_TURN_OFF_SCREEN = 100
    }

    private var turnOffScreenPreference: SwitchPreference? = null
    private var licencePreference: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        turnOffScreenPreference = findPreference(getString(R.string.preference_turn_off_screen_key))
        turnOffScreenPreference?.onPreferenceChangeListener = Preference
                .OnPreferenceChangeListener(fun(_, newValue): Boolean {
                    if (!(newValue as Boolean)) return true

                    val policyManager = activity?.getSystemService(Context.DEVICE_POLICY_SERVICE)
                            as DevicePolicyManager

                    val adminReceiver = ComponentName(requireContext(), SleepTimerAdminReceiver::class.java)

                    if (!policyManager.isAdminActive(adminReceiver)) {
                        askForDeviceAdmin(adminReceiver)
                    }
                    return policyManager.isAdminActive(adminReceiver)
                })

        licencePreference = findPreference(getString(R.string.preference_open_source_licenses_key))
        licencePreference?.onPreferenceClickListener = OnPreferenceClickListener { _ ->
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
                getString(R.string.device_admin_explanation))
        startActivityForResult(intent, REQUEST_TURN_OFF_SCREEN)
    }

    private fun displayLicensesDialogFragment() {
        val dialog = LicensesDialogFragment.newInstance()
        dialog.show(parentFragmentManager, "LicensesDialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TURN_OFF_SCREEN -> {
                if (resultCode == Activity.RESULT_OK) {
                    findPreference<SwitchPreference>(getString(R.string.preference_turn_off_screen_key))?.isChecked = true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if setting has been changed by DeviceAdminReceiver and update UI
        findPreference<SwitchPreference>(getString(R.string.preference_turn_off_screen_key))?.isChecked =
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(getString(R.string.preference_turn_off_screen_key), false)
    }
}
