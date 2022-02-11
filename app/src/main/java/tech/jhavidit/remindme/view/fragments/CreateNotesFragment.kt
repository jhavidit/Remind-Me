package tech.jhavidit.remindme.view.fragments

import android.R.attr
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
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
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import np.com.susanthapa.curved_bottom_navigation.CurvedBottomNavigationView
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentCreateNotesBinding
import tech.jhavidit.remindme.model.ColorModel
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.receiver.GeoFencingReceiver
import tech.jhavidit.remindme.util.*
import tech.jhavidit.remindme.view.adapters.SelectBackgroundColorAdapter
import tech.jhavidit.remindme.viewModel.NotesViewModel
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import android.R.attr.bitmap
import android.content.Context
import android.graphics.Matrix
import androidx.work.*
import com.airbnb.lottie.utils.Utils
import okio.ByteString.decodeBase64
import android.R.attr.bitmap
import android.media.ExifInterface

import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage


class CreateNotesFragment : Fragment(), SelectBackgroundColorAdapter.AdapterInterface {

    private lateinit var binding: FragmentCreateNotesBinding
    private lateinit var alarmReceiver: AlarmReceiver
    private lateinit var geoFencingReceiver: GeoFencingReceiver
    private lateinit var notesViewModel: NotesViewModel
    private lateinit var notes: NotesModel
    private lateinit var navController: NavController
    private var cameraImagePath: String? = null
    private var isPinned = false
    private lateinit var localKeyStorage: LocalKeyStorage
    private val args: CreateNotesFragmentArgs by navArgs()
    private var updated = false
    private var notesId = 0
    private var latestColor: String = "#FFFFFF"
    private var latestImage: String? = null
    var path: String = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.note.setBackgroundColor(Color.parseColor(args.currentNotes.backgroundColor))
        binding.notesImageCard.setBackgroundColor(Color.parseColor(args.currentNotes.backgroundColor))
        alarmReceiver = AlarmReceiver()
        geoFencingReceiver = GeoFencingReceiver()
        localKeyStorage = LocalKeyStorage(requireContext())
        notes = args.currentNotes
        notesId = args.currentNotes.id
        latestColor = notes.backgroundColor
        latestImage = notes.image
        val cameraPathLocal = localKeyStorage.getValue(LocalKeyStorage.CAMERA_IMAGE_PATH)
        val cameraPathIdLocal = localKeyStorage.getValue(LocalKeyStorage.CAMERA_IMAGE_ID)
        val imageSaved = localKeyStorage.getValue(LocalKeyStorage.IMAGE_SAVED)
        if (cameraPathIdLocal != null && cameraPathLocal != null && imageSaved != null && Integer.parseInt(
                cameraPathIdLocal
            ) == notesId
        ) {
            val bitmap = loadImageFromStorage(cameraPathLocal, notesId)
            val bit = bitmap?.let { cameraImagePath?.let { it1 -> checkingOrientation(it, it1) } }
            Glide.with(requireContext())
                .load(bit)
                .into(binding.image)
            val notes = NotesModel(
                id = notesId,
                title = args.currentNotes.title,
                description = args.currentNotes.description,
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
                image = cameraPathLocal
            )
            latestImage = cameraPathLocal

            notesViewModel.updateNotes(notes)
            localKeyStorage.deleteValue(LocalKeyStorage.CAMERA_IMAGE_PATH)
            localKeyStorage.deleteValue(LocalKeyStorage.CAMERA_IMAGE_ID)
            localKeyStorage.deleteValue(LocalKeyStorage.IMAGE_SAVED)
            localKeyStorage.deleteValue(LocalKeyStorage.CAMERA_PATH)


        }


        args.currentNotes.image?.let {

            if (checkStoragePermission(requireContext())) {
                binding.notesImageCard.visibility = VISIBLE
                val bitmap = loadImageFromStorage(it, notesId)
                Glide.with(requireContext())
                    .load(bitmap)
                    .into(binding.image)
            } else {
                binding.notesImageCard.visibility = GONE
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Need Storage Permission to see image",
                    Snackbar.LENGTH_LONG
                ).setAction("Enable", View.OnClickListener {
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri =
                        Uri.fromParts("package", activity?.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }).show()
            }
        } ?: run {
            binding.notesImageCard.visibility = GONE
        }

        val lastUpdated = args.currentNotes.lastUpdated?.toDateFormat()?.let { getPeriod(it) }

