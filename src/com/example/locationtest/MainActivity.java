package com.example.locationtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;



public class MainActivity extends Activity implements LocationListener{
	
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int MEDIA_TYPE_IMAGE = 1;
	private static final String HOST_ADDRESS = "131.193.40.41";
	private static final String LOCALHOST_ADDRESS = "http://highwaypolice.herokuapp.com/photoexifdatas"; //"http://10.0.2.2:3000/photoexifdatas";
	private static final String JSON_URL_LOCAL = "http://highwaypolice.herokuapp.com/coordinates.json";
	private static final String EXIF_URL = "http://highwaypolice.herokuapp.com/photoexifdatas.json";
	LocationManager locationManager;
	Context mContext;
	static Context mContext2;

	private TextView lattitudeField;
	private TextView longitudeField;
	public Location location;
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
	static int frequency;
	public static int photoCount;
	private File pictureFile;
	SFTPConnection l;
	RailsConnectionTask r;
	PhotoCountGetTask pc;
	public boolean computeBit = false;
	public static int imageCount = 0;
	public ArrayList<PhotoData> photoDatas = new ArrayList<PhotoData>();
	public static int imageSelected;
	
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
		
		SFTPConnection s = new SFTPConnection();
		j = new JsonUrlTask();
		j.execute();
		
		pc = new PhotoCountGetTask();
		pc.execute();
		//end test
		//SFTP Test and Rails connection
		/* PLEASE REMOVE THIS!! */
//		
//		l = new SFTPConnection();
//		l.execute();
//		
//		r = new RailsConnectionTask();
//		r.execute();
		
		
		
