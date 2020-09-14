package com.mikeni.tictactoe.util

import android.view.View
import com.google.android.material.snackbar.Snackbar


private fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}