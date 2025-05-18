/*
 * Copyright 2025 Meta Busters.
 */
package org.kijitora.develop.ssr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import org.kijitora.develop.ssr.viewmodel.UnitListViewModel
import org.kijitora.develop.ssr.viewmodel.UnitListViewModelFactory

class MainActivity : AppCompatActivity() {

    private val viewModel: UnitListViewModel by viewModels {
        UnitListViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

}