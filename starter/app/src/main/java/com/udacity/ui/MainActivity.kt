package com.udacity.ui

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
import com.udacity.utils.Util.isURL
import com.udacity.utils.Util.makeHTTP
import com.udacity.utils.Util.receive
import com.udacity.utils.Util.showToast
import com.udacity.utils.Util.verifyStoragePermission
import com.udacity.utils.notifications.NotificationUtil.createNotificationChannel
import com.udacity.utils.notifications.NotificationUtil.sendNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var urlSelected: String? = null

    private lateinit var downloadButton : LoadingButton
    private lateinit var notificationManager: NotificationManager
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
            when (radio_group.checkedRadioButtonId) {
                R.id.rb_glide_dl -> {
                    urlSelected = glideUrl
                    download()
                }
                R.id.rb_loadapp_dl -> {
                    urlSelected = loadAppUrl
                    download()
                }
                R.id.rb_retrofit_dl -> {
                    urlSelected = retrofitUrl
                    download()
                }
                R.id.rb_custom_dl -> {
                    val url = custom_url_et.text.toString().trim()
                    if (url.isURL()) {
                        urlSelected = url
                        download()
                    } else {
                        downloadButton.setLoadingButtonState(ButtonState.Init)
                        showToast(getString(R.string.select_a_valid_url))
                    }
                }
            }
        }

        rb_custom_dl.setOnCheckedChangeListener { _, isChecked ->
            custom_url_et.visibility = if (isChecked) {
                View.VISIBLE
            } else {
                custom_url_et.text = null
                View.GONE
            }
        }

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id){

                if (intent.receive(intent.action.toString(), context)){
                    // Yay!
                    downloadButton.setLoadingButtonState(ButtonState.Completed)
                    val notificationModel = NotificationModel(
                        urlSelected.toString(),
                        getString(R.string.success)
                    )
                    notificationManager.sendNotification(applicationContext, notificationModel)
                }else{
                    // Boo!
                    downloadButton.setLoadingButtonState(ButtonState.Failed)
                    val notificationModel = NotificationModel(
                        urlSelected.toString(),
                        getString(R.string.failure)
                    )
                    notificationManager.sendNotification(applicationContext, notificationModel)
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun download() {
        if (!urlSelected.isNullOrBlank()){

            if (!verifyStoragePermission()) {
                // return if permissions have not been given
                return
            }

            val urlString = urlSelected?.makeHTTP()

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
                DownloadManager.Request(Uri.parse(urlString))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        getString(R.string.stashed_repos)
                    )
            // enqueue puts the download request in the queue.
            downloadID = downloadManager.enqueue(request)

            val downloadProgressLD = DownloadProgressLiveData(application, downloadID)
            downloadProgressLD.observe(this, Observer {
                var progress = ((it.bytesDownloadedSoFar * 100L) / it.totalSizeBytes).toFloat()
                if (it.totalSizeBytes != (-1).toLong()){
                    progress = (progress / 100)
                    downloadButton.setButtonProgress(progress)
                }else if (it.bytesDownloadedSoFar > 1) {
                    downloadButton.addButtonProgress(0.05f)
                }

            })

        }else{
            downloadButton.setLoadingButtonState(ButtonState.Init)
            showToast(getString(R.string.nothing_selected))
        }

    }

    // radio button clicks from XML
    fun onRBClicked(view: View) {
        if (view is RadioButton && downloadButton.buttonState == ButtonState.Init){
            downloadButton.setLoadingButtonState(ButtonState.Clicked)
        }
    }

    companion object {
        const val glideUrl = "https://github.com/bumptech/glide/archive/master.zip"
        const val loadAppUrl = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        const val retrofitUrl = "https://github.com/square/retrofit/archive/master.zip"
    }

}
