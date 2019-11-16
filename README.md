# MaskedEditText
An easy to use masked edit text library

Implementation:

1- Add the following to your XML code

```
<com.appro.maskededittext.MaskedEditText
        android:id="@+id/maskedEditText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
```

2- Kotlin/Java Implementation
```
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
```
