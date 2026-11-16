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
    
