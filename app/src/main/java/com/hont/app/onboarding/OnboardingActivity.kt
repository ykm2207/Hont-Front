package com.hont.app.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.hont.app.MainActivity
import com.hont.app.databinding.ActivityOnboardingBinding
import com.hont.app.profile.ProfileData
import com.hont.app.profile.ProfileManager

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    private val step1 = OnboardingStep1Fragment()
    private val step2 = OnboardingStep2Fragment()
    private val step3 = OnboardingStep3Fragment()
    private val fragments = listOf(step1, step2, step3)

    private val dots get() = listOf(binding.dot1, binding.dot2, binding.dot3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        binding.vpOnboarding.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }
        // 스와이프로 이동 비활성화 (버튼으로만 이동)
        binding.vpOnboarding.isUserInputEnabled = false

        binding.vpOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                binding.btnBack.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                binding.btnNext.text = if (position == fragments.lastIndex) "시작하기" else "다음"
            }
        })
    }

    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            val current = binding.vpOnboarding.currentItem
            if (validateCurrentStep(current)) {
                if (current < fragments.lastIndex) {
                    binding.vpOnboarding.currentItem = current + 1
                } else {
                    completeOnboarding()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            val current = binding.vpOnboarding.currentItem
            if (current > 0) binding.vpOnboarding.currentItem = current - 1
        }
    }

    private fun validateCurrentStep(step: Int): Boolean {
        return when (step) {
            0 -> step1.validate()
            1 -> step2.validate()
            else -> true
        }
    }

    private fun updateDots(activeIndex: Int) {
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index == activeIndex) com.hont.app.R.drawable.shape_dot_active
                else com.hont.app.R.drawable.shape_dot_inactive
            )
        }
    }

    private fun completeOnboarding() {
        val profileData = ProfileData(
            name = step1.getName(),
            gender = step1.getGender(),
            heightCm = step2.getHeight(),
            weightKg = step2.getWeight(),
            goalWeightKg = step2.getGoalWeight(),
            workoutGoal = step3.getWorkoutGoal(),
            diseases = step3.getDiseases()
        )
        ProfileManager.saveProfile(this, profileData)

        Toast.makeText(this, "${profileData.name}님, 환영합니다!", Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
