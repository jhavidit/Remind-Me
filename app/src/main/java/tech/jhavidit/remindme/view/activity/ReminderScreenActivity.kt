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
import tech.jhavidit.remindme.util.NOTES_LOCATION
import tech.jhavidit.remindme.util.NOTES_TIME
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.util.toast
import tech.jhavidit.remindme.viewModel.NotesViewModel
import kotlin.math.abs


class ReminderScreenActivity : AppCompatActivity(), View.OnTouchListener {
    private lateinit var binding: ActivityTimeReminderBinding
    private lateinit var viewModel: NotesViewModel
    private lateinit var alarmReceiver: AlarmReceiver
    private lateinit var geoFencingReceiver: GeoFencingReceiver
    private var id: Int = 0
    private var snooze: Boolean = false
    private var reminder: String? = ""
    private var originalX = 50F
    private var originalY = 50F
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
        val snoozeAlarm = intent?.getBooleanExtra("snooze", false)
        val dismissAlarm = intent?.getBooleanExtra("dismiss", false)
        originalX = getRelativeLeft(binding.reminderFab).toFloat()
        originalY = getRelativeTop(binding.reminderFab).toFloat()

        binding.reminderFab.setOnTouchListener(this)
        notesTimeBundle?.let {
            id = notesTimeBundle.getInt("id")
            viewModel.selectedNote(id).observe(this, Observer {
                binding.reminderLocationTime.text =
                    it[0].reminderTime
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

        binding.openApp.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (snoozeAlarm == true) {
            snoozeReminder()
        }

        if (dismissAlarm == true) {
            cancelReminder()
        }

        notesTimeBundle?.clear()
        notesLocationBundle?.clear()

    }

    private fun cancelReminder() {
        if (!snooze && reminder == "location") {
            viewModel.selectedNote(id).observe(this, Observer { note ->
                val notes = note[0]
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
                finish()
            })


        } else if (reminder == "time") {
            viewModel.selectedNote(id).observe(this, Observer { note ->
                val notes = note[0]
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
                        locationName = notes.locationName,
                        backgroundColor = notes.backgroundColor
                    )
                    viewModel.updateNotes(notesModel)
                }
                val intentService = Intent(applicationContext, AlarmService::class.java)
                applicationContext.stopService(intentService)
                finish()

            })


        } else if (snooze && reminder == "location") {
            viewModel.selectedNote(id).observe(this, Observer { note ->
                val notes = note[0]
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
                    radius = null,
                    repeatValue = notes.repeatValue,
                    locationName = null,
                    backgroundColor = notes.backgroundColor
                )
                viewModel.updateNotes(notesModel)
                val intentService =
                    Intent(applicationContext, AlarmService::class.java)
                applicationContext.stopService(intentService)
                finish()
            })
        }
    }

    private fun snoozeReminder() {
        viewModel.selectedNote(id).observe(this, Observer { notes ->
            notes?.let {
                val note = it[0]
                reminder?.let { remind ->
                    alarmReceiver.scheduleSnoozeAlarm(this, note, remind)
                    toast(this, "Alarm snoozed for five minutes")
                    if (remind == "time") {
                        val intentService = Intent(applicationContext, AlarmService::class.java)
                        applicationContext.stopService(intentService)
                        finish()
                    } else if (remind == "location") {
                        val intentService =
                            Intent(applicationContext, LocationReminderService::class.java)
                        applicationContext.stopService(intentService)
                        finish()
                    }
                }
            }
        })

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
            newX = Math.max(
                layoutParams.leftMargin.toFloat(),
                newX
            ) // Don't allow the FAB past the left hand side of the parent
            newX = Math.min(
                (parentWidth - viewWidth - layoutParams.rightMargin).toFloat(),
                newX
            ) // Don't allow the FAB past the right hand side of the parent
            var newY = motionEvent.rawY + dY
            newY = Math.max(
                layoutParams.topMargin.toFloat(),
                newY
            ) // Don't allow the FAB past the top of the parent
            newY = Math.min(
                (parentHeight - viewHeight - layoutParams.bottomMargin).toFloat(),
                newY
            ) // Don't allow the FAB past the bottom of the parent
            view.animate()
                .x(newX)
                .y(newY)
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

    companion object {
        const val CLICK_DRAG_TOLERANCE =
            100f // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.
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

}