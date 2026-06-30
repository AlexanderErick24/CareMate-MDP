//package com.mdp.caremate
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.view.View
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import androidx.navigation.fragment.NavHostFragment
//import androidx.navigation.ui.setupWithNavController
//import com.mdp.caremate.R
//import com.mdp.caremate.databinding.ActivityMainBinding
//
//class MainActivity : AppCompatActivity() {
//    private val notificationPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { }
//    private lateinit var binding: ActivityMainBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        requestNotificationPermissionIfNeeded()
//        setupNavigation()
//    }
//
//    private fun setupNavigation() {
//        val navHostFragment = supportFragmentManager
//            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//
//        binding.bottomNav.setupWithNavController(navController)
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            binding.bottomNav.visibility = when (destination.id) {
//                R.id.dest_med_form, R.id.loginFragment, R.id.registerFragment, R.id.dest_event_detail, R.id.familyContainerFragment -> View.GONE
//                else -> View.VISIBLE
//            }
//        }
//    }
//
//    private fun requestNotificationPermissionIfNeeded() {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//        }
//    }
//}



// cornel


package com.mdp.caremate

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mdp.caremate.R
import com.mdp.caremate.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermissionIfNeeded()
        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Hubungkan awal ke navController dengan menu default (User/Caregiver)
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 1. ATUR MENU SECARA DINAMIS BERDASARKAN DESTINATION ID
            when (destination.id) {
                R.id.adminDashboard, R.id.adminEvents, R.id.adminUsers, R.id.adminReports, R.id.adminSettings -> {
                    // Jika tujuan navigasi adalah salah satu halaman admin, ganti menu ke admin
                    binding.bottomNav.menu.clear()
                    binding.bottomNav.inflateMenu(R.menu.menu_bottom_nav)
                    // Hubungkan ulang agar navController mengenali menu admin yang baru di-inflate
                    binding.bottomNav.setupWithNavController(navController)
                }
            }

            // 2. ATUR VISIBILITY BOTTOM NAVIGATION
            binding.bottomNav.visibility = when (destination.id) {
                R.id.dest_med_form,
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.dest_event_detail,
                R.id.familyContainerFragment -> View.GONE
                R.id.userDetail -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}