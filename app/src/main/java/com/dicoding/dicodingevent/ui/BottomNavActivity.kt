package com.dicoding.dicodingevent.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dicoding.dicodingevent.R
import com.dicoding.dicodingevent.databinding.ActivityBottomNavBinding
import com.dicoding.dicodingevent.ui.setting.SettingPreferences
import com.dicoding.dicodingevent.ui.setting.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BottomNavActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavBinding
    private lateinit var navController: NavController

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications permission rejected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyThemeFromPreferences()

        binding = ActivityBottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_bottom_nav)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_upcoming,
                R.id.navigation_finished,
                R.id.navigation_favorite,
                R.id.navigation_setting
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    // Pop back stack and navigate to home only if not already in home
                    if (navController.currentDestination?.id != R.id.navigation_home) {
                        navController.popBackStack(R.id.navigation_home, false)
                        navController.navigate(R.id.navigation_home)
                    }
                    true
                }

                R.id.navigation_upcoming -> {
                    // Navigate to upcoming only if not already in upcoming
                    if (navController.currentDestination?.id != R.id.navigation_upcoming) {
                        navController.navigate(R.id.navigation_upcoming)
                    }
                    true
                }

                R.id.navigation_finished -> {
                    // Navigate to finished only if not already in finished
                    if (navController.currentDestination?.id != R.id.navigation_finished) {
                        navController.navigate(R.id.navigation_finished)
                    }
                    true
                }

                R.id.navigation_favorite -> {
                    // Navigate to finished only if not already in favorite
                    if (navController.currentDestination?.id != R.id.navigation_favorite) {
                        navController.navigate(R.id.navigation_favorite)
                    }
                    true
                }

                R.id.navigation_setting -> {
                    // Navigate to finished only if not already in setting
                    if (navController.currentDestination?.id != R.id.navigation_setting) {
                        navController.navigate(R.id.navigation_setting)
                    }
                    true
                }

                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> {
                    supportActionBar?.title = "Home"
                }

                R.id.navigation_upcoming -> {
                    supportActionBar?.title = "Upcoming Events"
                }

                R.id.navigation_finished -> {
                    supportActionBar?.title = "Finished Events"
                }

                R.id.navigation_favorite -> {
                    supportActionBar?.title = "Favorite Events"
                }

                R.id.navigation_setting -> {
                    supportActionBar?.title = "Setting"
                }

                R.id.detailEventFragment -> {
                    supportActionBar?.title = "Detail Event"
                }

                else -> {
                    supportActionBar?.title = "Dicoding Event"
                }
            }
        }
    }

    private fun applyThemeFromPreferences() {
        // Membaca preferensi tema secara sinkron
        val isDarkModeActive = runBlocking {
            SettingPreferences.getInstance(dataStore).getThemeSetting().first()
        }
        if (isDarkModeActive) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    // Override this method to enable the back button functionality
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
