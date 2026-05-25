package com.hont.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hont.app.databinding.ActivityMainBinding
import com.hont.app.diet.DietFragment
import com.hont.app.profile.ProfileFragment
import com.hont.app.settings.SettingsFragment
import com.hont.app.workout.StartWorkoutSheet
import com.hont.app.workout.WorkoutFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 기본 화면: 운동 탭
        if (savedInstanceState == null) {
            replaceFragment(WorkoutFragment())
            binding.bottomNav.selectedItemId = R.id.nav_workout
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_workout -> replaceFragment(WorkoutFragment())
                R.id.nav_diet -> replaceFragment(DietFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
                R.id.nav_settings -> replaceFragment(SettingsFragment())
                R.id.nav_space -> return@setOnItemSelectedListener false
            }
            true
        }

        binding.fabCenter.setOnClickListener {
            StartWorkoutSheet().show(supportFragmentManager, "start_workout")
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
