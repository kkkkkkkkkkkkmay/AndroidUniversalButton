
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

    public UniversalButton(Context context) {
        super(context);

    }

    public UniversalButton(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Bitmap bitmap;
                //按下后背景改变为深色
                if (!isStillDown) {
                    if(isFirstClick.compareAndSet(true,false)){
                        sourceBackground = getBackground();
                    }
                    //如果没有背景，截图
                    if (sourceBackground == null) {
                        this.setDrawingCacheEnabled(true);
                        this.buildDrawingCache();
                        bitmap = this.getDrawingCache();
                    }
                    //如果背景是纯色
                    else if (sourceBackground instanceof ColorDrawable) {
                        int color = ((ColorDrawable) sourceBackground).getColor();
                        int destColor = makePressColor(color, 255);
                        setBackgroundDrawable(new ColorDrawable(destColor));
                        isStillDown = true;
                        break;
                    }
                    //如果背景是图片
                    else if (sourceBackground instanceof BitmapDrawable) {
                        bitmap = ((BitmapDrawable) sourceBackground).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
                    }
                    else {
                        this.setDrawingCacheEnabled(true);
                        this.buildDrawingCache();
                        bitmap = this.getDrawingCache();
                    }

                    ColorMatrix cMatrix = new ColorMatrix();
                    int brightness = -15;
                    cMatrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1,
                            0, 0, brightness,// 改变亮度
                            0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});
                    Paint paint = new Paint();
                    paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawBitmap(bitmap, 0, 0, paint);
                    setBackgroundDrawable(new BitmapDrawable(bitmap));

                    cMatrix.reset();
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
}