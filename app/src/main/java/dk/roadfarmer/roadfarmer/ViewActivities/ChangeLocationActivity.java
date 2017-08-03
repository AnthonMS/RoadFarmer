package dk.roadfarmer.roadfarmer.ViewActivities;

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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

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
