package udemy.course.drawingapp

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var currentColorButtonSelected: ImageButton? = null

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
}