package com.example.housevisitormanagementsystem.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.example.housevisitormanagementsystem.R

object Loader {
    private var dialog: Dialog? = null

    /**
     * Shows a non-cancelable loader dialog.
     * @param context The context where the loader should be shown.
     */
    fun showLoader(context: Context) {
        if (dialog != null && dialog!!.isShowing) return

        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.layout_loader)
            window?.let {
                it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
            setCancelable(false)
            show()
        }
    }

    /**
     * Closes the active loader dialog if it's showing.
     */
    fun closeLoader() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
            dialog = null
        }
    }
}
