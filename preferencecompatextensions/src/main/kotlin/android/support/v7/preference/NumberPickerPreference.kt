package androidx.preference

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import com.timowilhelm.preferencecompatextensions.R


class NumberPickerPreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context, attrs) {
    var number: Int = 0
        set(value){
            field = value
            persistInt(number)
        }

    var minValue = 0
    var maxValue = 0
    var wrapSelectorWheel = false
    var subtitle : String = ""

    private val dialogLayoutResId = R.layout.pref_dialog_number

    constructor(context: Context?) : this(context, null)

    init{
        val ta = context!!.obtainStyledAttributes(
                attrs, R.styleable.NumberPickerPreference)
        minValue = ta.getInt(R.styleable.NumberPickerPreference_minValue, minValue)
        maxValue = ta.getInt(R.styleable.NumberPickerPreference_maxValue, maxValue)
        wrapSelectorWheel = ta.getBoolean(R.styleable.NumberPickerPreference_wrapSelectorWheel, wrapSelectorWheel)
        subtitle = ta.getString(R.styleable.NumberPickerPreference_subtitle).toString()
        ta.recycle()
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        // Default value from attribute. Fallback value is set to 0.
        return a!!.getInt(index, 0)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        this.number = if(restorePersistedValue) getPersistedInt(number) else defaultValue as Int
    }

    override fun getDialogLayoutResource(): Int {
        return dialogLayoutResId
    }

    override fun getSummary(): CharSequence? {
        val summary = super.getSummary()
        return if (summary == null) {
            null
        } else {
            String.format(summary.toString(), number)
        }
    }
}
