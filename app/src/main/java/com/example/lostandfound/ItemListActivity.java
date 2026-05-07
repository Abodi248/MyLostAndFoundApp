package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ItemListActivity extends AppCompatActivity {

    private ListView     listView;
    private Spinner      spinnerFilter;
    private DatabaseHelper db;
    private List<Item>   currentItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        db           = new DatabaseHelper(this);
        listView     = findViewById(R.id.listViewItems);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        // Filter spinner
        String[] filters = {"All", "Electronics", "Pets", "Wallets", "Keys", "Clothing", "Other"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filters);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view,
                                       int position, long id) {
                String selected = filters[position];
                loadItems(selected.equals("All") ? null : selected);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        listView.setOnItemClickListener((parent, view, position, rowId) -> {
            Item item = currentItems.get(position);
            int realId = item.getId();
            Intent intent = new Intent(ItemListActivity.this, ItemDetailActivity.class);
            intent.putExtra("ITEM_ID", realId);
            startActivity(intent);
        });

        loadItems(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String selected = spinnerFilter.getSelectedItem().toString();
        loadItems(selected.equals("All") ? null : selected);
    }

    private void loadItems(String categoryFilter) {
        currentItems = db.getAllItems(categoryFilter);
        ItemAdapter adapter = new ItemAdapter(this, currentItems);
        listView.setAdapter(adapter);

        if (currentItems.isEmpty()) {
            Toast.makeText(this, "No items found", Toast.LENGTH_SHORT).show();
        }
    }
}
