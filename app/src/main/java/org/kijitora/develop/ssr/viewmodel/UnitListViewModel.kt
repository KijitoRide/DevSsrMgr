/*
 * Copyright 2025 Meta Busters.
 */
package org.kijitora.develop.ssr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData

import org.kijitora.develop.ssr.db.AppDatabase
import org.kijitora.develop.ssr.db.dataclass.entity.Account
import org.kijitora.develop.ssr.db.dataclass.UserUnitWithMaster
import org.kijitora.develop.ssr.db.dataclass.entity.UserUnit
import org.kijitora.develop.ssr.db.dataclass.entity.MasterUnit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UnitListViewModel(private val appDatabase: AppDatabase) : ViewModel() {

    // 監視可能なデータホルダー
    // つまりアカウントIDを監視している
    private val _currentAccountId = MutableLiveData<Long?>(null)
    val currentAccountId: LiveData<Long?> = _currentAccountId

    val accounts: LiveData<List<Account>> = appDatabase.accountDao().getAll().asLiveData()

    private val _searchText = MutableLiveData<String>("")
    val searchText: LiveData<String> = _searchText

    private val _refreshTrigger = MutableLiveData<Unit>(Unit)

    // アカウントIDが変更された時、または検索文字が変更された時にライブデータを更新する
    val userUnitsWithMaster: LiveData<List<UserUnitWithMaster>> =
        MediatorLiveData<List<UserUnitWithMaster>>().apply {
            fun load(accountId: Long?, query: String?) {
                if (accountId != null) {
                    viewModelScope.launch {
                        val result = appDatabase.userUnitDao()
                            .searchUserUnitsByAccountId(accountId, query ?: "")
                            .firstOrNull() ?: emptyList()
                        value = result
                    }
                } else {
                    value = emptyList()
                }
            }

            addSource(_currentAccountId) { load(it, _searchText.value) }
            addSource(_searchText) { load(_currentAccountId.value, it) }
            addSource(_refreshTrigger) { load(_currentAccountId.value, _searchText.value) }
        }

    fun refreshData() {
        _refreshTrigger.value = Unit
    }

    fun setCurrentAccount(accountId: Long) {
        _currentAccountId.value = accountId
    }

    fun setSearchText(text: String) {
        _searchText.value = text
    }

    // 新しいアカウントを追加
    fun addAccount(accountName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // アカウント名を登録して、自動採番されたアカウントIDを取得する
            val accountId = appDatabase.accountDao().insert(Account(accountName = accountName))

            if (accountId > 0) {
                appDatabase.masterUnitDao().getAllMasterUnits().flowOn(Dispatchers.IO)
                    .collect { masterUnits ->
                        val initialUserUnits = masterUnits.map { masterUnit ->
                            UserUnit(accountId = accountId, unitName = masterUnit.unitName)
                        }
                        appDatabase.userUnitDao().insertAll(initialUserUnits)
                    }
            }

        }
    }

    // アカウントを削除
    fun delAccount(accountId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.userUnitDao().delete(accountId)
            appDatabase.accountDao().delete(accountId)
        }
        _currentAccountId.value = 0
    }

    // ユーザーの機体情報を更新または追加
    fun updateUserUnit(userUnit: UserUnit) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.userUnitDao().insertOrUpdate(userUnit)
        }
    }

    // 初期マスターデータを投入する関数（必要に応じて）
    fun updateMasterUnits(units: List<MasterUnit>, accountId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.masterUnitDao().insertAll(units)

            appDatabase.masterUnitDao().getAllMasterUnits().flowOn(Dispatchers.IO)
                .collect { masterUnits ->
                    val initialUserUnits = masterUnits.map { masterUnit ->
                        UserUnit(accountId = accountId, unitName = masterUnit.unitName)
                    }
                    appDatabase.userUnitDao().insertAllIgnoreWhenConflict(initialUserUnits)
                }
        }

    }

    // 初期マスターデータを投入する関数（必要に応じて）
    fun insertInitialMasterUnits(units: List<MasterUnit>) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.masterUnitDao().insertAll(units)
        }
    }

}