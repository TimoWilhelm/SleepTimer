package com.timowilhelm.sleeptimer

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.webkit.WebView


class LicensesDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater
                .from(activity).inflate(R.layout.dialog_licenses, null) as WebView
        view.loadUrl("file:///android_asset/open_source_licenses.html")
        return AlertDialog.Builder(context as Context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(getString(R.string.preference_open_source_licenses_title))
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create()
    }

    companion object {

        fun newInstance(): LicensesDialogFragment {
            return LicensesDialogFragment()
        }
    }

}