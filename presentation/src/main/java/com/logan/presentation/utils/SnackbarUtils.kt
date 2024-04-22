package com.logan.presentation.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

object SnackbarUtils {

    private const val DURATION_SHORT = Snackbar.LENGTH_SHORT
    private const val DURATION_LONG = Snackbar.LENGTH_LONG

    // 显示短时Snackbar
    fun showShortSnackbar(view: View, message: String) {
        showSnackbar(view, message, DURATION_SHORT)
    }

    // 显示长时Snackbar
    fun showLongSnackbar(view: View, message: String) {
        showSnackbar(view, message, DURATION_LONG)
    }

    // 显示Snackbar
    private fun showSnackbar(view: View, message: String, duration: Int) {
        Snackbar.make(view, message, duration).show()
    }

    // 显示带Action的Snackbar
    fun showActionSnackbar(
        view: View,
        message: String,
        actionText: String,
        actionTextColor: Int,
        listener: View.OnClickListener
    ) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction(actionText, listener)
            .setActionTextColor(actionTextColor)
            .show()
    }
}
