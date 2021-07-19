package tech.jhavidit.remindme.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import np.com.susanthapa.curved_bottom_navigation.CbnMenuItem
import np.com.susanthapa.curved_bottom_navigation.CurvedBottomNavigationView
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityMainBinding
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.util.LocalKeyStorage
import tech.jhavidit.remindme.util.NOTES_LOCATION
import tech.jhavidit.remindme.util.NOTES_TIME
import tech.jhavidit.remindme.viewModel.NotesViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: CurvedBottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        val activeIndex = savedInstanceState?.getInt("activeIndex") ?: 1

        val navController = findNavController(R.id.NavHostFragment)


        val menuItems = arrayOf(

            CbnMenuItem(
                R.drawable.ic_reminder,
                R.drawable.avd_reminder,
                R.id.reminderFragment
            ),

            CbnMenuItem(
                R.drawable.ic_notes,
                R.drawable.avd_notes,
                R.id.notesFragment
            ),

            CbnMenuItem(
                R.drawable.ic_settings,
                R.drawable.avd_settings,
                R.id.settingsFragment
            )
        )

        binding.bottomNav.setMenuItems(menuItems, activeIndex)
        binding.bottomNav.setupWithNavController(navController)

        bottomNavigationView = binding.bottomNav

        if (LocalKeyStorage(this).getValue(LocalKeyStorage.ID) != null) {
            val id = LocalKeyStorage(this).getValue(LocalKeyStorage.ID)?.toInt()
            val reminder = LocalKeyStorage(this).getValue(LocalKeyStorage.REMINDER)
            var snooze = false
            if (LocalKeyStorage(this).getValue(LocalKeyStorage.SNOOZE) == "true")
                snooze = true
            val bundle = bundleOf("id" to id, "reminder" to reminder, "snooze" to snooze)
            val intent = Intent(this, ReminderScreenActivity::class.java)
            if (reminder == "time") {
                intent.putExtra(NOTES_TIME, bundle)
            } else
                intent.putExtra(NOTES_LOCATION, bundle)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        if (intent.hasExtra("notesModel") && intent.hasExtra("location")) {

            val locationModel = intent.getParcelableExtra<LocationModel>("location")
            val notesModel = intent.getParcelableExtra<NotesModel>("notesModel")
            val args = bundleOf("currentNotes" to notesModel, "location" to locationModel)
            intent.removeExtra("notesModel")
            intent.removeExtra("location")
            //    navController.popBackStack(R.id.locationReminderFragment, false)

            navController.navigate(R.id.locationReminderFragment, args)


        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("activeIndex", binding.bottomNav.getSelectedIndex())
        super.onSaveInstanceState(outState)
    }

}