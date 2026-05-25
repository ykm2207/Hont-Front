package com.hont.app.workout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hont.app.databinding.ActivityCreateRoutineBinding
import com.hont.app.databinding.ItemExerciseSelectBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateRoutineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRoutineBinding

    // 각 운동 종목 행의 바인딩 보관 (AVAILABLE_EXERCISES 순서와 동일)
    private val exerciseBindings = mutableListOf<ItemExerciseSelectBinding>()

    private var editingPlan: RoutinePlan? = null
    private var isRecurring = true
    private var targetDate: LocalDate? = null

    private val dayChipMap: List<Pair<com.google.android.material.chip.Chip, Int>> by lazy {
        listOf(
            binding.chipMon to 1,
            binding.chipTue to 2,
            binding.chipWed to 3,
            binding.chipThu to 4,
            binding.chipFri to 5,
            binding.chipSat to 6,
            binding.chipSun to 7
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRoutineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        editingPlan = intent.getSerializableExtra(EXTRA_PLAN) as? RoutinePlan
        isRecurring = intent.getBooleanExtra(EXTRA_IS_RECURRING, true)
        val targetDateStr = intent.getStringExtra(EXTRA_TARGET_DATE)
        if (targetDateStr != null) targetDate = LocalDate.parse(targetDateStr)

        setupToolbar()
        setupForm()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.title = if (editingPlan != null) "루틴 수정" else "루틴 만들기"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupForm() {
        // 반복/즉흥 모드 분기
        if (isRecurring) {
            binding.llDaysSection.visibility = View.VISIBLE
            binding.llDateSection.visibility = View.GONE
        } else {
            binding.llDaysSection.visibility = View.GONE
            binding.llDateSection.visibility = View.VISIBLE
            val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)")
            binding.tvTargetDate.text = targetDate?.format(formatter) ?: ""
        }

        // 운동 종목 행 동적 생성
        AVAILABLE_EXERCISES.forEach { exercise ->
            val exBinding = ItemExerciseSelectBinding.inflate(
                layoutInflater, binding.llExercises, false
            )
            exBinding.tvExerciseName.text = exercise.name
            exBinding.etSets.setText(exercise.sets.toString())
            exBinding.etReps.setText(exercise.repsOrSeconds.toString())
            exBinding.tvUnit.text = if (exercise.isTimeBased) "초" else "회"
            binding.llExercises.addView(exBinding.root)
            exerciseBindings.add(exBinding)
        }

        // 수정 모드: 기존 데이터 채우기
        editingPlan?.let { plan ->
            binding.etRoutineName.setText(plan.name)

            if (isRecurring) {
                dayChipMap.forEach { (chip, dayValue) ->
                    chip.isChecked = plan.days.contains(dayValue)
                }
            }

            exerciseBindings.forEachIndexed { idx, exBinding ->
                val saved = plan.exercises.find { it.name == AVAILABLE_EXERCISES[idx].name }
                if (saved != null) {
                    exBinding.cbExercise.isChecked = true
                    exBinding.etSets.setText(saved.sets.toString())
                    exBinding.etReps.setText(saved.repsOrSeconds.toString())
                }
            }

            binding.btnDelete.visibility = View.VISIBLE
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener { savePlan() }
        binding.btnDelete.setOnClickListener {
            editingPlan?.let { plan ->
                RoutinePlanManager.deletePlan(this, plan.id)
                Toast.makeText(this, "루틴이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun savePlan() {
        val name = binding.etRoutineName.text?.toString()?.trim()
        if (name.isNullOrBlank()) {
            Toast.makeText(this, "루틴 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedExercises = exerciseBindings.mapIndexedNotNull { idx, exBinding ->
            if (!exBinding.cbExercise.isChecked) return@mapIndexedNotNull null
            val original = AVAILABLE_EXERCISES[idx]
            val sets = exBinding.etSets.text?.toString()?.toIntOrNull() ?: original.sets
            val reps = exBinding.etReps.text?.toString()?.toIntOrNull() ?: original.repsOrSeconds
            PlannedExercise(original.name, sets, reps, original.isTimeBased)
        }

        if (selectedExercises.isEmpty()) {
            Toast.makeText(this, "운동 종목을 1개 이상 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val days = if (isRecurring) {
            dayChipMap.filter { (chip, _) -> chip.isChecked }.map { (_, dayValue) -> dayValue }
        } else emptyList()

        if (isRecurring && days.isEmpty()) {
            Toast.makeText(this, "반복 요일을 1개 이상 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val plan = RoutinePlan(
            id = editingPlan?.id ?: RoutinePlanManager.newId(),
            name = name,
            days = days,
            exercises = selectedExercises,
            isRecurring = isRecurring,
            targetDate = targetDate?.toString()
        )

        RoutinePlanManager.savePlan(this, plan)
        Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        private const val EXTRA_PLAN = "extra_plan"
        private const val EXTRA_IS_RECURRING = "extra_is_recurring"
        private const val EXTRA_TARGET_DATE = "extra_target_date"

        fun intentCreate(
            context: Context,
            isRecurring: Boolean = true,
            targetDate: String? = null
        ) = Intent(context, CreateRoutineActivity::class.java).apply {
            putExtra(EXTRA_IS_RECURRING, isRecurring)
            if (targetDate != null) putExtra(EXTRA_TARGET_DATE, targetDate)
        }

        fun intentEdit(context: Context, plan: RoutinePlan) =
            Intent(context, CreateRoutineActivity::class.java).apply {
                putExtra(EXTRA_PLAN, plan)
                putExtra(EXTRA_IS_RECURRING, plan.isRecurring)
                if (plan.targetDate != null) putExtra(EXTRA_TARGET_DATE, plan.targetDate)
            }
    }
}
