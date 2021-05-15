package tech.jhavidit.remindme.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast

@SuppressLint("LogNotTimber")
fun log(statement: String) {
    Log.d("TAG", statement)
}

fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}