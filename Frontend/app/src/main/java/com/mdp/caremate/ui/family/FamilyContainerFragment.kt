package com.mdp.caremate.ui.family

import android.os.Bundle
import android.view.View

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mdp.caremate.R

class FamilyContainerFragment :
    Fragment(R.layout.fragment_family_container) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        val navHost =
            childFragmentManager
                .findFragmentById(
                    R.id.familyNavHost
                ) as NavHostFragment

        val navController =
            navHost.navController

        val bottomNav =
            view.findViewById<BottomNavigationView>(
                R.id.familyBottomNav
            )

        bottomNav.setupWithNavController(
            navController
        )
    }
}