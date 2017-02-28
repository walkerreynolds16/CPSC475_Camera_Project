package com.example.solution_color;


import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.library.bitmap_utilities.BitMap_Helpers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.v7.appcompat.R.styleable.View;


public class MainActivity extends AppCompatActivity  {
    private static final int CAMERA_REQUEST = 1888;
    private static final String PREF_NAME = "pathName";


    private static final String STORAGE_PATH = "/cameraPhoto/";


    public Toolbar myToolbar;
    private String currentPhotoPath;

    public ImageView image;

    public Bitmap srcPhotoBitmap;
    public Bitmap sketchPhotoBitmap;
    public Bitmap colorPhotoBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setTitle("");

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.getBackground().setAlpha(100);
        image = (ImageView)findViewById(R.id.imageView);



        SharedPreferences sp = getSharedPreferences(PREF_NAME, 0);
        String path = sp.getString("path", null);

        if(path != null){
            Toast.makeText(this, path, Toast.LENGTH_LONG).show();
            currentPhotoPath = path;
            setBitmapAndDisplay(path);
        }

    }

    private void setBitmapAndDisplay(String path){
        DisplayMetrics dm = this.getResources().getDisplayMetrics();

        srcPhotoBitmap = Camera_Helpers.loadAndScaleImage(path, dm.heightPixels, dm.widthPixels);
        image.setImageBitmap(srcPhotoBitmap);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                break;

            case R.id.action_share:
                //when share icon is pressed
                doShare();
                break;

            case R.id.action_colorize:
                //when colorize icon is pressed
                doColorize();
                break;

            case R.id.action_greyscale:
                //when greyscale icon is pressed
                doGreyscale();
                break;

            case R.id.action_reset:
                //when reset icon is pressed
                doReset();

                break;

            default:
                super.onOptionsItemSelected(item);
                break;
        }
        return true;
    }

    private void doGreyscale(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int percent = 1;
        percent = preferences.getInt("sketchiness", percent);

        sketchPhotoBitmap = BitMap_Helpers.thresholdBmp(srcPhotoBitmap, percent);
        image.setImageBitmap(sketchPhotoBitmap);

        Camera_Helpers.saveProcessedImage(sketchPhotoBitmap, currentPhotoPath);

    }


    private void doColorize(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);


        int sketchPercent = 1;
        sketchPercent = preferences.getInt("sketchiness", sketchPercent);
        Bitmap sketchyBM = BitMap_Helpers.thresholdBmp(srcPhotoBitmap, sketchPercent);



        int satPercent = 150;
        satPercent = preferences.getInt("saturation", satPercent);
        Bitmap colorizeBM = BitMap_Helpers.colorBmp(srcPhotoBitmap, satPercent);


        BitMap_Helpers.merge(colorizeBM, sketchyBM);
        colorPhotoBitmap = colorizeBM;
        image.setImageBitmap(colorPhotoBitmap);
        Camera_Helpers.saveProcessedImage(colorPhotoBitmap, currentPhotoPath);


    }


    private void doShare(){
        File file = new File(currentPhotoPath);
        Uri uri = Uri.fromFile(file);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/jpg");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Look at this photo i took");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "New App Photo");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, "Photo"));

    }

    private void doReset(){
        Camera_Helpers.delSavedImage(currentPhotoPath);

        image.setImageResource(R.drawable.gutters);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setScaleType(ImageView.ScaleType.FIT_XY);

    }


    public void takePhoto(View view)throws IOException{

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File tempFile = File.createTempFile("camera", ".png", getExternalCacheDir());


        currentPhotoPath = tempFile.getAbsolutePath();
        System.out.println(currentPhotoPath);
        Uri uri = Uri.fromFile(tempFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CAMERA_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            setBitmapAndDisplay(currentPhotoPath);

            DisplayMetrics dm = this.getResources().getDisplayMetrics();
            Bitmap temp = Camera_Helpers.loadAndScaleImage(currentPhotoPath, dm.heightPixels, dm.widthPixels);

            //Camera_Helpers.saveProcessedImage(temp, Environment.getExternalStorageDirectory().getAbsolutePath() + STORAGE_PATH + "sourcePhoto.png");
        }
    }


    public boolean saveImageToExternalStorage(Bitmap image) {
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + STORAGE_PATH + "source";

        try {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream fOut = null;
            File file = new File(fullPath, "sourcePhoto.png");
            file.createNewFile();
            fOut = new FileOutputStream(file);

    // 100 means no compression, the lower you go, the stronger the compression
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(this.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            return true;

        } catch (Exception e) {
            Log.e("saveToExternalStorage()", e.getMessage());
            return false;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();


        SharedPreferences sp = getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("path", currentPhotoPath);
        editor.commit();



    }
}

