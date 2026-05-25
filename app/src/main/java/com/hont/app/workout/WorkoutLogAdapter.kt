package com.hont.app.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hont.app.R
import com.hont.app.databinding.ItemWorkoutLogBinding
import java.io.Serializable
import java.time.LocalDate

// 자세 피드백 (API 연동 시 서버 응답 모델로 교체)
data class ExerciseFeedback(
    val totalReps: Int,
    val badReps: Int,
    val issue: String
) : Serializable

// 운동 기록 데이터 모델 (API 연동 시 서버 응답 모델로 교체)
data class WorkoutLog(
    val id: Long,
    val date: LocalDate,
    val exerciseName: String,
    val sets: Int,
    val repsPerSet: Int,
    val isCompleted: Boolean,
    val feedbacks: List<ExerciseFeedback> = emptyList()
) : Serializable

class WorkoutLogAdapter(
    private val onItemClick: (WorkoutLog) -> Unit
) : RecyclerView.Adapter<WorkoutLogAdapter.ViewHolder>() {

    private val items = mutableListOf<WorkoutLog>()

    fun submitList(list: List<WorkoutLog>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemWorkoutLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(log: WorkoutLog) {
            binding.tvExerciseName.text = log.exerciseName
            binding.tvSetsReps.text = "${log.sets}세트 × ${log.repsPerSet}회"

            if (log.isCompleted) {
                binding.tvLogStatus.text = "완료"
                binding.tvLogStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.dot_green)
                )
            } else {
                binding.tvLogStatus.text = "예정"
                binding.tvLogStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.text_secondary)
                )
            }

            binding.root.setOnClickListener { onItemClick(log) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkoutLogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
