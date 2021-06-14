package tech.jhavidit.remindme.view.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_create_notes.*
import np.com.susanthapa.curved_bottom_navigation.CurvedBottomNavigationView
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.BottomSheetAddColorBinding
import tech.jhavidit.remindme.databinding.FragmentCreateNotesBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.receiver.GeoFencingReceiver
import tech.jhavidit.remindme.util.*
import tech.jhavidit.remindme.view.adapters.SelectBackgroundColorAdapter
import tech.jhavidit.remindme.viewModel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log


class CreateNotesFragment : Fragment(), SelectBackgroundColorAdapter.AdapterInterface {

    private lateinit var binding: FragmentCreateNotesBinding
    private lateinit var bind: BottomSheetAddColorBinding
    private lateinit var alarmReceiver: AlarmReceiver
    private lateinit var geoFencingReceiver: GeoFencingReceiver
    private lateinit var notesViewModel: NotesViewModel
    private lateinit var notes: NotesModel
    private lateinit var navController: NavController
    private var isPinned = false
    private val args: CreateNotesFragmentArgs by navArgs()
    private var updated = false
    private var notesId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateNotesBinding.inflate(inflater, container, false)
        bind = BottomSheetAddColorBinding.inflate(inflater, container, false)
        navController = Navigation.findNavController(requireActivity(), R.id.NavHostFragment)
        notesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        args.currentNotes.image?.let {

            if (checkStoragePermission(requireContext())) {
                binding.notesImageCard.visibility = VISIBLE
                binding.image.setImageURI(stringToUri(it))
            } else {
                binding.notesImageCard.visibility = GONE
                Snackbar.make(
                    binding.note,
                    "Nedd Storage Permission to see image",
                    Snackbar.LENGTH_LONG
                ).setAction("Enable", View.OnClickListener {
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri =
                        Uri.fromParts("package", activity?.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                })
            }
        } ?: run {
            binding.notesImageCard.visibility = GONE
        }
        binding.note.setBackgroundColor(Color.parseColor(args.currentNotes.backgroundColor))
        binding.notesImageCard.setBackgroundColor(Color.parseColor(args.currentNotes.backgroundColor))
        alarmReceiver = AlarmReceiver()
        geoFencingReceiver = GeoFencingReceiver()
        notes = args.currentNotes
        notesId = args.currentNotes.id
        val lastUpdated = args.currentNotes.lastUpdated?.toDateFormat()?.let { getPeriod(it) }

        lastUpdated?.let { binding.lastUpdated.text = "Last edit : ${lastUpdated}" }
            ?: run {
                binding.lastUpdated.text = "Last edit : recently"
            }

        if (notes.isPinned) {
            isPinned = true
            binding.pinBtn.setImageResource(R.drawable.ic_pin)
        } else {
            isPinned = false
            binding.pinBtn.setImageResource(R.drawable.ic_unpin)
        }

        val bottomNavigation: CurvedBottomNavigationView? = activity?.findViewById(R.id.bottom_nav)
        bottomNavigation?.visibility = GONE
        if (args.update == "update") {
            updated = true
        }
        if (updated) {
            binding.title.setText(notes.title)
            binding.description.setText(notes.description)
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.deleteImage.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Are you sure want to delete this image?")
                .setPositiveButton(
                    "Delete"
                ) { _, _ ->

                    val notesModel = NotesModel(
                        id = notesId,
                        title = binding.title.text.toString(),
                        description = binding.description.text.toString(),
                        locationReminder = args.currentNotes.locationReminder,
                        timeReminder = args.currentNotes.timeReminder,
                        reminderTime = args.currentNotes.reminderTime,
                        reminderWaitTime = args.currentNotes.reminderWaitTime,
                        reminderDate = args.currentNotes.reminderDate,
                        latitude = args.currentNotes.latitude,
                        isPinned = isPinned,
                        longitude = args.currentNotes.longitude,
                        radius = args.currentNotes.radius,
                        repeatValue = args.currentNotes.repeatValue,
                        locationName = args.currentNotes.locationName,
                        backgroundColor = args.currentNotes.backgroundColor,
                        image = null
                    )

                    notesViewModel.updateNotes(notesModel)
                    binding.notesImageCard.visibility = GONE
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()
        }

        binding.pinBtn.setOnClickListener {
            if (isPinned) {
                isPinned = false
                binding.pinBtn.setImageResource(R.drawable.ic_unpin)
            } else {
                isPinned = true
                binding.pinBtn.setImageResource(R.drawable.ic_pin)
            }
            val notes = NotesModel(
                id = notesId,
                title = binding.title.text.toString(),
                description = binding.description.text.toString(),
                locationReminder = args.currentNotes.locationReminder,
                timeReminder = args.currentNotes.timeReminder,
                reminderTime = args.currentNotes.reminderTime,
                reminderWaitTime = args.currentNotes.reminderWaitTime,
                reminderDate = args.currentNotes.reminderDate,
                latitude = args.currentNotes.latitude,
                isPinned = isPinned,
                longitude = args.currentNotes.longitude,
                radius = args.currentNotes.radius,
                repeatValue = args.currentNotes.repeatValue,
                locationName = args.currentNotes.locationName,
                image = args.currentNotes.image,
                backgroundColor = args.currentNotes.backgroundColor
            )
            notesViewModel.updateNotes(notes)
        }

        binding.deleteBtn.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Are you sure want to delete this note?")
                .setPositiveButton(
                    "Delete"
                ) { _, _ ->
                    notes.timeReminder?.let {
                        alarmReceiver.cancelAlarm(requireContext(), notesId)
                    }
                    notes.locationReminder?.let {
                        geoFencingReceiver.cancelLocationReminder(requireContext(), notesId)
                    }
                    notesViewModel.deleteNotes(notes)
                    navController.navigateUp()
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()

        }

        binding.shareBtn.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${binding.title.text}\n${binding.description.text}")
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        binding.colorBtn.setOnClickListener {
            if (!updated)
                insertDataToDatabase()
            showColorBottomSheet()
        }

        binding.uploadImageBtn.setOnClickListener {
            if (!updated)
                insertDataToDatabase()
            addImageBottomSheet()
        }

        binding.reminderBtn.setOnClickListener {
            if (!updated)
                insertDataToDatabase()
            val notes = NotesModel(
                id = notesId,
                title = binding.title.text.toString(),
                description = binding.description.text.toString(),
                locationReminder = args.currentNotes.locationReminder,
                timeReminder = args.currentNotes.timeReminder,
                reminderTime = args.currentNotes.reminderTime,
                reminderWaitTime = args.currentNotes.reminderWaitTime,
                reminderDate = args.currentNotes.reminderDate,
                isPinned = args.currentNotes.isPinned,
                latitude = args.currentNotes.latitude,
                longitude = args.currentNotes.longitude,
                radius = args.currentNotes.radius,
                repeatValue = args.currentNotes.repeatValue,
                locationName = args.currentNotes.locationName,
                image = args.currentNotes.image,
                backgroundColor = args.currentNotes.backgroundColor
            )
            navController.navigate(CreateNotesFragmentDirections.addReminder(notes))
        }

        binding.title.addTextChangedListener(textWatcher)
        binding.description.addTextChangedListener(textWatcher)

        return binding.root
    }

    private fun addImageBottomSheet() {
        val bottomSheetDialogs = BottomSheetDialog(requireContext())
        bottomSheetDialogs.setContentView(R.layout.bottom_sheet_add_image)
        val closeButton = bottomSheetDialogs.findViewById<ImageView>(R.id.close_btn_image)
        val cameraCard = bottomSheetDialogs.findViewById<MaterialCardView>(R.id.camera_card)
        val galleryCard = bottomSheetDialogs.findViewById<MaterialCardView>(R.id.gallery_card)
        closeButton?.setOnClickListener {
            bottomSheetDialogs.dismiss()
        }

        galleryCard?.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, PICK_IMAGE)
            } else {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    CAMERA_PERMISSION_CODE
                )
            }
        }
        cameraCard?.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, PICK_IMAGE_FROM_CAMERA)
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    STORAGE_PERMISSION_CODE
                )
            }
        }



        bottomSheetDialogs.show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, PICK_IMAGE_FROM_CAMERA)
            }
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Camera Permission Required")
                .setMessage("You need to provide camera permission to access this feature. Kindly enable it from settings")
                .setPositiveButton(
                    "Ok"
                ) { _, _ ->
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri =
                        Uri.fromParts("package", activity?.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()
        }
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, PICK_IMAGE)
            }
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Storage Permission Required")
                .setMessage("You need to provide storage permission to access this feature. Kindly enable it from settings")
                .setPositiveButton(
                    "Ok"
                ) { _, _ ->
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri =
                        Uri.fromParts("package", activity?.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            binding.notesImageCard.visibility = View.VISIBLE
            val pickedImage: Uri? = data?.data
            binding.image.setImageURI(pickedImage)
            val notes = NotesModel(
                id = notesId,
                title = binding.title.text.toString(),
                description = binding.description.text.toString(),
                locationReminder = args.currentNotes.locationReminder,
                timeReminder = args.currentNotes.timeReminder,
                reminderTime = args.currentNotes.reminderTime,
                reminderWaitTime = args.currentNotes.reminderWaitTime,
                reminderDate = args.currentNotes.reminderDate,
                latitude = args.currentNotes.latitude,
                longitude = args.currentNotes.longitude,
                isPinned = args.currentNotes.isPinned,
                radius = args.currentNotes.radius,
                repeatValue = args.currentNotes.repeatValue,
                locationName = args.currentNotes.locationName,
                backgroundColor = args.currentNotes.backgroundColor,
                lastUpdated = args.currentNotes.lastUpdated,
                image = pickedImage.toString()
            )
            notesViewModel.updateNotes(notes)


        } else if (requestCode == PICK_IMAGE_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            binding.notesImageCard.visibility = View.VISIBLE
            val bitmap = data?.extras?.get("data") as Bitmap
            val uri = bitmapToUri(requireContext(), bitmap)
            binding.image.setImageURI(uri)
            val notes = NotesModel(
                id = notesId,
                title = binding.title.text.toString(),
                description = binding.description.text.toString(),
                locationReminder = args.currentNotes.locationReminder,
                timeReminder = args.currentNotes.timeReminder,
                reminderTime = args.currentNotes.reminderTime,
                reminderWaitTime = args.currentNotes.reminderWaitTime,
                reminderDate = args.currentNotes.reminderDate,
                latitude = args.currentNotes.latitude,
                longitude = args.currentNotes.longitude,
                isPinned = args.currentNotes.isPinned,
                radius = args.currentNotes.radius,
                repeatValue = args.currentNotes.repeatValue,
                locationName = args.currentNotes.locationName,
                backgroundColor = args.currentNotes.backgroundColor,
                lastUpdated = args.currentNotes.lastUpdated,
                image = uri.toString()
            )
            notesViewModel.updateNotes(notes)


        } else {
            binding.notesImageCard.visibility = GONE
        }
    }


    private fun showColorBottomSheet() {
        val bottomSheetDialogs = BottomSheetDialog(requireContext())
        bottomSheetDialogs.setContentView(R.layout.bottom_sheet_add_color)
        val recyclerView =
            bottomSheetDialogs.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.color_recycler_view)
        val closeButton = bottomSheetDialogs.findViewById<ImageView>(R.id.close_btn_color)

        val adapter = SelectBackgroundColorAdapter(this)
        recyclerView?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView?.adapter = adapter

        closeButton?.setOnClickListener {
            bottomSheetDialogs.dismiss()
        }

        bottomSheetDialogs.show()

    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (updated)
                updateData()
            else {
                insertDataToDatabase()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateData() {
        val title = binding.title.text.toString()
        val description = binding.description.text.toString()

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val formattedDate: String = dateFormat.format(calendar.time)

        val notesModel = NotesModel(
            id = notesId,
            title = title,
            description = description,
            locationReminder = args.currentNotes.locationReminder,
            timeReminder = args.currentNotes.timeReminder,
            reminderTime = args.currentNotes.reminderTime,
            reminderWaitTime = args.currentNotes.reminderWaitTime,
            reminderDate = args.currentNotes.reminderDate,
            latitude = args.currentNotes.latitude,
            longitude = args.currentNotes.longitude,
            isPinned = args.currentNotes.isPinned,
            radius = args.currentNotes.radius,
            repeatValue = args.currentNotes.repeatValue,
            locationName = args.currentNotes.locationName,
            backgroundColor = args.currentNotes.backgroundColor,
            lastUpdated = formattedDate,
            image = args.currentNotes.image
        )
        notesViewModel.updateNotes(notesModel)
    }

    private fun insertDataToDatabase() {
        val title = binding.title.text.toString()
        val description = binding.description.text.toString()
        val notesModel = NotesModel(0, title, description)
        notesViewModel.addNotes(notesModel)
        updated = true
        notesViewModel.createdId.observe(viewLifecycleOwner, Observer {
            notesId = it ?: 0
        })
    }

    override fun clickListener(color: String) {
        changeBackground(color)
    }

    private fun changeBackground(color: String) {
        binding.note.setBackgroundColor(Color.parseColor(color))
        binding.notesImageCard.setBackgroundColor(Color.parseColor(color))
        val notes = NotesModel(
            id = notesId,
            title = binding.title.text.toString(),
            description = binding.description.text.toString(),
            locationReminder = args.currentNotes.locationReminder,
            timeReminder = args.currentNotes.timeReminder,
            reminderTime = args.currentNotes.reminderTime,
            reminderWaitTime = args.currentNotes.reminderWaitTime,
            reminderDate = args.currentNotes.reminderDate,
            latitude = args.currentNotes.latitude,
            longitude = args.currentNotes.longitude,
            isPinned = args.currentNotes.isPinned,
            radius = args.currentNotes.radius,
            repeatValue = args.currentNotes.repeatValue,
            locationName = args.currentNotes.locationName,
            image = args.currentNotes.image,
            backgroundColor = color,
            lastUpdated = args.currentNotes.lastUpdated
        )
        notesViewModel.updateNotes(notes)
    }


}