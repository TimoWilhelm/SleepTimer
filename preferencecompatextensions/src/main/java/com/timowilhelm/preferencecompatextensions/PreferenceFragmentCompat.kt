package com.timowilhelm.preferencecompatextensions

import android.support.v4.app.DialogFragment
import android.support.v7.preference.NumberPickerPreference
import android.support.v7.preference.NumberPickerPreferenceDialogFragmentCompat
import android.support.v7.preference.Preference

abstract class PreferenceFragmentCompat : android.support.v7.preference.PreferenceFragmentCompat() {

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is NumberPickerPreference) {
            dialogFragment = NumberPickerPreferenceDialogFragmentCompat
                    .newInstance(preference.key)
        }
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(fragmentManager,
                    "android.support.v7.preference" + ".PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

}


