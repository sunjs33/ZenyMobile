package userve.com.zenymobile;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.support.v4.graphics.PaintCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;


public class CameraSurfaceView extends SurfaceView {

    CameraSurfaceView(Context context) {
        super(context);
        this.setWillNotDraw(false);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWillNotDraw(false);
    }

    CameraSurfaceView(Context context, SurfaceView sv ) {
        super(context);
        this.setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if(true){

            //  Find Screen size first
            DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
            int screenWidth = metrics.widthPixels;
            // int screenHeight = (int) (metrics.heightPixels*0.9);
            int screenHeight = (int) (metrics.heightPixels);

            //  Set paint options
            Paint paint = new Paint();

            paint.setAntiAlias(true);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.argb(255, 255, 255, 255));

            int maxVerticalLine = 6;
            for(int i = 1; i < maxVerticalLine; i++) {
                canvas.drawLine((screenWidth/maxVerticalLine)*i,0,(screenWidth/maxVerticalLine)*i,screenHeight,paint);  // 세로
            }

            int maxHorizontalLine = 10;
            for(int i = 1; i < maxHorizontalLine; i++) {
                canvas.drawLine(0,(screenHeight/maxHorizontalLine)*i,screenWidth,(screenHeight/maxHorizontalLine)*i,paint);   // 가로
            }

            Paint guidePaint = new Paint();

            guidePaint.setAntiAlias(true);
            guidePaint.setStrokeWidth(5);
            guidePaint.setStyle(Paint.Style.STROKE);
            guidePaint.setColor(Color.argb(255, 255, 1, 1));

            canvas.drawLine((screenWidth/maxVerticalLine)*(maxVerticalLine/2),0+(screenHeight/maxHorizontalLine),(screenWidth/maxVerticalLine)*(maxVerticalLine/2),screenHeight-(screenHeight/maxHorizontalLine),guidePaint);  // 세로

            canvas.drawLine(0+(screenWidth/maxVerticalLine),(screenHeight/maxHorizontalLine)*1,screenWidth-(screenWidth/maxVerticalLine),(screenHeight/maxHorizontalLine)*1,guidePaint);   // 가로
            canvas.drawLine(0+(screenWidth/maxVerticalLine),(screenHeight/maxHorizontalLine)*(maxHorizontalLine-1),screenWidth-(screenWidth/maxVerticalLine),(screenHeight/maxHorizontalLine)*(maxHorizontalLine-1),guidePaint);   // 가로

        }
    }
}