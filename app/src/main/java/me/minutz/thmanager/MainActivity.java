package me.minutz.thmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static GPSTracker gps;
    public UUID uuid = UUID.randomUUID();

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 98;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(this)
                        .setTitle("Accepta locatia")
                        .setMessage("accepta")
                        .setPositiveButton("accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                    }

                } else {
                }
                return;
            }

        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private File getTempFile() {
        File imageFile = new File(getExternalCacheDir(), "TEMP.jpg");
        imageFile.getParentFile().mkdirs();
        return imageFile;
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }
    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    ImageView imageView;
    Bitmap bmp;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

                bmp = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bmp);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkLocationPermission();

        TextView tv1 = findViewById(R.id.acc);
        gps = new GPSTracker(this, tv1);
        if(!gps.canGetLocation()) {
            gps.showSettingsAlert();
        }

        imageView = findViewById(R.id.imageView);
        if(gps.location != null)
            tv1.setText("ACC: "+gps.location.getAccuracy());
        Button pozab = findViewById(R.id.poza_button);
        pozab.setOnClickListener(v -> dispatchTakePictureIntent());
        Button harta = findViewById(R.id.maps_button);
        harta.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        TextInputEditText tvindiciu = findViewById(R.id.indiciu);
        TextInputEditText tvrasp = findViewById(R.id.raspuns);

        Button trimite = findViewById(R.id.send);
        trimite.setOnClickListener(v -> {
            double lat = gps.getLocation().getLatitude(), lng = gps.getLocation().getLongitude();
            float acuratete = gps.getLocation().getAccuracy();
            String poza = "";
            if(bmp != null) {
                uuid = UUID.randomUUID();
                poza = uuid.toString();
            }
            int dif = 5;
            String indiciu = tvindiciu.getText().toString();
            String rasp = tvrasp.getText().toString();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("lat", lat);
                jsonObject.put("lng", lng);
                jsonObject.put("acuratete", acuratete);
                jsonObject.put("poza", poza);
                jsonObject.put("dif", dif);
                jsonObject.put("indiciu", indiciu);
                jsonObject.put("rasp", rasp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("IND",jsonObject.toString());
            sendIndiciu(jsonObject.toString(), poza);
            if(!poza.equals(""))
                imageView.setImageBitmap(null);
        });

    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public void sendIndiciu(String json, String poza){
        try {
            RequestBody body = RequestBody.create(JSON, json);
            postRequest("https://ro049.com/rvw-api/indicii/adauga_indiciu", body, true, poza);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", uuid.toString(), RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        postRequest("https://ro049.com/rvw-api/indicii/adauga_bmp", postBodyImage, false, null);
    }

    public void postRequest(String postUrl, RequestBody postBody, boolean ind, String poza) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Looper.prepare();
                Toast toast = Toast.makeText(MainActivity.this, "Eroare!", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    Looper.prepare();
                    Toast toast = Toast.makeText(MainActivity.this, response.body().string(), Toast.LENGTH_SHORT);
                    toast.show();
                    if(ind && !poza.equals("")) {
                        sendBitmap(bmp);
                        bmp.recycle();
                        System.gc();
                        bmp = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}