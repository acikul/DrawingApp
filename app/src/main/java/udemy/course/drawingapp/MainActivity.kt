package udemy.course.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var currentColorButtonSelected: ImageButton? = null
    private var customProgressDialog: Dialog? = null

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
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(
                            this@MainActivity,
                            "Read ext storage - granted",
                            Toast.LENGTH_LONG
                        ).show()
                        val pickImageIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(pickImageIntent)
                    } else if (permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        Toast.makeText(
                            this@MainActivity,
                            "Write ext storage - granted",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(
                            this@MainActivity,
                            "Read ext storage - NOT granted",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        Toast.makeText(
                            this@MainActivity,
                            "Write ext storage - NOT granted",
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

        val undoBtn: ImageButton = findViewById(R.id.undo_btn)
        undoBtn.setOnClickListener { drawingView?.undoer() }

        val saveBtn: ImageButton = findViewById(R.id.save_btn)
        saveBtn.setOnClickListener {
            if (isReadExtStorageAllowed()) {
                displayCustomProgressDialog()
                lifecycleScope.launch {
                    val frameLayout: FrameLayout = findViewById(R.id.frame_container)
                    saveBitmapToFile(getBitmapFromView(frameLayout))
                }
            }
        }
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

    private fun displayCustomProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog?.show()
    }

    private fun dismissCustomProgressDialog() {
        customProgressDialog?.dismiss()
        customProgressDialog = null
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

    private fun isReadExtStorageAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            showPermissionRationaleDialog(
                "Drawing App",
                "This app requires permission to read ext storage to load an image from the gallery, and permission to write to ext storage to save drawn image."
            )
        } else {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val retBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(retBitmap)
        val bkg = view.background
        if (bkg != null) {
            bkg.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return retBitmap
    }

    private suspend fun saveBitmapToFile(bitmap: Bitmap?): String {
        var res = ""
        withContext(Dispatchers.IO) {
            if (bitmap != null) {
                try {
                    val bytesOutStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytesOutStream)

                    val file =
                        File(externalCacheDir?.absoluteFile.toString() + File.separator + "DrawingApp_" + System.currentTimeMillis() / 1000 + ".png")
                    val fileOutStream = FileOutputStream(file)
                    fileOutStream.write(bytesOutStream.toByteArray())
                    fileOutStream.close()

                    res = file.absolutePath

                    runOnUiThread {
                        dismissCustomProgressDialog()
                        if (res.isNotEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "Image saved: $res",
                                Toast.LENGTH_LONG
                            ).show()
                            shareImage(res)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Something went wrong while saving the image",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    res = ""
                    e.printStackTrace()
                }
            }
        }
        return res
    }

    private fun shareImage(res: String) {
        MediaScannerConnection.scanFile(this, arrayOf(res), null) { path, uri ->
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"
            startActivity(Intent.createChooser(shareIntent, "Share drawn image"))
        }
    }

}