        lastUpdated?.let {
            binding.lastUpdated.text = String.format("%s", "Last edit : $lastUpdated")
        }
            ?: run {
                binding.lastUpdated.text = String.format("%s", "Last edit : recently")
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

        if (args.currentNotes.repeatValue == -1L && (System.currentTimeMillis() > args.currentNotes.reminderWaitTime!!)) {
            Snackbar.make(
                binding.coordinatorLayout,
                "The reminder time is already passed. Do you want to delete this time reminder?",
                Snackbar.LENGTH_SHORT
            ).setAction("Delete", View.OnClickListener {
                val notesModel = NotesModel(
                    id = notesId,
                    title = binding.title.text.toString(),
                    description = binding.description.text.toString(),
                    locationReminder = args.currentNotes.locationReminder,
                    timeReminder = null,
                    reminderTime = null,
                    reminderWaitTime = null,
                    reminderDate = null,
                    latitude = args.currentNotes.latitude,
                    isPinned = isPinned,
                    lastUpdated = args.currentNotes.lastUpdated,
                    longitude = args.currentNotes.longitude,
                    radius = args.currentNotes.radius,
                    repeatValue = null,
                    locationName = args.currentNotes.locationName,
                    backgroundColor = args.currentNotes.backgroundColor,
                    image = args.currentNotes.image
                )
                notesViewModel.updateNotes(notesModel)
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Missed Time Reminder Removed",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            ).show()
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
                        lastUpdated = args.currentNotes.lastUpdated,
                        longitude = args.currentNotes.longitude,
                        radius = args.currentNotes.radius,
                        repeatValue = args.currentNotes.repeatValue,
                        locationName = args.currentNotes.locationName,
                        backgroundColor = latestColor,
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
                lastUpdated = args.currentNotes.lastUpdated,
                repeatValue = args.currentNotes.repeatValue,
                locationName = args.currentNotes.locationName,
                image = latestImage,
                backgroundColor = latestColor
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
                lastUpdated = args.currentNotes.lastUpdated,
                radius = args.currentNotes.radius,
                repeatValue = args.currentNotes.repeatValue,
                locationName = args.currentNotes.locationName,
                image = latestImage,
                backgroundColor = latestColor
            )
            navController.navigate(CreateNotesFragmentDirections.addReminder(notes))
        }

        binding.title.addTextChangedListener(textWatcher)
        binding.description.addTextChangedListener(textWatcher)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateNotesBinding.inflate(inflater, container, false)
        navController = Navigation.findNavController(requireActivity(), R.id.NavHostFragment)
        notesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)


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
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                val galleryIntent = Intent()
                galleryIntent.type = "image/*"
                galleryIntent.action = Intent.ACTION_OPEN_DOCUMENT
                startActivityForResult(
                    Intent.createChooser(galleryIntent, "Select an image"),
                    PICK_IMAGE
                )
            } else {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
        cameraCard?.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                dispatchTakePictureIntent()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }

