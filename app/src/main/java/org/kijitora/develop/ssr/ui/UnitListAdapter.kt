/*
 * Copyright 2025 Meta Busters.
 */
package org.kijitora.develop.ssr.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.kijitora.develop.ssr.R
import org.kijitora.develop.ssr.db.dataclass.UserUnitWithMaster
import org.kijitora.develop.ssr.viewmodel.UnitListViewModel

/**
 * 機体一覧のアダプター.
 */
class UnitListAdapter(private val viewModel: UnitListViewModel) : androidx.recyclerview.widget.ListAdapter<UserUnitWithMaster, UnitListAdapter.UnitViewHolder>(UnitDiffCallback()) {

    class UnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // シリーズ名
        val seriesNameTextView: TextView = itemView.findViewById(R.id.seriesNameTextView)
        // 機体名
        val unitNameTextView: TextView = itemView.findViewById(R.id.unitNameTextView)
        // 入手経路
        val acquisitionMethodText: TextView = itemView.findViewById(R.id.acquisitionMethodTextView)
        // 初回クリア報酬
        val firstClearRewardTextView: TextView = itemView.findViewById(R.id.firstClearRewardTextView)
        // 凸数
        val dupeCountSpinner: Spinner = itemView.findViewById(R.id.dupeCountSpinner)
        // 研究技術書
        val technicalManualCountSpinner: Spinner = itemView.findViewById(R.id.technicalManualCountSpinner)
        // 機体レイアウト
        val unitLayout: LinearLayout = itemView.findViewById(R.id.unitLayout)

        val context: Context = itemView.context

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_unit, parent, false)
        return UnitViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val currentItem: UserUnitWithMaster = getItem(position)

        holder.seriesNameTextView.text = currentItem.masterUnit.seriesName
        if (position == 0 || getItem(position - 1).masterUnit.seriesName != currentItem.masterUnit.seriesName) {
            holder.seriesNameTextView.visibility = View.VISIBLE
        } else {
            holder.seriesNameTextView.visibility = View.GONE
        }
        holder.unitNameTextView.text = currentItem.masterUnit.unitName

        holder.acquisitionMethodText.text = currentItem.masterUnit.acquisitionMethod
        holder.firstClearRewardTextView.text = currentItem.masterUnit.firstClearReward

        val breakthroughCountArray: Array<String> = holder.context.resources.getStringArray(R.array.breakthrough_count_array)

        // 限界突破回数スピナーの設定
        val breakthroughCountAdapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, breakthroughCountArray)
        holder.dupeCountSpinner.adapter = breakthroughCountAdapter
        holder.dupeCountSpinner.setSelection(breakthroughCountArray.indexOf(currentItem.userUnit.breakThroughCount))
        holder.dupeCountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (currentItem.userUnit.breakThroughCount != breakthroughCountArray[position]) {
                    viewModel.updateUserUnit(currentItem.userUnit.copy(breakThroughCount = breakthroughCountArray[position]))
                    currentItem.userUnit.breakThroughCount = breakthroughCountArray[position]

                    updateLayoutBackground(holder, currentItem)

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 研究技術書数スピナーの設定
        val technicalManualCountOptions = (0..13).toList()
        val technicalManualCountAdapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, technicalManualCountOptions)
        holder.technicalManualCountSpinner.adapter = technicalManualCountAdapter
        holder.technicalManualCountSpinner.setSelection(technicalManualCountOptions.indexOf(currentItem.userUnit.technicalManualCount))
        holder.technicalManualCountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (currentItem.userUnit.technicalManualCount != technicalManualCountOptions[position]) {
                    viewModel.updateUserUnit(currentItem.userUnit.copy(technicalManualCount = technicalManualCountOptions[position]))
                    currentItem.userUnit.technicalManualCount  = technicalManualCountOptions[position]

                    updateLayoutBackground(holder, currentItem)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        updateLayoutBackground(holder, currentItem)

    }

    fun updateLayoutBackground(holder: UnitViewHolder, currentItem: UserUnitWithMaster) {

        // 限界突破の数が3だった場合
        if (currentItem.userUnit.breakThroughCount == "3") {
            holder.unitLayout.setBackgroundColor(Color.LTGRAY)
        } else if (currentItem.userUnit.breakThroughCount == "2" && 3 <= currentItem.userUnit.technicalManualCount) {
            holder.unitLayout.setBackgroundColor(Color.LTGRAY)
        } else if (currentItem.userUnit.breakThroughCount == "1" && 6 <= currentItem.userUnit.technicalManualCount) {
            holder.unitLayout.setBackgroundColor(Color.LTGRAY)
        } else if (currentItem.userUnit.breakThroughCount == "0" && 9 <= currentItem.userUnit.technicalManualCount) {
            holder.unitLayout.setBackgroundColor(Color.LTGRAY)
        } else if (12 <= currentItem.userUnit.technicalManualCount) {
            holder.unitLayout.setBackgroundColor(Color.LTGRAY)
        } else {
            holder.unitLayout.setBackgroundColor(Color.WHITE)
        }
    }

    class UnitDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<UserUnitWithMaster>() {
        override fun areItemsTheSame(oldItem: UserUnitWithMaster, newItem: UserUnitWithMaster): Boolean {
            return oldItem.userUnit.accountId == newItem.userUnit.accountId && oldItem.userUnit.unitName == newItem.userUnit.unitName
        }

        override fun areContentsTheSame(oldItem: UserUnitWithMaster, newItem: UserUnitWithMaster): Boolean {
            return oldItem == newItem
        }
    }
}