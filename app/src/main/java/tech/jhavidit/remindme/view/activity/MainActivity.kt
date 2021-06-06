package tech.jhavidit.remindme.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityMainBinding
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.view.fragments.LocationReminderFragment
import tech.jhavidit.remindme.view.fragments.LocationReminderFragmentArgs
import tech.jhavidit.remindme.viewModel.MainActivityViewModel
import tech.jhavidit.remindme.viewModel.NotesViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var viewModel : NotesViewModel
    private val activityViewModel: MainActivityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bottomNavigationView = binding.bottomNav
        navController = findNavController(R.id.NavHostFragment)
        if (intent.hasExtra("notes") && intent.hasExtra("location")) {
            val notesModel = intent.getParcelableExtra<NotesModel>("notes")
            val locationModel = intent.getParcelableExtra<LocationModel>("location")
            locationModel?.let { activityViewModel.getLocation(it) }
            notesModel?.let { activityViewModel.getNotes(it) }
           navController.navigate(R.id.locationReminderFragment)
        }
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.icon_notes -> {
                    navController.navigate(R.id.notesFragment)
                    true
                }
                R.id.icon_events -> {
                    navController.navigate(R.id.reminderFragment)
                    true
                }
                R.id.icon_settings -> {
                    navController.navigate(R.id.settingsFragment)
                    true
                }
                else -> {
                    false
                }
            }

        }
    }

}