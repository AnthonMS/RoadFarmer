package dk.roadfarmer.roadfarmer.ViewActivities;

import android.app.ProgressDialog;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dk.roadfarmer.roadfarmer.Models.User;
import dk.roadfarmer.roadfarmer.R;

public class LoginAcitivity extends AppCompatActivity implements
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
    private String chosenLanguage;

    // Firebase stuff
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRootRef;

    // Instatiate the stuff from layout xml file
    private Button loginBtn;
    private TextView forgotPassText, registerText;
    private EditText editEmail, editPass;
    private ProgressDialog progressDialog;
    private LoginButton facebookLoginBtn;
    private CallbackManager callbackManager;

    //List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login_acitivity);

        Intent intent = getIntent();
        //chosenLanguage = intent.getExtras().getString("selectedLanguage");
        SharedPreferences sharedPref = getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
        chosenLanguage = sharedPref.getString("currentLanguage", "");
        //toastMessage(testString);

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
        textViewTitleBar.setText(getString(R.string.title_activity_login));
        // Setting the selected language in the spinner if user selected on himself
        int pos = adapterLang.getPosition(chosenLanguage);
        spinnerLang.setSelection(pos);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.login_drawerLayout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.login_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        closeNavBtn = (ImageButton) findViewById(R.id.closeNavBar2);
        closeNavBtn.setOnClickListener(buttonClickListener);

        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(buttonClickListener);
        forgotPassText = (TextView) findViewById(R.id.forgotPassText);
        forgotPassText.setOnClickListener(buttonClickListener);
        registerText = (TextView) findViewById(R.id.registerText);
        registerText.setOnClickListener(buttonClickListener);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editPass = (EditText) findViewById(R.id.editPassword);
        //userList = new ArrayList<>();

        progressDialog = new ProgressDialog(this);

        callbackManager = CallbackManager.Factory.create();
        facebookLoginBtn = (LoginButton) findViewById(R.id.facebookLoginBtn);
        facebookLoginBtn.setReadPermissions("user_friends");
        facebookLoginBtn.setReadPermissions("public_profile");
        facebookLoginBtn.setReadPermissions("email");
        facebookLoginBtn.setReadPermissions("user_birthday");

        loginWithFb();
    }
    private void loginWithFb()
    {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                //toastMessage("Successfully logged in with Fb\n" + loginResult.getAccessToken());

                requestUserProfile(loginResult);

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                toastMessage("Login canceled");

            }

            @Override
            public void onError(FacebookException error) {
                toastMessage("Error: " + error.getMessage());
            }
        });
    }

    private String fbName, fbMail;
    private void requestUserProfile(LoginResult loginResult)
    {
        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                // Get facebook data from login
                Bundle bFacebookData = getFacebookData(object);
                String fName = bFacebookData.getString("first_name");
                String lName = bFacebookData.getString("last_name");
                String eemail = bFacebookData.getString("email");
                fbName = fName + " " + lName;
                fbMail = eemail;

                //toastMessage(fName + " " + lName + " " + eemail);
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private Bundle getFacebookData(JSONObject object)
    {
        Bundle bundle = new Bundle();
        try {
            String id = object.getString("id");

            bundle.putString("idFacebook", id);
            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
            if (object.has("last_name"))
                bundle.putString("last_name", object.getString("last_name"));
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));
            if (object.has("gender"))
                bundle.putString("gender", object.getString("gender"));
            if (object.has("birthday"))
                bundle.putString("birthday", object.getString("birthday"));
            if (object.has("location"))
                bundle.putString("location", object.getJSONObject("location").getString("name"));

        }
        catch(JSONException e) {

        }

        return bundle;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token)
    {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // task successful
                            toastMessage("Login successful");
                            saveNewFacebookUser();
                            startActivity(new Intent(LoginAcitivity.this, MapsActivity.class));
                            finish();
                        }
                        else
                        {
                            // Task not successful
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                toastLong(e.getMessage());
            }
        });
    }

    private void saveNewFacebookUser()
    {
        User user = new User();
        FirebaseUser fireUser = firebaseAuth.getInstance().getCurrentUser();

        /*String providerID = "";
        String providerName = "";
        String providerEmail = "";

        for (UserInfo profile : fireUser.getProviderData())
        {
            providerID = profile.getProviderId();
            providerName = profile.getDisplayName();
            providerEmail = profile.getEmail();

        }*/
        //toastLong("Profile: " + providerName + " " + providerEmail + " " + providerID);
        //toastLong("User: " + fireUser.getDisplayName() + " " + fireUser.getEmail() + " " + fireUser.getUid());

        user.setEmail(fbMail);
        user.setFullName(fbName);
        user.setPhone(fireUser.getPhoneNumber());
        user.setUserID(fireUser.getUid());



        myRootRef.child("Users").child(fireUser.getUid()).child("UserInfo").setValue(user);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //toastMessage("testing pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //toastMessage("testing resume");
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
                case R.id.loginBtn:
                    userLogin();
                    break;
                case R.id.registerText:
                    //toastMessage("Trying to register");
                    finish();
                    startActivity(new Intent(LoginAcitivity.this, MapsActivity.class));
                    break;
                case R.id.forgotPassText:
                    toastMessage("Trying to forgot pass");
                    break;
                case R.id.closeNavBar2:
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    break;
            }
        }
    };



    private void userLogin()
    {
        String email = editEmail.getText().toString().trim();
        String pass = editPass.getText().toString().trim();
        if((TextUtils.isEmpty(email)) || (TextUtils.isEmpty(pass)))
        {
            // Email or Password empty
            toastMessage("Please enter valid values");
            return;
        }

        progressDialog.setMessage("Checking credentials...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        progressDialog.dismiss();
                        if (task.isSuccessful())
                        {
                            toastMessage("Login successful");
                            // Redirect to the Menu screen for when you are logged in
                            startActivity(new Intent(LoginAcitivity.this, MapsActivity.class));
                            finish();
                        }
                        else
                        {
                            toastMessage("Login unsuccessful");
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        progressDialog.dismiss();
                        toastLong(e.getMessage());
                        //toastMessage("Login unsuccessful");
                    }
                });

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
                        toastMessage(getString(R.string.toast_loggedOut));
                        firebaseAuth.signOut();
                        // Logout of facebook - Doesn't seem to cause problems if not logged in to facebook.
                        LoginManager.getInstance().logOut();
                    }
                    else
                    {
                        toastMessage(getString(R.string.toast_notLoggedIn));
                    }

                    startActivity(new Intent(LoginAcitivity.this, MapsActivity.class));
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_kort:
                Intent intent = new Intent(LoginAcitivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_login:
                toastMessage(getString(R.string.toast_loginShowing));
                break;
            case R.id.nav_register:
                Intent intent2 = new Intent(LoginAcitivity.this, RegisterActivity.class);
                startActivity(intent2);
                finish();
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
