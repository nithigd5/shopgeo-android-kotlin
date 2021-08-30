package app.shopgeo.`in`.user.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import app.shopgeo.`in`.user.MainActivity
import app.shopgeo.`in`.R

private val NOTIFICATION_ID = 0

fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {
    val contentIntent = Intent(applicationContext, MainActivity::class.java)

//    val contentPendingIntent = PendingIntent.getActivity(
//        applicationContext,
//        NOTIFICATION_ID,
//        contentIntent,
//        PendingIntent.FLAG_UPDATE_CURRENT
//    )
    val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.notification_channel_offers),
            ).setSmallIcon(R.drawable.icon)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(messageBody)
        .setAutoCancel(true)

//            .setContentIntent(contentPendingIntent)

    notify(NOTIFICATION_ID, builder.build())
}
fun NotificationManager.cancelNotifications() {
    cancelAll()
}