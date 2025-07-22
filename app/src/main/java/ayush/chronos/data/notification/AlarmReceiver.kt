package ayush.chronos.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.core.graphics.drawable.toBitmap
import ayush.chronos.R
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("reminder_title") ?: return
        val notes = intent.getStringExtra("reminder_notes")
        val id = intent.getStringExtra("reminder_id") ?: System.currentTimeMillis().toString()
        val imageUrl = intent.getStringExtra("reminder_image_url")

        val channelId = "reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_HIGH)
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        var bigPictureBitmap = if (!imageUrl.isNullOrBlank()) {
            runBlocking {
                val loader = context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(request) as? SuccessResult)
                result?.drawable?.toBitmap()
            }
        } else {
            null
        }

        if (bigPictureBitmap == null) {
            try {
                val drawable = context.packageManager.getApplicationIcon(context.packageName)
                bigPictureBitmap = if (drawable is BitmapDrawable) {
                    drawable.bitmap
                } else {
                    BitmapFactory.decodeResource(
                        context.resources,
                        context.resources.getIdentifier(
                            "ic_launcher",
                            "mipmap",
                            context.packageName
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(notes)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (bigPictureBitmap != null) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(bigPictureBitmap)
            )
        }

        NotificationManagerCompat.from(context).notify(id.hashCode(), builder.build())
    }
}