		/* PLEASE REMOVE THIS!! */
		
		
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 
                14000, 
                1,
                this
        );
	

	 	mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
       
        mPicture = new PictureCallback() {

	         public void onPictureTaken(byte[] data, Camera camera) {

	             pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
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
	                 
	                 //l = new SFTPConnection();
	                 //l.execute();
	                 
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

	public void createLog(String log) {
		Log.i("log",log);
		
	}

	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
		
		
	    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp/"+photoCount);
	    /// This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            
	            return null;
	        }
	        else
	        {
	        	
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	    	
	    	Log.i("photocountnew","is"+photoCount);
	    	
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator + imageCount +".jpg");
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
	    if (mCamera == null){
	        mCamera = getCameraInstance();}
//	    
//		locationManager.requestLocationUpdates(provider, 400, 1, this);
//		makeToast("registered");
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
	
		//photo upload/not upload
		mPicture = new PictureCallback() {

	         public void onPictureTaken(byte[] data, Camera camera) {

	             pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
	             Log.i("Entered","callback");
	             if (pictureFile == null){
	            	 Log.i("null","image");
	 	                 
	            	 return;
	             }
//             r = new RailsConnectionTask();
//             r.execute(location.getLatitude(),location.getLongitude());

	             try {
	                 FileOutputStream fos = new FileOutputStream(pictureFile);
	                 fos.write(data);
	                 fos.close();
	                 MediaStore.Images.Media.insertImage(getContentResolver(), pictureFile.getAbsolutePath(), pictureFile.getName(), pictureFile.getName());
	                makeToast("photo saved");
	                 mCamera.startPreview();
	                 //SFTP upload to web server starts..
	      
	                 
	                 //Post to rails database
	               
	                 
	             } catch (FileNotFoundException e) {

	             } catch (IOException e) {

	             }
	         }
	        };
	
	        Toast.makeText(this, "onLocationChanged",
		            Toast.LENGTH_SHORT).show();
	        Log.i("in","lochchanged");
	  
 
            // getting GPS status
           
            
		   	lat = (double) (location.getLatitude());
		    lng = (double) (location.getLongitude());
		    lattitudeField.setText(String.valueOf(lat));
		    longitudeField.setText(String.valueOf(lng));
		   
		  PhotoData pd = getDistance(lat, lng);
		    if(pd.isClose)
		   {
			  if(!computeBit)
			  {
				  photoCount++;
			  }
			   computeBit = true;
			  photoDatas.add(pd);
			   makeToast("Taking photo...");
			   mCamera.takePicture(null, null, mPicture); 
		   }
		   else if(computeBit)
		   {
			   imageCount = 0;
			   computeBit = false;
			   Collections.sort(photoDatas, new DistanceComparator());
	
				 for(int k = 0; k < photoDatas.size(); k++){
				
						System.out.println(photoDatas.get(k).imageCount);
					}
				 
				 PhotoData selected;
			if(photoDatas.size() == 1)
			{
				imageSelected = photoDatas.get(0).imageCount;
				selected = photoDatas.get(0);
			}
			else if((photoDatas.get(0).imageCount)>(photoDatas.get(1).imageCount))
				 {
					 imageSelected = photoDatas.get(1).imageCount;
					 selected = photoDatas.get(1);
				 }
				 else
				 {
					 imageSelected = photoDatas.get(0).imageCount;
					 selected = photoDatas.get(0);
				 }
				 File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp/"+photoCount);
				 File oldFile = new File(mediaStorageDir.getPath() + File.separator + imageSelected +".jpg");
			 File newFile =new File(Environment.getExternalStorageDirectory(), "MyCameraApp/"+photoCount + ".jpg");
			 makeToast(oldFile.getPath());
			 makeToast(newFile.getPath());
				 oldFile.renameTo(newFile);
		           SFTPConnection sp = new SFTPConnection();
                  sp.execute(selected.lattitude,selected.longitude);
			   photoDatas.clear();
		   }
		   else
		   {
			   imageCount = 0;
		   }
		   
		   
		
	
		    		
		    	
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
	
	

	public class SFTPConnection extends AsyncTask<Double,Void,Void>
	{

		Double pLattitude,pLongitude;
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			makeToast("Photo uploaded to server");
			RailsConnectionTask r2 = new RailsConnectionTask();
			r2.execute(pLattitude,pLongitude);
			
		}

		@Override
		protected Void doInBackground(Double... params) {
			// TODO Auto-generated method stub
			
			pLattitude = params[0];
			pLongitude = params[1];
			boolean conStatus = false;
			Session session = null;
		    Channel channel = null;
		    java.util.Properties config = new java.util.Properties(); 
		    config.put("StrictHostKeyChecking", "no");
		  
		    Log.i("Session","is"+conStatus);
		    try {
		    	JSch ssh = new JSch();
				session = ssh.getSession("pmody", HOST_ADDRESS, 22);
				session.setPassword("667758482");
				session.setConfig(config);
				session.connect();
				 conStatus = session.isConnected();
				Log.i("Session","is"+conStatus);
		        channel = session.openChannel("sftp");
		        channel.connect();
		        ChannelSftp sftp = (ChannelSftp) channel;
		        Vector filelist = sftp.ls("/web/grad3/pmody");
	            for(int i=0; i<filelist.size();i++){
	                System.out.println(filelist.get(i).toString());
	            }
	          
		        sftp.put("/sdcard/MyCameraApp/"+photoCount+".jpg", "/web/grad3/pmody/WWW/HighwayPolice/");
		        
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("Session","is"+conStatus);
			} catch (SftpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("Session","is"+conStatus);
			}
			return null;
		}
		
	}
	
	public class RailsConnectionTask extends AsyncTask<Double,Void,Void>
	{

		@Override
		protected Void doInBackground(Double... arguments) {
			// TODO Auto-generated method stub

	        HttpClient client = new DefaultHttpClient();
	      
	        HttpPost postRequest = new HttpPost(LOCALHOST_ADDRESS);
	    

	         try {
	              
	                                
//	                                 for(int i = 0; i<array.length(); i++)
//	                                 {
//	                                	 JSONObject placeObject = array.getJSONObject(i);
//	                                 }
	                                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	                                 nameValuePairs.add(new BasicNameValuePair("lattitude", arguments[0].toString()));
	                                 nameValuePairs.add(new BasicNameValuePair("longitude", arguments[1].toString()));
	                                 postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//
//	                                 // Execute HTTP Post Request
                                HttpResponse responsePost = client.execute(postRequest);
	                                 
                    
	                                        
	         	}
	         

	         catch (IOException e) {
	                            // TODO Auto-generated catch block
	                            e.printStackTrace();
	                       } 
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			makeToast("Successfully contacted rails server");
			
		
			
		}
		
	}
	
	public class JsonUrlTask extends AsyncTask<Void,Void,Void> 
	{

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
		
			 
			    
	       //createLog(con.getReplyString()); 
			  	HttpClient client = new DefaultHttpClient();
				HttpGet getRequest = new HttpGet(JSON_URL_LOCAL);
				try {
					org.apache.http.HttpResponse response =  client.execute(getRequest);
				  StatusLine statusline = response.getStatusLine();
					int statusCode = statusline.getStatusCode();
					  Log.i("in","JsonUrlTask");
					InputStream jsonStream = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(jsonStream));
					StringBuilder builder = new StringBuilder();
					String line;
					while((line = reader.readLine())!= null)
					{
						builder.append(line);
					}
					
				
					
					
					String jsonData = builder.toString();
					JSONArray array = new JSONArray(jsonData);
					
					placesArrayList.clear();
					Log.i("length","is"+array.length());
					
					for(int i = 0; i<array.length(); i++)
					{
						p = new Place();
						JSONObject place = array.getJSONObject(i);
						
						double longitude = place.getDouble("longitude");
						double lattitude = place.getDouble("lattitude");
						 
						p.lattitude = lattitude;
						p.longitude = longitude;
						p.name = place.getString("place");
						p.frequency = place.getInt("frequency");
						placesArrayList.add(p);
					
						
						
					}
					 for(int k = 0; k < placesArrayList.size(); k++){
						 System.out.println(k);
							System.out.println(placesArrayList.get(k).lattitude + " " + placesArrayList.get(k).frequency);
						}
					
				
					Log.i("size","is"+placesArrayList.size());
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					   Log.i("in","JsonUrlTask"+e);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					   Log.i("in","JsonUrlTask");
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					   Log.i("in","JsonUrlTask");
					e.printStackTrace();
				}
			return null;
		}
		
	}
	
	public class PhotoCountGetTask extends AsyncTask<Void,Void,Void>
	{
		ProgressDialog dialog;
		@Override
		protected void onPostExecute(Void result)
		{
			makeToast("Done and Total"+photoCount);
			
			
		}
		
		@Override
		protected void onPreExecute()
		{
			makeToast("Processing..");

		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			
			  HttpClient client = new DefaultHttpClient();
		      HttpGet getRequest = new HttpGet(EXIF_URL);
		      
			try {
				org.apache.http.HttpResponse response = client.execute(getRequest);
				StatusLine statusline = response.getStatusLine();

	              int statusCode = statusline.getStatusCode();
	                                 if(statusCode!=200)
	                                 {
	                                      return null;
	                                 }
	                                 InputStream jsonStream = response.getEntity().getContent();
	                                 BufferedReader reader = new BufferedReader(new InputStreamReader(jsonStream));
	                                 StringBuilder builder = new StringBuilder();
	                                 String line;
	                                 while((line = reader.readLine())!= null)
	                                 {
	                                      builder.append(line);
	                                 }
	                                 String jsonData = builder.toString();

	                                 JSONArray array = new JSONArray(jsonData);
	                                 photoCount = array.length();
	                                 Log.i("photo","count"+photoCount);
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
	
	public PhotoData getDistance(double lt1, double lg1)
	{
		imageCount++;
		double lat1 = lt1;
		double lng1 = lg1;
		boolean photo = false;
		PhotoData pd = new PhotoData();
		for(int i = 0; i < placesArrayList.size(); i++)
		{
			double lat2 = placesArrayList.get(i).lattitude;
			double lng2 = placesArrayList.get(i).longitude;
			double R = 6371; // km
			double dLat = (lat2-lat1)* (Math.PI / 180);
			double dLon = (lng2-lng1)* (Math.PI / 180);
			lat1 = lat1* (Math.PI / 180);
			lat2 = lat2* (Math.PI / 180);
			double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
		
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
			
			double d = R*c*1000;
			Log.i("distance","is "+d);
		//	makeToast("dist"+d);
			if(d<75)
			{
				photo = true;
				closestPlaceName = placesArrayList.get(i).name;
				frequency = placesArrayList.get(i).frequency;
				pd.distance = d;
				pd.isClose = true;
				pd.imageCount = imageCount;
				Log.i("closest place",closestPlaceName);
				makeToast(closestPlaceName+" "+d);
				
				break;
			}
			else
			{
				pd.isClose = false;
			}
			pd.lattitude = lt1;
			pd.longitude = lg1;
			
		
			
			 lat1 = lat1* (180 / Math.PI);//Reset lat1
		}
		return pd;
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
