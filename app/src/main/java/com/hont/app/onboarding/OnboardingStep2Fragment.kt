package com.hont.app.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hont.app.databinding.FragmentOnboardingStep2Binding

class OnboardingStep2Fragment : Fragment() {

    private var _binding: FragmentOnboardingStep2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    fun getHeight() = binding.etHeight.text.toString().toIntOrNull() ?: 0
    fun getWeight() = binding.etWeight.text.toString().toFloatOrNull() ?: 0f
    fun getGoalWeight() = binding.etGoalWeight.text.toString().toFloatOrNull() ?: 0f

    fun validate(): Boolean {
        if (getHeight() <= 0) {
            binding.etHeight.error = "키를 입력해주세요"
            return false
        }
        if (getWeight() <= 0f) {
            binding.etWeight.error = "몸무게를 입력해주세요"
            return false
        }
        if (getGoalWeight() <= 0f) {
            binding.etGoalWeight.error = "목표 몸무게를 입력해주세요"
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
