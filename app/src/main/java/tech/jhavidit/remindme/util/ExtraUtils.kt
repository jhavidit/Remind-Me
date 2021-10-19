package tech.jhavidit.remindme.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.*
import java.util.*
import kotlin.math.min


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

fun showCurrentTime(): String {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR)
    val minutes = calendar.get(Calendar.MINUTE)
    if (hour > 12) {
        return "$hour:$minutes PM"
    } else if (hour < 10) {
        return "0$hour:$minutes AM"
    } else {
        return "$hour:$minutes AM"
    }
}

fun uriToBitmap(context: Context, selectedFileUri: Uri): Bitmap? {
    return try {
        val parcelFileDescriptor: ParcelFileDescriptor? =
            context.contentResolver.openFileDescriptor(selectedFileUri, "r")
        val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        image
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun saveToInternalStorage(bitmapImage: Bitmap, id: Int, activity: Activity): String? {
    val cw = ContextWrapper(activity.applicationContext)
    // path to /data/data/yourapp/app_data/imageDir
    val directory: File = cw.getDir("imageDir", Context.MODE_PRIVATE)
    // Create imageDir
    val myPath = File(directory, "image$id.jpg")
    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(myPath)
        // Use the compress method on the BitMap object to write image to the OutputStream
        bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return directory.absolutePath
}

fun loadImageFromStorage(path: String, id: Int): Bitmap? {
    return try {
        val f = File(path, "image$id.jpg")
        BitmapFactory.decodeStream(FileInputStream(f))
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}


fun getRadius(minRadius: Double, maxRadius: Double, progress: Int): Double {
    return ((progress.toDouble() / 100.0 * (maxRadius - minRadius)) + minRadius)
}

fun showLocationPermissionAlertDialog(context: Context) {
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

fun getNameFromUri(context: Context, uri: Uri): String? {
    val returnCursor = context.contentResolver.query(uri, null, null, null, null)
    val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor?.moveToFirst()
    var fileName = nameIndex?.let { returnCursor.getString(it) }
    returnCursor?.close()

    fileName?.let {
        var name = it.replace('_', ' ', false)
        name = name.substring(0, name.indexOf("."))
        if (name.length > 16)
            name = it.substring(0, 16) + "..."
        fileName = name
    }
    return fileName
}
