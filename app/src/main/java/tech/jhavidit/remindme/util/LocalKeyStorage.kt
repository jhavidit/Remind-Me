package tech.jhavidit.remindme.util

import android.content.Context
import android.content.SharedPreferences
import tech.jhavidit.remindme.R

class LocalKeyStorage(context: Context) {
    private var prefs: SharedPreferences? =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {

    }

    fun saveValue(key: String, value: String) {
        val editor = prefs?.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    fun getValue(key: String): String? {
        return prefs?.getString(key, null)
    }

    fun deleteValue(key: String) {
        val editor = prefs?.edit()
        editor?.putString(key, null)
        editor?.apply()
    }
}