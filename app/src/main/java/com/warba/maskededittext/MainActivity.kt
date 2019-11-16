package com.warba.maskededittext

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.appro.maskededittext.MaskedEditText
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        maskedEditText.setPattern("Visa Card 4371-####-##21-4321")

        maskedEditText.setOnFieldsChangedListener(object : MaskedEditText.OnFieldsChangedListener{
            override fun onFieldsChanged(
                maskedEditText: MaskedEditText,
                value: String,
                isDone: Boolean
            ) {
                Toast.makeText(this@MainActivity, "$value | isDone: $isDone", Toast.LENGTH_SHORT).show()
            }

        })

    }
}
