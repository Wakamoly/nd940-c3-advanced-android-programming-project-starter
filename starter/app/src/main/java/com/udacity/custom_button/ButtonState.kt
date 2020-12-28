package com.udacity.custom_button

import androidx.annotation.StringRes
import com.udacity.R


sealed class ButtonState(@StringRes internal val textID: Int) {
    object Init : ButtonState(R.string.select_a_repo)
    object Clicked : ButtonState(R.string.button_name)
    object Loading : ButtonState(R.string.button_loading)
    object Completed : ButtonState(R.string.download_complete)
    object Failed : ButtonState(R.string.failure)
}