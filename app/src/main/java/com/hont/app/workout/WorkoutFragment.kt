package com.hont.app.workout

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hont.app.R
import com.hont.app.databinding.FragmentWorkoutBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import android.widget.TextView
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!

    private val adapter = WorkoutLogAdapter { log ->
        WorkoutDetailSheet.newInstance(log)
            .show(childFragmentManager, "detail")
    }

    private var selectedDate: LocalDate = LocalDate.now()
    private val today = LocalDate.now()
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 M월")

    // RoutinePlanManager에서 로드한 계획된 날짜 (미래 파란 점)
    private var plannedDates: Set<LocalDate> = emptySet()

    // 임시 운동 기록 (TODO: API 연동 후 교체)
    private val allLogs: List<WorkoutLog> = listOf(
        WorkoutLog(1, today, "스쿼트", 3, 12, true,
            listOf(ExerciseFeedback(12, 4, "무릎이 밖으로 향함"))),
        WorkoutLog(2, today, "푸시업", 3, 15, true),
        WorkoutLog(3, today, "플랭크", 3, 45, true),
        WorkoutLog(4, today.minusDays(1), "버피", 4, 10, true,
            listOf(ExerciseFeedback(10, 2, "착지 시 무릎이 과도하게 구부러짐"))),
        WorkoutLog(5, today.minusDays(1), "런지", 3, 12, true),
        WorkoutLog(6, today.minusDays(3), "스쿼트", 4, 15, true),
        WorkoutLog(7, today.minusDays(3), "마운틴 클라이머", 3, 20, true),
        WorkoutLog(8, today.minusDays(5), "푸시업", 4, 20, true,
            listOf(ExerciseFeedback(20, 6, "팔꿈치가 너무 벌어짐"))),
        WorkoutLog(9, today.minusDays(8), "스쿼트", 3, 12, true)
    )

    // 완료된 운동이 있는 날짜 집합
    private val completedDates: Set<LocalDate> = allLogs.filter { it.isCompleted }.map { it.date }.toSet()
    // 예정됐지만 미완료된 운동이 있는 날짜 집합 (노란 점 대상)
    private val scheduledNotDoneDates: Set<LocalDate> = allLogs.filter { !it.isCompleted }.map { it.date }.toSet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupCalendar()
        setupMonthNavigation()
        refreshPlannedDates()
        updateLogList(selectedDate)
    }

    override fun onResume() {
        super.onResume()
        // CreateRoutineActivity에서 돌아올 때 파란 점 갱신
        refreshPlannedDates()
        binding.calendarView.notifyCalendarChanged()
    }

    private fun refreshPlannedDates() {
        val from = today.plusDays(1)
        val to = YearMonth.now().plusMonths(12).atEndOfMonth()
        plannedDates = RoutinePlanManager.getPlannedDates(requireContext(), from, to)
    }

    private fun setupRecyclerView() {
        binding.rvWorkoutLogs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWorkoutLogs.adapter = adapter
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)

        binding.calendarView.setup(startMonth, endMonth, DayOfWeek.SUNDAY)
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.dayBinder = object : MonthDayBinder<DayContainer> {
            override fun create(view: View) = DayContainer(view)
            override fun bind(container: DayContainer, data: CalendarDay) {
                container.tvDay.text = data.date.dayOfMonth.toString()

                // 운동 기록 점 표시 (이번 달 날짜만)
                if (data.position != DayPosition.MonthDate) {
                    container.vDot.visibility = View.INVISIBLE
                } else {
                    val isCompleted = completedDates.contains(data.date)
                    val isFuture = data.date.isAfter(today)
                    val isScheduledNotDone = scheduledNotDoneDates.contains(data.date)
                            && data.date.isBefore(today)

                    val dotColorRes = when {
                        isCompleted -> R.color.dot_green
                        isScheduledNotDone -> R.color.dot_yellow
                        isFuture && plannedDates.contains(data.date) -> R.color.dot_blue
                        else -> null // 루틴 없는 날 또는 오늘 미완료 → 점 없음
                    }

                    if (dotColorRes != null) {
                        container.vDot.visibility = View.VISIBLE
                        container.vDot.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), dotColorRes)
                        )
                    } else {
                        container.vDot.visibility = View.INVISIBLE
                    }
                }

                when {
                    // 선택된 날짜: 파란 원
                    data.date == selectedDate -> {
                        container.tvDay.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.shape_selected_day)
                        container.tvDay.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.white)
                        )
                    }
                    // 오늘: 테두리 원
                    data.date == today -> {
                        container.tvDay.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.shape_today_ring)
                        container.tvDay.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.primary)
                        )
                    }
                    // 이번 달 날짜
                    data.position == DayPosition.MonthDate -> {
                        container.tvDay.background = null
                        val colorRes = when (data.date.dayOfWeek) {
                            DayOfWeek.SUNDAY -> R.color.calendar_sunday
                            DayOfWeek.SATURDAY -> R.color.calendar_saturday
                            else -> R.color.text_primary
                        }
                        container.tvDay.setTextColor(
                            ContextCompat.getColor(requireContext(), colorRes)
                        )
                    }
                    // 이전/다음 달 날짜: 흐리게
                    else -> {
                        container.tvDay.background = null
                        container.tvDay.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.text_disabled)
                        )
                    }
                }

                container.view.setOnClickListener {
                    if (data.position == DayPosition.MonthDate) {
                        val prevSelected = selectedDate
                        selectedDate = data.date
                        binding.calendarView.notifyDateChanged(prevSelected)
                        binding.calendarView.notifyDateChanged(selectedDate)
                        updateLogList(selectedDate)
                    }
                }
            }
        }

        binding.calendarView.monthScrollListener = { month ->
            binding.tvMonthYear.text = month.yearMonth.format(monthFormatter)
        }
    }

    private fun setupMonthNavigation() {
        binding.btnPrevMonth.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.minusMonths(1))
            }
        }
        binding.btnNextMonth.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.plusMonths(1))
            }
        }
    }

    private fun updateLogList(date: LocalDate) {
        // TODO: API 연동 후 해당 날짜 기록 조회로 교체
        val completedLogs = allLogs.filter { it.date == date }

        // 루틴 계획이 있는 날짜(과거/미래 모두)는 계획된 운동을 "예정" 상태로 표시
        val plannedLogs = if (completedLogs.isEmpty()) {
            RoutinePlanManager.getPlansForDate(requireContext(), date)
                .flatMap { plan ->
                    plan.exercises.map { exercise ->
                        WorkoutLog(
                            id = -1L,
                            date = date,
                            exerciseName = exercise.name,
                            sets = exercise.sets,
                            repsPerSet = exercise.repsOrSeconds,
                            isCompleted = false
                        )
                    }
                }
        } else emptyList()

        val displayList = completedLogs.ifEmpty { plannedLogs }
        adapter.submitList(displayList)

        val hasItems = displayList.isNotEmpty()
        binding.rvWorkoutLogs.visibility = if (hasItems) View.VISIBLE else View.GONE
        binding.llEmpty.visibility = if (hasItems) View.GONE else View.VISIBLE

        // 미래 날짜이고 아무 계획도 없을 때만 "이 날 운동 추가" 버튼 표시
        val isFutureDate = date.isAfter(today)
        binding.btnAddPlan.visibility = if (!hasItems && isFutureDate) View.VISIBLE else View.GONE
        binding.btnAddPlan.setOnClickListener {
            startActivity(
                CreateRoutineActivity.intentCreate(
                    requireContext(),
                    isRecurring = false,
                    targetDate = date.toString()
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class DayContainer(view: View) : ViewContainer(view) {
    val tvDay: TextView = view.findViewById(R.id.tv_day)
    val vDot: View = view.findViewById(R.id.v_dot)
}
