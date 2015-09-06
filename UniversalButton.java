import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.Button;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kot32 on 15/7/26.
 */
public class UniversalButton extends Button {


    private boolean isStillDown = false;
    private AtomicBoolean isFirstClick = new AtomicBoolean(true);
    private Drawable sourceBackground;


    //是否有自定义的按下图片
    private boolean hasCustomSetting = false;
    private Bitmap onActionDownPicBitmap;
    //自定义背景图片按下时的深度
    private int pressColorDeep = 15;//0-100

    public UniversalButton(Context context) {
        super(context);
    }

    public UniversalButton(Context context, String text) {
        super(context);
        setText(text);
    }

    public UniversalButton(Context context, AttributeSet attrs) {
        super(context, attrs);

    }


    public UniversalButton(Context context, AttributeSet attrs, Bitmap onActionDownPicBitmap) {
        super(context, attrs);
        this.onActionDownPicBitmap = onActionDownPicBitmap;
        hasCustomSetting = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //按下后背景改变为深色
                if (!isStillDown) {
                    if (isFirstClick.compareAndSet(true, false)) {
                        sourceBackground = getBackground();
                    }
                    //如果背景是纯色
                    if (sourceBackground instanceof ColorDrawable) {
                        int color = ((ColorDrawable) sourceBackground).getColor();
                        int destColor = makePressColor(color, 255);
                        setBackgroundDrawable(new ColorDrawable(destColor));
                        isStillDown = true;
                        break;
                    } else {
                        setBackgroundDrawable(new BitmapDrawable(getProcessedBitmap()));
                    }

                    isStillDown = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                isStillDown = false;
                setBackgroundDrawable(sourceBackground);
                break;
            case MotionEvent.ACTION_CANCEL:
                isStillDown = false;
                setBackgroundDrawable(sourceBackground);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private int makePressColor(int color, int alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        r = (r - 30 < 0) ? 0 : r - 30;
        g = (g - 30 < 0) ? 0 : g - 30;
        b = (b - 30 < 0) ? 0 : b - 30;
        return Color.argb(alpha, r, g, b);
    }

    public void setOnActionDownPicBitmap(Bitmap onActionDownPicBitmap) {
        this.onActionDownPicBitmap = onActionDownPicBitmap;
        hasCustomSetting = true;
    }


    public Bitmap getProcessedBitmap() {
        Bitmap bitmap = null;
        //如果有自定义设置，那么应用
        if (hasCustomSetting) {
            if (onActionDownPicBitmap != null) {
                return onActionDownPicBitmap;
            }
        }
        //如果没有背景，截图
        if (sourceBackground == null) {
            this.setDrawingCacheEnabled(true);
            this.buildDrawingCache();
            bitmap = this.getDrawingCache();
        }

        //如果背景是图片
        else if (sourceBackground instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) sourceBackground).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
        } else {
            this.setDrawingCacheEnabled(true);
            this.buildDrawingCache();
            bitmap = this.getDrawingCache();
        }

        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.set(new float[]{1, 0, 0, 0, -pressColorDeep, 0, 1,
                0, 0, -pressColorDeep,// 改变亮度
                0, 0, 1, 0, -pressColorDeep, 0, 0, 0, 1, 0});
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        cMatrix.reset();
        return bitmap;
    }

    /**
     * 自适应文字大小
     */
    private void autoFitTextSize() {
        Paint p = getPaint();
        p.setTypeface(getTypeface());
        p.setTextSize(getTextSize());

        float needWidth = getPaddingLeft() + getPaddingRight() + p.measureText(getText().toString());
        Paint.FontMetrics fm = p.getFontMetrics();
        float needHeight = (float) (Math.ceil(fm.descent - fm.ascent) + 1);
        if (needWidth > getWidth()) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() - 0.5f);
            autoFitTextSize();
        }
        if (needHeight > getHeight()) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() - 0.2f);
            autoFitTextSize();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        autoFitTextSize();
    }

    public int getPressColorDeep() {
        return pressColorDeep;
    }

    public void setPressColorDeep(int pressColorDeep) {
        this.pressColorDeep = pressColorDeep;
    }
}
