package dk.roadfarmer.roadfarmer.ViewActivities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.BitmapCompat;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import dk.roadfarmer.roadfarmer.Models.SellingLocation;
import dk.roadfarmer.roadfarmer.Models.User;
import dk.roadfarmer.roadfarmer.R;

public class CreateLocationActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener
{
    private DrawerLayout mDrawerLayout;
    private final Context context = this;
    public static final int REQUEST_CODE_CAMERA = 98;

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
    private Button addImgBtn, getLocBtn, createSellingLocBtn;
    private TextView addItemsText, addedItemTextView;
    private Bitmap locationViewPhoto;
    private Uri photoUri;
    private String photoID;

    private String lastAddress, lastCity, lastZip;

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
        SharedPreferences sharedPref2 = getSharedPreferences("savedLocation", Context.MODE_PRIVATE);
        lastAddress = sharedPref2.getString("lastKnownAddress", "");
        lastZip = sharedPref2.getString("lastKnownZip", "");
        lastCity = sharedPref2.getString("lastKnownCity", "");
        //toastMessage(lastAddress + " " + lastZip + " " + lastCity);

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
        addItemsText = (TextView) findViewById(R.id.create_addSellingItems);
        addImgBtn = (Button) findViewById(R.id.create_addImgBtn);
        getLocBtn = (Button) findViewById(R.id.create_getLocBtn);
        createSellingLocBtn = (Button) findViewById(R.id.create_createLocBtn);
        // Set OnClickListener
        addImgBtn.setOnClickListener(buttonClickListener);
        getLocBtn.setOnClickListener(buttonClickListener);
        createSellingLocBtn.setOnClickListener(buttonClickListener);
        addItemsText.setOnClickListener(buttonClickListener);

    }

    private void createSellingLocation()
    {
        if (! checkEmptyFields() && ! checkEmptyItems())
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
                iNo = Integer.parseInt(getNo);
                iZip = Integer.parseInt(getZip);
            } catch (NumberFormatException e)
            {
                e.printStackTrace();
            }

            String locationID = myRootRef.push().getKey();
            SellingLocation sellingLocation = new SellingLocation(getRoad, getCity, iNo, iZip, locationID);
            sellingLocation.setDescription(getDesc);
            sellingLocation.setUserID(firebaseAuth.getCurrentUser().getUid());
            if (! TextUtils.isEmpty(photoID))
            {
                sellingLocation.setPhotoDownloadURL(photoUri.toString());
                sellingLocation.setPhotoID(photoID);
            }
            sellingLocation.setOverallCategory(overallCategory);
            sellingLocation.setSpecificItem1(specificItem1);
            if (!TextUtils.isEmpty(specificItem2))
            {
                sellingLocation.setSpecificItem2(specificItem2);
            }
            if (!TextUtils.isEmpty(specificItem3))
            {
                sellingLocation.setSpecificItem3(specificItem3);
            }
            if (!TextUtils.isEmpty(specificItem4))
            {
                sellingLocation.setSpecificItem4(specificItem4);
            }
            if (!TextUtils.isEmpty(specificItem5))
            {
                sellingLocation.setSpecificItem5(specificItem5);
            }
            if (!TextUtils.isEmpty(overallCategory2))
            {
                sellingLocation.setOverallCategory2(overallCategory2);
            }
            if (!TextUtils.isEmpty(overallCategory3))
            {
                sellingLocation.setOverallCategory3(overallCategory3);
            }
            if (!TextUtils.isEmpty(overallCategory4))
            {
                sellingLocation.setOverallCategory4(overallCategory4);
            }
            if (!TextUtils.isEmpty(overallCategory5))
            {
                sellingLocation.setOverallCategory5(overallCategory5);
            }
            // Saving under RootSellingLocations
            myRootRef.child("RootSellingLocations").child(locationID).setValue(sellingLocation);
            // Saving under OverallSellingLocations/overallCategory
            myRootRef.child("OverallSellingLocations").child(overallCategory).child(locationID).setValue(sellingLocation);
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

            if (!TextUtils.isEmpty(overallCategory2))
            {
                myRootRef.child("OverallSellingLocations").child(overallCategory2).child(locationID).setValue(sellingLocation);
            }
            if (!TextUtils.isEmpty(overallCategory3))
            {
                myRootRef.child("OverallSellingLocations").child(overallCategory3).child(locationID).setValue(sellingLocation);
            }
            if (!TextUtils.isEmpty(overallCategory4))
            {
                myRootRef.child("OverallSellingLocations").child(overallCategory4).child(locationID).setValue(sellingLocation);
            }
            if (!TextUtils.isEmpty(overallCategory5))
            {
                myRootRef.child("OverallSellingLocations").child(overallCategory5).child(locationID).setValue(sellingLocation);
            }

            myRootRef.child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("UserInfo").child("numberOfCreatedLocations").setValue(1);
            myRootRef.child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("UserInfo").child("locationID").setValue(locationID);

            //SharedPreferences sharedPref = getSharedPreferences("numberOfCreatedLocations", Context.MODE_PRIVATE);
            //SharedPreferences.Editor editor = sharedPref.edit();
            //editor.putString("currentNumber", "1").apply();

        }
        else
        {
            // empty text fields and item fields
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

    private void setOverallCategory()
    {
        if (TextUtils.isEmpty(overallCategory)) // If overallCategory is empty, set overallCategory
        {
            overallCategory = getChosenCategory();
        }
        else if ((TextUtils.isEmpty(overallCategory2))
                && (! getChosenCategory().equals(overallCategory))) // if overallCategory was not empty and oaCategory is not the same
        {
            overallCategory2 = getChosenCategory();
        }
        else if ((TextUtils.isEmpty(overallCategory3))
                && (! getChosenCategory().equals(overallCategory))
                && (! getChosenCategory().equals(overallCategory2))) // so on
        {
            overallCategory3 = getChosenCategory();
        }
        else if ((TextUtils.isEmpty(overallCategory4))
                && (! getChosenCategory().equals(overallCategory))
                && (! getChosenCategory().equals(overallCategory2))
                && (! getChosenCategory().equals(overallCategory3))) // so on
        {
            overallCategory4 = getChosenCategory();
        }
        else if ((TextUtils.isEmpty(overallCategory5))
                && (! getChosenCategory().equals(overallCategory))
                && (! getChosenCategory().equals(overallCategory2))
                && (! getChosenCategory().equals(overallCategory3))
                && (! getChosenCategory().equals(overallCategory4))) // so on
        {
            overallCategory5 = getChosenCategory();
        }
    }

    private String getChosenCategory()
    {
        String tempString = "";
        int i = spinnerOverall.getSelectedItemPosition();
        //Object obj = spinnerOverall.getItemAtPosition(i);
        //overallCategory = obj.toString();
        switch (i)
        {
            case 1:
                tempString = "Berries";
                break;
            case 2:
                tempString = "Fruits";
                break;
            case 3:
                tempString = "Vegetables";
                break;
            case 4:
                tempString = "Meat";
                break;
            case 5:
                tempString = "Other";
                break;
            case 6:
                break;
        }

        return tempString;
    }

    private String getSpecificBerries()
    {
        String tempString = "";
        int i = spinnerSpecific.getSelectedItemPosition();
        switch (i)
        {
            case 1: // Cherries
                tempString = "Cherries";
                break;
            case 2: // Blueberries
                tempString = "Blueberries";
                break;
            case 3: // Raspberries
                tempString = "Raspberries";
                break;
            case 4: // Strawberries
                tempString = "Strawberries";
                break;
            case 5: // Other
                tempString = "OtherBerries";
                break;
        }
        return tempString;
    }

    private String getSpecificFruits()
    {
        String tempString = "";
        int i = spinnerSpecific.getSelectedItemPosition();
        switch (i)
        {
            case 1: // Apples
                tempString = "Apples";
                break;
            case 2: // Blueberries
                tempString = "Pears";
                break;
            case 3: // Raspberries
                tempString = "Plums";
                break;
            case 4: // Strawberries
                tempString = "Oranges";
                break;
            case 5: // Other
                tempString = "OtherFruits";
                break;
        }
        return tempString;
    }

    private String getSpecificVegetables()
    {
        String tempString = "";
        int i = spinnerSpecific.getSelectedItemPosition();
        switch (i)
        {
            case 1: // Apples
                tempString = "Peas";
                break;
            case 2: // Blueberries
                tempString = "Veggie 2";
                break;
            case 3: // Raspberries
                tempString = "Veggie 3";
                break;
            case 4: // Strawberries
                tempString = "Veggie 4";
                break;
            case 5: // Other
                tempString = "OtherVegetables";
                break;
        }
        return tempString;
    }

    private String getSpecificMeats()
    {
        String tempString = "";
        int i = spinnerSpecific.getSelectedItemPosition();
        switch (i)
        {
            case 1: // Apples
                tempString = "Fresh";
                break;
            case 2: // Blueberries
                tempString = "Frost";
                break;
            case 3: // Raspberries
                tempString = "OtherMeat";
                break;
        }
        return tempString;
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
                //toastMessage(getChosenCategory());
                setOverallCategory();
                //toastMessage(overallCategory + " " + overallCategory2 + " " + overallCategory3 + " " + overallCategory4 + " " + overallCategory5);

                if (spinnerOverall.getSelectedItemPosition() == 1) // Berries chosen in overallCategory spinner
                {
                    setSpecificItem(getSpecificBerries());
                }
                else if (spinnerOverall.getSelectedItemPosition() == 2) // Fruits chosen
                {
                    setSpecificItem(getSpecificFruits());
                }
                else if (spinnerOverall.getSelectedItemPosition() == 3) // Vegetables chosen
                {
                    setSpecificItem(getSpecificVegetables());
                }
                else if (spinnerOverall.getSelectedItemPosition() == 4) // Meats chosen
                {
                    setSpecificItem(getSpecificMeats());
                }
                else if (spinnerOverall.getSelectedItemPosition() == 5) // Other chosen
                {
                    //setSpecificItem("Other");
                    setOverallCategory();
                }
                //toastMessage(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);

                if (! checkEmptyItems())
                {
                    setItemView(specificItem1, item1ImgView);
                    setItemView(specificItem2, item2ImgView);
                    setItemView(specificItem3, item3ImgView);
                    setItemView(specificItem4, item4ImgView);
                    setItemView(specificItem5, item5ImgView);
                }
                else
                {

                }

                addedItemTextView.setText(specificItem1 + " " + specificItem2 + " " + specificItem3 + " " + specificItem4 + " " + specificItem5);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setItemView(String str, ImageView imgView)
    {
        if (str != "")
        {
            // Not empty
            if (str == "Cherries")
            {
                imgView.setImageResource(R.drawable.cherry_one);
            }
            else if (str == "Blueberries")
            {
                imgView.setImageResource(R.drawable.blueberry);
            }
            else if (str == "Raspberries")
            {
                imgView.setImageResource(R.drawable.rasp_two);
            }
            else if (str == "Strawberries")
            {
                imgView.setImageResource(R.drawable.straw_one);
            }
            else if (str == "Apples")
            {
                imgView.setImageResource(R.drawable.apple_one);
            }
            else if (str == "Pears")
            {
                imgView.setImageResource(R.drawable.pare_one);
            }
            else if (str == "Plums")
            {
                imgView.setImageResource(R.drawable.plum_one);
            }
            else if (str == "Oranges")
            {
                imgView.setImageResource(R.drawable.orange_one);
            }
            else if (str == "Peas")
            {
                imgView.setImageResource(R.drawable.peas_one);
            }
            else if (str == "Veggie 2")
            {

            }
            else if (str == "Fresh")
            {
                imgView.setImageResource(R.drawable.fresh_one);
            }
            else if (str == "Frost")
            {
                imgView.setImageResource(R.drawable.frost_one);
            }
        }
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

    private void launchCamera()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            locationViewPhoto = (Bitmap) extras.get("data");
            locationImgView.setBackgroundColor(Color.TRANSPARENT);
            locationImgView.setImageBitmap(locationViewPhoto);
            photoID = myRootRef.push().getKey();

            //uploadPicture();
        }
    }

    private void uploadPicture() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        locationViewPhoto.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteData = baos.toByteArray();
        StorageReference filepath = mStorage.child("Photos").child(photoID);

        UploadTask uploadTask = filepath.putBytes(byteData);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // handle successful uploads
                photoUri = taskSnapshot.getDownloadUrl();
                //vtoastMessage(photoUri.toString());
                // Call createSellingLocation method here. As the downloadURL needs to be handled first!!
                createSellingLocation();
            }
        });
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
                case R.id.create_closeNavBar:
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    break;
                case R.id.create_addImgBtn:
                    launchCamera();
                    break;
                case R.id.create_getLocBtn:
                    //toastMessage("Get location");
                    if (! TextUtils.isEmpty(lastAddress))
                    {
                        editRoad.setText(lastAddress);
                        editZip.setText(lastZip);
                        editCity.setText(lastCity);
                        toastMessage("Please move the number to the number field.\nThanks!");
                    }
                    break;
                case R.id.create_createLocBtn:
                    if (!TextUtils.isEmpty(photoID))
                    {
                        //toastMessage("String is not empty");
                        uploadPicture(); // and create selling location
                    }
                    else
                    {
                        //toastMessage("String is empty");
                        createSellingLocation();
                    }
                    //uploadPicture(); // And create selling location
                    break;
                case R.id.create_addSellingItems:
                    if (!TextUtils.isEmpty(specificItem5))
                    {
                        toastMessage("Maximum is 5 items");
                    }
                    else
                    {
                        showAddItemDialog("Add Item");
                    }
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
