package lib.core.heartspy;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import lib.service.ServiceState;

public class WeatherIndicator extends ImageView implements Runnable {

    private String LogTag = this.getClass().getSimpleName();
    private Handler Synchronized = null;
    public int WidthToHeightFactor = 1;

    private Bitmap Sensor_NotConnected_Background;
    private Bitmap Sensor_Connected_Background;
    private Bitmap Sensor_Heart_Pulsing;

    private BitmapShader ShaderImage = null;
    private Paint ShaderPainter = null;
    private Paint TextPainter = null;
    private Matrix ImageScaler = null;

    private ObjectAnimator ScaleAnimated;
    private AnimatorSet ScaleAnimation;

    private int StoredWidth=0;
    private int StoredHeight=0;

    private float ScaleFactor = 0.0f;

    private int Frequency = 0;
    private int OperateMode = ServiceState.Waiting;

    public WeatherIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        ShaderPainter = new Paint();
        ImageScaler = new Matrix();
        ScaleAnimation = new AnimatorSet();

        TextPainter = new Paint();
        TextPainter.setTextAlign(Paint.Align.CENTER);
        TextPainter.setColor(Color.parseColor("#aaccffff"));
        TextPainter.setTypeface(Typeface.DEFAULT_BOLD);

        Synchronized = new Handler(Looper.getMainLooper());
    }

    public void setHeartRate(int Value){
        Frequency = Value;
        Synchronized.post(this);
    }

    public void setMode(int State){
        OperateMode = State;
        Synchronized.post(this);
    }

    void LoadResources(int Width, int Height) {        // Loading or Reloading Bitmaps ...
        if ((Height == 0) || (Width == 0)) return;
        if (( Height == StoredHeight) && (Width == StoredWidth)) return;

        StoredHeight = Height;
        StoredWidth = Width;
        Resources EmbeddedDatas = getContext().getResources();
        Sensor_NotConnected_Background = Resizer.getScaledBitmap(StoredWidth, StoredHeight, EmbeddedDatas, R.drawable.sensor_not_connected);
        Sensor_Connected_Background = Resizer.getScaledBitmap(StoredWidth, StoredHeight, EmbeddedDatas, R.drawable.sensor_connected);
        Sensor_Heart_Pulsing = Resizer.getScaledBitmap(StoredWidth, StoredHeight, EmbeddedDatas, R.drawable.heart_pulsing);
    }

    void LoadShader() { // Creating shader for Heart Scaling Animation ...
        if (Sensor_Heart_Pulsing == null) return;
        ShaderImage = new BitmapShader(Sensor_Heart_Pulsing, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        ShaderPainter.setShader(ShaderImage);

        TextPainter.setTextSize(Sensor_Heart_Pulsing.getHeight()/4);
    }

    void LoadAnimation() { //Redefine Animation
        float[] KeyValues = {1.00f,0.80f,0.90f,1.00f};
        ScaleAnimated = ObjectAnimator.ofFloat(this, "ScaleFactor", KeyValues);
        ScaleAnimated.setDuration(300);
        ScaleAnimated.setStartDelay(0);

        ScaleAnimation.play(ScaleAnimated);
        ScaleAnimation.setInterpolator(new LinearInterpolator());
    }
    // Called by ScaleAnimated ..
    public void setScaleFactor(float ScaleFactor) {
        this.ScaleFactor = ScaleFactor;
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int Width = MeasureSpec.getSize(widthMeasureSpec);
        int Height = Math.min(Width/ WidthToHeightFactor, MeasureSpec.getSize(heightMeasureSpec));
        ScaleAnimation.cancel();
        LoadResources(Width, Height);
        if (Sensor_NotConnected_Background ==null) return;
        LoadAnimation();
        LoadShader();
        this.setMeasuredDimension(Sensor_NotConnected_Background.getWidth(), Sensor_NotConnected_Background.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (OperateMode == ServiceState.Running) {
            // Draw image Background
            canvas.drawBitmap(Sensor_Connected_Background, 0f, 0f, null);

            // Draw Heart with scaling effect
            ShaderImage.getLocalMatrix(ImageScaler);
            ImageScaler.setScale(ScaleFactor, ScaleFactor, canvas.getWidth() / 2, canvas.getHeight() / 2);
            ShaderImage.setLocalMatrix(ImageScaler);
            canvas.drawPaint(ShaderPainter);

            // Write Heart Rate ...
            canvas.drawText(Integer.toString(Frequency), canvas.getWidth() / 2, canvas.getHeight() / 2, TextPainter);
        }

        if (OperateMode == ServiceState.Searching) {
            canvas.drawBitmap(Sensor_NotConnected_Background, 0f, 0f, null);
            canvas.drawText("?", canvas.getWidth() / 2, canvas.getHeight() / 2, TextPainter);
        }

        if (OperateMode == ServiceState.Waiting) {
            canvas.drawBitmap(Sensor_NotConnected_Background, 0f, 0f, null);
            canvas.drawText("!", canvas.getWidth() / 2, canvas.getHeight() / 2, TextPainter);
        }
        super.onDraw(canvas);
    }

    /*********************************************************************************************
     *  Refreshing and animating UI thread
     *********************************************************************************************/
    @Override
    public void run() {
        invalidate();
        ScaleAnimation.start();
    }
}

