package com.example.solution_color;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.v7.appcompat.R.styleable.View;


public class MainActivity extends AppCompatActivity  {
    private static final int CAMERA_REQUEST = 1888;

    private Uri mImageUri;
    private String mCurrentPhotoPath;

    public Toolbar myToolbar;
    private String currentPhotoPath;

    public ViewGroup relativeLayout;

    public ImageView image;


    //TODO handle switching from portrait to landscape



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);


        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.getBackground().setAlpha(100);


        relativeLayout = (ViewGroup)findViewById(R.id.relative_layout);

        image = (ImageView)findViewById(R.id.imageView);

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

                break;

            case R.id.action_colorize:
                //when colorize icon is pressed

                break;

            case R.id.action_greyscale:
                //when greyscale icon is pressed

                break;

            case R.id.action_reset:
                //when reset icon is pressed


                break;

            default:
                super.onOptionsItemSelected(item);
                break;
        }
        return true;
    }


    public void takePhoto(View view){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager()) != null){
            File photo = null;
            try{
                photo = createImageFile();

            }catch (IOException e){
                e.printStackTrace();
            }

            if(photo != null){
                Uri uri = FileProvider.getUriForFile(this, "com.example.solution_color.fileprovider", photo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, CAMERA_REQUEST);
            }

        }

    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            try{
//                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(currentPhotoPath));
//                Drawable photo = new BitmapDrawable(bitmap);
//                relativeLayout.setBackgroundDrawable(photo);

                File image = new File(mCurrentPhotoPath);
                if(image.exists()){
                    Bitmap bm = BitmapFactory.decodeFile(image.getAbsolutePath());
                    Drawable photo = new BitmapDrawable(bm);
                    relativeLayout.setBackgroundDrawable(photo);
                }

            }catch (Exception e){

                System.out.println("IOException in onActivityResult");
                e.printStackTrace();
            }

        }
    }

}

