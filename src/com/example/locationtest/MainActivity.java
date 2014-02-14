package com.example.locationtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
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
	ArrayList<Place> placesArrayList ;
	public int olcCount = 0;
	boolean takePicture = false;
	String feedURL = "http://www.cs.uic.edu/~pmody/generated.json";
	JsonUrlTask j;
	Place p;
	static String closestPlaceName;
	
	private void makeToast(String result) {
		// TODO Auto-generated method stub
		Toast.makeText(this,result, Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 lattitudeField = (TextView) findViewById(R.id.textView1);
		 longitudeField = (TextView) findViewById(R.id.textView2);
		  captureButton = (Button) findViewById(R.id.captureButton);
		  placesArrayList = new ArrayList<Place>();
		  mImageView = (ImageView) findViewById(R.id.imageView1);
		 // Get the location manager
		  //testing json
		
			j = new JsonUrlTask();
			j.execute();
		  //end test
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
	        "IMG_"+ timeStamp + closestPlaceName + ".jpg");
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
	
	        Toast.makeText(this, "onLocationChanged",
		            Toast.LENGTH_SHORT).show();
		   	lat = (double) (location.getLatitude());
		     lng = (double) (location.getLongitude());
		    lattitudeField.setText(String.valueOf(lat));
		    longitudeField.setText(String.valueOf(lng));
		    for(int i = 0; i < placesArrayList.size(); i++){
				System.out.println(placesArrayList.get(i).lattitude + " " + placesArrayList.get(i).longitude);
			}
		    GPSFence g = new GPSFence();
		   g.execute();
		   
		
	
		    		
		    	
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

	public class JsonUrlTask extends AsyncTask<Void,Void,Void> 
	{

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			  HttpClient client = new DefaultHttpClient();
				HttpGet getRequest = new HttpGet(feedURL);
				try {
					org.apache.http.HttpResponse response =  client.execute(getRequest);
				  StatusLine statusline = response.getStatusLine();
					int statusCode = statusline.getStatusCode();
				
					InputStream jsonStream = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(jsonStream));
					StringBuilder builder = new StringBuilder();
					String line;
					while((line = reader.readLine())!= null)
					{
						builder.append(line);
					}
					
				
					
					
					String jsonData = builder.toString();
					JSONObject json = new JSONObject(jsonData);
					
					placesArrayList.clear();
					JSONArray results = json.getJSONArray("results");
					Log.i("resultsLength","is "+results.length());
					for(int i = 0; i<results.length(); i++)
					{
						p = new Place();
						JSONObject place = results.getJSONObject(i);
						double longitude = place.getDouble("longitude");
						double lattitude = place.getDouble("lattitude");
						 
						p.lattitude = lattitude;
						p.longitude = longitude;
						p.name = place.getString("name");
						placesArrayList.add(p);
						
//						Log.i("Long","is"+longitude);
//						Log.i("Lat","is"+lattitude);
						
						
					}
					
				
					Log.i("size","is"+placesArrayList.size());
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return null;
		}
		
	}

	public class GPSFence extends AsyncTask<Void,Void,Void>  {
	
	double currentLat;
	double currentLong;
	boolean photo = false;
	
	public boolean takePhoto(ArrayList<Place> placesArrayList)
	{
		
		Log.i("in","takePhoto");
		return photo;
		
	}
	
	public void grabLocation(double lat,double lng)
	{
		currentLat = lat;
		currentLong = lng;
	}
	
@Override
	protected Void doInBackground(Void... params) {
	Log.i("sizeCurrent","is"+placesArrayList.size());
	for(int i=0; i<placesArrayList.size(); i++)
	{
	// TODO Auto-generated method stub
	String distanceURL = "http://maps.googleapis.com/maps/api/directions/json?origin="+Double.toString(lat)+","+Double.toString(lng)+"&destination="+placesArrayList.get(i).lattitude+","+placesArrayList.get(i).longitude+"&sensor=false&mode=walking";

	Log.i("distURl",distanceURL);
	  HttpClient client = new DefaultHttpClient();
	  HttpGet getRequest = new HttpGet(distanceURL);
		try {
			org.apache.http.HttpResponse response =  client.execute(getRequest);
			StatusLine statusline = response.getStatusLine();
			int statusCode = statusline.getStatusCode();
		
			InputStream jsonStream = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(jsonStream));
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = reader.readLine())!= null)
			{
				builder.append(line);
			}
			
			String jsonData = builder.toString();
			JSONObject json = new JSONObject(jsonData);
			JSONArray routes = json.getJSONArray("routes");
			JSONObject direction = routes.getJSONObject(0);
			JSONArray legs = direction.getJSONArray("legs");	
			JSONObject details = legs.getJSONObject(0);
			JSONObject distance = details.getJSONObject("distance");
			double distanceBetween = distance.getDouble("value")*(0.62/1000);
			
			Log.i("Distance","is"+distanceBetween+" miles");
			
			if(distanceBetween < 0.4)
			{
				closestPlaceName = placesArrayList.get(i).name;
				photo = true;
				break;
			}

		
///				Log.i("Lat","is"+lattitude);
				
				
					
				
			
			
		

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
}
	return null;
}
	
	protected void onPostExecute(Void result)
	
	{
	super.onPostExecute(result);
	
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
       if(photo)
       {    	  makeToast("Taking photo...");
    	   mCamera.takePicture(null, null, mPicture);}
       
       Log.i("closest","is"+closestPlaceName);
	}

	
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
