package tech.jhavidit.remindme.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityTimeReminderBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.receiver.GeoFencingReceiver
import tech.jhavidit.remindme.service.AlarmService
import tech.jhavidit.remindme.service.LocationReminderService
import tech.jhavidit.remindme.util.*
import tech.jhavidit.remindme.viewModel.NotesViewModel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class ReminderScreenActivity : AppCompatActivity(), View.OnTouchListener {
    private lateinit var binding: ActivityTimeReminderBinding
    private lateinit var viewModel: NotesViewModel
    private lateinit var alarmReceiver: AlarmReceiver
    private lateinit var geoFencingReceiver: GeoFencingReceiver
    private var notes: NotesModel? = null
    private var id: Int = 0
    private var snooze: Boolean = false
    private var reminder: String? = ""
    private var originalX = 0F
    private var originalY = 0F
    private var downRawX = 0f
    private var downRawY = 0f
    private var dX = 0f
    private var dY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_time_reminder)
        alarmReceiver = AlarmReceiver()
        geoFencingReceiver = GeoFencingReceiver()
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        val notesTimeBundle = intent?.getBundleExtra(NOTES_TIME)


        binding.reminderFab.setOnTouchListener(this)
        notesTimeBundle?.let {
            id = notesTimeBundle.getInt("id")
            val time = notesTimeBundle.getString("reminderTime")
            viewModel.selectedNote(id).observe(this, Observer {
                binding.reminderLocationTime.text =
                    time
                binding.title.text = it[0].title
                binding.description.text = it[0].description
            })
            snooze = notesTimeBundle.getBoolean("snooze")
            reminder = notesTimeBundle.getString("reminder") ?: ""
            binding.reminderIcon.setImageResource(R.drawable.ic_alarm_set)
        }

        val notesLocationBundle = intent?.getBundleExtra(NOTES_LOCATION)

        notesLocationBundle?.let {
            id = notesLocationBundle.getInt("id")
            snooze = notesLocationBundle.getBoolean("snooze")
            reminder = notesLocationBundle.getString("reminder") ?: ""
            binding.reminderIcon.setImageResource(R.drawable.ic_alarm_set)
            viewModel.selectedNote(id).observe(this, Observer {
                binding.reminderLocationTime.text =
                    it[0].locationName
                binding.title.text = it[0].title
                binding.description.text = it[0].description
            })
            binding.reminderIcon.setImageResource(R.drawable.ic_add_location)


        }

        viewModel.selectedNote(id).observe(this, {
            notes = it[0]
        })

        binding.openApp.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        notesTimeBundle?.clear()
        notesLocationBundle?.clear()

    }

    private fun cancelReminder() {

        notes?.let { notes ->
            if (reminder == "time") {
                if (notes.repeatValue == -1L) {
                    val notesModel = NotesModel(
                        id = notes.id,
                        title = notes.title,
                        description = notes.description,
                        locationReminder = notes.locationReminder,
                        timeReminder = null,
                        reminderWaitTime = null,
                        reminderTime = null,
                        reminderDate = null,
                        image = notes.image,
                        isPinned = notes.isPinned,
                        latitude = notes.latitude,
                        longitude = notes.longitude,
                        radius = notes.radius,
                        repeatValue = null,
                        lastUpdated = notes.lastUpdated,
                        locationName = notes.locationName,
                        backgroundColor = notes.backgroundColor
                    )
                    viewModel.updateNotes(notesModel)
                }
                val intentService = Intent(applicationContext, AlarmService::class.java)
                applicationContext.stopService(intentService)
            } else if (reminder == "location" && !snooze) {

                geoFencingReceiver.cancelLocationReminder(this, notes.id)
                val notesModel = NotesModel(
                    id = notes.id,
                    title = notes.title,
                    description = notes.description,
                    locationReminder = null,
                    timeReminder = notes.timeReminder,
                    reminderWaitTime = notes.reminderWaitTime,
                    reminderTime = notes.reminderTime,
                    reminderDate = notes.reminderDate,
                    isPinned = notes.isPinned,
                    latitude = null,
                    lastUpdated = notes.lastUpdated,
                    image = notes.image,
                    longitude = null,
                    radius = null,
                    repeatValue = notes.repeatValue,
                    locationName = null,
                    backgroundColor = notes.backgroundColor
                )
                viewModel.updateNotes(notesModel)
                val intentService =
                    Intent(applicationContext, LocationReminderService::class.java)
                applicationContext.stopService(intentService)


            } else if (reminder == "location" && snooze) {

                val notesModel = NotesModel(
                    id = notes.id,
                    title = notes.title,
                    description = notes.description,
                    locationReminder = null,
                    timeReminder = notes.timeReminder,
                    reminderWaitTime = notes.reminderWaitTime,
                    reminderTime = notes.reminderTime,
                    image = notes.image,
                    reminderDate = notes.reminderDate,
                    isPinned = notes.isPinned,
                    latitude = null,
                    longitude = null,
                    lastUpdated = notes.lastUpdated,
                    radius = null,
                    repeatValue = notes.repeatValue,
                    locationName = null,
                    backgroundColor = notes.backgroundColor
                )
                viewModel.updateNotes(notesModel)
                val intentService =
                    Intent(applicationContext, AlarmService::class.java)
                applicationContext.stopService(intentService)
            }

            deleteKey()
            finish()
        }

    }

    private fun snoozeReminder() {

        notes?.let { notes ->
            reminder?.let { remind ->
                alarmReceiver.scheduleSnoozeAlarm(this, notes, remind)
                toast(this, "Reminder snoozed for five minutes")
                if (remind == "time") {
                    val intentService = Intent(applicationContext, AlarmService::class.java)
                    applicationContext.stopService(intentService)
                    finish()
                } else if (remind == "location") {
                    val intentService =
                        Intent(applicationContext, LocationReminderService::class.java)
                    applicationContext.stopService(intentService)
                    deleteKey()
                    finish()
                }
            }
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        originalX =
            getRelativeLeft(binding.reminderFab).toFloat() - getRelativeLeft(binding.reminderOffCard).toFloat()
        originalY =
            getRelativeTop(binding.reminderFab).toFloat() - getRelativeTop(binding.reminderOffCard).toFloat()
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        val action = motionEvent.action
        return if (action == MotionEvent.ACTION_DOWN) {
            downRawX = motionEvent.rawX
            downRawY = motionEvent.rawY
            dX = view.x - downRawX
            dY = view.y - downRawY
            true // Consumed
        } else if (action == MotionEvent.ACTION_MOVE) {
            val viewWidth = view.width
            val viewHeight = view.height
            val viewParent = view.parent as View
            val parentWidth = viewParent.width
            val parentHeight = viewParent.height
            var newX = motionEvent.rawX + dX
            newX = max(
                layoutParams.leftMargin.toFloat(),
                newX
            ) // Don't allow the FAB past the left hand side of the parent
            newX = min(
                (parentWidth - viewWidth - layoutParams.rightMargin).toFloat(),
                newX
            ) // Don't allow the FAB past the right hand side of the parent
            var newY = motionEvent.rawY + dY
//            newY = max(
//                layoutParams.topMargin.toFloat(),
//                newY
//            ) // Don't allow the FAB past the top of the parent
//            newY = min(
//                (parentHeight - viewHeight - layoutParams.bottomMargin).toFloat(),
//                newY
            //   ) // Don't allow the FAB past the bottom of the parent
            view.animate()
                .x(newX)
                .y(originalY)
                .setDuration(0)
                .start()
            true // Consumed
        } else if (action == MotionEvent.ACTION_UP) {
            val upRawX = motionEvent.rawX
            val upRawY = motionEvent.rawY
            val upDX = upRawX - downRawX
            val upDY = upRawY - downRawY
            log("upRaw  $upRawX $upRawY $upDX $upDY")
            view.animate()
                .x(abs(originalX))
                .y(abs(originalY))
                .setDuration(100)
                .start()
            if (upDX > 150)
                snoozeReminder()
            else if (upDX < -150)
                cancelReminder()



            true // Consumed

        } else {
            super.onTouchEvent(motionEvent)
        }
    }

    private fun getRelativeLeft(myView: View): Int {
        return if (myView.parent === myView.rootView) myView.left else myView.left + getRelativeLeft(
            myView.parent as View
        )
    }

    private fun getRelativeTop(myView: View): Int {
        return if (myView.parent === myView.rootView) myView.top else myView.top + getRelativeTop(
            myView.parent as View
        )
    }

    private fun deleteKey() {
        LocalKeyStorage(this).deleteValue(LocalKeyStorage.SNOOZE)
        LocalKeyStorage(this).deleteValue(LocalKeyStorage.REMINDER)
        LocalKeyStorage(this).deleteValue(LocalKeyStorage.ID)
    }

}