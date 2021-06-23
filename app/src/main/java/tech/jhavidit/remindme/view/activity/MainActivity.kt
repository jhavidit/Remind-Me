package tech.jhavidit.remindme.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import np.com.susanthapa.curved_bottom_navigation.CbnMenuItem
import np.com.susanthapa.curved_bottom_navigation.CurvedBottomNavigationView
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityMainBinding
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.viewModel.NotesViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: CurvedBottomNavigationView
    private lateinit var viewModel: NotesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)

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

        if (intent.hasExtra("id") && intent.hasExtra("location")) {
            val id = intent.getIntExtra("id", -1)
            if (id != -1) {
                viewModel.selectedNote(id).observe(this, Observer {
                    val notesModel = it.firstOrNull()
                    val locationModel = intent.getParcelableExtra<LocationModel>("location")
                    val args = bundleOf("currentNotes" to notesModel, "location" to locationModel)
                    navController.navigate(R.id.locationReminderFragment, args)
                })
            }


        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("activeIndex", binding.bottomNav.getSelectedIndex())
        super.onSaveInstanceState(outState)
    }

}