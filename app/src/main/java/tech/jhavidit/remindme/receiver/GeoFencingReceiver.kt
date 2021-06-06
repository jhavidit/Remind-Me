package tech.jhavidit.remindme.receiver

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices
import tech.jhavidit.remindme.service.AlarmService
import tech.jhavidit.remindme.util.GeoFencingHelper
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.util.notification
import tech.jhavidit.remindme.util.toast
import tech.jhavidit.remindme.view.activity.MainActivity
import tech.jhavidit.remindme.view.activity.TimeReminderActivity

class GeoFencingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            log(errorMessage)
            return
        }

        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {

                log("GEOFENCE_TRANSITION_ENTER")
                context?.let {
                    intent?.let { it1 -> startGeoFencingService(it, it1) }
//                    val intentNotification = Intent(context, TimeReminderActivity::class.java)
//                    val pendingIntent =
//                        PendingIntent.getActivity(
//                            context,
//                            0,
//                            intentNotification,
//                            PendingIntent.FLAG_UPDATE_CURRENT
//                        )
//                    notification(context, "geofencing reached", pendingIntent)
//                }
                }
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                // Log the error.
                log("GEOFENCE_TRANSITION_DWELL")
               /* context?.let {
                    val intentNotification = Intent(context, TimeReminderActivity::class.java)
                    val pendingIntent =
                        PendingIntent.getActivity(
                            context,
                            0,
                            intentNotification,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    notification(context, "GEOFENCE_TRANSITION_DWELL ", pendingIntent)
                }*/
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {

                context?.let {
                    /*  val intentNotification = Intent(context, TimeReminderActivity::class.java)
                    val pendingIntent =
                        PendingIntent.getActivity(
                            context,
                            0,
                            intentNotification,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    notification(context, "GEOFENCE_TRANSITION_EXIT ", pendingIntent)
                }
            }*/
                    val intentService = Intent(it, AlarmService::class.java)
                    it.stopService(intentService)
                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    fun addLocationReminder(
        context: Context,
        id: Int,
        latitude: Double,
        longitude: Double,
        radius: Double
    ) {
        val geoFencingHelper = GeoFencingHelper(context)
        val geoFencingClient = LocationServices.getGeofencingClient(context)
        val geofence: Geofence? = geoFencingHelper.getGeofence(
            id = id.toString(),
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            transitionTypes = Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest = geoFencingHelper.getGeoFencingRequest(geofence)
        val pendingIntent = geoFencingHelper.getPendingIntent(id)
        geoFencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                log("Granted Geofencing successfully")
            }
            .addOnFailureListener {
                log("error in geofence " + it.cause)
            }
    }

    fun cancelLocationReminder(context: Context, id: Int) {
        val geoFencingClient = LocationServices.getGeofencingClient(context)
        val intent = Intent(context, GeoFencingReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        geoFencingClient.removeGeofences(pendingIntent).run {
            addOnSuccessListener {
                toast(context, "Location Reminder Removed")
            }
            addOnFailureListener {
                toast(context, "Failed to remove location reminder")
            }
        }
    }

    private fun startGeoFencingService(
        context: Context,
        intent: Intent
    ) {
        val intentService = Intent(context, AlarmService::class.java)
        intentService.putExtra(
            "title",
            intent.getStringExtra("title") ?: ""
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService)
        } else {
            context.startService(intentService)
        }
    }


}
