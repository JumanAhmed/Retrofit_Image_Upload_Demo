package com.capsulestudio.retofitimageupload;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_CAMERA_IMAGE = 2;
    private static final int PICK_GALLERY_IMAGE = 1;

    public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    public static final String IMAGE_DIRECTORY = "SylhetFoodie";

    private ImageView ivshow;
    private EditText etBrand;
    private EditText etModel;
    private Button btnUpload;
    private Button btnGallery;
    private Button btnCamera;
    private TextView tvtitle;

    private String filePath = null;
    public static final int RequestPermissionCode = 1;

    private File file;
    private File sourceFile;
    private File destFile;
    private File compressedImageFile;

    private SimpleDateFormat dateFormatter;
    private Uri imageCaptureUri;

    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        file = new File(Environment.getExternalStorageDirectory()
                + "/" + IMAGE_DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }

        dateFormatter = new SimpleDateFormat(
                DATE_FORMAT, Locale.US);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading..Plz wait...");

    }



    private void init() {
        this.btnCamera = (Button) findViewById(R.id.btnCamera);
        this.btnGallery = (Button) findViewById(R.id.btnGallery);
        this.btnUpload = (Button) findViewById(R.id.btnUpload);
        this.etModel = (EditText) findViewById(R.id.etModel);
        this.etBrand = (EditText) findViewById(R.id.etBrand);
        this.ivshow = (ImageView) findViewById(R.id.ivshow);
        this.tvtitle = (TextView) findViewById(R.id.tvtitle);

        btnGallery.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {

            case R.id.btnGallery:
                selectImageFromGallery();

                break;
            case R.id.btnCamera:
                 captureImageFromCamera();
                break;

            case R.id.btnUpload:
                    nowUpload();
                break;
        }
    }



    private void nowUpload() {

         ApiInterface  api = ApiClient.getApiInterface();

         String brandName = etBrand.getText().toString();
         String modelName = etModel.getText().toString();

          //String newPath = imageCompress(filePath);

         if (!TextUtils.isEmpty(brandName) && !TextUtils.isEmpty(modelName) && !TextUtils.isEmpty(compressedImageFile.getName())){
            progressDialog.show();
            RequestBody rBrandName = RequestBody.create(MediaType.parse("text/plain"), brandName );
            RequestBody rModel = RequestBody.create(MediaType.parse("text/plain"), modelName );
             RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), compressedImageFile);

            MultipartBody.Part body = MultipartBody.Part.createFormData("image", compressedImageFile.getName(), requestFile);

            Call<DataModel> resultCall = api.uploadCarData(rBrandName, rModel, body);

            resultCall.enqueue(new Callback<DataModel>() {
                @Override
                public void onResponse(Call<DataModel> call, Response<DataModel> response) {
                    if (response.isSuccessful()){
                        progressDialog.dismiss();
                        DataModel data = response.body();

                        Toast.makeText(getApplicationContext(),""+data.getResponse(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<DataModel> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Message:"+t.getMessage(), Toast.LENGTH_LONG).show();

                }
            });

        }else{
            Toast.makeText(getApplicationContext(), "Field Must Not be Empty", Toast.LENGTH_LONG).show();
        }

    }

    private void selectImageFromGallery() {

        if (checkPermission()){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_GALLERY_IMAGE);
        }else {
            requestPermission();
        }

    }

    private void captureImageFromCamera() {
        if (checkPermission()){
            sourceFile = new File(file, "img_"
                    + dateFormatter.format(new Date()).toString() + ".png");
            imageCaptureUri = Uri.fromFile(sourceFile);

            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
            startActivityForResult(intentCamera, PICK_CAMERA_IMAGE);
        }else{
            requestPermission();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode){

                case PICK_GALLERY_IMAGE:
                    Uri imageUri = data.getData();
                    ivshow.setImageURI(imageUri);
                    // Glide.with(this).load(fileBitmap).into(ivshow);
                    //filePath = getRealPathFromURI(imageUri);
                    sourceFile = new File(getRealPathFromURI(imageUri)); // Make file from path, for that we need  real path, that's why wee use getRealPathFromURI()
                    compressImageWithZetbaitsuLibrary(sourceFile);       // Here i'm compress the image

                    viewHideAndSeek();

                    break;
                case PICK_CAMERA_IMAGE:
                    if (imageCaptureUri == null){
                         Toast.makeText(getApplicationContext(), "Uri empty", Toast.LENGTH_LONG).show();
                    }else{
                        ivshow.setImageURI(imageCaptureUri);
                        compressImageWithZetbaitsuLibrary(sourceFile);
                        viewHideAndSeek();

                    }

                    break;
            }


        }

    }

    private void compressImageWithZetbaitsuLibrary(File sourceFile) {
        new Compressor(this)
                .setMaxWidth(1024)
                .setMaxHeight(1024)
                .setQuality(80)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES+ "/" + IMAGE_DIRECTORY).getAbsolutePath())
                .compressToFileAsFlowable(sourceFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) {
                        compressedImageFile = file;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    private void viewHideAndSeek() {
        ivshow.setVisibility(View.VISIBLE);
        etBrand.setVisibility(View.VISIBLE);
        etModel.setVisibility(View.VISIBLE);
        tvtitle.setVisibility(View.GONE);
        btnGallery.setVisibility(View.GONE);
        btnCamera.setVisibility(View.GONE);
        btnUpload.setEnabled(true);
    }

    /*
    * This method is fetching the absolute path of the image file
    * if you want to upload other kind of files like .pdf, .docx
    * you need to make changes on this method only
    * Rest part will be the same
    * */
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }



    public boolean checkPermission() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission()
        {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        CAMERA,
                        READ_EXTERNAL_STORAGE,
                        WRITE_EXTERNAL_STORAGE

                }, RequestPermissionCode);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean InternalStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean InternalStoragePermission1 = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission  && InternalStoragePermission && InternalStoragePermission1) {

                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.action_clear:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                break;

            case R.id.action_settings:

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private static String getTempFilename(Context context) throws IOException {
        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile("image", "tmp", outputDir);
        return outputFile.getAbsolutePath();
    }



    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }



    //get bitmap image from byte array
    private Bitmap convertToBitmap(byte[] b){

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }

    //COnvert and resize our image to 400dp for faster uploading our images to DB
    protected Bitmap decodeUri(Uri selectedImage, int REQUIRED_SIZE) {

        try {

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            // final int REQUIRED_SIZE =  size;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //Convert bitmap to bytes
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private byte[] profileImage(Bitmap b){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 30, bos);
        return bos.toByteArray();

    }

    // Convert bitmap to File
    public File bitmapToFile(Bitmap bitmap){
        File newImage;
        try {
            newImage = new File(file, "jaki.png");
            FileOutputStream fos = new FileOutputStream(newImage);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);  // 100 is high quality
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return newImage;
    }

}
