package tech.jhavidit.remindme.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
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
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence service is not available now"
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Your app has registered too many geofences"
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "You have provided too many PendingIntents to the addGeofences() call"
        else -> "Unknown error: the Geofence service is not available now"
    }
}

fun stringToUri(image: String): Uri? {
    return Uri.parse(image)
}

fun bitmapToUri(inContext: Context, inImage: Bitmap): Uri? {
    val bytes = ByteArrayOutputStream()
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path =
        MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
    return Uri.parse(path)
}

fun getRadius(minRadius: Double, maxRadius: Double, progress: Int): Double {
    return ((progress.toDouble() / 100.0 * (maxRadius - minRadius)) + minRadius)
}