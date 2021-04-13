package com.ken.plant;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dmax.dialog.SpotsDialog;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//import static android.R.drawable.ic_dialog_info;

public class Prediction extends AppCompatActivity {

    ImageView predImage;
    Button callBtn, medBtn;
    Bitmap finalBitmap;
    TextView predRes;
    TextView predProb;
    ListView listview;

    String filePath;
    String prediction;
    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");

    Boolean upload = false;
    public android.app.AlertDialog dialog;
    String resultList[][] = {
            {"黑點病", ""},
            {"缺鎂", ""},
            {"潛葉蛾", ""},
            {"油胞病", ""},
    };

    Animation expandIn;
    Animation expandOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        predImage = findViewById(R.id.predImage);
        callBtn = findViewById(R.id.callBtn);
        medBtn = findViewById(R.id.medBtn);
        predRes = findViewById(R.id.predResult);

        //predProb = findViewById(R.id.predProb);

//        // Get data from last page
//        byte[] byteArray = getIntent().getByteArrayExtra("image");
//        finalBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        filePath = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filePath);
            finalBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage(R.string.uploading)
                .setCancelable(true)
                .setTheme(R.style.Custom)
                .build();

        dialog.show();


        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                connectServer();
            }
        });

        t1.start();

        predImage.setImageBitmap(finalBitmap);
        predRes.setVisibility(View.GONE);
        medBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(prediction!=null) {
                    Intent intent = new Intent(Prediction.this, PesticideInfo.class);
                    intent.putExtra("prediction", prediction);
                    startActivity(intent);
                }else{
                    Toast.makeText(Prediction.this,"等一下!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            public void onClick(View view) {
                new AlertDialog.Builder(Prediction.this).setTitle("諮詢專線")
                        .setIcon(R.drawable.icons8_phone_100)
                        .setMessage("確定撥打電話給農改場嗎?")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse("tel:0987654321");
                                Intent it = new Intent(Intent.ACTION_DIAL, uri);
                                startActivity(it);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create().show();

            }
        });


        predRes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                initPopWindow(v);
            }
        });


    }

    public void connectServer() {
        String ipv4Address = "134.208.3.54";
        String portNumber = "7789";
        Matcher matcher = IP_ADDRESS.matcher(ipv4Address);
        if (!matcher.matches()) {
            //responseText.setText("Invalid IPv4 Address. Please Check Your Inputs.");
            return;
        }

        String postUrl = "http://" + ipv4Address + ":" + portNumber + "/";

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        String fileName = filePath.substring(filePath.lastIndexOf("/")+1);

        multipartBodyBuilder.addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("image/*"), byteArray));

        RequestBody postBodyImage = multipartBodyBuilder.build();

        Log.d("test", postBodyImage.toString());

        postRequest(postUrl, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //TextView responseText = findViewById(R.id.responseText);
                        //responseText.setText("Failed to Connect to Server. Please Try Again.");
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, final okhttp3.Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(Prediction.this,"get result", Toast.LENGTH_SHORT).show();
                            String result = response.body().string();
                            String r[] = result.split(",");
                            /*
                             * r[0] 辨識結果:油胞病
                             * r[1] 黑點病: 0.0%
                             * r[2] 缺鎂:   0.0%
                             * r[3] 潛葉蛾: 0.1609%
                             * r[4] 油胞病: 99.8391%
                             * r[5] 健康:   0.0%
                             * */
                            resultList[0][1]=r[1].split(":")[1];
                            resultList[1][1]=r[2].split(":")[1];
                            resultList[2][1]=r[3].split(":")[1];
                            resultList[3][1]=r[4].split(":")[1];

                            prediction=r[0].split(":")[1].replaceAll("\\s+", "");
                            predRes.setText(prediction);
                            predRes.setVisibility(View.VISIBLE);
                            dialog.dismiss();




                        } catch (Exception e) {
                            Log.d("Get response:", e.toString());
                            e.printStackTrace();
                        }
                        Toast.makeText(Prediction.this,"預測結果:"+prediction, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void initPopWindow(View v) {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup, null);


        //將資料轉換成<key,value>的型態
        List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
        for (int i=0;i < resultList.length;i++){
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("tvDis", resultList[i][0]);
            item.put("tvProb", resultList[i][1]);
            items.add(item);
        }
        Log.d("List", items.toString());

        //帶入對應資料
        SimpleAdapter adapter = new SimpleAdapter(
                Prediction.this,
                items,
                R.layout.mylistview,
                new String[]{"tvDis", "tvProb"},
                new int[]{R.id.tvDis, R.id.tvProb}
        );


        listview = popupView.findViewById(R.id.listview);
        listview.setAdapter(adapter);

        expandIn = AnimationUtils.loadAnimation(this, R.anim.pop_enter_anim);
        expandOut = AnimationUtils.loadAnimation(this, R.anim.pop_exit_anim);
        popupView.startAnimation(expandIn);
        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //popupWindow.setAnimationStyle(R.style.popup_window_animation_phone);
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        backgroundAlpha(0.4f);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.startAnimation(expandOut);
                popupWindow.dismiss();
                return true;
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupView.startAnimation(expandOut);
                backgroundAlpha(1f);
            }
        });
    }

    public void backgroundAlpha(float bg_alpha){
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bg_alpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Prediction.this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}