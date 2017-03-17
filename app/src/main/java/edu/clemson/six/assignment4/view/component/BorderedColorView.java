package edu.clemson.six.assignment4.view.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import edu.clemson.six.assignment4.R;

/**
 * Creates a view that draws a border around itself.
 *
 * @author jthollo
 */
public class BorderedColorView extends View {
    private float borderWidth = 1;
    private float borderRadius = 0;
    private int borderColor = Color.BLACK;

    private Paint strokePaint;
    private Rect bounds = new Rect();
    private Rect border = new Rect();

    public BorderedColorView(Context context) {
        super(context);
        init(null, 0);
    }

    public BorderedColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BorderedColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.BorderedColorView, defStyle, 0);

        borderColor = a.getColor(R.styleable.BorderedColorView_borderColor, borderColor);

        borderRadius = a.getDimension(R.styleable.BorderedColorView_borderRadius, borderRadius);

        borderWidth = a.getDimension(R.styleable.BorderedColorView_borderWidth, borderWidth);

        a.recycle();

        // Set up a default TextPaint object
        strokePaint = new Paint();
        strokePaint.setColor(borderColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(borderWidth);
        strokePaint.setStrokeMiter(borderRadius);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.getClipBounds(bounds);
        border.set(1, 1, bounds.right - 1, bounds.bottom - 1);
        canvas.drawRect(border, strokePaint);
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        strokePaint.setStrokeWidth(borderWidth);
    }

    public float getBorderRadius() {
        return borderRadius;
    }

    public void setBorderRadius(float borderRadius) {
        this.borderRadius = borderRadius;
        strokePaint.setStrokeMiter(borderRadius);
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        strokePaint.setColor(borderColor);
    }
}
