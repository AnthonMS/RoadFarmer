package dk.roadfarmer.roadfarmer.ViewActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

import dk.roadfarmer.roadfarmer.Models.SellingLocation;
import dk.roadfarmer.roadfarmer.Models.User;
import dk.roadfarmer.roadfarmer.R;

public class ChangeLocationActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener
{
    private DrawerLayout mDrawerLayout;
    private final Context context = this;
    private static final String TAG = "ChangeLocationActivity";

    // Buttons and stuff from app_bar class
    private ImageButton burgerMenuBtn;
    private Spinner spinnerHelp, spinnerLang;
    private ArrayAdapter<CharSequence> adapterHelp;
    private ArrayAdapter<CharSequence> adapterLang;
    private TextView textViewTitleBar;
    // Close button from NavigationBar
    private ImageButton closeNavBtn;
    private NavigationView navigationView;
    private String chosenLanguage;

    // Firebase stuff
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRootRef;
    private StorageReference mStorage;

    // Initialize from layout file
    private ImageView locationImgView,
            item1ImgView, item2ImgView, item3ImgView, item4ImgView, item5ImgView;
    private EditText editRoad, editNo, editZip, editCity, editCustomItems, editDescription;
    private Button editSellingLocBtn, deleteLocBtn;
    private Bitmap locationViewPhoto;
    private Uri photoUri;
    private String photoID;
    private String specificItem1, specificItem2, specificItem3, specificItem4, specificItem5;
    private String overallCat1, overallCat2, overallCat3, overallCat4, overallCat5;

