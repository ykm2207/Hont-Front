package com.hont.app.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hont.app.R
import com.hont.app.databinding.FragmentOnboardingStep3Binding

class OnboardingStep3Fragment : Fragment() {

    private var _binding: FragmentOnboardingStep3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    fun getWorkoutGoal(): String {
        return when (binding.chipGroupGoal.checkedChipId) {
            R.id.chip_diet -> "DIET"
            R.id.chip_muscle -> "MUSCLE"
            R.id.chip_fitness -> "FITNESS"
            R.id.chip_health -> "HEALTH"
            else -> ""
        }
    }

    fun getDiseases(): List<String> {
        val diseases = mutableListOf<String>()
        if (binding.chipHypertension.isChecked) diseases.add("고혈압")
        if (binding.chipDiabetes.isChecked) diseases.add("당뇨")
        if (binding.chipHeart.isChecked) diseases.add("심장질환")
        if (binding.chipArthritis.isChecked) diseases.add("관절염")
        return diseases
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
