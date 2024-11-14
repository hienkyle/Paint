package edu.tcu.hienminhdau.paint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val pathList = mutableListOf<CustomPath>()
    private var path = CustomPath(Color.BLACK, 10 * resources.displayMetrics.density)

    init{
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                pathList.add(path)
                path.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                path = CustomPath(path.color, path.width)
            }
        }

        return true
    }

    // define setPathColor(color: Int)
    fun setPathColor(color: Int){
        path = CustomPath(color, path.width)
    }

    // define setPathWidth(width: Int)
    fun setPathWidth(width: Int){
        path = CustomPath(path.color, width * resources.displayMetrics.density)
    }

    // define undoPath()
    fun undoPath(){
        if(pathList.isNotEmpty()){
            pathList.removeAt(pathList.lastIndex)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        for(path in pathList){
            paint.color = path.color
            paint.strokeWidth = path.width
            canvas.drawPath(path, paint)
        }
    }

    private data class CustomPath(val color: Int, val width: Float) : Path()
}