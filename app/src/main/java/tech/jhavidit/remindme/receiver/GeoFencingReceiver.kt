package tech.jhavidit.remindme.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.util.notification
import tech.jhavidit.remindme.view.activity.MainActivity

class GeoFencingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            log(errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            log("GEOFENCE_TRANSITION_ENTER")
            context?.let {
                val intentNotification = Intent(context, MainActivity::class.java)
                val pendingIntent =
                    PendingIntent.getActivity(context, 0, intentNotification, PendingIntent.FLAG_UPDATE_CURRENT)
                notification(context, "geofencing reached", pendingIntent)
            }
        } else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            // Log the error.
            log("GEOFENCE_TRANSITION_DWELL")
            context?.let {
                val intentNotification = Intent(context, MainActivity::class.java)
                val pendingIntent =
                    PendingIntent.getActivity(context, 0, intentNotification, PendingIntent.FLAG_UPDATE_CURRENT)
                notification(context, "GEOFENCE_TRANSITION_DWELL ", pendingIntent)
            }
        }
        else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {

            context?.let {
                val intentNotification = Intent(context, MainActivity::class.java)
                val pendingIntent =
                    PendingIntent.getActivity(context, 0, intentNotification, PendingIntent.FLAG_UPDATE_CURRENT)
                notification(context, "GEOFENCE_TRANSITION_EXIT ", pendingIntent)
            }
        }
    }
}
