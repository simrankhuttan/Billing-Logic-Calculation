package com.example.billinglogiccalculation

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

data class BillRecord(
    val name: String,
    val services: String,
    val products: String,
    val finalAmount: Double,
    val serviceType: String,
    val date: String
)

class HistoryActivity : AppCompatActivity() {

    private lateinit var cbPremium: CheckBox
    private lateinit var cbUrgent: CheckBox
    private lateinit var cbAbove2000: CheckBox
    private lateinit var btnApplyFilters: Button
    private lateinit var layoutHistory: LinearLayout

    private val sharedPrefFile = "SalonHistoryPref"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        cbPremium = findViewById(R.id.cbPremium)
        cbUrgent = findViewById(R.id.cbUrgent)
        cbAbove2000 = findViewById(R.id.cbAbove2000)
        btnApplyFilters = findViewById(R.id.btnApplyFilters)
        layoutHistory = findViewById(R.id.layoutHistory)

        btnApplyFilters.setOnClickListener {
            displayHistory()
        }

        displayHistory() // show all history by default
    }

    private fun displayHistory() {
        layoutHistory.removeAllViews()

        val sharedPref = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val allRecords = sharedPref.all.mapNotNull {
            val data = it.value as? String
            data?.let { parseRecord(it) }
        }

        val filteredRecords = allRecords.filter { record ->
            var valid = true
            if (cbPremium.isChecked) valid = valid && record.serviceType == "Premium"
            if (cbUrgent.isChecked) valid = valid && record.serviceType == "Urgent"
            if (cbAbove2000.isChecked) valid = valid && record.finalAmount > 2000
            valid
        }

        if (filteredRecords.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No records found"
            layoutHistory.addView(tv)
        } else {
            filteredRecords.forEach { record ->
                val tv = TextView(this)
                tv.text = """
                    Name: ${record.name}
                    Services: ${record.services}
                    Products: ${record.products}
                    Type: ${record.serviceType}
                    Amount: ₹${String.format("%.2f", record.finalAmount)}
                    Date: ${record.date}
                """.trimIndent()
                tv.setPadding(0, 0, 0, 16)
                layoutHistory.addView(tv)
            }
        }
    }

    private fun parseRecord(data: String): BillRecord {
        // Stored as CSV: name|services|products|amount|type|date
        val parts = data.split("|")
        return BillRecord(
            name = parts[0],
            services = parts[1],
            products = parts[2],
            finalAmount = parts[3].toDouble(),
            serviceType = parts[4],
            date = parts[5]
        )
    }
}
