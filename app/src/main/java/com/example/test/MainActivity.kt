package com.example.test

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog.Calls
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.doOnTextChanged
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private val allDuration = mutableListOf<Int>()
    private val callLog = arrayOf(
        Calls._ID,
        Calls.NUMBER,
        Calls.TYPE,
        Calls.DURATION,
        Calls.DATE
    )

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestContactPermission()
        editText.setText("+77")
        editText.doOnTextChanged { _, _, _, _ ->
            textView4.text = "0"
        }
        if (editText.text.length != 12) {
            buttonIn.isClickable = false
            buttonOut.isClickable = false
        }
        buttonIn.setOnClickListener {
            allDuration.clear()
            callLogItems("1", 0)
        }
        buttonOut.setOnClickListener {
            allDuration.clear()
            callLogItems("2", 3)
        }
        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun requestContactPermission() {
        val permissions = arrayOf(Manifest.permission.READ_CALL_LOG)
        this.let {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.READ_CALL_LOG
                )
            ) {
                val firstrun =
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true)
                if (firstrun) {

                    val alert = AlertDialog.Builder(this).setCancelable(false)
                    alert.setMessage("Разрешение для использование контактов")
                    alert.setCancelable(false)
                    alert.setPositiveButton("Разрешить") { _, _ ->
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                            .edit()
                            .putBoolean("firstrun", false)
                            .commit()
                        ActivityCompat.requestPermissions(it, permissions, 1)
                    }
                    alert.create()
                    alert.show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                } else {
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                }
            }
        }
        return
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {

        }
    }

    @SuppressLint("Recycle", "Range")
    private fun callLogItems(typeCalls: String, day: Int) {

        val calendar = Calendar.getInstance()
        val toDate = calendar.timeInMillis.toString()
        calendar.add(Calendar.DAY_OF_YEAR, -day).toString()
        var fromDate = calendar.timeInMillis.toString()
        if (day == 0) {
            fromDate = "0"
        }

        val whereValue = arrayOf(fromDate, toDate)
        val requestResult: Cursor? = contentResolver.query(
            Calls.CONTENT_URI,
            callLog,
            Calls.DATE + " BETWEEN ? AND ?",
            whereValue,
            Calls.DATE + " DESC"

        )
        while (requestResult?.moveToNext() == true) {

            val callNumber = requestResult.getString(requestResult.getColumnIndex(Calls.NUMBER))
            val duration = requestResult.getInt(requestResult.getColumnIndex(Calls.DURATION))
            val type = requestResult.getInt(requestResult.getColumnIndex(Calls.TYPE)).toString()
            val date = requestResult.getInt(requestResult.getColumnIndex(Calls.DATE))

            if (type == typeCalls && callNumber == editText.text.toString()) {
                allDuration.add(duration)
            }
        }

        var text = ""
        when (typeCalls) {

            "1" -> text = "incoming call duration"
            "2" -> text = "out call duration"
        }

        val durationSum = allDuration.sum()
        durationSum.let { textView4.setText("${text} : ${it}") }
    }
}
