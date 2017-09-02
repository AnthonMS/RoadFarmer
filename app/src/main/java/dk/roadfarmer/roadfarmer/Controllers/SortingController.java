package dk.roadfarmer.roadfarmer.Controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.roadfarmer.roadfarmer.Models.SellingLocation;

/**
 * Created by Anthon on 19-07-2017.
 */

public class SortingController
{
    private static final String TAG = "SortingController";

    private GoogleMap mMap;
    private Location lastLocation;
    private Context context;
    private String sortingValue;
    private int whatToShow; // 0 = OverallCategory, 1 = specificItem, 2 = RootSellingLocation

    private List<SellingLocation> sellingLocationList;
    //List<SellingLocation> sortedLocationList;

    private Map<Marker, SellingLocation> markerMap;

    // Firebase stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference myRootRef;

    public SortingController(Context context, GoogleMap mMap, Location lastLocation, String sortingValue, int whatToShow)
    {
        this.context = context;
        this.mMap = mMap;
        this.lastLocation = lastLocation;
        this.sortingValue = sortingValue;
        sellingLocationList = new ArrayList<>();
        this.whatToShow = whatToShow;
        markerMap = new HashMap<Marker, SellingLocation>();

        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRootRef = mFirebaseDatabase.getReference();

        /*myRootRef.child("RootSellingLocations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    //toastMessage(ds.getKey());
                    SellingLocation sellingLocation = ds.getValue(SellingLocation.class);
                    sellingLocationList.add(sellingLocation);
                    //toastLong(sellingLocation.getDescription());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        if (whatToShow == 0) // OverallCategory
        {
            addOverallMarkers();
        }
        else if (whatToShow == 1) // Specific item
        {
            addSpecificMarkers();
        }
        else if (whatToShow == 2) // RootSellingLocation
        {
            // This is supposed to just show all the selling locations from root of firebase
            // has not been made yet.
            // Should be easy to make
            addAllMarkers();
        }

        //addBerriesMarkers();
    }

    private void addAllMarkers()
    {
        markerMap.clear();
        mMap.clear();

        myRootRef.child("RootSellingLocations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    SellingLocation sellingLocation = ds.getValue(SellingLocation.class);
                    toastMessage(sellingLocation.getLocationID());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addSpecificMarkers()
    {
        markerMap.clear();
        mMap.clear();

        myRootRef.child("SpecificSellingLocations").child(sortingValue).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    SellingLocation sellingLocation = ds.getValue(SellingLocation.class);
                    //toastLong(sellingLocation.getDescription());
                    Geocoder geoCoder = new Geocoder(context);
                    List<Address> addressList = new ArrayList<Address>();
                    MarkerOptions mo = new MarkerOptions();
                    String stringAddress = sellingLocation.getRoad() + " " + sellingLocation.getNo() + ", " + sellingLocation.getZip() + " " + sellingLocation.getCity();

                    try {
                        addressList = geoCoder.getFromLocationName(stringAddress, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "Get latLng from address - specific: " + e.toString());
                    }

                    Marker tempMarker = null;
                    for (Address ad : addressList)
                    {
                        LatLng latLng = new LatLng(ad.getLatitude(), ad.getLongitude());
                        mo.position(latLng);
                        mo.title(ad.getAddressLine(0) + ", " + ad.getPostalCode() + " " + ad.getLocality());
                        mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        tempMarker = mMap.addMarker(mo);
                    }

                    markerMap.put(tempMarker, sellingLocation);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addOverallMarkers()
    {
        markerMap.clear();
        mMap.clear();

        myRootRef.child("OverallSellingLocations").child(sortingValue).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    SellingLocation sellingLocation = ds.getValue(SellingLocation.class);
                    //toastLong(sellingLocation.getDescription());
                    Geocoder geoCoder = new Geocoder(context);
                    List<Address> addressList = new ArrayList<Address>();
                    MarkerOptions mo = new MarkerOptions();
                    String stringAddress = sellingLocation.getRoad() + " " + sellingLocation.getNo() + ", " + sellingLocation.getZip() + " " + sellingLocation.getCity();

                    try {
                        addressList = geoCoder.getFromLocationName(stringAddress, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "Get latLng from address - overall: " + e.toString());
                    }

                    Marker tempMarker = null;
                    for (Address ad : addressList)
                    {
                        LatLng latLng = new LatLng(ad.getLatitude(), ad.getLongitude());
                        mo.position(latLng);
                        mo.title(ad.getAddressLine(0) + ", " + ad.getPostalCode() + " " + ad.getLocality());
                        mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        tempMarker = mMap.addMarker(mo);
                    }

                    markerMap.put(tempMarker, sellingLocation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public List<SellingLocation> getSellingLocationList() {
        return sellingLocationList;
    }

    public Map<Marker, SellingLocation> getMarkerMap() {
        return markerMap;
    }

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    private void toastLong(String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
