package cn.fnrice.gpsinfo.ui.components

import android.content.Context
import android.widget.Toast

object ToastUtils {
    private var currentToast: Toast? = null

    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context.applicationContext, message, duration)
        currentToast?.show()
    }

    fun showToast(context: Context, resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context.applicationContext, resId, duration)
        currentToast?.show()
    }
}