        bottomSheetDialogs.show()
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun createImageFileFromCamera(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            absolutePath.also { cameraImagePath = it }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFileFromCamera()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "tech.jhavidit.remindme.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                    startActivityForResult(takePictureIntent, PICK_IMAGE_FROM_CAMERA)
                }
            }
        }
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
        }
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val galleryIntent = Intent()
                galleryIntent.type = "image/*"
                galleryIntent.action = Intent.ACTION_OPEN_DOCUMENT
                startActivityForResult(
                    Intent.createChooser(galleryIntent, "Select an image"),
                    PICK_IMAGE
                )

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            binding.notesImageCard.visibility = VISIBLE
            val pickedImage: Uri? = data?.data
            var path: String? = null
            pickedImage?.let {
                val bitmap = uriToBitmap(requireContext(), pickedImage)
                bitmap?.let { bit ->
                    path = saveToInternalStorage(bit, notesId, requireContext())
                    Glide.with(requireContext())
                        .load(bitmap)
                        .into(binding.image)
                }
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
                longitude = args.currentNotes.longitude,
                isPinned = args.currentNotes.isPinned,
                radius = args.currentNotes.radius,
                repeatValue = args.currentNotes.repeatValue,
                locationName = args.currentNotes.locationName,
                backgroundColor = latestColor,
                lastUpdated = args.currentNotes.lastUpdated,
                image = path
            )
            latestImage = path
            notesViewModel.updateNotes(notes)


        } else if (requestCode == PICK_IMAGE_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            binding.notesImageCard.visibility = VISIBLE


            val dir = File(cameraImagePath)
            val path = dir.path
            val bitmap = BitmapFactory.decodeFile(path)

            val bit = cameraImagePath?.let { checkingOrientation(bitmap, it) }

            val localKeyStorage = LocalKeyStorage(requireContext())

            localKeyStorage.saveValue(LocalKeyStorage.CAMERA_IMAGE_ID, notesId.toString())
            localKeyStorage.saveValue(LocalKeyStorage.CAMERA_IMAGE_PATH, path)
            cameraImagePath?.let {
                localKeyStorage.saveValue(
                    LocalKeyStorage.CAMERA_PATH,
                    it
                )
            }
//            val bitmap: Bitmap =
//            val file = File(cameraImagePath)
//            val fOut = FileOutputStream(dir)
//            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
//            fOut.flush()
//            fOut.close()

            //                // Get the dimensions of the bitmap
//                inJustDecodeBounds = true
//
//                val photoW: Int = outWidth
//                val photoH: Int = outHeight
//
//                // Determine how much to scale down the image
//                val scaleFactor: Int = max(1, min(photoW, photoH))
//
//                // Decode the image file into a Bitmap sized to fill the View
//                inJustDecodeBounds = false
//                inSampleSize = scaleFactor
//                inPurgeable = true
//            }
//            val bitmap: Bitmap = BitmapFactory.decodeFile(cameraImagePath, bmOptions)
//

            Glide.with(requireContext())
                .load(bit)
                .into(binding.image)

            val request = OneTimeWorkRequestBuilder<UploadWorker>()
                .build()

            WorkManager.getInstance(requireContext())
                .enqueue(request)

        } else {
            binding.notesImageCard.visibility = GONE
        }
    }

    private fun checkingOrientation(bitmap: Bitmap, photoPath: String): Bitmap? {
        val ei = ExifInterface(photoPath)
        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val rotatedBitmap: Bitmap? = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> this.rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> this.rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> this.rotateImage(bitmap, 270f)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> bitmap
        }
        return rotatedBitmap
    }

    private fun rotateImage(bitmap: Bitmap, angle: Float): Bitmap {
        val matrix: Matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height,
            matrix, true
        );
    }


    private fun showColorBottomSheet() {
        val bottomSheetDialogs = BottomSheetDialog(requireContext())
        bottomSheetDialogs.setContentView(R.layout.bottom_sheet_add_color)
        val recyclerView =
            bottomSheetDialogs.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.color_recycler_view)
        val closeButton = bottomSheetDialogs.findViewById<ImageView>(R.id.close_btn_color)
        val adapter =
            SelectBackgroundColorAdapter(this, ColorModel.getColorIndex(notes.backgroundColor))
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
            backgroundColor = latestColor,
            lastUpdated = formattedDate,
            image = latestImage
        )
        notesViewModel.updateNotes(notesModel)
    }

    @SuppressLint("SimpleDateFormat")
    private fun insertDataToDatabase() {
        val title = binding.title.text.toString()
        val description = binding.description.text.toString()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val formattedDate: String = dateFormat.format(calendar.time)
        val notesModel = NotesModel(
            id = 0,
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
            backgroundColor = latestColor,
            lastUpdated = formattedDate,
            image = latestImage
        )
        notesViewModel.addNotes(notesModel)
        updated = true
        notesViewModel.createdId.observe(viewLifecycleOwner, {
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
            image = latestImage,
            backgroundColor = color,
            lastUpdated = args.currentNotes.lastUpdated
        )
        latestColor = color
        notesViewModel.updateNotes(notes)
    }

    private fun galleryAddPick() {
        cameraImagePath?.let {
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val f = File(it)
                mediaScanIntent.data = Uri.fromFile(f)
                requireActivity().sendBroadcast(mediaScanIntent)
            }
        }
    }

    fun uploadImage(context: Context) {
        val localKeyStorage = LocalKeyStorage(context)
        val str = localKeyStorage.getValue(LocalKeyStorage.CAMERA_IMAGE_PATH)
        val notesId = Integer.parseInt(localKeyStorage.getValue(LocalKeyStorage.CAMERA_IMAGE_ID)!!)
        val cameraImagePath = localKeyStorage.getValue(LocalKeyStorage.CAMERA_PATH)
        str?.let {
            var bitmap = BitmapFactory.decodeFile(str)
            bitmap = cameraImagePath?.let { it1 -> checkingOrientation(bitmap, it1) }
            log(bitmap.toString() + " " + cameraImagePath.toString())
            val path = saveToInternalStorage(bitmap, notesId, context)
            path?.let {
                localKeyStorage.saveValue(LocalKeyStorage.CAMERA_IMAGE_PATH, it)
                localKeyStorage.saveValue(LocalKeyStorage.IMAGE_SAVED, "true")
            }


//            val notes = NotesModel(
//                id = notesId,
//                title = binding.title.text.toString(),
//                description = binding.description.text.toString(),
//                locationReminder = args.currentNotes.locationReminder,
//                timeReminder = args.currentNotes.timeReminder,
//                reminderTime = args.currentNotes.reminderTime,
//                reminderWaitTime = args.currentNotes.reminderWaitTime,
//                reminderDate = args.currentNotes.reminderDate,
//                latitude = args.currentNotes.latitude,
//                longitude = args.currentNotes.longitude,
//                isPinned = args.currentNotes.isPinned,
//                radius = args.currentNotes.radius,
//                repeatValue = args.currentNotes.repeatValue,
//                locationName = args.currentNotes.locationName,
//                backgroundColor = latestColor,
//                lastUpdated = args.currentNotes.lastUpdated,
//                image = path
//            )
//            latestImage = path
//            notesViewModel.updateNotes(notes)
//            localKeyStorage.deleteValue(LocalKeyStorage.CAMERA_IMAGE_PATH)
//            localKeyStorage.deleteValue(LocalKeyStorage.CAMERA_IMAGE_ID)
        }
    }
}


class UploadWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val obj = CreateNotesFragment()
        obj.uploadImage(context)
        log("chalega kya yeh")
        //toast(context = context,"Kya chutiye developer ne app banai hai")
        return Result.success()
    }
}