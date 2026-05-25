package com.hont.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hont.app.network.RetrofitClient
import com.hont.app.settings.NotificationHelper
import com.hont.app.settings.SettingsManager

class HontApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.initialize(this)
        // 저장된 테마 적용 (앱 시작 전에 적용해야 깜빡임 없음)
        AppCompatDelegate.setDefaultNightMode(
            if (SettingsManager.isDarkMode(this)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        // 알림 채널 생성
        NotificationHelper.createChannel(this)
    }
}
