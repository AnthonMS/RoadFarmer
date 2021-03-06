package dk.roadfarmer.roadfarmer.ViewActivities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dk.roadfarmer.roadfarmer.Controllers.SortingController;
import dk.roadfarmer.roadfarmer.Models.User;
import dk.roadfarmer.roadfarmer.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        NavigationView.OnNavigationItemSelectedListener
{
    private static final String TAG = "MapsActivity";
    private DrawerLayout mDrawerLayout;
    private final Context context = this;
    private int numberOfCreatedLocations;

    // Google Maps stuff
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    public static final int REQUEST_LOCATION_CODE = 99;

    // Buttons and stuff from app_bar class
    private ImageButton burgerMenuBtn;
    private Spinner spinnerHelp, spinnerLang;
    private ArrayAdapter<CharSequence> adapterHelp;
    private ArrayAdapter<CharSequence> adapterLang;
    private TextView textViewTitleBar;
    private ImageButton filterBtn;
    // Close button from NavigationBar
    private ImageButton closeNavBtn;
    private NavigationView navigationView;
    private String chosenLanguage;

    // Firebase stuff
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_maps);

        //Intent intent = getIntent();
        //chosenLanguage = intent.getExtras().getString("selectedLanguage");
        SharedPreferences sharedPref = getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
        chosenLanguage = sharedPref.getString("currentLanguage", "");
        //toastMessage(testString);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRootRef = mFirebaseDatabase.getReference();

        if (firebaseAuth.getCurrentUser() != null)
        {
            myRootRef.child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("UserInfo").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    numberOfCreatedLocations = user.getNumberOfCreatedLocations();
                    SharedPreferences sharedPref = getSharedPreferences("numberOfLocations", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("numberOfCreatedLocations", numberOfCreatedLocations).apply();
                    //toastMessage(numberOfCreatedLocations + "");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else
        {
            //toastMessage("current user == null");
        }


        // Everything here is from app_bar class -----------------
        burgerMenuBtn = (ImageButton) findViewById(R.id.burgerMenuBtn);
        burgerMenuBtn.setOnClickListener(buttonClickListener);
        textViewTitleBar = (TextView) findViewById(R.id.textView_titleBar);
        filterBtn = (ImageButton) findViewById(R.id.imageBtn_filter);
        filterBtn.setOnClickListener(buttonClickListener);
        filterBtn.setImageResource(R.drawable.filter_one);

        spinnerHelp = (Spinner) findViewById(R.id.spinner_helpDropDown);
        adapterHelp = ArrayAdapter.createFromResource(this, R.array.listSettingSelection, android.R.layout.simple_spinner_item);
        adapterHelp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHelp.setAdapter(adapterHelp);
        spinnerHelp.setOnItemSelectedListener(dropDownListener);

        spinnerLang = (Spinner) findViewById(R.id.spinner_languageSelect);
        adapterLang = ArrayAdapter.createFromResource(this, R.array.listLanguages, android.R.layout.simple_spinner_item);
        adapterLang.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLang.setAdapter(adapterLang);
        spinnerLang.setOnItemSelectedListener(dropDownListener2);
        // Setting the title of this specific page.
        textViewTitleBar.setText(getString(R.string.title_activity_maps));
        // Setting the selected language in the spinner if user selected on himself
        if (!TextUtils.isEmpty(chosenLanguage))
        {
            int pos = adapterLang.getPosition(chosenLanguage);
            spinnerLang.setSelection(pos);
        }
        else
        {

        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.maps_drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.maps_navView);
        navigationView.setNavigationItemSelectedListener(this);
        //navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setCheckedItem(R.id.nav_kort2);

        //navigationView.getMenu().add(R.id.nav_userGroup, Menu.NONE, 0, "Change created location").setIcon(R.drawable.change_icon);

        closeNavBtn = (ImageButton) findViewById(R.id.closeNavBar);
        closeNavBtn.setOnClickListener(buttonClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null)
        {
            //navigationView.getMenu().clear();
            //navigationView.inflateMenu(R.menu.navigation_menu_signedin);
            //toastMessage("SIGNED IN!");
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.navigation_menu_two);
            //navigationView.getMenu().getItem(0).setChecked(true);
            navigationView.setCheckedItem(R.id.nav_kort2);
        }
        else
        {
            //toastMessage("NOT SIGNED IN!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // Permission granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (client == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else // permission is denied
                {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch(view.getId())
            {
                case R.id.burgerMenuBtn:
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                    break;
                case R.id.closeNavBar:
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    break;
                case R.id.imageBtn_filter:
                    //toastMessage("Trying to open filter");
                    showAddItemDialog("Title");
                    break;
            }
        }
    };


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
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        //mMap.setMapType(GoogleMap.);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient()
    {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        //toastMessage(getCompleteAddress(lastLocation.getLatitude(), lastLocation.getLongitude()));

        // This method gets the address from latLng and saves it in sharedPrefs for later usage.
        saveLastLocation(lastLocation.getLatitude(), lastLocation.getLongitude());

        //editor.putFloat("lastLat", i1).apply();
        //editor.putFloat("lastLong", i2).apply();

        if (currentLocationMarker != null)
        {
            currentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentLocationMarker = mMap.addMarker(markerOptions);


        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomBy(1));

        if (client != null)
        {
            // if null = no location value
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    private void saveLastLocation(double LAT, double LONG)
    {
        List<Address> addresses = new ArrayList<>();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(LAT, LONG, 1);
        } catch (Exception e)
        {
            Log.d(TAG, "saveLastLocation: " + e.toString());
            //toastMessage(e.toString());
        }

        String address = addresses.get(0).getAddressLine(0);
        String city = addresses.get(0).getLocality();
        String zip = addresses.get(0).getPostalCode();
        //toastMessage(address + " " + city + " " + zip);

        SharedPreferences sharedPref = getSharedPreferences("savedLocation", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("lastKnownAddress", address).apply();
        editor.putString("lastKnownCity", city).apply();
        editor.putString("lastKnownZip", zip).apply();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    public boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    Spinner spinnerOverall;
    Spinner spinnerSpecific;
    private void showAddItemDialog(String title)
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog_add_item);
        dialog.setTitle(title);
        dialog.setCanceledOnTouchOutside(true);

        // Custom dialog components
        TextView titleView = (TextView) dialog.findViewById(R.id.dialog_titleView3);
        titleView.setText(title);

        spinnerOverall = (Spinner) dialog.findViewById(R.id.spinner_chooseOverallCategory);
        spinnerSpecific = (Spinner) dialog.findViewById(R.id.spinner_chooseSpecificCategory);
        spinnerOverall.setOnItemSelectedListener(overallListener);
        //spinnerSpecific.setOnItemSelectedListener(fruitListener);

        Button dialog_okBtn = (Button) dialog.findViewById(R.id.dialog_okBtn);
        dialog_okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //toastMessage(getChosenCategory());

                idkWhatToCallThis();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void idkWhatToCallThis()
    {
        switch (spinnerOverall.getSelectedItemPosition())
        {
            case 1: // Berries
                //toastMessage("You chose berries");
                //getSpecificBerries();
                int i = spinnerSpecific.getSelectedItemPosition();
                switch (i)
                {
                    case 0: // nothing specific
                        SortingController sortingController = new SortingController(context, mMap, lastLocation, "Berries", 0);
                        break;
                    case 1: // cherries
                        sortingController = new SortingController(context, mMap, lastLocation, "Cherries", 1);
                        break;
                    case 2: // blue
                        sortingController = new SortingController(context, mMap, lastLocation, "Blueberries", 1);
                        break;
                    case 3: // rasp
                        sortingController = new SortingController(context, mMap, lastLocation, "Raspberries", 1);
                        break;
                    case 4: // straw
                        sortingController = new SortingController(context, mMap, lastLocation, "Strawberries", 1);
                        break;
                    case 5: // other
                        sortingController = new SortingController(context, mMap, lastLocation, "OtherBerries", 1);
                        break;
                }
                break;
            case 2: // Fruits
                //toastMessage("You chose fruits");
                i = spinnerSpecific.getSelectedItemPosition();
                switch (i)
                {
                    case 0: // nothing specific selected
                        SortingController sortingController = new SortingController(context, mMap, lastLocation, "Fruits", 0);
                        break;
                    case 1: // apples
                        sortingController = new SortingController(context, mMap, lastLocation, "Apples", 1);
                        break;
                    case 2: // pears
                        sortingController = new SortingController(context, mMap, lastLocation, "Pears", 1);
                        break;
                    case 3: // plums
                        sortingController = new SortingController(context, mMap, lastLocation, "Plums", 1);
                        break;
                    case 4: // Oranges
                        sortingController = new SortingController(context, mMap, lastLocation, "Oranges", 1);
                        break;
                    case 5: // other
                        sortingController = new SortingController(context, mMap, lastLocation, "OtherFruits", 1);
                        break;
                }
                break;
            case 3: // Veggies
                i = spinnerSpecific.getSelectedItemPosition();
                switch (i)
                {
                    case 0: // nothing specific selected
                        SortingController sortingController = new SortingController(context, mMap, lastLocation, "Vegetables", 0);
                        break;
                    case 1: // apples
                        sortingController = new SortingController(context, mMap, lastLocation, "Peas", 1);
                        break;
                    case 2: // pears
                        sortingController = new SortingController(context, mMap, lastLocation, "Veggie 2", 1);
                        break;
                    case 3: // plums
                        sortingController = new SortingController(context, mMap, lastLocation, "Veggie 3", 1);
                        break;
                    case 4: // Oranges
                        sortingController = new SortingController(context, mMap, lastLocation, "Veggie 4", 1);
                        break;
                    case 5: // other
                        sortingController = new SortingController(context, mMap, lastLocation, "OtherVegetables", 1);
                        break;
                }
                break;
            case 4: // Meat
                i = spinnerSpecific.getSelectedItemPosition();
                switch (i)
                {
                    case 0: // nothing specific selected
                        SortingController sortingController = new SortingController(context, mMap, lastLocation, "Meat", 0);
                        break;
                    case 1: // apples
                        sortingController = new SortingController(context, mMap, lastLocation, "Fresh", 1);
                        break;
                    case 2: // pears
                        sortingController = new SortingController(context, mMap, lastLocation, "Frost", 1);
                        break;
                    case 3: // plums
                        sortingController = new SortingController(context, mMap, lastLocation, "OtherMeat", 1);
                        break;
                }
                break;
            case 5: // other
                SortingController sortingController = new SortingController(context, mMap, lastLocation, "Other", 0);
                break;
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private AdapterView.OnItemSelectedListener overallListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            String str = parent.getItemAtPosition(position).toString();
            switch (position)
            {
                case 0:
                    // Choose one
                    break;
                case 1:
                    // Berries selected
                    // Setting the drop down menu for the specific spinner
                    ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.listSpecificBerries, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSpecific.setAdapter(adapter);

                    // Set the overallCategory to the overall item selected
                    //setOverallCategory("Berries");

                    break;
                case 2:
                    // Fruits selected
                    ArrayAdapter adapter2 = ArrayAdapter.createFromResource(context, R.array.listSpecificFruits, android.R.layout.simple_spinner_item);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSpecific.setAdapter(adapter2);

                    // Set the overallCategory to the overall item selected
                    //setOverallCategory("Fruits");
                    break;
                case 3:
                    // Vegetables selected
                    ArrayAdapter adapter3 = ArrayAdapter.createFromResource(context, R.array.listSpecificVeggies, android.R.layout.simple_spinner_item);
                    adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSpecific.setAdapter(adapter3);

                    // Set the overallCategory to the overall item selected
                    //setOverallCategory("Vegetables");
                    break;
                case 4:
                    // Meat selected
                    ArrayAdapter adapter4 = ArrayAdapter.createFromResource(context, R.array.listSpecificMeats, android.R.layout.simple_spinner_item);
                    adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSpecific.setAdapter(adapter4);

                    // Set the overallCategory to the overall item selected
                    //setOverallCategory("Meat");
                    break;
                case 5: // other
                    //toastMessage("Other selected");
                    break;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private AdapterView.OnItemSelectedListener dropDownListener2 = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            String str = parent.getItemAtPosition(position).toString();
            switch (str)
            {
                case "NO":
                    //toastMessage("NO valgt");
                    chosenLanguage = "NO";
                    break;
                case "DA":
                    //toastMessage("DA valgt");
                    Locale mLocale = new Locale("da");
                    Locale.setDefault(mLocale);
                    Configuration config = getBaseContext().getResources().getConfiguration();
                    if (!config.locale.equals(mLocale))
                    {
                        config.locale = mLocale;
                        getBaseContext().getResources().updateConfiguration(config, null);
                        recreate();
                    }
                    chosenLanguage = "DA";
                    spinnerLang.setBackgroundResource(R.drawable.dk_flag_icon);

                    SharedPreferences sharedPref = getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("currentLanguage", chosenLanguage).apply();

                    break;
                case "NL":
                    //toastMessage("NL valgt");
                    chosenLanguage = "NL";
                    break;
                case "SV":
                    //toastMessage("SV valgt");
                    chosenLanguage = "SV";
                    break;
                case "EN":
                    //toastMessage("EN valgt");
                    Locale mLocale2 = new Locale("default");
                    Locale.setDefault(mLocale2);
                    Configuration config2 = getBaseContext().getResources().getConfiguration();
                    if (!config2.locale.equals(mLocale2))
                    {
                        config2.locale = mLocale2;
                        getBaseContext().getResources().updateConfiguration(config2, null);
                        recreate();
                    }
                    chosenLanguage = "EN";
                    spinnerLang.setBackgroundResource(R.drawable.uk_flag_icon);

                    SharedPreferences sharedPref2 = getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor2 = sharedPref2.edit();
                    editor2.putString("currentLanguage", chosenLanguage).apply();
                    break;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    // This is the drop down menu with Help, Settings and About page buttons ----------------------------------
    private AdapterView.OnItemSelectedListener dropDownListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            switch (position)
            {
                case 0:
                    //toastMessage("Vælg en!");
                    break;
                case 1:
                    toastMessage("Hjælp");
                    spinnerHelp.setSelection(0);
                    break;
                case 2:
                    toastMessage("Indstillinger");
                    spinnerHelp.setSelection(0);
                    break;
                case 3:
                    toastMessage("Om");
                    spinnerHelp.setSelection(0);
                    break;
                case 4:
                    //toastMessage("Log ud");
                    if (firebaseAuth.getCurrentUser() != null)
                    {
                        toastMessage(getString(R.string.toast_loggedOut));
                        firebaseAuth.signOut();
                        navigationView.getMenu().clear();
                        navigationView.inflateMenu(R.menu.navigation_menu);
                        // Logout of facebook - Doesn't seem to cause problems if not logged in to facebook.
                        LoginManager.getInstance().logOut();
                    }
                    else
                    {
                        toastMessage(getString(R.string.toast_notLoggedIn));
                    }
                    break;
                case 5:
                    break;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_kort:
                toastMessage(getString(R.string.toast_mapShowing));
                break;
            case R.id.nav_login:
                Intent intent = new Intent(MapsActivity.this, LoginAcitivity.class);
                startActivity(intent);
                //startActivity(new Intent(MapsActivity.this, LoginAcitivity.class));
                finish();
                break;
            case R.id.nav_register:
                //startActivity(new Intent(MapsActivity.this, RegisterActivity.class));
                Intent intent2 = new Intent(MapsActivity.this, RegisterActivity.class);
                startActivity(intent2);
                finish();
                break;
            case R.id.nav_kort2:
                toastMessage(getString(R.string.toast_mapShowing));
                break;
            case R.id.nav_account2:
                //startActivity(new Intent(MapsActivity.this, AccountActivity.class));
                Intent intent3 = new Intent(MapsActivity.this, AccountActivity.class);
                startActivity(intent3);
                finish();
                break;
            case R.id.nav_create2:
                // Try and get the lastLocation in a string from Shared Preferences.
                // If that doesn't work, try and send it with the intent.
                //toastMessage("Trying to create location");
                if (numberOfCreatedLocations == 0) // no location created on beforehand
                {
                    Intent intent4 = new Intent(MapsActivity.this, CreateLocationActivity.class);
                    startActivity(intent4);
                    finish();
                }
                else // Already has one location created.
                {
                    toastMessage("You already have one created.\nGo under change to see the settings.");
                }

                break;
            case R.id.nav_change2:
                if (numberOfCreatedLocations == 0) // no location created on beforehand
                {
                    toastMessage("No location created.\nRedirected to create one.");
                    Intent intent4 = new Intent(MapsActivity.this, CreateLocationActivity.class);
                    startActivity(intent4);
                    finish();
                }
                else // Already has one location created.
                {
                    Intent intent4 = new Intent(MapsActivity.this, ChangeLocationActivity.class);
                    startActivity(intent4);
                    finish();
                }
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Run this in the onCreate to get SHA1 key.
    // then just open log after running and search for KeyHash
    private void getKeyHash()
    {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "dk.roadfarmer.roadfarmer",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}
