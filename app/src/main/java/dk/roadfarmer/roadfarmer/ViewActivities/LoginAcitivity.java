package dk.roadfarmer.roadfarmer.ViewActivities;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import dk.roadfarmer.roadfarmer.Models.User;
import dk.roadfarmer.roadfarmer.R;

public class LoginAcitivity extends AppCompatActivity implements
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

    List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login_acitivity);

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
        userList = new ArrayList<>();

        progressDialog = new ProgressDialog(this);

        callbackManager = CallbackManager.Factory.create();
        facebookLoginBtn = (LoginButton) findViewById(R.id.facebookLoginBtn);

        loginWithFb();
    }

    private void loginWithFb()
    {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                //toastMessage("Successfully logged in with Fb\n" + loginResult.getAccessToken());
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
        user.setEmail(fireUser.getEmail());
        user.setFullName(fireUser.getDisplayName());
        user.setPhone(fireUser.getPhoneNumber());
        user.setUserID(fireUser.getUid());

        myRootRef.child("FacebookUsers").child(fireUser.getUid()).setValue(user);

        /*boolean checkUser = false;
        for (User u : userList)
        {
            if (user.equals(u) == true)
            {
                // User already exist
                checkUser = true;
            }
            else
            {
                // New facebook user signing in

            }
        }*/
    }

    /*private void createUserList()
    {
        myRootRef.child("FacebookUsers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    User user = ds.getValue(User.class);
                    userList.add(user);
                    toastMessage(user.getFullName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/

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
                    toastMessage("Log ud");
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
        int id = item.getItemId();

        if (item.getItemId() == R.id.nav_kort) {
            startActivity(new Intent(LoginAcitivity.this, MapsActivity.class));
            finish();
        }
        else if (id == R.id.nav_login) {
            //toastMessage("Already in login");

        } else if (id == R.id.nav_register) {
            startActivity(new Intent(LoginAcitivity.this, RegisterActivity.class));
            finish();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
