package com.hont.app.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hont.app.databinding.SheetStartWorkoutBinding
import java.time.LocalDate

class StartWorkoutSheet : BottomSheetDialogFragment() {

    private var _binding: SheetStartWorkoutBinding? = null
    private val binding get() = _binding!!

    private val routineAdapter = RoutineSelectAdapter(
        onStartClick = { plan -> startWorkout(plan) },
        onEditClick = { plan ->
            startActivity(CreateRoutineActivity.intentEdit(requireContext(), plan))
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetStartWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        // CreateRoutineActivity에서 돌아올 때 데이터 갱신
        refreshData()
    }

    private fun setupRecyclerView() {
        binding.rvRoutines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRoutines.adapter = routineAdapter
    }

    private fun setupButtons() {
        binding.cardTodayRoutine.setOnClickListener {
            val todayPlan = RoutinePlanManager.getPlansForDate(requireContext(), LocalDate.now())
                .firstOrNull()
            if (todayPlan != null) startWorkout(todayPlan)
        }

        binding.btnCreateRoutine.setOnClickListener {
            startActivity(CreateRoutineActivity.intentCreate(requireContext(), isRecurring = true))
        }

        binding.btnFreeWorkout.setOnClickListener {
            Toast.makeText(requireContext(), "자유 운동을 시작합니다", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun refreshData() {
        val context = requireContext()
        val today = LocalDate.now()
        val todayPlans = RoutinePlanManager.getPlansForDate(context, today)
        val allPlans = RoutinePlanManager.getAllPlans(context).filter { it.isRecurring }

        // 오늘의 루틴 카드
        val todayFirst = todayPlans.firstOrNull()
        if (todayFirst != null) {
            binding.cardTodayRoutine.visibility = View.VISIBLE
            binding.llNoTodayRoutine.visibility = View.GONE
            binding.tvTodayRoutineName.text = todayFirst.name
            binding.tvTodayRoutineDetail.text =
                todayFirst.exercises.joinToString(" · ") { it.name }
        } else {
            binding.cardTodayRoutine.visibility = View.GONE
            binding.llNoTodayRoutine.visibility = View.VISIBLE
        }

        // 전체 루틴 목록
        routineAdapter.submitList(allPlans)
        binding.rvRoutines.visibility = if (allPlans.isNotEmpty()) View.VISIBLE else View.GONE
        binding.tvNoRoutines.visibility = if (allPlans.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun startWorkout(plan: RoutinePlan) {
        // TODO: 운동 세션 화면으로 이동 (모션 인식 포함)
        Toast.makeText(
            requireContext(),
            "${plan.name} 시작!",
            Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
