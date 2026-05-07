package com.example.lostandfound;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lostandfound.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ITEMS = "items";

    public static final String COL_ID          = "id";
    public static final String COL_POST_TYPE   = "post_type";
    public static final String COL_NAME        = "name";
    public static final String COL_PHONE       = "phone";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE        = "date";
    public static final String COL_LOCATION    = "location";
    public static final String COL_CATEGORY    = "category";
    public static final String COL_IMAGE_PATH  = "image_path";
    public static final String COL_TIMESTAMP   = "timestamp";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
            COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_POST_TYPE   + " TEXT, " +
            COL_NAME        + " TEXT, " +
            COL_PHONE       + " TEXT, " +
            COL_DESCRIPTION + " TEXT, " +
            COL_DATE        + " TEXT, " +
            COL_LOCATION    + " TEXT, " +
            COL_CATEGORY    + " TEXT, " +
            COL_IMAGE_PATH  + " TEXT, " +
            COL_TIMESTAMP   + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    public long insertItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_POST_TYPE,   item.getPostType());
        values.put(COL_NAME,        item.getName());
        values.put(COL_PHONE,       item.getPhone());
        values.put(COL_DESCRIPTION, item.getDescription());
        values.put(COL_DATE,        item.getDate());
        values.put(COL_LOCATION,    item.getLocation());
        values.put(COL_CATEGORY,    item.getCategory());
        values.put(COL_IMAGE_PATH,  item.getImagePath());
        long id = db.insert(TABLE_ITEMS, null, values);
        db.close();
        return id;
    }

    public List<Item> getAllItems(String categoryFilter) {
        List<Item> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = null;
        String[] selectionArgs = null;
        if (categoryFilter != null && !categoryFilter.equals("All")) {
            selection = COL_CATEGORY + " = ?";
            selectionArgs = new String[]{categoryFilter};
        }

        Cursor cursor = db.query(TABLE_ITEMS, null, selection, selectionArgs,
                null, null, COL_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public Item getItemById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, null,
                COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
        Item item = null;
        if (cursor.moveToFirst()) {
            item = cursorToItem(cursor);
        }
        cursor.close();
        db.close();
        return item;
    }

    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    private Item cursorToItem(Cursor cursor) {
        Item item = new Item();
        item.setId(       cursor.getInt(   cursor.getColumnIndexOrThrow(COL_ID)));
        item.setPostType( cursor.getString(cursor.getColumnIndexOrThrow(COL_POST_TYPE)));
        item.setName(     cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
        item.setPhone(    cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)));
        item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
        item.setDate(     cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
        item.setLocation( cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)));
        item.setCategory( cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
        item.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH)));
        item.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
        return item;
    }
}
