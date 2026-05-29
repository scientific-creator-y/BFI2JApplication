package com.dino.personalmonster.ui

import android.view.View
import androidx.compose.foundation.layout.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

object InsetsUtil {
    fun applyBottomInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(bottom = bars.bottom)

            insets
        }
    }

    fun applySystemBarsInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                top = bars.top,
                bottom = bars.bottom
            )

            insets
        }
    }
}