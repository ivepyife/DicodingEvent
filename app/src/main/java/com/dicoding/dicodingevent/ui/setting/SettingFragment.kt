package com.dicoding.dicodingevent.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dicoding.dicodingevent.R
import com.dicoding.dicodingevent.ui.MainViewModel
import com.dicoding.dicodingevent.ui.MyWorker
import com.dicoding.dicodingevent.ui.ViewModelFactory
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.concurrent.TimeUnit

class SettingFragment : Fragment() {

    private lateinit var switchReminder: SwitchMaterial
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchTheme = view.findViewById<SwitchMaterial>(R.id.switch_theme)
        switchReminder = view.findViewById(R.id.switch_daily_reminder)

        val pref = SettingPreferences.getInstance(requireContext().dataStore)
        val mainViewModel: MainViewModel by viewModels {
            ViewModelFactory.getInstance(requireContext())
        }

        // Theme settings
        mainViewModel.getThemeSettings().observe(viewLifecycleOwner) { isDarkModeActive: Boolean ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                switchTheme.isChecked = true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                switchTheme.isChecked = false
            }
        }

        switchTheme.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            mainViewModel.saveThemeSetting(isChecked)
        }

        // Daily reminder settings
        mainViewModel.getNotificationSettings().observe(viewLifecycleOwner) { isNotificationActive ->
            switchReminder.isChecked = isNotificationActive
            if (isNotificationActive) {
                startDailyReminder()
            } else {
                cancelDailyReminder()
            }
        }

        switchReminder.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            mainViewModel.saveNotificationSetting(isChecked)
        }
    }

    private fun startDailyReminder() {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<MyWorker>(
            1, TimeUnit.DAYS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    private fun cancelDailyReminder() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    companion object {
        private const val WORK_NAME = "daily_reminder_work"
    }
}