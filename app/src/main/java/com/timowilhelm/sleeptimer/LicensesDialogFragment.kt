package com.timowilhelm.sleeptimer

import android.app.Dialog
import android.view.LayoutInflater
import android.webkit.WebView
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog


class LicensesDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_licenses, null) as WebView
        view.loadUrl("file:///android_asset/open_source_licenses.html")
        return AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(getString(R.string.openSourceLicenses))
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