    private SellingLocation sellingLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_change_location);

        SharedPreferences sharedPref = getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
        chosenLanguage = sharedPref.getString("currentLanguage", "");

        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRootRef = mFirebaseDatabase.getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        // Everything here is from app_bar class -----------------
        burgerMenuBtn = (ImageButton) findViewById(R.id.burgerMenuBtn);
        burgerMenuBtn.setOnClickListener(buttonClickListener);
        textViewTitleBar = (TextView) findViewById(R.id.textView_titleBar);
        spinnerHelp = (Spinner) findViewById(R.id.spinner_helpDropDown);
        adapterHelp = ArrayAdapter.createFromResource(this, R.array.listSettingSelection, android.R.layout.simple_spinner_item);
        adapterHelp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHelp.setAdapter(adapterHelp);
        spinnerHelp.setOnItemSelectedListener(dropDownListener);
        spinnerLang = (Spinner) findViewById(R.id.spinner_languageSelect);
        adapterLang = ArrayAdapter.createFromResource(this, R.array.listLanguages, android.R.layout.simple_spinner_item);
        adapterLang.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLang.setAdapter(adapterLang);
        spinnerLang.setOnItemSelectedListener(languageListener);
        // Setting the title of this specific page.
        textViewTitleBar.setText(getString(R.string.title_activity_change));
        // Setting the selected language in the spinner if user selected on himself
        int pos = adapterLang.getPosition(chosenLanguage);
        spinnerLang.setSelection(pos);

        // Everything from the Navigation Menu
        mDrawerLayout = (DrawerLayout) findViewById(R.id.change_drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.change_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.navigation_menu_two);
        //navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setCheckedItem(R.id.nav_change2);
        closeNavBtn = (ImageButton) findViewById(R.id.change_closeNavBar);
        closeNavBtn.setOnClickListener(buttonClickListener);

        // Instantiate all the layout stuff
        locationImgView = (ImageView) findViewById(R.id.change_imageView);
        item1ImgView = (ImageView) findViewById(R.id.change_itemView1);
        item2ImgView = (ImageView) findViewById(R.id.change_itemView2);
        item3ImgView = (ImageView) findViewById(R.id.change_itemView3);
        item4ImgView = (ImageView) findViewById(R.id.change_itemView4);
        item5ImgView = (ImageView) findViewById(R.id.change_itemView5);
        editRoad = (EditText) findViewById(R.id.change_editRoad);
        editNo = (EditText) findViewById(R.id.change_editNo);
        editZip = (EditText) findViewById(R.id.change_editZip);
        editCity = (EditText) findViewById(R.id.change_editCity);
        editCustomItems = (EditText) findViewById(R.id.change_addCustomItems);
        editDescription = (EditText) findViewById(R.id.change_editDescription);
        editSellingLocBtn = (Button) findViewById(R.id.change_createLocBtn);
        deleteLocBtn = (Button) findViewById(R.id.change_deleteLocBtn);
        // Set OnClickListener
        editSellingLocBtn.setOnClickListener(buttonClickListener);
        deleteLocBtn.setOnClickListener(buttonClickListener);

        getMyLocationID();

    }

    private String locationID;
    private void getMyLocationID()
    {
        myRootRef.child("Users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("UserInfo")
                .child("locationID")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                locationID = dataSnapshot.getValue(String.class);
                getMySellingLocation();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getMySellingLocation()
    {
        myRootRef.child("RootSellingLocations")
                .child(locationID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        sellingLocation = dataSnapshot.getValue(SellingLocation.class);
                        //toastMessage(sellingLocation.getLocationID());
                        updateFields();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateFields()
    {
        editRoad.setText(sellingLocation.getRoad());
        editNo.setText(sellingLocation.getNo() + "");
        editZip.setText(sellingLocation.getZip() + "");
        editCity.setText(sellingLocation.getCity());
        editDescription.setText(sellingLocation.getDescription());

        //toastMessage(sellingLocation.getSpecificItem1());
        setItemView(sellingLocation.getSpecificItem1(), item1ImgView);
        specificItem1 = sellingLocation.getSpecificItem1();

        if (!TextUtils.isEmpty(sellingLocation.getSpecificItem2()))
        {
            setItemView(sellingLocation.getSpecificItem2(), item2ImgView);
            specificItem2 = sellingLocation.getSpecificItem2();
            if (!TextUtils.isEmpty(sellingLocation.getSpecificItem3()))
            {
                setItemView(sellingLocation.getSpecificItem3(), item3ImgView);
                specificItem3 = sellingLocation.getSpecificItem3();
                if (!TextUtils.isEmpty(sellingLocation.getSpecificItem4()))
                {
                    setItemView(sellingLocation.getSpecificItem4(), item4ImgView);
                    specificItem4 = sellingLocation.getSpecificItem4();
                    if (!TextUtils.isEmpty(sellingLocation.getSpecificItem5()))
                    {
                        setItemView(sellingLocation.getSpecificItem5(), item5ImgView);
                        specificItem5 = sellingLocation.getSpecificItem5();

                    }
                }
            }
        }

        //setItemView("Blueberries", item3ImgView);
    }

    private void updateFirebase()
    {
        String getRoad = editRoad.getText().toString().trim();
        String getNo = editNo.getText().toString().trim();
        String getZip = editZip.getText().toString().trim();
        String getCity = editCity.getText().toString().trim();
        String getDesc = editDescription.getText().toString().trim();
        int iZip = 0;
        int iNo = 0;
        try {
            iNo = Integer.parseInt(getNo);
            iZip = Integer.parseInt(getZip);
        } catch (NumberFormatException e)
        {
            e.printStackTrace();
        }

        SellingLocation sellingLocation = new SellingLocation(getRoad, getCity, iNo, iZip, locationID);
        sellingLocation.setDescription(getDesc);
        sellingLocation.setUserID(firebaseAuth.getCurrentUser().getUid());
        // Saving under RootSellingLocations
        myRootRef.child("RootSellingLocations").child(locationID).setValue(sellingLocation);
        // Saving under OverallSellingLocations/overallCategory
        myRootRef.child("OverallSellingLocations").child(overallCat1).child(locationID).setValue(sellingLocation);
        // Saving in the specificSellingLocations/specificItem1
        myRootRef.child("SpecificSellingLocations").child(specificItem1).child(locationID).setValue(sellingLocation);

        if (!TextUtils.isEmpty(specificItem2))
        {
            myRootRef.child("SpecificSellingLocations").child(specificItem2).child(locationID).setValue(sellingLocation);
        }
        if (!TextUtils.isEmpty(specificItem3))
        {
            myRootRef.child("SpecificSellingLocations").child(specificItem3).child(locationID).setValue(sellingLocation);
        }
        if (!TextUtils.isEmpty(specificItem4))
        {
            myRootRef.child("SpecificSellingLocations").child(specificItem4).child(locationID).setValue(sellingLocation);
        }
        if (!TextUtils.isEmpty(specificItem5))
        {
            myRootRef.child("SpecificSellingLocations").child(specificItem5).child(locationID).setValue(sellingLocation);
        }

        if (!TextUtils.isEmpty(overallCat2))
        {
            myRootRef.child("OverallSellingLocations").child(overallCat2).child(locationID).setValue(sellingLocation);
        }
        if (!TextUtils.isEmpty(overallCat3))
        {
            myRootRef.child("OverallSellingLocations").child(overallCat3).child(locationID).setValue(sellingLocation);
        }
        if (!TextUtils.isEmpty(overallCat4))
        {
            myRootRef.child("OverallSellingLocations").child(overallCat4).child(locationID).setValue(sellingLocation);
        }
        if (!TextUtils.isEmpty(overallCat5))
        {
            myRootRef.child("OverallSellingLocations").child(overallCat5).child(locationID).setValue(sellingLocation);
        }
    }

    private void setItemView(String str, ImageView imgView)
    {
        if (str != "")
        {
            // Not empty
            if (str.matches("Cherries"))
            {
                imgView.setImageResource(R.drawable.cherry_one);
            }
            else if (str.matches("Blueberries"))
            {
                imgView.setImageResource(R.drawable.blueberry);
            }
            else if (str.matches("Raspberries"))
            {
                imgView.setImageResource(R.drawable.rasp_two);
            }
            else if (str.matches("Strawberries"))
            {
                imgView.setImageResource(R.drawable.straw_one);
            }
            else if (str.matches("Apples"))
            {
                imgView.setImageResource(R.drawable.apple_one);
            }
            else if (str.matches("Pears"))
            {
                imgView.setImageResource(R.drawable.pare_one);
            }
            else if (str.matches("Plums"))
            {
                imgView.setImageResource(R.drawable.plum_one);
            }
            else if (str.matches("Oranges"))
            {
                imgView.setImageResource(R.drawable.orange_one);
            }
            else if (str.matches("Peas"))
            {
                imgView.setImageResource(R.drawable.peas_one);
            }
            else if (str.matches("Veggie 2"))
            {

            }
            else if (str.matches("Fresh"))
            {
                imgView.setImageResource(R.drawable.fresh_one);
            }
            else if (str.matches("Frost"))
            {
                imgView.setImageResource(R.drawable.frost_one);
            }
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
                case R.id.change_closeNavBar:
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    break;
                case R.id.change_deleteLocBtn:
                    toastMessage("Wanna delete");
                    break;
                case R.id.change_createLocBtn:
                    //toastMessage("Wanna change");
                    //updateFirebase();
                    break;
            }
        }
    };
    private AdapterView.OnItemSelectedListener languageListener = new AdapterView.OnItemSelectedListener()
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
                        // Logout of facebook - Doesn't seem to cause problems if not logged in to facebook.
                        LoginManager.getInstance().logOut();
                    }
                    else
                    {
                        toastMessage(getString(R.string.toast_notLoggedIn));
                    }

                    startActivity(new Intent(ChangeLocationActivity.this, MapsActivity.class));
                    finish();
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
    private void toastLong(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_kort2:
                Intent intent = new Intent(ChangeLocationActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_account2:
                Intent intent2 = new Intent(ChangeLocationActivity.this, AccountActivity.class);
                startActivity(intent2);
                finish();
                break;
            case R.id.nav_create2:
                toastMessage("You already have one created.");
                break;
            case R.id.nav_change2:
                toastMessage("Already in the process.");
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
