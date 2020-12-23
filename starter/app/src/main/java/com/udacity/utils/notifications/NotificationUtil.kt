package com.udacity.utils.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.udacity.ui.DetailActivity
import com.udacity.R
import com.udacity.models.NotiChannelDetails
import com.udacity.models.NotificationModel

object NotificationUtil {

    private val NOTIFICATION_ID = 0
    private val REQUEST_CODE_DL = 0
    private val FLAGS = 0

    /**
     * Builds and delivers the notification.
     *
     * @param applicationContext, context.
     * @param notificationModel, Body of our notification text and status, the status of the notification completion
     * wrapped in a Parcelized data class
     */
    fun NotificationManager.sendNotification(applicationContext: Context, notificationModel: NotificationModel) {

        // Create Intent to open a specific Activity
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        // putting Parcelized extra instead of strings to hold data
        contentIntent.putExtra("notification_model", notificationModel)

        // Create PendingIntent
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create a builder and pass our specific channel id
        val notificationBuilder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(
                R.string.notification_channel_id
            )
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext.getString(R.string.download_complete))
            .setContentText(notificationModel.file_name)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            // we want to have a peek view at our downloaded repo, as well as an announcement
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                NotificationCompat.Action(
                    null,
                    applicationContext.getString(R.string.view_details),
                    contentPendingIntent
                )
            )

        // Notify the user
        notify(NOTIFICATION_ID, notificationBuilder.build())

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context, channelDetails: NotiChannelDetails) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationChannel(
            channelDetails.id,
            channelDetails.name,
            channelDetails.importance
        ).apply {
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
            lightColor = context.getColor(R.color.colorAccent)
            description = channelDetails.description
            lockscreenVisibility = channelDetails.visibility
            notificationManager.createNotificationChannel(this)
        }
    }

    fun clearNotification(context: Context, notificationId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(notificationId)
    }

}