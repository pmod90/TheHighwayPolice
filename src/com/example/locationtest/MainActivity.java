package com.example.locationtest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.location.Criteria;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.MediaStore.Files.FileColumns;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;



public class MainActivity extends Activity implements LocationListener{
	
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	public static final int MEDIA_TYPE_IMAGE = 1;
	LocationManager locationManager;
	String provider;
	private TextView lattitudeField;
	private TextView longitudeField;
	private Location location;
	Button captureButton;
	ImageView mImageView;
	private Camera mCamera;
	private CameraPreview mPreview;
	public double lat,lng;
	PictureCallback mPicture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 lattitudeField = (TextView) findViewById(R.id.textView1);
		 longitudeField = (TextView) findViewById(R.id.textView2);
		  captureButton = (Button) findViewById(R.id.captureButton);
	        
		  mImageView = (ImageView) findViewById(R.id.imageView1);
		 // Get the location manager
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
	     location = locationManager.getLastKnownLocation(provider);
	 	mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
	     mPicture = new PictureCallback() {

	         public void onPictureTaken(byte[] data, Camera camera) {

	             File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
	             Log.i("Entered","callback");
	             if (pictureFile == null){
	            	 Log.i("null","image");
	 	                 
	            	 return;
	             }

	             try {
	                 FileOutputStream fos = new FileOutputStream(pictureFile);
	                 fos.write(data);
	                 fos.close();
	                 MediaStore.Images.Media.insertImage(getContentResolver(), pictureFile.getAbsolutePath(), pictureFile.getName(), pictureFile.getName());
	                 mCamera.startPreview();
	             } catch (FileNotFoundException e) {

	             } catch (IOException e) {

	             }
	         }
	        };
	        captureButton.setOnClickListener(

	                new View.OnClickListener() {

	                    public void onClick(View v) {
	                        // get an image from the camera   

	                        System.out.println("Photo Taking!");
	                       // while(true)
	                       // {
	                        	//if(lat > 35)
	                        mCamera.takePicture(null, null, mPicture);
	                   
	                       
	                        //}


	                    }
	                }
	            );
	}

	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    }
	   
	    else {
	        return null;
	    }

	    return mediaFile;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	 /* Request updates at startup */
	  @Override
	  protected void onResume() {
	    super.onResume();
	    
		locationManager.requestLocationUpdates(provider, 400, 1, this);
	  }

	  /* Remove the location listener updates when Activity is paused */
	  @Override
	  protected void onPause() {
	    super.onPause();
	    locationManager.removeUpdates(this);
	  }


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		   lat = (double) (location.getLatitude());
		     lng = (double) (location.getLongitude());
		    lattitudeField.setText(String.valueOf(lat));
		    longitudeField.setText(String.valueOf(lng));
		    
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	    Toast.makeText(this, "Enabled new provider " + provider,
	            Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	public void showCoordinates(View v)
	{

		 
		
	}


	public static Camera getCameraInstance() {
		// TODO Auto-generated method stub
		  Camera c = null;
		    try {
		        c = Camera.open(); // attempt to get a Camera instance
		    }
		    catch (Exception e){
		        // Camera is not available (in use or does not exist)
		    	Log.i("Camera","null");
		    }
		    return c; // returns null if camera is unavailable
		
	}
	
	
	


}
