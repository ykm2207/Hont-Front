package com.hont.app.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hont.app.auth.LoginActivity
import com.hont.app.auth.TokenManager
import com.hont.app.databinding.FragmentSettingsBinding
import com.hont.app.profile.ProfileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Android 13+ 알림 권한 요청
    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toast.makeText(requireContext(), "알림 권한이 필요해요", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        val ctx = requireContext()
        // 계정
        binding.tvAccountName.text = ProfileManager.getProfile(ctx).name.ifBlank { "사용자" }

        // 알림 스위치 상태
        binding.swWorkoutNotif.isChecked = SettingsManager.isWorkoutNotifEnabled(ctx)
        binding.swBreakfastNotif.isChecked = SettingsManager.isDietNotifEnabled(ctx, "BREAKFAST")
        binding.swLunchNotif.isChecked = SettingsManager.isDietNotifEnabled(ctx, "LUNCH")
        binding.swDinnerNotif.isChecked = SettingsManager.isDietNotifEnabled(ctx, "DINNER")

        // 알림 시간 표시
        updateNotifTimeLabel("WORKOUT")
        updateNotifTimeLabel("BREAKFAST")
        updateNotifTimeLabel("LUNCH")
        updateNotifTimeLabel("DINNER")

        // 알림 시간 행 가시성
        setTimeRowVisible("WORKOUT", binding.swWorkoutNotif.isChecked)
        setTimeRowVisible("BREAKFAST", binding.swBreakfastNotif.isChecked)
        setTimeRowVisible("LUNCH", binding.swLunchNotif.isChecked)
        setTimeRowVisible("DINNER", binding.swDinnerNotif.isChecked)

        // 앱 설정
        binding.swDarkMode.isChecked = SettingsManager.isDarkMode(ctx)
        binding.tvCalorieGoalValue.text = "${SettingsManager.getCalorieGoal(ctx)} kcal"
        binding.tvWorkoutFreqValue.text = "주 ${SettingsManager.getWorkoutFreq(ctx)}회"

        // 버전
        binding.tvAppVersion.text = try {
            requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
        } catch (e: Exception) { "1.0.0" }
    }

    private fun setupListeners() {
        // 로그아웃
        binding.llLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠어요?")
                .setPositiveButton("로그아웃") { _, _ -> logout() }
                .setNegativeButton("취소", null)
                .show()
        }

        // 회원 탈퇴
        binding.llDeleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("회원 탈퇴")
                .setMessage("탈퇴하면 모든 데이터가 삭제되며 복구할 수 없어요.\n정말 탈퇴하시겠어요?")
                .setPositiveButton("탈퇴") { _, _ ->
                    // TODO: API 연동 후 계정 삭제 API 호출 추가
                    logout()
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 알림 스위치
        binding.swWorkoutNotif.setOnCheckedChangeListener { _, checked ->
            if (checked) requestNotifPermission()
            SettingsManager.setWorkoutNotifEnabled(requireContext(), checked)
            setTimeRowVisible("WORKOUT", checked)
            if (checked) scheduleNotif("WORKOUT")
            else NotificationHelper.cancel(requireContext(), NotificationHelper.NOTIF_ID_WORKOUT)
        }

        binding.swBreakfastNotif.setOnCheckedChangeListener { _, checked ->
            if (checked) requestNotifPermission()
            SettingsManager.setDietNotifEnabled(requireContext(), "BREAKFAST", checked)
            setTimeRowVisible("BREAKFAST", checked)
            if (checked) scheduleNotif("BREAKFAST")
            else NotificationHelper.cancel(requireContext(), NotificationHelper.NOTIF_ID_BREAKFAST)
        }

        binding.swLunchNotif.setOnCheckedChangeListener { _, checked ->
            if (checked) requestNotifPermission()
            SettingsManager.setDietNotifEnabled(requireContext(), "LUNCH", checked)
            setTimeRowVisible("LUNCH", checked)
            if (checked) scheduleNotif("LUNCH")
            else NotificationHelper.cancel(requireContext(), NotificationHelper.NOTIF_ID_LUNCH)
        }

        binding.swDinnerNotif.setOnCheckedChangeListener { _, checked ->
            if (checked) requestNotifPermission()
            SettingsManager.setDietNotifEnabled(requireContext(), "DINNER", checked)
            setTimeRowVisible("DINNER", checked)
            if (checked) scheduleNotif("DINNER")
            else NotificationHelper.cancel(requireContext(), NotificationHelper.NOTIF_ID_DINNER)
        }

        // 알림 시간 선택
        binding.llWorkoutNotifTime.setOnClickListener { showTimePicker("WORKOUT") }
        binding.llBreakfastNotifTime.setOnClickListener { showTimePicker("BREAKFAST") }
        binding.llLunchNotifTime.setOnClickListener { showTimePicker("LUNCH") }
        binding.llDinnerNotifTime.setOnClickListener { showTimePicker("DINNER") }

        // 다크 모드
        binding.swDarkMode.setOnCheckedChangeListener { _, checked ->
            SettingsManager.setDarkMode(requireContext(), checked)
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // 칼로리 목표
        binding.llCalorieGoal.setOnClickListener { showCalorieGoalDialog() }

        // 주간 운동 목표
        binding.llWorkoutFreq.setOnClickListener { showWorkoutFreqDialog() }

        // 개인정보처리방침 / 이용약관
        binding.llPrivacyPolicy.setOnClickListener {
            // TODO: 실제 URL로 교체
            openUrl("https://hont.app/privacy")
        }
        binding.llTerms.setOnClickListener {
            openUrl("https://hont.app/terms")
        }
    }

    private fun setTimeRowVisible(type: String, visible: Boolean) {
        val row = when (type) {
            "WORKOUT" -> binding.llWorkoutNotifTime
            "BREAKFAST" -> binding.llBreakfastNotifTime
            "LUNCH" -> binding.llLunchNotifTime
            else -> binding.llDinnerNotifTime
        }
        row.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun updateNotifTimeLabel(type: String) {
        val ctx = requireContext()
        val (hour, minute) = when (type) {
            "WORKOUT" -> SettingsManager.getWorkoutNotifTime(ctx)
            else -> SettingsManager.getDietNotifTime(ctx, type)
        }
        val timeText = NotificationHelper.formatTime(hour, minute)
        when (type) {
            "WORKOUT" -> binding.tvWorkoutNotifTime.text = timeText
            "BREAKFAST" -> binding.tvBreakfastNotifTime.text = timeText
            "LUNCH" -> binding.tvLunchNotifTime.text = timeText
            "DINNER" -> binding.tvDinnerNotifTime.text = timeText
        }
    }

    private fun showTimePicker(type: String) {
        val ctx = requireContext()
        val (hour, minute) = when (type) {
            "WORKOUT" -> SettingsManager.getWorkoutNotifTime(ctx)
            else -> SettingsManager.getDietNotifTime(ctx, type)
        }
        TimePickerDialog(ctx, { _, h, m ->
            if (type == "WORKOUT") SettingsManager.setWorkoutNotifTime(ctx, h, m)
            else SettingsManager.setDietNotifTime(ctx, type, h, m)
            updateNotifTimeLabel(type)
            scheduleNotif(type)
        }, hour, minute, false).show()
    }

    private fun scheduleNotif(type: String) {
        val ctx = requireContext()
        when (type) {
            "WORKOUT" -> {
                val (h, m) = SettingsManager.getWorkoutNotifTime(ctx)
                NotificationHelper.schedule(ctx, NotificationHelper.NOTIF_ID_WORKOUT, h, m, "운동할 시간이에요 💪", "오늘의 운동을 시작해보세요!")
            }
            "BREAKFAST" -> {
                val (h, m) = SettingsManager.getDietNotifTime(ctx, "BREAKFAST")
                NotificationHelper.schedule(ctx, NotificationHelper.NOTIF_ID_BREAKFAST, h, m, "아침 식단을 기록해보세요 🍳", "오늘 아침은 무엇을 드셨나요?")
            }
            "LUNCH" -> {
                val (h, m) = SettingsManager.getDietNotifTime(ctx, "LUNCH")
                NotificationHelper.schedule(ctx, NotificationHelper.NOTIF_ID_LUNCH, h, m, "점심 식단을 기록해보세요 🍱", "오늘 점심은 무엇을 드셨나요?")
            }
            "DINNER" -> {
                val (h, m) = SettingsManager.getDietNotifTime(ctx, "DINNER")
                NotificationHelper.schedule(ctx, NotificationHelper.NOTIF_ID_DINNER, h, m, "저녁 식단을 기록해보세요 🍽️", "오늘 저녁은 무엇을 드셨나요?")
            }
        }
    }

    private fun showCalorieGoalDialog() {
        val ctx = requireContext()
        val picker = NumberPicker(ctx).apply {
            minValue = 1000
            maxValue = 5000
            value = SettingsManager.getCalorieGoal(ctx)
        }
        AlertDialog.Builder(ctx)
            .setTitle("일일 칼로리 목표")
            .setView(picker)
            .setPositiveButton("저장") { _, _ ->
                SettingsManager.setCalorieGoal(ctx, picker.value)
                binding.tvCalorieGoalValue.text = "${picker.value} kcal"
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showWorkoutFreqDialog() {
        val ctx = requireContext()
        val options = Array(7) { "주 ${it + 1}회" }
        val current = SettingsManager.getWorkoutFreq(ctx) - 1
        AlertDialog.Builder(ctx)
            .setTitle("주간 운동 목표")
            .setSingleChoiceItems(options, current) { dialog, which ->
                SettingsManager.setWorkoutFreq(ctx, which + 1)
                binding.tvWorkoutFreqValue.text = "주 ${which + 1}회"
                dialog.dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermissionLauncher.launch(permission)
            }
        }
    }

    private fun logout() {
        CoroutineScope(Dispatchers.Main).launch {
            TokenManager.clearTokens(requireContext())
        }
        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
