package com.devcode.colordetection.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private  var paint: Paint = Paint()
    private var touch_x = 0f
    private var touch_y = 0f
    private var isTouched = false

    init {
        paint.color = -0x11282829
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // TODO Auto-generated method stub
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        Log.d("parentWidth", "${parentWidth.toString()} x ${parentHeight.toString()}")
        setMeasuredDimension(parentWidth, parentHeight)
        touch_x = (parentWidth / 2).toFloat()
        touch_y = (parentHeight / 2).toFloat()
        Log.d("touch", "${touch_x.toString()} x ${touch_y.toString()}")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    public override fun onDraw(canvas: Canvas) {
        val lineLength = 30f
        val offset = 80f
        canvas.drawLine(
            touch_x - offset, touch_y - offset, touch_x - offset + lineLength,
            touch_y - offset, paint
        )
        canvas.drawLine(
            touch_x + offset - lineLength, touch_y - offset, touch_x + offset,
            touch_y - offset, paint
        )
        canvas.drawLine(
            touch_x - offset, touch_y - offset, touch_x - offset,
            touch_y - offset + lineLength, paint
        )
        canvas.drawLine(
            touch_x + offset, touch_y - offset, touch_x + offset,
            touch_y - offset + lineLength, paint
        )
        canvas.drawLine(
            touch_x - offset, touch_y + offset - lineLength, touch_x - offset,
            touch_y + offset, paint
        )
        canvas.drawLine(
            touch_x - offset, touch_y + offset, touch_x - offset + lineLength,
            touch_y + offset, paint
        )
        canvas.drawLine(
            touch_x + offset - lineLength, touch_y + offset, touch_x + offset,
            touch_y + offset, paint
        )
        canvas.drawLine(
            touch_x + offset, touch_y + offset - lineLength, touch_x + offset,
            touch_y + offset, paint
        )
        canvas.drawCircle(touch_x, touch_y, 20f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // TODO Auto-generated method stub
        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> isTouched = true
            MotionEvent.ACTION_UP -> isTouched = false
        }
        return true
    }
}