package udemy.course.drawingapp

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setBrushSize(20F)

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
}