package com.prem.ylan.player.helpers

import android.view.View

class DoubleClickListener(
    private val doubleClickTimeLimitMills: Long,
    private val callback: DoubleClickCallback
) :
    View.OnClickListener {
    private var lastClicked = -1L
    override fun onClick(view: View) {
        lastClicked = if (lastClicked == -1L) {
            System.currentTimeMillis()
        } else if (isDoubleClicked) {
            callback.doubleClicked()
            -1L
        } else {
            System.currentTimeMillis()
        }
    }

    private fun getTimeDiff(from: Long, to: Long): Long {
        return to - from
    }

    private val isDoubleClicked: Boolean
        private get() = getTimeDiff(
            lastClicked,
            System.currentTimeMillis()
        ) <= doubleClickTimeLimitMills

    fun interface DoubleClickCallback {
        fun doubleClicked()
    }
}
