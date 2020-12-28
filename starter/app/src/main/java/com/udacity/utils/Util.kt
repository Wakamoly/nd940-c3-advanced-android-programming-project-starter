package com.udacity.utils

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.udacity.R
import com.udacity.utils.Util.isURL
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL


object Util {

    fun Intent.receive(action: String, context: Context) : Boolean{
        var result = false
        if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val query = DownloadManager.Query()
            query.setFilterById(getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0))
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val cursor: Cursor = manager.query(query)
            if (cursor.moveToFirst()) {
                if (cursor.count > 0) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) result = true
                }
            }
        }
        return result
    }

    fun Activity.showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun ObjectAnimator.disableViewDuringAnimation(view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                view.isEnabled = true
            }
        })
    }

    fun Activity.verifyStoragePermission(): Boolean {

        // This will return the current Status
        val permissionExternalMemory = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
            val storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            // If permission not granted then ask for permission in real time.
            showToast(resources.getString(R.string.no_permission))
            ActivityCompat.requestPermissions(this, storagePermissions, 1)
            return false
        }
        return true
    }

    fun String.isURL(): Boolean {
        val thisString = this.makeHTTP()
        return try {
            URL(thisString).toURI()
            Patterns.WEB_URL.matcher(thisString).matches()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            false
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            false
        }
    }

    fun String.makeHTTP(): String {
        var tempString = this
        if (!this.startsWith("http")) {
            tempString = "https://$tempString"
        }
        return tempString
    }

}