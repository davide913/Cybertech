package it.unive.cybertech;

import static it.unive.cybertech.utils.CachedUser.user;
import static it.unive.cybertech.utils.Showables.showShortToast;
import static it.unive.cybertech.utils.Utils.logout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import it.unive.cybertech.utils.Utils;

/**
 * ProfileActivity is the main activity that allow user to view and edit some personal, account or
 * localization info:
 * - Position can be updated from {@link #updateGPS()}
 * - Email update is manage in "{@link it.unive.cybertech.EditEmail}"
 * - Password update is manage in "{@link it.unive.cybertech.EditPassword}"
 *
 * @author Daniele Dotto
 * @since 1.0
 */
public class ProfileActivity extends AppCompatActivity {
    private final @NonNull
    Context context = ProfileActivity.this;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private final @NonNull
    FirebaseUser currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser());
    private @Nullable
    FloatingActionButton editInfo, logoutButton;
    private @Nullable
    EditText name, surname, dateOfBirth, sex, country, address, city, email, pwd;
    private @Nullable
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initActionBar();
        bindLayoutObjects();
        setTextEditTexts();
        initGPSsettings();

        getEmail().setOnClickListener(v -> startActivity(new Intent(context, EditEmail.class)));

        getPwd().setOnClickListener(v -> {
            @NonNull String provider = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getProviderId();
            if (!provider.equals(getString(R.string.googleProvider))) {
                startActivity(new Intent(context, EditPassword.class));
            } else {
                showShortToast(getString(R.string.googleProviderAlert), context);
            }
        });

        getEditInfo().setOnClickListener(v -> updateGPS());

        getLogoutButton().setOnClickListener(v -> logout(context));

    }

    /**
     * Initialize GPS settings.
     *
     * @author Daniele Dotto
     * @since 1.1
     */
    private void initGPSsettings() {
        @NonNull final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(100);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Set values to EditTexts contained in Layout.
     *
     * @author Daniele Dotto
     * @since 1.1
     */
    private void setTextEditTexts() {
        getName().setText(user.getName());
        getSurname().setText(user.getSurname());
        getSex().setText(user.getSex().toString().toUpperCase().substring(0, 1));
        if(user.getBirthDayToDate() != null) {
            @NonNull String dateOfBirthString = Utils.formatDateToString(user.getBirthDayToDate());
            getDateOfBirth().setText(dateOfBirthString);
        }

        getCountry().setText(user.getCountry());
        getCity().setText(user.getCity());
        getAddress().setText(user.getAddress());

        getEmail().setText(currentUser.getEmail());
        getPwd().setText("********");
    }

    /**
     * Bind all EditText contained in Layout.
     *
     * @author Daniele Dotto
     * @since 1.1
     */
    private void bindLayoutObjects() {
        name = findViewById(R.id.profile_name);
        surname = findViewById(R.id.profile_surname);
        sex = findViewById(R.id.profile_sex);
        dateOfBirth = findViewById(R.id.profile_dateOfBirth);

        country = findViewById(R.id.profile_country);
        city = findViewById(R.id.profile_city);
        address = findViewById(R.id.profile_address);

        email = findViewById(R.id.profile_email);
        pwd = findViewById(R.id.profile_pwd);

        editInfo = findViewById(R.id.profile_editInfo);
        logoutButton = findViewById(R.id.profile_logout);
    }

    /**
     * Behaviour according to permission given by user.
     *
     * @author Daniele Dotto
     * @since 1.0
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            updateGPS();
        } else {
            showShortToast(getString(R.string.positionPrivilegeNeeded), context);
        }
    }

    /**
     * Update GPS coordinates (latitude and longitude).
     * The user is asked to give permission for geolocalisation if they have not been given yet.
     *
     * @author Daniele Dotto
     * @since 1.0
     */
    private void updateGPS() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getFusedLocationProviderClient().getLastLocation().addOnSuccessListener(this, location -> {
                showShortToast(getString(R.string.localizationUpdated), context);
                updateValues(location);
            }).addOnFailureListener(e -> {
                showShortToast(getString(R.string.genericError), context);
                e.printStackTrace();
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }

    /**
     * Update EditText values about geolocalisation:
     *
     * @author Daniele Dotto
     * @see "{@link #country}"
     * @see "{@link #city}"
     * @see "{@link #address}"
     * @since 1.0
     */
    private void updateValues(@NonNull Location location) {
        @NonNull Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        @NonNull List<Address> addresses;
        try {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            @NonNull String newCountry = addresses.get(0).getCountryName();
            getCountry().setText(newCountry);
            @NonNull String newCity = addresses.get(0).getLocality();
            getCity().setText(newCity);
            @NonNull String newAddress = addresses.get(0).getThoroughfare();
            getAddress().setText(newAddress);
            @NonNull Thread t = new Thread(() -> user.updateLocation(newCountry, newCity, newAddress, latitude, longitude));
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finish the activity when 'go back' button is pressed.
     *
     * @author Daniele Dotto
     * @since 1.0
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize action bar.
     *
     * @author Daniele Dotto
     * @since 1.0
     */
    private void initActionBar() {
        @NonNull final ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.profile);
    }

    /**
     * Return update GPS button only if that is not null.
     *
     * @return "{@link #editInfo}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    FloatingActionButton getEditInfo() {
        return Objects.requireNonNull(editInfo);
    }

    /**
     * Return logout button only if that is not null.
     *
     * @return "{@link #logoutButton}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    FloatingActionButton getLogoutButton() {
        return Objects.requireNonNull(logoutButton);
    }

    /**
     * Return name EditText only if that is not null.
     *
     * @return "{@link #name}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    EditText getName() {
        return Objects.requireNonNull(name);
    }

    /**
     * Return surname EditText only if that is not null.
     *
     * @return "{@link #surname}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    EditText getSurname() {
        return Objects.requireNonNull(surname);
    }

    /**
     * Return date of birth EditText only if that is not null.
     *
     * @return "{@link #dateOfBirth}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    EditText getDateOfBirth() {
        return Objects.requireNonNull(dateOfBirth);
    }

    /**
     * Return sex EditText only if that is not null.
     *
     * @return "{@link #sex}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    EditText getSex() {
        return Objects.requireNonNull(sex);
    }

    /**
     * Return country EditText only if that is not null.
     *
     * @return "{@link #country}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    EditText getCountry() {
        return Objects.requireNonNull(country);
    }

    /**
     * Return city EditText only if that is not null.
     *
     * @return "{@link #city}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    EditText getCity() {
        return Objects.requireNonNull(city);
    }

    /**
     * Return address EditText only if that is not null.
     *
     * @return "{@link #address}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    EditText getAddress() {
        return Objects.requireNonNull(address);
    }

    /**
     * Return email EditText only if that is not null.
     *
     * @return "{@link #email}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    EditText getEmail() {
        return Objects.requireNonNull(email);
    }

    /**
     * Return password EditText only if that is not null.
     *
     * @return "{@link #pwd}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    EditText getPwd() {
        return Objects.requireNonNull(pwd);
    }

    /**
     * Return fusedLocationProviderClient only if that is not null.
     *
     * @return "{@link #fusedLocationProviderClient}"
     * @author Daniele Dotto
     * @since 1.1
     */
    private @NonNull
    FusedLocationProviderClient getFusedLocationProviderClient() {
        return Objects.requireNonNull(fusedLocationProviderClient);
    }


}