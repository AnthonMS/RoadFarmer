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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dk.roadfarmer.roadfarmer.Models.User;
import dk.roadfarmer.roadfarmer.R;

public class RegisterActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener
{
    private final String title = "Register";

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
    private Button registerBtn;
    private EditText editEmail, editPass, editEmailRe, editPassRe;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.register_drawerLayout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.register_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        closeNavBtn = (ImageButton) findViewById(R.id.closeNavBar3);
        closeNavBtn.setOnClickListener(buttonClickListener);

        registerBtn = (Button) findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(buttonClickListener);
        editEmail = (EditText) findViewById(R.id.editEmail1);
        editEmailRe = (EditText) findViewById(R.id.editEmail2);
        editPass = (EditText) findViewById(R.id.editPassword1);
        editPassRe = (EditText) findViewById(R.id.editPassword2);
        progressDialog = new ProgressDialog(this);
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
                case R.id.closeNavBar3:
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    break;
                case R.id.registerBtn:
                    registerUser();
                    break;
            }
        }
    };

    private void registerUser()
    {
        String email = editEmail.getText().toString().trim();
        String emailRe = editEmailRe.getText().toString().trim();
        String pass = editPass.getText().toString().trim();
        String passRe = editPassRe.getText().toString().trim();

        // Check if not empty
        if ( ! checkEmpty(email, emailRe, pass, passRe))
        {
            // Not empty
            progressDialog.setMessage("Registering user...");
            progressDialog.show();
            firebaseAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                toastMessage("Successfully registered");
                                progressDialog.dismiss();
                                saveNewUser();

                                // Redirect to the Menu screen for when you are logged in
                                startActivity(new Intent(RegisterActivity.this, MapsActivity.class));
                                finish();
                            }
                            else
                            {
                                //toastMessage("User NOT registered successfully");
                                progressDialog.dismiss();
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            toastLong(e.getMessage());
                            progressDialog.dismiss();
                        }
                    });
        }
        else
        {
            // Empty String
        }
    }

    private void saveNewUser()
    {
        User user = new User();
        user.setEmail(editEmail.getText().toString().trim());
        String str = firebaseAuth.getInstance().getCurrentUser().getUid();
        user.setUserID(str);

        myRootRef.child("FirebaseUsers").child(str).setValue(user);
    }


    private boolean checkEmpty(String mail, String mailR, String pass, String passR)
    {
        if (TextUtils.isEmpty(mail))
        {
            toastMessage("Email empty");
            return true;
        }
        /*if (TextUtils.isEmpty(mailR))
        {
            toastMessage("The repeated email is empty");
            return true;
        }*/
        if (TextUtils.isEmpty(pass))
        {
            toastMessage("Password empty");
            return true;
        }
        /*if (TextUtils.isEmpty(passR))
        {
            toastMessage("The repeated password is empty");
            return true;
        }*/

        return false;
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
                    toastMessage("Vælg en!");
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
            startActivity(new Intent(RegisterActivity.this, MapsActivity.class));
            finish();
        }
        else if (id == R.id.nav_login) {
            startActivity(new Intent(RegisterActivity.this, MapsActivity.class));
            finish();

        } else if (id == R.id.nav_register) {
            //toastMessage("Already in Register");
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
