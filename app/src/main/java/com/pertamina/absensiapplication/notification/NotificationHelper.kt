package com.pertamina.absensiapplication.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_VIBRATE
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.view.MainActivity


class NotificationHelper {

    companion object NotificationApplication {
        private const val CHANNEL_ID = "absensi_application"
        private const val CHANNEL_NAME = "Absensi Application"
        private const val CHANNEL_DESC = "Absensi Application Notification"

        fun displayNotification(context: Context, title: String, body: String, image: String) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent = PendingIntent.getActivity(
                    context,
                    100,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            )
            Glide.with(context)
                    .asBitmap()
                    .load(image)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(getCircleBitmap(resource))
                                    .setContentTitle(title)
                                    .setContentText(body)
                                    .setContentIntent(pendingIntent)
                                    .setDefaults(DEFAULT_VIBRATE)
                                    .setAutoCancel(true)
                                    .setPriority(NotificationCompat.PRIORITY_MAX)

                            val notifManager = NotificationManagerCompat.from(context)
                            notifManager.notify(1, builder.build())

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val channel =
                                        NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                                channel.description = CHANNEL_DESC
                                channel.setShowBadge(true)
                                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                                val manager = context.getSystemService(NotificationManager::class.java)
                                manager?.createNotificationChannel(channel)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // this is called when imageView is cleared on lifecycle call or for
                            // some other reason.
                            // if you are referencing the bitmap somewhere else too other than this imageView
                            // clear it here as you can no longer have the bitmap
                        }
                    })
        }

        private fun getCircleBitmap(bitmap: Bitmap): Bitmap {

            val srcRect: Rect

            val dstRect: Rect

            val r: Float

            val paint = Paint()

            val width: Int = bitmap.width

            val height: Int = bitmap.height

            val widthToGenerate = 100F

            val heightToGenerate = 100F

            val borderWidth: Float = 1.toFloat()

            val output: Bitmap

            val canvas: Canvas

            if (width > height) {

                output = Bitmap.createBitmap(widthToGenerate.toInt(), heightToGenerate.toInt(), Bitmap.Config.ARGB_8888)

                canvas = Canvas(output)
                val scale: Float = (widthToGenerate / width)


                val xTranslation = 0.0f
                val yTranslation: Float = (heightToGenerate - height * scale) / 2.0f


                val transformation = Matrix()
                transformation.postTranslate(xTranslation, yTranslation)
                transformation.preScale(scale, scale)

                val color: Int = Color.WHITE
                paint.isAntiAlias = true
                paint.color = color

                canvas.drawBitmap(bitmap, transformation, paint)

            } else {
                output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
                canvas = Canvas(output)
                val top: Int = (height - width) / 2
                val bottom: Int = top + width
                srcRect = Rect(0, top, width, bottom)
                dstRect = Rect(0, 0, width, width)
                r = (width / 2).toFloat()


                val color: Int = Color.GRAY
                paint.isAntiAlias = true
                canvas.drawARGB(0, 0, 0, 0)
                paint.color = color


                canvas.drawCircle(r + borderWidth, r + borderWidth, r + borderWidth, paint)
                canvas.drawCircle(r, r, r, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

                canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
            }

            return output

        }
    }
}