package tech.jhavidit.remindme.util


const val TAG = "tag"
const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 201
const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 202
const val LOCATION_PERMISSION_INDEX = 0
const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
const val PICK_IMAGE = 101
const val PICK_IMAGE_FROM_CAMERA = 102
const val CAMERA_PERMISSION_CODE = 203
const val TIME = "time"
const val LOCATION = "location"
const val UPDATE = "update"
const val CREATE = "created"
const val AUTOCOMPLETE_REQUEST_CODE = 1
val runningQOrLater =
    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q