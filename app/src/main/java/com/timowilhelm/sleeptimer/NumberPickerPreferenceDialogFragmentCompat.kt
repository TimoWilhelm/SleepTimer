package com.timowilhelm.sleeptimer

import android.widget.NumberPicker
import android.os.Bundle
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.view.View


class NumberPickerPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    private lateinit var numberPicker : NumberPicker

    companion object{
        fun newInstance(key: String): NumberPickerPreferenceDialogFragmentCompat {
            val fragment = NumberPickerPreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b

            return fragment
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        numberPicker = view.findViewById<View>(R.id.edit) as NumberPicker

        var number: Int? = null
        val preference = preference
        if (preference is NumberPickerPreference) {
            numberPicker.minValue = preference.minValue
            numberPicker.maxValue = preference.maxValue
            numberPicker.wrapSelectorWheel = preference.wrapSelectorWheel
            number = preference.number
        }

        if (number != null) {
            numberPicker.value = number
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val number = numberPicker.value

            val preference = preference
            if (preference is NumberPickerPreference) {
                if (preference.callChangeListener(
                                number)) {
                    preference.number = number
                    preference.onUpdateValue()
                }
            }
        }
    }

}