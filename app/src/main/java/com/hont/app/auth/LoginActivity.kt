package com.hont.app.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hont.app.MainActivity
import com.hont.app.databinding.ActivityLoginBinding
import com.hont.app.onboarding.OnboardingActivity
import com.hont.app.profile.ProfileManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val BASE_URL = "https://hont-production.up.railway.app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 딥링크로 돌아온 경우 토큰 처리
        handleDeepLink(intent)

        // 버튼 설정
        binding.btnGoogleLogin.setOnClickListener {
            openOAuth("$BASE_URL/oauth2/authorization/google")
        }
        binding.btnKakaoLogin.setOnClickListener {
            openOAuth("$BASE_URL/oauth2/authorization/kakao")
        }
        binding.btnIdLogin.setOnClickListener {
            // TODO: 아이디 로그인 화면 구현
            Toast.makeText(this, "준비 중입니다", Toast.LENGTH_SHORT).show()
        }

        // 이미 로그인된 경우 바로 이동 (토큰 존재 여부 확인)
        lifecycleScope.launch {
            val token = TokenManager.getAccessToken(this@LoginActivity).first()
            if (!token.isNullOrBlank()) {
                if (ProfileManager.isOnboardingDone(this@LoginActivity)) {
                    goToMain()
                } else {
                    goToOnboarding()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun openOAuth(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data ?: return
        if (data.scheme == "hont" && data.host == "auth") {
            val accessToken = data.getQueryParameter("accessToken") ?: return
            val refreshToken = data.getQueryParameter("refreshToken") ?: return

            lifecycleScope.launch {
                TokenManager.saveTokens(this@LoginActivity, accessToken, refreshToken)
                if (ProfileManager.isOnboardingDone(this@LoginActivity)) {
                    goToMain()
                } else {
                    goToOnboarding()
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun goToOnboarding() {
        startActivity(Intent(this, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
