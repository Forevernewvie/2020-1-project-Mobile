package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class photoActivity extends AppCompatActivity {
    protected  String File_Path;
    //protected  String photo_uri;
    //protected  String code;
    private  TextView  testView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        ImageView imageview = (ImageView)findViewById(R.id.temp);
        testView = (TextView)findViewById(R.id.testEmotion);
        File_Path = getIntent().getStringExtra("FilePath");
        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);



        ExifInterface exif = null;

        try {
            exif = new ExifInterface(File_Path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation;
        int exifDegree;

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        } else {
            exifDegree = 0;
        }

        imageview.setImageBitmap(rotate(bitmap, exifDegree));

        Button analysis = (Button)findViewById(R.id.analysisButton);
        analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testUploadImage(File_Path);

            }
        });

    }



    private void testUploadImage(String file_path)
    {


        File imgFile = new File(file_path);

        imageUpload service =  ServiceGenerator.createService(imageUpload.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/jpg"), imgFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imgFile.getName(), requestFile);

        Call<ResponseBody> call = service.uploadImage(body);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    //textViewResult.setText("code: " + response.code());
                    return;
                }
                testConnect();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });

    }

    private void testConnect()
    {

        //imageUpload service =  ServiceGenerator.createService(imageUpload.class);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.221.100:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<Post> call = jsonPlaceHolderApi.getPost();

        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (!response.isSuccessful()) {
                    testView.setText("code: " + response.code());
                    return;
                }

                Post posts = response.body();
                String content="";
                content +=  posts.getEmotion();

                Intent intent = new Intent(photoActivity.this,HisoryActiviy.class);
                intent.putExtra("result",content);
                startActivity(intent);


            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                testView.setText(t.getMessage() +"에러가 났어요");

            }
        });
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }






}
