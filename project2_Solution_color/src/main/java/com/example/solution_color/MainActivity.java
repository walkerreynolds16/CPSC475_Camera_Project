package com.example.solution_color;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.library.bitmap_utilities.BitMap_Helpers;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity  {
    private static final int CAMERA_REQUEST = 1888;
    private static final String PREF_NAME = "pathName";

    public Toolbar myToolbar;
    private String currentPhotoPath;

    public ImageView image;

    public Bitmap srcPhotoBitmap;
    public Bitmap sketchPhotoBitmap;
    public Bitmap colorizePhotoBitmap;
    public Bitmap colorBitmap;

    public int prevSketchiness;
    public int prevSaturation;

    public boolean changeGreyscale = true;
    public boolean changeColorize = true;


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
        }else{
            image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.gutters));
            srcPhotoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gutters);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int sketchPercent = 10;
        prevSketchiness = preferences.getInt("sketchiness", sketchPercent);

        int satPercent = 150;
        prevSaturation = preferences.getInt("saturation", satPercent);


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
        int sketchPercent = 1;
        sketchPercent = preferences.getInt("sketchiness", sketchPercent);

        if(sketchPercent == prevSketchiness && !changeGreyscale){
            image.setImageBitmap(sketchPhotoBitmap);
        }else {
            if(!changeGreyscale){
                prevSketchiness = sketchPercent;
            }
            sketchPhotoBitmap = BitMap_Helpers.thresholdBmp(srcPhotoBitmap, sketchPercent);
            image.setImageBitmap(sketchPhotoBitmap);


            changeGreyscale = false;
        }

        Camera_Helpers.saveProcessedImage(sketchPhotoBitmap, currentPhotoPath);

    }


    private void doColorize(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);


        int sketchPercent = 1;
        sketchPercent = preferences.getInt("sketchiness", sketchPercent);

        if(sketchPercent != prevSketchiness || changeGreyscale){
            sketchPhotoBitmap = BitMap_Helpers.thresholdBmp(srcPhotoBitmap, sketchPercent);

            if(!changeGreyscale){
                prevSketchiness = sketchPercent;
            }

            changeGreyscale = false;
        }




        int satPercent = 150;
        satPercent = preferences.getInt("saturation", satPercent);
        if(satPercent != prevSaturation || changeColorize){
            colorBitmap = BitMap_Helpers.colorBmp(srcPhotoBitmap, satPercent);


            if(!changeColorize){
                prevSaturation = satPercent;
            }

            changeColorize = false;
        }

        BitMap_Helpers.merge(colorBitmap, sketchPhotoBitmap);
        colorizePhotoBitmap = colorBitmap;
        image.setImageBitmap(colorizePhotoBitmap);
        Camera_Helpers.saveProcessedImage(colorizePhotoBitmap, currentPhotoPath);


    }

    private void doShare(){
        File file = new File(currentPhotoPath);
        Uri uri = Uri.fromFile(file);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String text = preferences.getString("text", "Look at this photo i took");
        String subject = preferences.getString("subject", "New App Photo");


        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/jpg");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, "Photo"));

    }

    private void doReset(){
        Camera_Helpers.delSavedImage(currentPhotoPath);

        image.setImageResource(R.drawable.gutters);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setScaleType(ImageView.ScaleType.FIT_XY);

        srcPhotoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gutters);

        changeColorize = true;
        changeGreyscale = true;

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

            changeColorize = true;
            changeGreyscale = true;

        }
    }

    @Override
    protected void onStop() {
        super.onStop();


        SharedPreferences sp = getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("path", currentPhotoPath);
        editor.apply();

    }
}

