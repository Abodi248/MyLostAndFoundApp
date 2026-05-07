package com.example.lostandfound;

import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        db     = new DatabaseHelper(this);
        itemId = getIntent().getIntExtra("ITEM_ID", -1);

        android.util.Log.d("DETAIL", "Received ITEM_ID = " + itemId);

        if (itemId == -1) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Item item = db.getItemById(itemId);
        if (item == null) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvDetailPostType))
                .setText(item.getPostType());
        ((TextView) findViewById(R.id.tvDetailName))
                .setText("Name: " + item.getName());
        ((TextView) findViewById(R.id.tvDetailPhone))
                .setText("Phone: " + item.getPhone());
        ((TextView) findViewById(R.id.tvDetailDescription))
                .setText("Description: " + item.getDescription());
        ((TextView) findViewById(R.id.tvDetailDate))
                .setText("Date: " + item.getDate());
        ((TextView) findViewById(R.id.tvDetailLocation))
                .setText("Location: " + item.getLocation());
        ((TextView) findViewById(R.id.tvDetailCategory))
                .setText("Category: " + item.getCategory());
        ((TextView) findViewById(R.id.tvDetailTimestamp))
                .setText("Posted: " + item.getTimestamp());

        ImageView ivDetail = findViewById(R.id.ivDetailImage);
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                ivDetail.setImageURI(Uri.parse(item.getImagePath()));
            } catch (Exception e) {
                ivDetail.setVisibility(android.view.View.GONE);
            }
        }

        Button btnRemove = findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(v -> confirmRemove());
    }

    private void confirmRemove() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Advert")
                .setMessage("Are you sure you want to remove this advert?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    db.deleteItem(itemId);
                    Toast.makeText(this, "Advert removed", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
