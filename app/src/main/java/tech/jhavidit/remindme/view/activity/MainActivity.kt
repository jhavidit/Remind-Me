package tech.jhavidit.remindme.view.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityMainBinding
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.viewModel.MainActivityViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private val activityViewModel: MainActivityViewModel by viewModels()
    private val LOCATION_REQUEST_CODE = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bottomNavigationView = binding.bottomNav
        navController = findNavController(R.id.NavHostFragment)
        // activityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
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
                    navController.navigate(R.id.eventsFragment)
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