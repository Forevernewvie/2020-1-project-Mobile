package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.CursorLoader;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "test";
    protected ImageView imageView;
    private  File tempFile;
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFilePath;
    private Uri photoUri;
    private Bitmap imageBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //퍼미션 허가
        //showPermission();

        //종료
        Button buttonExit = (Button)findViewById(R.id.btn_exit);
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());

            }
        });

        //카메라
        Button buttonCamera = (Button)findViewById(R.id.btn_camera);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTakePhotoIntent();
            }
        });


        //앨범
        Button buttonAlbum = (Button)findViewById(R.id.btn_picture);
        buttonAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

    }

    //팝업
    public void mOnPopupClick(View v){
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("data", "카메라 버튼을 눌러 사진 촬영 후 분석 버튼을 클릭하거나"+'\r'+"앨범 버튼을 눌러 있는 사진을 가져와 분석하면 됩니다"
               );
        startActivity(intent);
    }
    /*
    private void showPermission(){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this,"승인",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this,"거절",Toast.LENGTH_SHORT).show();
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("권한 설정 허가 요청")
                .setPermissions(new String[] {

                        Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})

                .check();
    }*/

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //앨범
        if (requestCode == 1) {


            if (resultCode == RESULT_OK) {
                try {

                    String File_path=getRealPathFromURI(data.getData());
                    // 선택한 이미지에서 비트맵 생성
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    Bitmap img = BitmapFactory.decodeStream(in);


                    float scale = (float) (1024/(float)img.getWidth());
                    int image_w = (int) (img.getWidth() * scale * 0.75);
                    int image_h = (int) (img.getHeight() * scale * 0.75);
                    Bitmap resize = Bitmap.createScaledBitmap(img, image_w, image_h, true);
                    resize.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                    byte[] byteArray = stream.toByteArray();
                    //String code = "album";
                    Intent intent = new Intent(MainActivity.this,photoActivity.class);
                    intent.putExtra("image",byteArray);
                    intent.putExtra("FilePath",File_path);
                    //intent.putExtra("code",code);
                    startActivity(intent);
                    in.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //촬영
        else if(requestCode ==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK){

                try {
                     imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                float scale = (float) (1024/(float)imageBitmap.getWidth());
                int image_w = (int) (imageBitmap.getWidth() * scale *0.75 );
                int image_h = (int) (imageBitmap.getHeight() * scale *0.75 );
                Bitmap resize = Bitmap.createScaledBitmap(imageBitmap, image_w, image_h, true);
                resize.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                byte[] byteArray = stream.toByteArray();

            //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileRri);
            // 이미지 표시
                //String code = "camera";
                Intent intent = new Intent(MainActivity.this,photoActivity.class);
                 intent.putExtra("image",byteArray);
                intent.putExtra("FilePath",imageFilePath);
                // intent.putExtra("code",code);
                startActivity(intent);
        }

    }

    private String getRealPathFromURI(Uri contentUri) {
        int column_index=0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(column_index);
    }




    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }


}











