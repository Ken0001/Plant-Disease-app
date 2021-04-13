package com.ken.plant;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Some component in MainActivity
    ImageView chosenImage;
    TextView imageTextView;
    Button upload;
    ConstraintLayout mainac;
    private static final int CAMERA_REQUEST=100;
    private static final int STORAGE_REQUEST=200;

    Bitmap finalBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        chosenImage = findViewById(R.id.chosenImage);
        imageTextView = findViewById(R.id.imageTextView);
        upload = findViewById(R.id.uploadBtn);
        mainac = findViewById(R.id.mainac);

        chosenImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalBitmap==null){
                    Toast.makeText(MainActivity.this, "尚未選擇圖片", Toast.LENGTH_LONG).show();
                }else{
                    try {
                        //Write file
                        String filename = "bitmap.png";
                        FileOutputStream stream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
                        finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                        //Cleanup
                        stream.close();
                        //finalBitmap.recycle();

                        //Pop intent
                        Intent in1 = new Intent(MainActivity.this, Prediction.class);
                        in1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        in1.putExtra("image", filename);
                        startActivity(in1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    // Pick image by CropImage library
    private void pickImage(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setBorderLineColor(Color.RED)
                .setGuidelinesColor(Color.WHITE)
                .start(MainActivity.this);
    }

    public static Bitmap toOvalBitmap(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        int color = 0xff424242;
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawARGB(0, 255, 255, 255);
        //canvas.drawColor(Color.WHITE);


        RectF rect = new RectF(0, 0, width, height);
        canvas.drawOval(rect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        bitmap.recycle();

        return output;
    }

    public static Bitmap cut(Bitmap image) {
        Bitmap imageWithBG = Bitmap.createBitmap(image.getWidth(), image.getHeight(),image.getConfig()); // Create another image the same size
        imageWithBG.eraseColor(Color.WHITE); // set its background to white, or whatever color you want
        Canvas canvas = new Canvas(imageWithBG); // create a canvas to draw on the new image
        canvas.drawBitmap(image, 0f, 0f, null); // draw old image on the background
        image.recycle(); // clear out old image
        return imageWithBG;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    bitmap = toOvalBitmap(bitmap);

                    // Process bitmap to oval crop
                    finalBitmap = cut(toOvalBitmap(bitmap));
                    chosenImage.setImageBitmap(finalBitmap);
                    imageTextView.setVisibility(View.GONE);
                    Log.d("URI", ""+resultUri.getPath());
                } catch (IOException e) {
                    Log.e("E", e.toString());
                    e.printStackTrace();
                }



            }
        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            // 监控返回键
            new AlertDialog.Builder(MainActivity.this).setTitle("離開應用程式")
                    .setIcon(R.drawable.warning)
                    .setMessage("確定要退出程式嗎?")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create().show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}