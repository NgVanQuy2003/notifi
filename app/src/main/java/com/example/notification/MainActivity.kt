package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val CHANNEL_ID = "boiling_channel"
    private val NOTIFICATION_ID = 1
    private var countDownTimer: CountDownTimer? = null
    private lateinit var tvTimer: TextView
    private val REQUEST_NOTIFICATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        tvTimer = findViewById(R.id.tvTimer)

        val btnChickenEgg: Button = findViewById(R.id.btnChickenEgg)
        btnChickenEgg.setOnClickListener { startBoiling(1 * 60 * 1000, "Trứng gà đã chín!") }

        val btnDuckEgg: Button = findViewById(R.id.btnDuckEgg)
        btnDuckEgg.setOnClickListener { startBoiling(2 * 60 * 1000, "Trứng vịt đã chín!") }

        val btnFertilizedDuckEgg: Button = findViewById(R.id.btnFertilizedDuckEgg)
        btnFertilizedDuckEgg.setOnClickListener { startBoiling(3 * 60 * 1000, "Trứng vịt lộn đã chín!") }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Boiling Channel"
            val descriptionText = "Channel for boiling egg notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startBoiling(duration: Long, message: String) {
        if (countDownTimer != null) {
            countDownTimer?.cancel()
            tvTimer.text = "Hết nồi, không luộc được ngay."
            countDownTimer = null
            return
        }

        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "Thời gian còn lại: ${millisUntilFinished / 1000} giây"
            }

            override fun onFinish() {
                tvTimer.text = "Hoàn thành!"
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    sendNotification(message)
                } else {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
                }
                countDownTimer = null
            }
        }.start()
    }

    private fun sendNotification(message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Thông báo")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            tvTimer.text = "Không thể gửi thông báo vì không có quyền."
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, show notification
                sendNotification("Trứng đã chín!")
            } else {
                // Permission denied, handle accordingly
                tvTimer.text = "Không thể gửi thông báo vì không có quyền."
            }
        }
    }
}