package com.example.billinglogiccalculation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var etVisits: EditText
    private lateinit var etPromoCode: EditText
    private lateinit var cbHaircut: CheckBox
    private lateinit var cbHairColor: CheckBox
    private lateinit var cbFacial: CheckBox
    private lateinit var cbManicure: CheckBox
    private lateinit var cbPedicure: CheckBox
    private lateinit var cbShampoo: CheckBox
    private lateinit var cbConditioner: CheckBox
    private lateinit var cbMask: CheckBox
    private lateinit var rgServiceType: RadioGroup
    private lateinit var btnCalculateBill: Button
    private lateinit var btnViewHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        etName = findViewById(R.id.etName)
        etAge = findViewById(R.id.etAge)
        etVisits = findViewById(R.id.etVisits)
        etPromoCode = findViewById(R.id.etPromoCode)

        cbHaircut = findViewById(R.id.cbHaircut)
        cbHairColor = findViewById(R.id.cbHairColor)
        cbFacial = findViewById(R.id.cbFacial)
        cbManicure = findViewById(R.id.cbManicure)
        cbPedicure = findViewById(R.id.cbPedicure)

        cbShampoo = findViewById(R.id.cbShampoo)
        cbConditioner = findViewById(R.id.cbConditioner)
        cbMask = findViewById(R.id.cbMask)

        rgServiceType = findViewById(R.id.rgServiceType)
        btnCalculateBill = findViewById(R.id.btnCalculateBill)
        btnViewHistory = findViewById(R.id.btnViewHistory)

        btnCalculateBill.setOnClickListener { calculateBill() }

        btnViewHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun calculateBill() {
        val name = etName.text.toString()
        val ageStr = etAge.text.toString()
        val visitsStr = etVisits.text.toString()
        val promoCode = etPromoCode.text.toString()

        if (name.isEmpty() || ageStr.isEmpty() || visitsStr.isEmpty()) {
            Toast.makeText(this, "Enter Name, Age & Previous Visits", Toast.LENGTH_SHORT).show()
            return
        }

        val visits = visitsStr.toInt()

        val serviceCosts = mapOf(
            "Haircut" to 500,
            "Hair Color" to 1500,
            "Facial" to 1000,
            "Manicure" to 700,
            "Pedicure" to 800
        )
        val productCosts = mapOf(
            "Shampoo" to 300,
            "Conditioner" to 350,
            "Mask" to 400
        )

        var total = 0
        if (cbHaircut.isChecked) total += serviceCosts["Haircut"]!!
        if (cbHairColor.isChecked) total += serviceCosts["Hair Color"]!!
        if (cbFacial.isChecked) total += serviceCosts["Facial"]!!
        if (cbManicure.isChecked) total += serviceCosts["Manicure"]!!
        if (cbPedicure.isChecked) total += serviceCosts["Pedicure"]!!

        if (cbShampoo.isChecked) total += productCosts["Shampoo"]!!
        if (cbConditioner.isChecked) total += productCosts["Conditioner"]!!
        if (cbMask.isChecked) total += productCosts["Mask"]!!

        var discount = when {
            visits >= 5 -> total * 0.20
            visits >= 3 -> total * 0.10
            else -> 0.0
        }

        val selectedTypeId = rgServiceType.checkedRadioButtonId
        var surcharge = 0.0
        var selectedType = "Normal"
        if (selectedTypeId != -1) {
            selectedType = findViewById<RadioButton>(selectedTypeId).text.toString()
            surcharge = when(selectedType) {
                "Urgent" -> total * 0.15
                "Premium" -> total * 0.25
                else -> 0.0
            }
        }

        if (promoCode.equals("BEAUTY10", true)) {
            discount += (total - discount) * 0.10
        }

        val gst = (total - discount + surcharge) * 0.18
        val finalAmount = total - discount + surcharge + gst

        Toast.makeText(this, "Final Bill: ₹${String.format("%.2f", finalAmount)}", Toast.LENGTH_LONG).show()

        // --- SAVE TO SHARED PREFERENCES ---
        val sharedPref = getSharedPreferences("SalonHistoryPref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val servicesSelected = listOf(
            if (cbHaircut.isChecked) "Haircut" else "",
            if (cbHairColor.isChecked) "Hair Color" else "",
            if (cbFacial.isChecked) "Facial" else "",
            if (cbManicure.isChecked) "Manicure" else "",
            if (cbPedicure.isChecked) "Pedicure" else ""
        ).filter { it.isNotEmpty() }.joinToString(", ")

        val productsSelected = listOf(
            if (cbShampoo.isChecked) "Shampoo" else "",
            if (cbConditioner.isChecked) "Conditioner" else "",
            if (cbMask.isChecked) "Mask" else ""
        ).filter { it.isNotEmpty() }.joinToString(", ")

        val currentDate = java.text.SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        val recordData = "$name|$servicesSelected|$productsSelected|${String.format("%.2f", finalAmount)}|$selectedType|$currentDate"

        val key = System.currentTimeMillis().toString()
        editor.putString(key, recordData)
        editor.apply()
    }
}
