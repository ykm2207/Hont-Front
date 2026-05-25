package com.hont.app.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hont.app.databinding.SheetEditProfileBinding

class EditProfileSheet : BottomSheetDialogFragment() {

    private var _binding: SheetEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCurrentProfile()
        binding.btnSaveProfile.setOnClickListener { saveProfile() }
    }

    private fun loadCurrentProfile() {
        val profile = ProfileManager.getProfile(requireContext())
        binding.etEditName.setText(profile.name)
        if (profile.heightCm > 0) binding.etEditHeight.setText(profile.heightCm.toString())
        if (profile.weightKg > 0) binding.etEditWeight.setText(profile.weightKg.toString())
        if (profile.goalWeightKg > 0) binding.etEditGoalWeight.setText(profile.goalWeightKg.toString())
    }

    private fun saveProfile() {
        val current = ProfileManager.getProfile(requireContext())
        val updated = current.copy(
            name = binding.etEditName.text.toString().trim().ifBlank { current.name },
            heightCm = binding.etEditHeight.text.toString().toIntOrNull() ?: current.heightCm,
            weightKg = binding.etEditWeight.text.toString().toFloatOrNull() ?: current.weightKg,
            goalWeightKg = binding.etEditGoalWeight.text.toString().toFloatOrNull() ?: current.goalWeightKg
        )
        ProfileManager.updateProfile(requireContext(), updated)
        Toast.makeText(requireContext(), "저장됐어요", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
