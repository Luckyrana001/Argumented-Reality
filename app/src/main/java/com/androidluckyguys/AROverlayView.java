package com.androidluckyguys;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.opengl.Matrix;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.androidluckyguys.helper.LocationHelper;
import com.androidluckyguys.model.ARPoint;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.bottom;
import static android.R.attr.left;
import static android.R.attr.right;
import static android.R.attr.top;

/**
 * Created by ntdat on 1/13/17.
 */

public class AROverlayView extends View {

    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<ARPoint> arPoints;


    public AROverlayView(Context context) {
        super(context);

        this.context = context;

        //Demo points
        arPoints = new ArrayList<ARPoint>() {{
            add(new ARPoint("KLPAC", 3.1850, 101.6868, 0));
            add(new ARPoint("Twin Tower", 3.1579, 101.7116, 0));
        }};
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation){
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }

        final int radius = 30;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

        for (int i = 0; i < arPoints.size(); i ++) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();

                Drawable d = null;

                if(arPoints.get(i).getName().equals("KLPAC"))
                {
                    d = ContextCompat.getDrawable(context, R.drawable.klpac);
                }
                else
                {
                    d = ContextCompat.getDrawable(context, R.drawable.twin_tower);
                }

                Bitmap myBitmap = ((BitmapDrawable)d).getBitmap();


                canvas.drawBitmap(myBitmap, x - (30 * arPoints.get(i).getName().length() / 2), y - 80, paint);
                //canvas.drawCircle(x, y, radius, paint);
                canvas.drawText(arPoints.get(i).getName(), x - (30 * arPoints.get(i).getName().length() / 2), y - 80, paint);


            }
        }
    }
}
