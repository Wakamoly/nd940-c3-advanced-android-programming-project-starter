package com.udacity.ui

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.udacity.R
import com.udacity.custom_button.ButtonState
import com.udacity.custom_button.LoadingButton
import com.udacity.models.NotiChannelDetails
import com.udacity.models.NotificationModel
import com.udacity.utils.DownloadProgressLiveData
import com.udacity.utils.Util.receive
import com.udacity.utils.Util.showToast
import com.udacity.utils.Util.verifyPermissions
import com.udacity.utils.notifications.NotificationUtil.createNotificationChannel
import com.udacity.utils.notifications.NotificationUtil.sendNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var urlSelected: String? = null

    private lateinit var downloadButton : LoadingButton
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent : PendingIntent
    private lateinit var action : NotificationCompat.Action
    private val downloadManager by lazy {
        application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        downloadButton = findViewById(R.id.custom_button)
        downloadButton.setOnClickListener {
            downloadButton.setLoadingButtonState(ButtonState.Clicked)
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id){

                /*if (intent.receive(intent.action.toString(), context)){
                    downloadButton.setLoadingButtonState(ButtonState.Completed)
                    val notificationModel = NotificationModel(
                        urlSelected.toString(),
                        getString(R.string.success)
                    )
                    notificationManager.sendNotification(applicationContext, notificationModel)
                }else{
                    downloadButton.setLoadingButtonState(ButtonState.Failed)
                    val notificationModel = NotificationModel(
                        urlSelected.toString(),
                        getString(R.string.failure)
                    )
                    notificationManager.sendNotification(applicationContext, notificationModel)
                }*/

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun download() {
        if (urlSelected != null){

            if (!verifyPermissions()) {
                // return if permissions have not been given
                return
            }

            // Ensure our button reflects that we are now loading
            downloadButton.setLoadingButtonState(ButtonState.Loading)

            // Create a notification manager
            notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            ) as NotificationManager

            // Create a notification channel for And. O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotiChannelDetails(
                    applicationContext.getString(R.string.notification_channel_id),
                    applicationContext.getString(R.string.notification_channel_name),
                    applicationContext.getString(R.string.notification_channel_desc),
                    NotificationManager.IMPORTANCE_HIGH,
                    NotificationCompat.PRIORITY_HIGH,
                    NotificationCompat.VISIBILITY_PUBLIC
                )
                // Noti util to create a channel with the values we put above
                createNotificationChannel(applicationContext, channel)
            }

            // If directory doesn't exist, create path
            val file = File(getExternalFilesDir(null), getString(R.string.stashed_repo_folder))
            if (!file.exists()) {
                file.mkdirs()
            }

            // Create a download request
            val request =
                DownloadManager.Request(Uri.parse(urlSelected))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        // TODO: 12/22/20 Get file name and extension
                        getString(R.string.stashed_repos)
                    )
            // enqueue puts the download request in the queue.
            downloadID = downloadManager.enqueue(request)

            val downloadProgressLD = DownloadProgressLiveData(application, downloadID)
            downloadProgressLD.observe(this, Observer {
                when (it.status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        downloadButton.setLoadingButtonState(ButtonState.Completed)
                        val notificationModel = NotificationModel(
                            urlSelected.toString(),
                            getString(R.string.success)
                        )
                        notificationManager.sendNotification(applicationContext, notificationModel)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        downloadButton.setLoadingButtonState(ButtonState.Failed)
                        val notificationModel = NotificationModel(
                            urlSelected.toString(),
                            getString(R.string.failure)
                        )
                        notificationManager.sendNotification(applicationContext, notificationModel)
                    }
                    else -> {
                        var progress = ((it.bytesDownloadedSoFar * 100L) / it.totalSizeBytes).toFloat()
                        Log.d("TAG", "download: $progress")
                        if (it.totalSizeBytes != (-1).toLong()){
                            progress = (progress / 100)
                            downloadButton.setButtonProgress(progress)
                            Log.d("TAG", "download2: $progress")
                        }else {
                            downloadButton.addButtonProgress(0.05f)
                        }
                    }
                }
            })

        }else{
            downloadButton.setLoadingButtonState(ButtonState.Completed)
            showToast(getString(R.string.nothing_selected))
        }

    }

    fun onRBClicked(view: View) {
        if (view is RadioButton){
            when (view.id) {
                R.id.rb_glide_dl -> {
                    if (view.isChecked) urlSelected = getString(R.string.glide_url)
                }
                R.id.rb_loadapp_dl -> {
                    if (view.isChecked) urlSelected = getString(R.string.loadapp_url)
                }
                R.id.rb_retrofit_dl -> {
                    if (view.isChecked) urlSelected = getString(R.string.retrofit_url)
                }
            }
        }
    }

}
