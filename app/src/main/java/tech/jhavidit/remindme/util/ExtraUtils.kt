package tech.jhavidit.remindme.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.GeofenceStatusCodes
import java.io.ByteArrayOutputStream


@SuppressLint("LogNotTimber")
fun log(statement: String) {
    Log.d(TAG, statement)
}

fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> ">Geofence service is not available now"
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Your app has registered too many geofences"
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "You have provided too many PendingIntents to the addGeofences() call"
        else -> "Unknown error: the Geofence service is not available now"
    }
}

fun bitmapToString(bitmap: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val b: ByteArray = baos.toByteArray()
    return Base64.encodeToString(b, Base64.DEFAULT)
}

fun stringToBitmap(encodedString : String):Bitmap?{
    return try {
        val encodeByte =
            Base64.decode(encodedString, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    } catch (e: Exception) {
        e.message
        null
    }
}