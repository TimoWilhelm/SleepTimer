package android.support.v7.preference

import android.widget.NumberPicker
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.timowilhelm.preferencecompatextensions.R


class NumberPickerPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    private lateinit var numberPicker: NumberPicker
    private lateinit var subtitleTextView: TextView

    companion object {
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

        numberPicker = view.findViewById(R.id.edit) as NumberPicker
        subtitleTextView = view.findViewById(R.id.numberpicker_subtitle)

        var number: Int? = null
        val preference = preference
        if (preference is NumberPickerPreference) {
            numberPicker.minValue = preference.minValue
            numberPicker.maxValue = preference.maxValue
            numberPicker.wrapSelectorWheel = preference.wrapSelectorWheel
            number = preference.number
            subtitleTextView.text = preference.subtitle
        }

        if (number != null) {
            numberPicker.value = number
            numberPicker.value = number
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val number = numberPicker.value

            val preference = preference
            if (preference is NumberPickerPreference) {
                if (preference.callChangeListener(number)) {
                    preference.number = number
                    preference.notifyChanged()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) dismiss()
    }
}