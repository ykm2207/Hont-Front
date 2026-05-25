package com.hont.app.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hont.app.databinding.FragmentOnboardingStep1Binding

class OnboardingStep1Fragment : Fragment() {

    private var _binding: FragmentOnboardingStep1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    fun getName() = binding.etName.text.toString().trim()

    fun getGender(): String {
        return when (binding.toggleGender.checkedButtonId) {
            binding.btnMale.id -> "MALE"
            binding.btnFemale.id -> "FEMALE"
            else -> ""
        }
    }

    fun validate(): Boolean {
        if (getName().isBlank()) {
            binding.etName.error = "이름을 입력해주세요"
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
