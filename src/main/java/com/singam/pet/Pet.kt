package com.singam.pet

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin


class PetView(ctx: Context) : View(ctx) {
    private val d = ctx.resources.displayMetrics.density
    private fun p(c: Long, s: Paint.Style = Paint.Style.FILL) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c.toInt(); style = s; strokeCap = Paint.Cap.ROUND }

    private val fur = p(0xFFC98A4B); private val dark = p(0xFF8F5A2B)
    private val leg = p(0xFFB07A3E); private val light = p(0xFFF4E6D0)
    private val ink = p(0xFF2B1A10); private val white = p(0xFFFFFFFF)
    private val tongue = p(0xFFF08A96); private val shadow = p(0x33000000)
    private val tailP = p(0xFF8F5A2B, Paint.Style.STROKE).apply { strokeWidth = 13f }
    private val lid = p(0xFF2B1A10, Paint.Style.STROKE).apply { strokeWidth = 3f }
    private val txt = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 24f; textAlign = Paint.Align.CENTER }

    private var happyUntil = 0L
    private var lastTouch = SystemClock.uptimeMillis()

    /** Called when the user taps the pet. */
    fun pet() {
        happyUntil = SystemClock.uptimeMillis() + 1600
        lastTouch = SystemClock.uptimeMillis()
    }

    override fun onMeasure(w: Int, h: Int) {
        val s = (150 * d).toInt()
        setMeasuredDimension(s, s)
    }

    private fun oval(cx: Float, cy: Float, rx: Float, ry: Float, pt: Paint, c: Canvas) =
        c.drawOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), pt)

    override fun onDraw(c: Canvas) {
        val now = SystemClock.uptimeMillis()
        val t = now / 1000f
        val happy = now < happyUntil
        val asleep = !happy && now - lastTouch > 30000   // 30 sec la thoongidum
        val blink = (now % 4000) > 3880

        c.save()
        c.scale(width / 200f, height / 200f)
        oval(100f, 184f, 60f, 7f, shadow, c)
        c.translate(0f, if (happy) -abs(sin(t * 10)) * 18f else 0f)

        // body + tail + legs (breathing)
        c.save()
        c.scale(1f, 1f + (if (asleep) 0.03f else 0.02f) * sin(t * 2f), 100f, 182f)
        c.save()
        c.rotate(if (asleep) 0f else sin(t * (if (happy) 25f else 5f)) * 18f, 146f, 128f)
        c.drawPath(Path().apply { moveTo(146f, 128f); quadTo(176f, 104f, 170f, 80f) }, tailP)
        c.restore()
        c.drawRoundRect(RectF(50f, 150f, 72f, 182f), 10f, 10f, leg)
        c.drawRoundRect(RectF(128f, 150f, 150f, 182f), 10f, 10f, leg)
        oval(100f, 146f, 56f, 34f, fur, c)
        oval(100f, 156f, 34f, 20f, light, c)
        c.drawRoundRect(RectF(66f, 152f, 88f, 182f), 10f, 10f, fur)
        c.drawRoundRect(RectF(112f, 152f, 134f, 182f), 10f, 10f, fur)
        oval(77f, 181f, 13f, 5f, light, c)
        oval(123f, 181f, 13f, 5f, light, c)
        c.restore()

        // head
        c.save()
        if (asleep) { c.translate(0f, 12f); c.rotate(6f, 100f, 100f) }
        c.save(); c.rotate(25f, 70f, 84f); oval(70f, 84f, 12f, 24f, dark, c); c.restore()
        c.save(); c.rotate(-25f, 130f, 84f); oval(130f, 84f, 12f, 24f, dark, c); c.restore()
        c.drawCircle(100f, 94f, 36f, fur)
        oval(100f, 106f, 19f, 14f, light, c)
        if (asleep || blink) {
            c.drawArc(RectF(79f, 86f, 91f, 96f), 0f, 180f, false, lid)
            c.drawArc(RectF(109f, 86f, 121f, 96f), 0f, 180f, false, lid)
        } else {
            c.drawCircle(85f, 90f, 5f, ink); c.drawCircle(115f, 90f, 5f, ink)
            c.drawCircle(86.5f, 88.5f, 1.6f, white); c.drawCircle(116.5f, 88.5f, 1.6f, white)
        }
        oval(100f, 100f, 7f, 5f, ink, c)
        val m = p(0xFF2B1A10, Paint.Style.STROKE).apply { strokeWidth = 2f }
        c.drawLine(100f, 105f, 100f, 109f, m)
        c.drawArc(RectF(89f, 104f, 100f, 114f), 0f, 180f, false, m)
        c.drawArc(RectF(100f, 104f, 111f, 114f), 0f, 180f, false, m)
        if (happy) oval(100f, 117f, 5f, 7f, tongue, c)
        c.restore()

        // hearts when happy
        if (happy) {
            val pr = 1f - (happyUntil - now) / 1600f
            for (i in 0..2) {
                txt.color = 0xFFFF6F91.toInt()
                txt.alpha = ((1f - pr) * 255).toInt()
                c.drawText("♥", 80f + i * 20f, 60f - pr * 50f - i * 10f, txt)
            }
        }
        // zzz when asleep
        if (asleep) {
            for (i in 0..2) {
                val ph = (t * 0.5f + i * 0.33f) % 1f
                txt.color = 0xFF5B6CFF.toInt()
                txt.alpha = (sin(ph * PI.toFloat()) * 255).toInt()
                c.drawText("z", 140f + ph * 14f, 70f - ph * 35f, txt)
            }
        }
        c.restore()
        postInvalidateOnAnimation()
    }
}



