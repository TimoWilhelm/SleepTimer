package com.timowilhelm.preferencecompatextensions

import androidx.fragment.app.DialogFragment
import androidx.preference.NumberPickerPreference
import androidx.preference.NumberPickerPreferenceDialogFragmentCompat
import androidx.preference.Preference


abstract class PreferenceFragmentCompat : androidx.preference.PreferenceFragmentCompat() {

    companion object {
        private const val DIALOG_FRAGMENT_TAG = "android.support.v7.preference" + ".PreferenceFragment.DIALOG"
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is NumberPickerPreference) {
            dialogFragment = NumberPickerPreferenceDialogFragmentCompat
                    .newInstance(preference.key)
        }
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}


