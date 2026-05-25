package com.hont.app.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hont.app.R
import com.hont.app.databinding.SheetWorkoutDetailBinding
import java.time.format.DateTimeFormatter

class WorkoutDetailSheet : BottomSheetDialogFragment() {

    private var _binding: SheetWorkoutDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_LOG = "arg_log"

        fun newInstance(log: WorkoutLog): WorkoutDetailSheet {
            return WorkoutDetailSheet().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_LOG, log)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetWorkoutDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @Suppress("DEPRECATION")
        val log = arguments?.getSerializable(ARG_LOG) as? WorkoutLog ?: return
        bindLog(log)
    }

    private fun bindLog(log: WorkoutLog) {
        val formatter = DateTimeFormatter.ofPattern("M월 d일 (E)")
        binding.tvExerciseTitle.text = log.exerciseName
        binding.tvDetailDate.text = log.date.format(formatter)

        if (log.isCompleted) {
            binding.tvDetailStatus.text = "완료"
            binding.tvDetailStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.dot_green)
            )
        } else {
            binding.tvDetailStatus.text = "예정"
            binding.tvDetailStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.text_secondary)
            )
        }

        binding.tvSetsRepsDetail.text = "${log.sets}세트 × ${log.repsPerSet}회"

        if (log.feedbacks.isEmpty()) {
            binding.tvFeedbackEmpty.visibility = View.VISIBLE
            binding.llFeedbackContainer.visibility = View.GONE
        } else {
            binding.tvFeedbackEmpty.visibility = View.GONE
            binding.llFeedbackContainer.visibility = View.VISIBLE
            log.feedbacks.forEach { feedback ->
                addFeedbackItem(
                    "${log.exerciseName} ${feedback.totalReps}회 중 " +
                    "${feedback.badReps}회는 ${feedback.issue}"
                )
            }
        }
    }

    private fun addFeedbackItem(text: String) {
        val marginPx = (8 * resources.displayMetrics.density).toInt()
        val tv = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = marginPx }
            this.text = "• $text"
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        }
        binding.llFeedbackContainer.addView(tv)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
