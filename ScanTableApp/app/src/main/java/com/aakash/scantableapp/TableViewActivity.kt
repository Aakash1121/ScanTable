package com.aakash.scantableapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.aakash.scantableapp.databinding.ActivityTableViewBinding
import com.aakash.scantableapp.model.TableModel
import com.google.gson.Gson

class TableViewActivity : AppCompatActivity() {
    lateinit var mBinding: ActivityTableViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_table_view)



        // Add headers
        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        val tableModelObject = Gson().fromJson(intent.getStringExtra("INTENT_DATA"),TableModel::class.java)
        val dataList: List<List<String>> = tableModelObject.tableTextList

        // Add rows and cells to the table
        for (rowList in dataList) {
            val tableRow = TableRow(this)
            for (cellValue in rowList) {
                val textView = TextView(this).apply {
                    text = cellValue
                    // Add any additional styling if needed
                }
                tableRow.addView(textView)
            }
            tableLayout.addView(tableRow)
        }

    }
}