package com.example.lostandfound;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CreateAdvertActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private RadioButton  rbLost, rbFound;
    private EditText     etName, etPhone, etDescription, etDate, etLocation;
    private Spinner      spinnerCategory;
    private ImageView    ivPreview;
    private Button       btnPickImage, btnPickDate, btnGetLocation, btnSave;

    private String  selectedImageUri = "";
    private double  selectedLat = 0.0;
    private double  selectedLng = 0.0;
    private DatabaseHelper db;
    private FusedLocationProviderClient fusedLocationClient;

    // Image picker
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        selectedImageUri = uri.toString();
                        ivPreview.setImageURI(uri);
                        ivPreview.setVisibility(android.view.View.VISIBLE);
                    }
                }
            });

    // Places Autocomplete
    private final ActivityResultLauncher<Intent> autocompleteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    (ActivityResult result) -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Place place = Autocomplete.getPlaceFromIntent(result.getData());
                            etLocation.setText(place.getDisplayName());
                            if (place.getLocation() != null) {
                                selectedLat = place.getLocation().latitude;
                                selectedLng = place.getLocation().longitude;
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        db = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Wire views
        rbLost          = findViewById(R.id.rbLost);
        rbFound         = findViewById(R.id.rbFound);
        etName          = findViewById(R.id.etName);
        etPhone         = findViewById(R.id.etPhone);
        etDescription   = findViewById(R.id.etDescription);
        etDate          = findViewById(R.id.etDate);
        etLocation      = findViewById(R.id.etLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        ivPreview       = findViewById(R.id.ivPreview);
        btnPickImage    = findViewById(R.id.btnPickImage);
        btnPickDate     = findViewById(R.id.btnPickDate);
        btnGetLocation  = findViewById(R.id.btnGetLocation);
        btnSave         = findViewById(R.id.btnSave);

        // Category spinner
        String[] categories = {"Electronics", "Pets", "Wallets", "Keys", "Clothing", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Date picker
        btnPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) ->
                    etDate.setText(day + "/" + (month + 1) + "/" + year),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        });
        etLocation.setFocusable(false);
        etLocation.setOnClickListener(v -> openAutocomplete());

        // Get Current Location button
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());

        // Image picker
        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Save
        btnSave.setOnClickListener(v -> saveItem());
    }
    private void openAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        autocompleteLauncher.launch(intent);
    }
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                selectedLat = location.getLatitude();
                selectedLng = location.getLongitude();
                etLocation.setText(selectedLat + ", " + selectedLng);
            } else {
                Toast.makeText(this, "Could not get location. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveItem() {
        String name        = etName.getText().toString().trim();
        String phone       = etPhone.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date        = etDate.getText().toString().trim();
        String location    = etLocation.getText().toString().trim();
        String category    = spinnerCategory.getSelectedItem().toString();
        String postType    = rbFound.isChecked() ? "Found" : "Lost";

        if (name.isEmpty() || phone.isEmpty() || description.isEmpty()
                || date.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri.isEmpty()) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        Item item = new Item();
        item.setPostType(postType);
        item.setName(name);
        item.setPhone(phone);
        item.setDescription(description);
        item.setDate(date);
        item.setLocation(location);
        item.setCategory(category);
        item.setImagePath(selectedImageUri);
        item.setLatitude(selectedLat);
        item.setLongitude(selectedLng);

        long id = db.insertItem(item);
        if (id != -1) {
            Toast.makeText(this, "Advert saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving advert", Toast.LENGTH_SHORT).show();
        }
    }
}