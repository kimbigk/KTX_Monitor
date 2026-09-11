package com.example.ktxmonitor.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object KorailLinkHelper {
    private const val KORAIL_TALK_PACKAGE = "com.korail.talk"
    private const val KORAIL_MOBILE_WEB = "https://m.letskorail.com"

    fun openKorailReservation(context: Context) {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(KORAIL_TALK_PACKAGE)

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(KORAIL_MOBILE_WEB)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}
