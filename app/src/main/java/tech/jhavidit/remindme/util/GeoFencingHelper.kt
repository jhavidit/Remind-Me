package tech.jhavidit.remindme.util

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import tech.jhavidit.remindme.receiver.GeoFencingReceiver


class GeoFencingHelper(context: Context) : ContextWrapper(context) {
    private lateinit var pendingIntent: PendingIntent

    fun getGeoFencingRequest(geofence: Geofence?): GeofencingRequest? {
        return GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()
    }

    fun getGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
        transitionTypes: Int
    ): Geofence? {
        return Geofence.Builder()
            .setCircularRegion(latitude, longitude, radius.toFloat())
            .setRequestId(id)
            .setTransitionTypes(transitionTypes)
            .setLoiteringDelay(5000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    fun getPendingIntent(id: Int): PendingIntent? {

        val intent = Intent(this, GeoFencingReceiver::class.java)
        pendingIntent =
            PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return pendingIntent
    }

}