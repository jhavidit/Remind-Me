package tech.jhavidit.remindme.util

import android.content.Context
import android.content.SharedPreferences
import tech.jhavidit.remindme.R

class LocalKeyStorage(context: Context) {
    private var prefs: SharedPreferences? =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val RINGTONE = "ringtone"
        const val RINGTONE_NAME = "ringtone_name"
        const val DO_NOT_DISTURB = "do_not_disturb"
        const val MIN_RADIUS = "min_radius"
        const val MAX_RADIUS = "max_radius"
        const val REMINDER = "reminder"
        const val SNOOZE = "snooze"
        const val ID = "id"
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