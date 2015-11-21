package com.team6.busbuzz;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker gilmanMarker, regentMarker, arribaMarker, lebonMarker, nobelMarker, stop;

    //Parse Objects
    private ParseObject stopObj;

    //Buttons
    private Button emptyButton, spaciousButton, fullButton, busLeftButton;

    //ImageView
    private ImageView dImage, fImage, sImage, eImage;

    //TextViews
    public TextView voteText, leftText;

    //Store last report time for each stop
    public String gilmanReport, regentReport, arribaReport, lebonReport, nobelReport;

    public void whenButtonClicked(String status)
    {
        //reset button highlight
        emptyButton.setBackgroundColor(Color.WHITE);
        emptyButton.setTextColor(Color.BLACK);
        spaciousButton.setBackgroundColor(Color.WHITE);
        spaciousButton.setTextColor(Color.BLACK);
        fullButton.setBackgroundColor(Color.WHITE);
        fullButton.setTextColor(Color.BLACK);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        String reportTime = df.format(c.getTime());

        stopObj.put("Routes", "UCSD_Shuttle");
        stopObj.put("Status", status);
        stopObj.put("indicateVote", 1);
        stopObj.put("isValid", 1);
        stopObj.put("voteTime", reportTime);

        if (stop.equals(gilmanMarker)) {
            stopObj.put("Stops", "Gilman");
        } else if (stop.equals(regentMarker)) {
            stopObj.put("Stops", "Regent");
        } else if (stop.equals(arribaMarker)) {
            stopObj.put("Stops", "Arriba");
        } else if (stop.equals(lebonMarker)) {
            stopObj.put("Stops", "Lebon");
        } else if (stop.equals(nobelMarker)) {
            stopObj.put("Stops", "Nobel");
        }

        stopObj.saveInBackground();

        if (stop.equals(gilmanMarker)) {
            getDataFromQuery("Gilman");
        } else if (stop.equals(regentMarker)) {
            getDataFromQuery("Regent");
        } else if (stop.equals(arribaMarker)) {
            getDataFromQuery("Arriba");
        } else if (stop.equals(lebonMarker)) {
            getDataFromQuery("Lebon");
        } else if (stop.equals(nobelMarker)) {
            getDataFromQuery("Nobel");
        }
    }

    public void setImage(String status) {
        if(status == "full") {
            setImageInvisible();
            fImage.setVisibility(View.VISIBLE);
        }
        else if(status == "spacious") {
            setImageInvisible();
            sImage.setVisibility(View.VISIBLE);
        }
        else if (status == "empty") {
            setImageInvisible();
            eImage.setVisibility(View.VISIBLE);
        }
        else {
            setImageInvisible();
            dImage.setVisibility(View.VISIBLE);
        }
    }
    //set Image invisible
    public void setImageInvisible() {
        eImage.setVisibility(View.INVISIBLE);
        sImage.setVisibility(View.INVISIBLE);
        fImage.setVisibility(View.INVISIBLE);
        dImage.setVisibility(View.INVISIBLE);
    }

    public void showUI(){
        //set UI visible
        findViewById(R.id.imageView2).setVisibility(View.VISIBLE);
        findViewById(R.id.leftTime).setVisibility(View.VISIBLE);
        findViewById(R.id.NumReport_Text).setVisibility(View.VISIBLE);
        findViewById(R.id.LastBus_Text).setVisibility(View.VISIBLE);
        findViewById(R.id.reportCount).setVisibility(View.VISIBLE);
        findViewById(R.id.textView8).setVisibility(View.VISIBLE);
        emptyButton.setVisibility(View.VISIBLE);
        spaciousButton.setVisibility(View.VISIBLE);
        fullButton.setVisibility(View.VISIBLE);
        busLeftButton.setVisibility(View.VISIBLE);

        //reset button highlight
        emptyButton.setBackgroundColor(Color.WHITE);
        emptyButton.setTextColor(Color.BLACK);
        spaciousButton.setBackgroundColor(Color.WHITE);
        spaciousButton.setTextColor(Color.BLACK);
        fullButton.setBackgroundColor(Color.WHITE);
        fullButton.setTextColor(Color.BLACK);
    }

    public void hideUI(){
        //set textView invisible
        findViewById(R.id.leftTime).setVisibility(View.INVISIBLE);
        findViewById(R.id.busStop).setVisibility(View.INVISIBLE);
        findViewById(R.id.NumReport_Text).setVisibility(View.INVISIBLE);
        findViewById(R.id.LastBus_Text).setVisibility(View.INVISIBLE);
        findViewById(R.id.reportCount).setVisibility(View.INVISIBLE);
        findViewById(R.id.textView8).setVisibility(View.INVISIBLE);
        findViewById(R.id.imageView2).setVisibility(View.INVISIBLE);
        //set buttons invisible
        emptyButton.setVisibility(View.INVISIBLE);
        spaciousButton.setVisibility(View.INVISIBLE);
        fullButton.setVisibility(View.INVISIBLE);
        busLeftButton.setVisibility(View.INVISIBLE);

        setImageInvisible();
    }

    public void getDataFromQuery(final String stopName){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Bus");
        query.whereExists("objectId");
        query.orderByDescending("updatedAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> statusList, com.parse.ParseException e) {
                if (e == null) {
                    checkRefresh(statusList, stopName);
                    String status = median(statusList, stopName);
                    String numPeople = numberofPeople(statusList, stopName);
                    String lastBus = lastTime(statusList, stopName);

                    if(stopName.equals("Gilman")) {
                        gilmanMarker.setSnippet("Status: " + status);
                    }else if(stopName.equals("Regent")){
                        regentMarker.setSnippet("Status: " + status);
                    }else if(stopName.equals("Arriba")){
                        arribaMarker.setSnippet("Status: " + status);
                    }else if(stopName.equals("Nobel")){
                        nobelMarker.setSnippet("Status: " + status);
                    }else if(stopName.equals("Lebon")){
                        lebonMarker.setSnippet("Status: " + status);
                    }

                    setImage(status);
                    leftText.setText(lastBus);
                    voteText.setText(numPeople);
                } else {
                }
            }
        });
    }

    //This function calculate median value and return "string"
    public String median(List<ParseObject> data, String stopName) {
        double sum = 0;
        String result = "";
        double count = 0;

        for(int i = 0; i < data.size(); i++) {
           if(data.get(i).getInt("isValid") == 1 && data.get(i).getInt("indicateVote") == 1 && data.get(i).getString("Stops").equals(stopName)) {
               //If people only click last bus left, then return value 1.
               count++;
               if (data.get(i).getString("Status").equals("spacious")) {
                   sum += 1;
               } else if (data.get(i).getString("Status").equals("full")) {
                   sum += 2;
               }
           }
        }
        sum = sum/count;

        if(sum < 0.5) {
            result = "empty";
        }
        else if (sum <= 1.5 && sum >= 0.5) {
            result = "spacious";
        }
        else if(sum > 1.5) {
            result = "full";
        }
        else {
            //result = Double.toString(sum);
            result = "Not Available";
        }
        return result;
    }

    //This function return last time as a "string"
    public void checkRefresh(List<ParseObject> data, String stopName) {
        //Take current time
        Date storedTime = null;
        Date currentTime = null;
        String storedTimeString = "";
        long diff = 0;

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        String timeClick = df.format(c.getTime());

        for(int i = 0; i < data.size(); i++) {
            if (data.get(i).getInt("isValid") == 1 && data.get(i).getInt("indicateVote") == 1 && data.get(i).getString("Stops").equals(stopName)) {
                storedTimeString = data.get(i).getString("voteTime");
                try {
                    currentTime = df.parse(timeClick); // always same
                    storedTime = df.parse(storedTimeString);
                    diff = currentTime.getTime() - storedTime.getTime();
                    diff = diff / (60*1000)%60; // change to "min"
                }
                catch (Exception e) {}

                if( diff >= 15 ) {
                    //refresh !!
                    data.get(i).put("isValid", 0);
                    data.get(i).saveInBackground();
                }
            }
        }
    }

    //This function return last time as a "string"
    public String lastTime(List<ParseObject> data, String stopName) {
        //int integer = 0;
        for(int i = 0; i < data.size(); i++) {
            //integer = i;
            if (data.get(i).getInt("indicateTime") == 1 && data.get(i).getString("StopsforTime").equals(stopName)) {
                return data.get(i).getString("LastTime");
            }
        }
        return "Not Available";
    }

    //This function return number of people who voted at that time.
    public String numberofPeople(List<ParseObject> data, String stopName) {
        int numPeople = 0;

        for(int i = 0; i < data.size(); i++) {
            if(data.get(i).getInt("isValid") == 1 && data.get(i).getInt("indicateVote") == 1 && data.get(i).getString("Stops").equals(stopName)) {
                numPeople++;
            }
        }
        return Integer.toString(numPeople);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "jEzU7yAVpJXiqxwXLg3WSLTT274HJM8hoWIQxNfc", "5eAjXOb630iaYXcOpmDqLBqrh0q7i6WrLhgck0LJ");

        stopObj = new ParseObject("Bus");
        stopObj.put("indicateVote", 0);
        stopObj.put("indicateTime", 0);
        emptyButton =  (Button)findViewById(R.id.button);
        spaciousButton =  (Button)findViewById(R.id.button2);
        fullButton =  (Button)findViewById(R.id.button3);
        busLeftButton = (Button)findViewById(R.id.button4);

        //Initialize Images
        dImage = (ImageView)findViewById(R.id.imageView2);
        fImage = (ImageView)findViewById(R.id.imageView6);
        sImage = (ImageView)findViewById(R.id.imageView5);
        eImage = (ImageView)findViewById(R.id.imageView4);

        voteText = (TextView)findViewById(R.id.reportCount);
        leftText = (TextView)findViewById(R.id.leftTime);

        emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whenButtonClicked("empty");
                emptyButton.setBackgroundColor(Color.BLACK);
                emptyButton.setTextColor(Color.WHITE);
            }
        });

        spaciousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whenButtonClicked("spacious");
                spaciousButton.setBackgroundColor(Color.BLACK);
                spaciousButton.setTextColor(Color.WHITE);
            }
        });

        fullButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whenButtonClicked("full");
                fullButton.setBackgroundColor(Color.BLACK);
                fullButton.setTextColor(Color.WHITE);
            }
        });

        busLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                String lastBus = df.format(c.getTime());
                if (stop.equals(gilmanMarker)) {
                    stopObj.put("Routes", "UCSD_Shuttle");
                    stopObj.put("StopsforTime", "Gilman");
                    stopObj.put("LastTime", lastBus);
                    stopObj.put("isValid", 1);
                    stopObj.put("indicateTime", 1);
                } else if (stop.equals(regentMarker)) {
                    stopObj.put("Routes", "UCSD_Shuttle");
                    stopObj.put("StopsforTime", "Regent");
                    stopObj.put("LastTime", lastBus);
                    stopObj.put("isValid", 1);
                    stopObj.put("indicateTime", 1);
                } else if (stop.equals(arribaMarker)) {
                    stopObj.put("Routes", "UCSD_Shuttle");
                    stopObj.put("StopsforTime", "Arriba");
                    stopObj.put("LastTime", lastBus);
                    stopObj.put("isValid", 1);
                    stopObj.put("indicateTime", 1);
                } else if (stop.equals(lebonMarker)) {
                    stopObj.put("Routes", "UCSD_Shuttle");
                    stopObj.put("StopsforTime", "Lebon");
                    stopObj.put("LastTime", lastBus);
                    stopObj.put("isValid", 1);
                    stopObj.put("indicateTime", 1);
                } else if (stop.equals(nobelMarker)) {
                    stopObj.put("Routes", "UCSD_Shuttle");
                    stopObj.put("StopsforTime", "Nobel");
                    stopObj.put("LastTime", lastBus);
                    stopObj.put("isValid", 1);
                    stopObj.put("indicateTime", 1);
                }
                stopObj.saveInBackground();

                if (stop.equals(gilmanMarker)) {
                    getDataFromQuery("Gilman");
                } else if (stop.equals(regentMarker)) {
                    getDataFromQuery("Regent");
                } else if (stop.equals(arribaMarker)) {
                    getDataFromQuery("Arriba");
                } else if (stop.equals(lebonMarker)) {
                    getDataFromQuery("Lebon");
                } else if (stop.equals(nobelMarker)) {
                    getDataFromQuery("Nobel");
                }
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        //User permission check for accessing last known positon
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        Location location = locationManager.getLastKnownLocation(provider);

        //Initial Map Camera Location
        LatLng UCSD = new LatLng(32.879228, -117.228738);

        //Bus Stops
        LatLng Gilman = new LatLng(32.877196, -117.235784);
        LatLng Nobel_Regent = new LatLng(32.867459, -117.219267);
        LatLng Arriba_Regent = new LatLng(32.861505, -117.223343);
        LatLng Lebon_Palmila = new LatLng(32.865053, -117.225050);
        LatLng Nobel_Lebon = new LatLng(32.868551, -117.225306);

        //Add bus pin
        gilmanMarker = mMap.addMarker(new MarkerOptions().position(Gilman).title("Gilman Dr & Myer's")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
        regentMarker = mMap.addMarker(new MarkerOptions().position(Nobel_Regent).title("Regent Rd & Nobel Dr")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
        arribaMarker = mMap.addMarker(new MarkerOptions().position(Arriba_Regent).title("Arriba St & Regent Rd")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
        lebonMarker  = mMap.addMarker(new MarkerOptions().position(Lebon_Palmila).title("Lebon Dr & Palmila Dr")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
        nobelMarker  = mMap.addMarker(new MarkerOptions().position(Nobel_Lebon).title("Nobel Dr & Lebon Dr")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));

        //Camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(UCSD));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //hide UI when map is clicked
                hideUI();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //show UI when marker is clicked
                showUI();

                if (marker.equals(gilmanMarker)) {
                    findViewById(R.id.busStop).setVisibility(View.VISIBLE);
                    TextView text = (TextView) findViewById(R.id.busStop);
                    text.setText("Gilman Dr & Myer's");
                    stop = gilmanMarker;
                    getDataFromQuery("Gilman");
                } else if (marker.equals(regentMarker)) {
                    findViewById(R.id.busStop).setVisibility(View.VISIBLE);
                    TextView text = (TextView) findViewById(R.id.busStop);
                    text.setText("Regents Rd & Nobel Dr");
                    stop = regentMarker;
                    getDataFromQuery("Regent");
                } else if (marker.equals(arribaMarker)) {
                    findViewById(R.id.busStop).setVisibility(View.VISIBLE);
                    TextView text = (TextView) findViewById(R.id.busStop);
                    text.setText("Arriba St & Regent Rd");
                    stop = arribaMarker;
                    getDataFromQuery("Arriba");
                } else if (marker.equals(lebonMarker)) {
                    findViewById(R.id.busStop).setVisibility(View.VISIBLE);
                    TextView text = (TextView) findViewById(R.id.busStop);
                    text.setText("Lebon Dr & Palmila Dr");
                    stop = lebonMarker;
                    getDataFromQuery("Lebon");
                } else if (marker.equals(nobelMarker)) {
                    findViewById(R.id.busStop).setVisibility(View.VISIBLE);
                    TextView text = (TextView) findViewById(R.id.busStop);
                    text.setText("Nobel Dr & Lebon Dr");
                    stop = nobelMarker;
                    getDataFromQuery("Nobel");
                }
                return false;
            }
        });
    }
}

