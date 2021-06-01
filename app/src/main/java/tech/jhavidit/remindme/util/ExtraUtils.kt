package tech.jhavidit.remindme.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

fun showLocationPermissionAlertDialog(context: Context){
    MaterialAlertDialogBuilder(context)
        .setTitle("Location Permission Required")
        .setMessage("You need to provide location permission to access this feature. Kindly enable it from settings")
        .setPositiveButton(
            "Ok"
        ) { _, _ ->
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri =
                Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
        .setNegativeButton(
            "Cancel"
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        .show()
}