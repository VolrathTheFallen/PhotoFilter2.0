package com.example.erik2013.photofilter20;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    private ImageView imageView = null;
    private static boolean firstCall = true;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static String root = null;
    private static String imageFolderPath = null;
    private static Bitmap originalBitmap = null;
    private static Bitmap editedBitmap = null;
    private static Bitmap tempBitmap = null;
    private static boolean edited = false;
    private static float[] filter;
    private Matrix mat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();   //Hides actionbar

        imageView = (ImageView) findViewById(R.id.imageView);
        ImageButton camera = (ImageButton) findViewById(R.id.camera);
        ImageButton save = (ImageButton) findViewById(R.id.save);
        ImageButton blur = (ImageButton) findViewById(R.id.blur);
        ImageButton pixelate = (ImageButton) findViewById(R.id.pixelate);

        mat = new Matrix();

        if(firstCall == true)
        {
            takePhoto();
            firstCall = false;
        }

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePhotoAux();
            }
        });

        blur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blurPhoto();
            }
        });

        pixelate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelatePhoto();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void takePhoto()
    {
        edited = false;
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            originalBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(originalBitmap);
        }

    }

    public void pixelatePhoto()
    {
        if(!edited)  //Choose appropriate base
            tempBitmap = Bitmap.createBitmap(originalBitmap);
        else
            tempBitmap = Bitmap.createBitmap(editedBitmap);

        editedBitmap = ConvolutionMatrix.pixelate(tempBitmap);
        imageView.setImageBitmap(editedBitmap);

        edited = true;
    }

    public void blurPhoto()
    {
        filter = new float[]{1, 2, 1,
                             2, 4, 2,
                             1, 2, 1};
        mat.setValues(filter);

        if(!edited)  //Choose appropriate base
            tempBitmap = Bitmap.createBitmap(originalBitmap);
        else
            tempBitmap = Bitmap.createBitmap(editedBitmap);

        editedBitmap = ConvolutionMatrix.convolute(tempBitmap, mat, 16, 0);
        imageView.setImageBitmap(editedBitmap);

        edited = true;
    }

    public void savePhotoAux()
    {
        if(edited)
            savePhoto(editedBitmap);
        else
            savePhoto(originalBitmap);
    }

    private void savePhoto(Bitmap imageToSave)
    {
        // fetching the root directory
        root = Environment.getExternalStorageDirectory().toString()
                + "/Photo_Filter";

        // Creating folders for Image
        imageFolderPath = root + "/saved_images";
        File imagesFolder = new File(imageFolderPath);

        if(!imagesFolder.exists())
            imagesFolder.mkdirs();

        // Generating file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // Creating image here
        File imageFile = new File(imageFolderPath, timeStamp +".jpg");

        //Fill file with data
        try{
            FileOutputStream fOut = new FileOutputStream(imageFile);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
            MakeSureFileWasCreatedThenMakeAvailable(imageFile);
            AbleToSave();
        }
        catch (FileNotFoundException e) {UnableToSave();}
        catch (IOException e){UnableToSave();}

    }

    private void MakeSureFileWasCreatedThenMakeAvailable(File file)
    {
        MediaScannerConnection.scanFile(this, new String[] {file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.e("ExternalStorage", "Scanned " + path + ":");
                Log.e("ExternalStorage", "-> uri=" + uri);
            }
        });
    }

    private void UnableToSave()
    {
        Toast.makeText(this, "Picture failed to save.", Toast.LENGTH_SHORT).show();
    }

    private void AbleToSave()
    {
        Toast.makeText(this, "Picture saved correctly.", Toast.LENGTH_SHORT).show();
    }

}