class PetService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var view: PetView
    private lateinit var lp: WindowManager.LayoutParams

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        if (i?.action == "STOP") { stopSelf(); return START_NOT_STICKY }
        if (!::view.isInitialized) { startFg(); addPet() }
        return START_NOT_STICKY
    }

    private fun startFg() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("pet", "Kutty Pet", NotificationManager.IMPORTANCE_LOW))
        val stop = PendingIntent.getService(
            this, 0, Intent(this, PetService::class.java).setAction("STOP"), PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(this, "pet")
            .setContentTitle("குட்டி screen la irukku 🐶")
            .setContentText("Pet ah niruthha, inge thattunga")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(stop)
            .build()
        if (Build.VERSION.SDK_INT >= 34)
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(1, n)
    }

    private fun addPet() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        view = PetView(this)
        lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 40; y = 600 }

        var sx = 0f; var sy = 0f; var ox = 0; var oy = 0; var moved = false
        view.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = e.rawX; sy = e.rawY; ox = lp.x; oy = lp.y; moved = false }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - sx).toInt(); val dy = (e.rawY - sy).toInt()
                    if (abs(dx) > 10 || abs(dy) > 10) moved = true
                    if (moved) { lp.x = ox + dx; lp.y = oy + dy; wm.updateViewLayout(view, lp) }
                }
                MotionEvent.ACTION_UP -> if (!moved) view.pet()
            }
            true
        }
        wm.addView(view, lp)
    }

    override fun onDestroy() {
        if (::view.isInitialized) wm.removeView(view)
        super.onDestroy()
    }
}



class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Build.VERSION.SDK_INT >= 33)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(48, 48, 48, 48)
        }
        root.addView(TextView(this).apply { text = "குட்டி 🐶"; textSize = 28f; gravity = Gravity.CENTER })
        root.addView(Button(this).apply {
            text = "Pet ah start pannu"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    // "Display over other apps" permission allow panna vendum
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                } else startForegroundService(Intent(this@MainActivity, PetService::class.java))
            }
        })
        root.addView(Button(this).apply {
            text = "Pet ah nirutthu"
            setOnClickListener { stopService(Intent(this@MainActivity, PetService::class.java)) }
        })
        setContentView(root)
    }
}
