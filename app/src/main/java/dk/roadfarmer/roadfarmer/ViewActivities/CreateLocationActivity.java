package dk.roadfarmer.roadfarmer.ViewActivities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.Locale;

import dk.roadfarmer.roadfarmer.Models.SellingLocation;
import dk.roadfarmer.roadfarmer.R;

public class CreateLocationActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener
{
    private DrawerLayout mDrawerLayout;
    private final Context context = this;

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

    // Initialize from layout file
    private ImageView locationImgView,
            item1ImgView, item2ImgView, item3ImgView, item4ImgView, item5ImgView;
    private EditText editRoad, editNo, editZip, editCity, editCustomItems, editDescription;
    private Button addImgBtn, getLocBtn, createSellingLocBtn;
    private TextView addedItemTextView;

    // Variables used to check which items selected to sell
    private String overallCategory, overallCategory2, overallCategory3, overallCategory4, overallCategory5;
    private String specificItem1;
    private String specificItem2;
    private String specificItem3;
    private String specificItem4;
    private String specificItem5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_create_location);

        SharedPreferences sharedPref = getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
        chosenLanguage = sharedPref.getString("currentLanguage", "");

        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRootRef = mFirebaseDatabase.getReference();

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
        textViewTitleBar.setText(getString(R.string.title_activity_account));
        // Setting the selected language in the spinner if user selected on himself
        int pos = adapterLang.getPosition(chosenLanguage);
        spinnerLang.setSelection(pos);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.create_drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.create_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_create2);
        closeNavBtn = (ImageButton) findViewById(R.id.create_closeNavBar);
        closeNavBtn.setOnClickListener(buttonClickListener);

        // Instantiate all the layout stuff
        locationImgView = (ImageView) findViewById(R.id.create_imageView);
        item1ImgView = (ImageView) findViewById(R.id.create_itemView1);
        item2ImgView = (ImageView) findViewById(R.id.create_itemView2);
        item3ImgView = (ImageView) findViewById(R.id.create_itemView3);
        item4ImgView = (ImageView) findViewById(R.id.create_itemView4);
        item5ImgView = (ImageView) findViewById(R.id.create_itemView5);
        editRoad = (EditText) findViewById(R.id.create_editRoad);
        editNo = (EditText) findViewById(R.id.create_editNo);
        editZip = (EditText) findViewById(R.id.create_editZip);
        editCity = (EditText) findViewById(R.id.create_editCity);
        editCustomItems = (EditText) findViewById(R.id.create_addCustomItems);
        editDescription = (EditText) findViewById(R.id.create_editDescription);
        addedItemTextView = (TextView) findViewById(R.id.create_addedItemText);
        addImgBtn = (Button) findViewById(R.id.create_addImgBtn);
        getLocBtn = (Button) findViewById(R.id.create_getLocBtn);
        createSellingLocBtn = (Button) findViewById(R.id.create_createLocBtn);
        // Set OnClickListener
        addImgBtn.setOnClickListener(buttonClickListener);
        getLocBtn.setOnClickListener(buttonClickListener);
        createSellingLocBtn.setOnClickListener(buttonClickListener);

    }

    private void createSellingLocation()
    {
        if (! checkEmptyFields())
        {
            // No empty fields and at least one overallCategory and one specificItem chosen
            String getRoad = editRoad.getText().toString().trim();
            String getNo = editNo.getText().toString().trim();
            String getZip = editZip.getText().toString().trim();
            String getCity = editCity.getText().toString().trim();
            String getDesc = editDescription.getText().toString().trim();
            int iZip = 0;
            int iNo = 0;
            try {
                iZip = Integer.parseInt(getNo);
                iNo = Integer.parseInt(getZip);
            } catch (NumberFormatException e)
            {
                e.printStackTrace();
            }
            overallCategory = "Bær"; // F.ex Bær, Frugt, Grøntsager, Kød
            specificItem1 = "Jordbær"; // F.ex. Jordbær, Hindtbær, Blåbær osv. anden kategori Kartofler, Ærter osv.
            specificItem2 = "Hindbær";

            String locationID = myRootRef.push().getKey();
            SellingLocation sellingLocation = new SellingLocation(getRoad, getCity, iNo, iZip, locationID);
            sellingLocation.setOverallCategory(overallCategory);
            sellingLocation.setSpecificItem1(specificItem1);
            sellingLocation.setSpecificItem2(specificItem2);
            // Saving under RootSellingLocations
            myRootRef.child("RootSellingLocations").child(locationID).setValue(sellingLocation);
            // Saving under OverallSellingLocations/overallCategory
            myRootRef.child("OverallSellingLocations").child(overallCategory).child(locationID).setValue(sellingLocation);
            // Saving in the specificSellingLocations/specificItem1
            myRootRef.child("SpecifigSellingLocations").child(overallCategory).child(specificItem1).child(locationID).setValue(sellingLocation);
            myRootRef.child("SpecifigSellingLocations").child(overallCategory).child(specificItem2).child(locationID).setValue(sellingLocation);
        }
        else
        {
            // empty text fields
        }
    }

    private boolean checkEmptyFields()
    {
        String getRoad = editRoad.getText().toString().trim();
        String getNo = editNo.getText().toString().trim();
        String getZip = editZip.getText().toString().trim();
        String getCity = editCity.getText().toString().trim();
        String getDesc = editDescription.getText().toString().trim();
        if (TextUtils.isEmpty(getRoad))
        {
            toastMessage("Road is empty");
            return true;
        }
        if (TextUtils.isEmpty(getNo))
        {
            toastMessage("House number is empty");
            return true;
        }
        if (TextUtils.isEmpty(getZip))
        {
            toastMessage("Zip/Postal code is empty");
            return true;
        }
        if (TextUtils.isEmpty(getCity))
        {
            toastMessage("City is empty");
            return true;
        }
        if (TextUtils.isEmpty(getDesc))
        {
            toastMessage("Description is empty");
            return true;
        }
        /*if (TextUtils.isEmpty(overallCategory))
        {
            toastMessage("No overall category chosen");
            return true;
        }
        if (TextUtils.isEmpty(specificItem1))
        {
            toastMessage("You need at least one specific item");
            return true;
        }*/
        return false;
    }

    private void setOverallCategory(String oaCategory)
    {
        if (TextUtils.isEmpty(overallCategory)) // If overallCategory is empty, set overallCategory
        {
            overallCategory = oaCategory;
        }
        else if ((TextUtils.isEmpty(overallCategory2))
                && (! oaCategory.equals(overallCategory))) // if overallCategory was not empty and oaCategory is not the same
        {
            overallCategory2 = oaCategory;
        }
        else if ((TextUtils.isEmpty(overallCategory3))
                && (! oaCategory.equals(overallCategory))
                && (! oaCategory.equals(overallCategory2))) // so on
        {
            overallCategory3 = oaCategory;
        }
        else if ((TextUtils.isEmpty(overallCategory4))
                && (! oaCategory.equals(overallCategory))
                && (! oaCategory.equals(overallCategory2))
                && (! oaCategory.equals(overallCategory3))) // so on
        {
            overallCategory4 = oaCategory;
        }
        else if ((TextUtils.isEmpty(overallCategory5))
                && (! oaCategory.equals(overallCategory))
                && (! oaCategory.equals(overallCategory2))
                && (! oaCategory.equals(overallCategory3))
                && (! oaCategory.equals(overallCategory4))) // so on
        {
            overallCategory5 = oaCategory;
        }
    }

    private void setSpecificItem(String item)
    {
        if (TextUtils.isEmpty(specificItem1)) // If overallCategory is empty, set overallCategory
        {
            specificItem1 = item;
        }
        else if ((TextUtils.isEmpty(specificItem2))
                && (! item.equals(specificItem1))) // if overallCategory was not empty and oaCategory is not the same
        {
            specificItem2 = item;
        }
        else if ((TextUtils.isEmpty(specificItem3))
                && (! item.equals(specificItem1))
                && (! item.equals(specificItem2))) // so on
        {
            specificItem3 = item;
        }
        else if ((TextUtils.isEmpty(specificItem4))
                && (! item.equals(specificItem1))
                && (! item.equals(specificItem2))
                && (! item.equals(specificItem3))) // so on
        {
            specificItem4 = item;
        }
        else if ((TextUtils.isEmpty(specificItem5))
                && (! item.equals(specificItem1))
                && (! item.equals(specificItem2))
                && (! item.equals(specificItem3))
                && (! item.equals(specificItem4))) // so on
        {
            specificItem5 = item;
        }
    }

    private boolean checkEmptyItems()
    {
        if (TextUtils.isEmpty(overallCategory))
        {
            toastMessage("You need to select at least one overall category");
            return true;
        }
        if (TextUtils.isEmpty(specificItem1))
        {
            toastMessage("You need to select at least one item");
            return  true;
        }
        return false;
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
                int i = spinnerOverall.getSelectedItemPosition();
                //Object obj = spinnerOverall.getItemAtPosition(i);
                //overallCategory = obj.toString();
                if (i == 1) // Berries
                {
                    overallCategory = "Berries";
                }
                else if (i == 2) // Fruits
                {
                    overallCategory = "Fruits";
                }
                toastMessage(overallCategory);
                //addedItemTextView.setText(overallCategory);
            }
        });

        dialog.show();
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
                    spinnerSpecific.setOnItemSelectedListener(berryListener);

                    // Set the overallCategory to the overall item selected
                    //setOverallCategory("Berries");

                    break;
                case 2:
                    // Fruits selected
                    ArrayAdapter adapter2 = ArrayAdapter.createFromResource(context, R.array.listSpecificFruits, android.R.layout.simple_spinner_item);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSpecific.setAdapter(adapter2);
                    spinnerSpecific.setOnItemSelectedListener(fruitListener);

                    // Set the overallCategory to the overall item selected
                    //setOverallCategory("Fruits");
                    break;
                case 3:
                    // Vegetables selected
                    ArrayAdapter adapter3 = ArrayAdapter.createFromResource(context, R.array.listSpecificVeggies, android.R.layout.simple_spinner_item);
                    adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSpecific.setAdapter(adapter3);
                    spinnerSpecific.setOnItemSelectedListener(veggieListener);

                    // Set the overallCategory to the overall item selected
                    //setOverallCategory("Vegetables");
                    break;
                case 4:
                    // Meat selected
                    ArrayAdapter adapter4 = ArrayAdapter.createFromResource(context, R.array.listSpecificMeats, android.R.layout.simple_spinner_item);
                    adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSpecific.setAdapter(adapter4);
                    spinnerSpecific.setOnItemSelectedListener(meatListener);

                    // Set the overallCategory to the overall item selected
                    //setOverallCategory("Meat");
                    break;
                case 5: // other
                    toastMessage("Other selected");
                    break;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };


    private AdapterView.OnItemSelectedListener berryListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            switch (position)
            {
                case 0: // Choose one
                    break;
                case 1: // Cherries
                    //setSpecificItem("Cherries");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 2: // Blueberry
                    //setSpecificItem("Blueberry");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 3: // Raspberry
                    //setSpecificItem("Raspberry");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 4: // Strawberry
                    //setSpecificItem("Strawberry");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 5: // Other
                    //setSpecificItem("Other");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 6: // For later
                    break;
                case 7: // For later
                    break;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private AdapterView.OnItemSelectedListener fruitListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            switch (position)
            {
                case 0: // Choose one
                    break;
                case 1: // Apples
                    //setSpecificItem("Apples");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 2: // Pares
                    //setSpecificItem("Pares");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 3: // Plumes
                    //setSpecificItem("Plumes");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 4: // Oranges
                    //setSpecificItem("Oranges");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 5: // Other
                    //setSpecificItem("Other");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 6: // For later
                    break;
                case 7: // For later
                    break;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private AdapterView.OnItemSelectedListener veggieListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            switch (position)
            {
                case 0: // Choose one
                    break;
                case 1: // Peas
                    //setSpecificItem("Peas");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 2: // Veggie 2
                    //setSpecificItem("Veggie2");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 3: // Veggie 3
                    //setSpecificItem("Veggie3");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 4: // Veggie 4
                    //setSpecificItem("Veggie4");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 5: // Other
                    //setSpecificItem("Other");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 6: // For later
                    break;
                case 7: // For later
                    break;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private AdapterView.OnItemSelectedListener meatListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            switch (position)
            {
                case 0: // Choose one
                    break;
                case 1: // fresh
                    //setSpecificItem("Fresh");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 2: // Frost
                    //setSpecificItem("Frost");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
                case 3: // Other
                    //setSpecificItem("Other");
                    //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);
                    break;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };


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
                case R.id.create_closeNavBar:
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    break;
                case R.id.create_addImgBtn:
                    toastMessage("add image");
                    showAddItemDialog("Add image");
                    break;
                case R.id.create_getLocBtn:
                    toastMessage("Get location");
                    break;
                case R.id.create_createLocBtn:
                    //toastMessage("Create selling location");
                    createSellingLocation();
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

                    startActivity(new Intent(CreateLocationActivity.this, MapsActivity.class));
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
                Intent intent = new Intent(CreateLocationActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_account2:
                Intent intent2 = new Intent(CreateLocationActivity.this, AccountActivity.class);
                startActivity(intent2);
                finish();
                break;
            case R.id.nav_create2:
                toastMessage(getString(R.string.toast_createShowing));
                break;
            case R.id.nav_change2:
                toastMessage("Trying to change location");
                /*Intent intent3 = new Intent(AccountActivity.this, ChangeLocationActivity.class);
                startActivity(intent3);
                finish();*/
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
