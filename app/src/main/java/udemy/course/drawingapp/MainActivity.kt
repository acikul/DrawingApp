package udemy.course.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var currentColorButtonSelected: ImageButton? = null

    val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageBkg: ImageView = findViewById(R.id.bkg_image)
                imageBkg.setImageURI(result.data?.data)
            }
        }

    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val permissionGranted = it.value

                if (permissionGranted) {
                    Toast.makeText(
                        this@MainActivity,
                        "Read ext storage - granted",
                        Toast.LENGTH_LONG
                    ).show()
                    val pickImageIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickImageIntent)
                } else {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(
                            this@MainActivity,
                            "Read ext storage - NOT granted",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setBrushSize(20F)

        val colorsRow: LinearLayout = findViewById(R.id.colors_row)
        currentColorButtonSelected = colorsRow[0] as ImageButton
        currentColorButtonSelected?.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallete_selected
            )
        )

        val brushSizeBtn: ImageButton = findViewById(R.id.brush_size_btn)
        brushSizeBtn.setOnClickListener { brushSizeDialog() }

        val galleryBtn: ImageButton = findViewById(R.id.gallery_btn)
        galleryBtn.setOnClickListener { requestStoragePermission() }
    }

    private fun brushSizeDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_select_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn: ImageButton? = brushDialog.findViewById(R.id.img_btn_brush_small)
        smallBtn?.setOnClickListener {
            drawingView?.setBrushSize(10F)
            brushDialog.dismiss()
        }
        val mediumBtn: ImageButton? = brushDialog.findViewById(R.id.img_btn_brush_medium)
        mediumBtn?.setOnClickListener {
            drawingView?.setBrushSize(20F)
            brushDialog.dismiss()
        }
        val largeBtn: ImageButton? = brushDialog.findViewById(R.id.img_btn_brush_large)
        largeBtn?.setOnClickListener {
            drawingView?.setBrushSize(30F)
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    fun colorSelected(view: View) {
        if (currentColorButtonSelected !== view) {
            val colorButton = view as ImageButton
            val colorTag = colorButton.tag.toString()
            drawingView?.setBrushColor(colorTag)

            colorButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallete_selected
                )
            )
            currentColorButtonSelected?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallete_normal
                )
            )
            currentColorButtonSelected = view
        }
    }

    private fun customProgressDialog() {
        val customProgressDialog = Dialog(this)

        customProgressDialog.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog.show()
    }

    private fun showPermissionRationaleDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            showPermissionRationaleDialog(
                "Kids Drawing App",
                "This app requires permission to read ext storage to load an image from the gallery"
            )
        } else {
            requestPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }
}