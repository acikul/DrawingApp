package udemy.course.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attributes: AttributeSet) : View(context, attributes) {
    private var drawPath: CustomPath? = null // instance of CustomPath inner class for further use
    private var canvasBitmap: Bitmap? = null // bitmap instance
    private var drawPaint: Paint? = null     // style and color information for drawing
    private var canvasPaint: Paint? = null   // instance of canvas paint view
    private var brushSize: Float = 0.toFloat()
    private var colorGlobal = Color.BLACK
    private var canvas: Canvas? = null
    private val paths = ArrayList<CustomPath>()
    private val undoPaths = ArrayList<CustomPath>()

    init {
        setup()
    }

    private fun setup() {
        drawPaint = Paint()
        drawPath = CustomPath(colorGlobal, brushSize)
        drawPaint?.color = colorGlobal
        drawPaint?.style = Paint.Style.STROKE
        drawPaint?.strokeJoin = Paint.Join.ROUND
        drawPaint?.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)

        for (path in paths) {
            drawPaint!!.strokeWidth = path.brushSize
            drawPaint!!.color = path.color
            canvas?.drawPath(path, drawPaint!!)
        }

        if (!drawPath!!.isEmpty) {
            drawPaint!!.strokeWidth = drawPath!!.brushSize
            drawPaint!!.color = drawPath!!.color
            canvas?.drawPath(drawPath!!, drawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath!!.color = colorGlobal
                drawPath!!.brushSize = brushSize

                drawPath!!.reset()
                drawPath!!.moveTo(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE -> {
                drawPath!!.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                paths.add(drawPath!!)
                drawPath = CustomPath(colorGlobal, brushSize)
            }
            else -> return false
        }
        invalidate()

        return true
    }

    fun setBrushSize(newSize: Float) {
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        drawPaint!!.strokeWidth = brushSize
    }

    fun setBrushColor(colorParam: String) {
        colorGlobal = Color.parseColor(colorParam)
        drawPaint!!.color = colorGlobal
    }

    fun undoer() {
        if (paths.isNotEmpty()) {
            undoPaths.add(paths.removeLast())
            invalidate()
        }
    }

    internal inner class CustomPath(var color: Int, var brushSize: Float) : Path() {

    }
}