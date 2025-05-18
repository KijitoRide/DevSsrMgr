/*
 * Copyright 2025 Meta Busters.
 */
package org.kijitora.develop.ssr.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import org.kijitora.develop.ssr.db.AppDatabase

class UnitListViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UnitListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UnitListViewModel(AppDatabase.getDatabase(application)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}