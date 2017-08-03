package dk.roadfarmer.roadfarmer.ViewActivities;

import android.accounts.Account;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
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

import org.w3c.dom.Text;

import java.util.Locale;

import dk.roadfarmer.roadfarmer.Models.User;
import dk.roadfarmer.roadfarmer.R;

public class AccountActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener
{
    private DrawerLayout mDrawerLayout;

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
    private TextView textViewName, textViewPhone, textViewMail, textViewSellingLoc;
    private User userModel;
    private final Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_account);

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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.account_drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.account_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_account2);
        closeNavBtn = (ImageButton) findViewById(R.id.account_closeNavBar);
        closeNavBtn.setOnClickListener(buttonClickListener);

        textViewName = (TextView) findViewById(R.id.account_txtViewName);
        textViewName.setOnClickListener(buttonClickListener);
        textViewMail = (TextView) findViewById(R.id.account_txtViewMail);
        textViewMail.setOnClickListener(buttonClickListener);
        textViewPhone = (TextView) findViewById(R.id.account_txtViewPhone);
        textViewPhone.setOnClickListener(buttonClickListener);
        textViewSellingLoc = (TextView) findViewById(R.id.account_txtViewSellingLoc);
        textViewSellingLoc.setOnClickListener(buttonClickListener);

        getUserInfoFromFirebase();

    }

    private void getUserInfoFromFirebase()
    {
        //toastMessage("TestRoot " + firebaseAuth.getCurrentUser().getUid());
        myRootRef.child("Users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("UserInfo")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                User tempUser = dataSnapshot.getValue(User.class);
                userModel = tempUser;
                //toastMessage(tempUser.getEmail());

                if (TextUtils.isEmpty(tempUser.getFullName()))
                {
                    // Name is empty
                    textViewName.setText("NoName");
                    textViewName.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                }
                else
                {
                    // Name is not empty
                    textViewName.setText(tempUser.getFullName());
                    textViewName.setClickable(false);
                }
                if (TextUtils.isEmpty(tempUser.getEmail()))
                {
                    // Email is empty
                    textViewMail.setText("NoMail");
                    textViewMail.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                }
                else
                {
                    // Email is not empty
                    textViewMail.setText(tempUser.getEmail());
                    textViewMail.setClickable(false);
                }
                if (TextUtils.isEmpty(tempUser.getPhone()))
                {
                    // phone is empty
                    textViewPhone.setText("NoPhone");
                    textViewPhone.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                }
                else
                {
                    // Phone is not empty
                    textViewPhone.setText(tempUser.getPhone());
                    textViewPhone.setClickable(false);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                case R.id.account_closeNavBar:
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    break;
                case R.id.account_txtViewName:
                    toastMessage("Trying to change name");
                    showCustomDialog("Add name", 0);
                    break;
                case R.id.account_txtViewMail:
                    toastMessage("Trying to change mail");
                    showCustomDialog("Add mail", 1);
                    break;
                case R.id.account_txtViewPhone:
                    toastMessage("Trying to change phone no.");
                    showCustomDialog("Add phone", 2);
                    break;
                case R.id.account_txtViewSellingLoc:
                    toastMessage("Trying to go to ChangeSellingLocation");
                    break;
            }
        }
    };

    private void editMissingInfo(int i, String newValue)
    {
        switch (i)
        {
            case 0:
                userModel.setFullName(newValue);
                textViewName.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                textViewName.setClickable(false);
                break;
            case 2:
                userModel.setPhone(newValue);
                textViewPhone.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                textViewPhone.setClickable(false);
                break;
            case 1:
                userModel.setEmail(newValue);
                textViewMail.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                textViewMail.setClickable(false);
                break;
        }

        myRootRef.child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("UserInfo").setValue(userModel);
    }

    private void showCustomDialog(String title, int i)
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog_change_value);
        dialog.setTitle(title);
        dialog.setCanceledOnTouchOutside(false);

        // Custom dialog components
        TextView titleView = (TextView) dialog.findViewById(R.id.dialog_titleView);
        titleView.setText(title);

        final EditText dialog_editValue = (EditText) dialog.findViewById(R.id.dialog_editValue);
        final int tempInt = i;

        Button dialog_changeBtn = (Button) dialog.findViewById(R.id.dialog_changeBtn);
        dialog_changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                String tempStr = dialog_editValue.getText().toString().trim();

                showYesNoDialog(getString(R.string.dialog_noGoingBack), getString(R.string.dialog_adding) + tempStr, tempInt, tempStr);
                //editMissingInfo(tempInt, tempStr);
            }
        });

        Button dialog_cancelBtn = (Button) dialog.findViewById(R.id.dialog_cancelBtn);
        dialog_cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { dialog.dismiss(); }
        });

        dialog.show();
    }

    private void showYesNoDialog(String title, String body, final int tempInt, final String newValue)
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog_yes_no);
        dialog.setTitle(title);
        dialog.setCanceledOnTouchOutside(false);

        // Custom dialog components
        TextView titleView = (TextView) dialog.findViewById(R.id.dialog_titleView2);
        titleView.setText(title);
        TextView bodyView = (TextView) dialog.findViewById(R.id.dialog_bodyView2);
        bodyView.setText(body);

        Button yesBtn = (Button) dialog.findViewById(R.id.dialog_yesBtn);
        Button noBtn = (Button) dialog.findViewById(R.id.dialog_noBtn);
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMissingInfo(tempInt, newValue);
                dialog.dismiss();
            }
        });
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

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

                    startActivity(new Intent(AccountActivity.this, MapsActivity.class));
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

    // test

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
                Intent intent = new Intent(AccountActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_account2:
                toastMessage(getString(R.string.toast_accountShowing));
                break;
            case R.id.nav_create2:
                //toastMessage("Trying to create Location");
                Intent intent2 = new Intent(AccountActivity.this, CreateLocationActivity.class);
                startActivity(intent2);
                finish();
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
