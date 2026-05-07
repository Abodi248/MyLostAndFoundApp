package com.example.lostandfound;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CreateAdvertActivity extends AppCompatActivity {

    private RadioButton  rbLost, rbFound;
    private EditText     etName, etPhone, etDescription, etDate, etLocation;
    private Spinner      spinnerCategory;
    private ImageView    ivPreview;
    private Button       btnPickImage, btnPickDate, btnSave;

    private String       selectedImageUri = "";
    private DatabaseHelper db;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        db = new DatabaseHelper(this);

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
        btnSave         = findViewById(R.id.btnSave);

        String[] categories = {"Electronics", "Pets", "Wallets", "Keys", "Clothing", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Date picker
        btnPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String date = day + "/" + (month + 1) + "/" + year;
                etDate.setText(date);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
               cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Image picker
        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Save
        btnSave.setOnClickListener(v -> saveItem());
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

        long id = db.insertItem(item);
        if (id != -1) {
            Toast.makeText(this, "Advert saved!", Toast.LENGTH_SHORT).show();
            finish(); // go back
        } else {
            Toast.makeText(this, "Error saving advert", Toast.LENGTH_SHORT).show();
        }
    }
}
