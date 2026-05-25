package com.hont.app.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hont.app.R
import com.hont.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnEditProfile.setOnClickListener {
            EditProfileSheet().show(childFragmentManager, "edit_profile")
        }
    }

    override fun onResume() {
        super.onResume()
        renderProfile()
    }

    private fun renderProfile() {
        val profile = ProfileManager.getProfile(requireContext())

        // 아바타 (이름 첫 글자)
        binding.tvAvatar.text = profile.name.firstOrNull()?.toString() ?: "?"
        binding.tvProfileName.text = if (profile.name.isNotBlank()) profile.name else "이름 없음"
        binding.tvProfileGoal.text = profile.workoutGoalLabel

        // 신체 정보
        binding.tvHeightValue.text = if (profile.heightCm > 0) profile.heightCm.toString() else "-"
        binding.tvWeightValue.text = if (profile.weightKg > 0) profile.weightKg.toString() else "-"
        binding.tvGoalWeightValue.text = if (profile.goalWeightKg > 0) profile.goalWeightKg.toString() else "-"

        // BMI
        if (profile.bmi > 0f) {
            binding.tvBmiValue.text = String.format("%.1f", profile.bmi)
            binding.tvBmiLabel.text = profile.bmiLabel
            val bmiColor = when (profile.bmiLabel) {
                "저체중" -> R.color.calendar_saturday
                "정상" -> R.color.primary
                "과체중" -> android.R.color.holo_orange_dark
                "비만" -> R.color.calendar_sunday
                else -> R.color.text_secondary
            }
            binding.tvBmiValue.setTextColor(ContextCompat.getColor(requireContext(), bmiColor))
        } else {
            binding.tvBmiValue.text = "-"
            binding.tvBmiLabel.text = "BMI"
        }

        // 건강 정보
        binding.tvWorkoutGoal.text = profile.workoutGoalLabel
        binding.tvDiseases.text = if (profile.diseases.isEmpty()) "해당 없음"
        else profile.diseases.joinToString(", ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
