package dk.roadfarmer.roadfarmer.ViewActivities;

import android.accounts.Account;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import dk.roadfarmer.roadfarmer.R;

public class AccountActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener
{
    private final String title = "Login";

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

    // Firebase stuff
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_account);

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
        // Setting the title of this specific page.
        textViewTitleBar.setText(title);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.account_drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.account_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        closeNavBtn = (ImageButton) findViewById(R.id.account_closeNavBar);
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
                case R.id.account_closeNavBar:
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    break;
            }
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
                    break;
                case 2:
                    toastMessage("Indstillinger");
                    break;
                case 3:
                    toastMessage("Om");
                    break;
                case 4:
                    //toastMessage("Log ud");
                    if (firebaseAuth.getCurrentUser() != null)
                    {
                        toastMessage("Du er nu logget ud");
                        firebaseAuth.signOut();
                        // Logout of facebook - Doesn't seem to cause problems if not logged in to facebook.
                        LoginManager.getInstance().logOut();
                    }
                    else
                    {
                        toastMessage("Du er ikke logget ind");
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
                toastMessage("Map is already showing");
                break;
            case R.id.nav_account2:
                toastMessage("Trying to manage account");
                break;
            case R.id.nav_create2:
                toastMessage("Trying to create location");
                break;
            case R.id.nav_change2:
                toastMessage("Trying to change location");
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
