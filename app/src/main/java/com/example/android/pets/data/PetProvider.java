package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;


import com.example.android.pets.data.PetContract.PetEntry;



/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    // Tag for the log messages
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    // URI Matcher code for the content URI for the pets table
    private static final int PETS = 100;

    // URI Matcher code for the content URI for a single pet in the pets table
    private static final int PET_ID = 101;

    /**
     * URIMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patters the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code
        // to return when a match is found.

        // Uri Matcher where we act on the entire pets table
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        // Uri Matcher where we act on a single pet in the pets table
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /** Created and initialized PetDbHelper object for access to pets DB: a global variable,
     * & can be referenced from other ContentProvider methods. */
    private PetDbHelper mDbHelper;


    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        /*
        Instead of using this keyword as the argument like in the other activities getContext()
        is passed instead since I am instantiating an abstract class since it's extended directly
        from the java.lang.Object [inheriting properties of ContentProvider and other classes it
        extends from]
         */
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This will hold the query result
        Cursor cursor;

        // To figure out if URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                /*
                For PETS code, query pets table directly with the given projection, selection,
                selection arguments, and sort order. The cursor could contain multiple rows of
                the pets table.
                 */
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PET_ID:
                /*
                For the PET_ID code, extract ID from URI.
                i.e. "content://com.example.android.pets/pets/3"
                The selection = "_id=?" and argument = string array with ID (3).
                For every "?" in selection, need element in selectionArgs with ratio 1:1.
                i.e. SQL statement of SELECT id FROM pets WHERE _id=3
                 */
                selection = PetEntry._ID + "=?";
                // The following line extracts the number from the query and converts to string
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform query on pets table where _id = 3 to return cursor w/ that row.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query known URI " + uri);
        }

        // Set notification URI on the Cursor, so we know what content URI the Cursor was created
        // for. If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }


    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        // Check if there's a match
        final int match = sUriMatcher.match(uri);
        // Determine which case it falls into -- ONLY PETS CASE supports insertion since acting on
        // entire table. Otherwise it will resort to default.
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert new pet into DB with content values. Return new content URI for specific row in DB.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        // Check that name is not null-- added native isEmpty check;
        // could also be || name.isEmpty() ) {...
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null || TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        // Check that gender is valid
        // Could be simplified to return gender == GENDER_UNKNOWN || gender == GENDER_MALE ||
        // gender == GENDER_FEMALE;
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        // Check that weight is valid
        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        // No need to check breed, any value including null is valid.

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert new pet with given values
        long id = database.insert(PetEntry.TABLE_NAME, null, values);
        // If ID is -1, insertion failed. Log error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert new row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        // uri: content://com.example.android.pets/pets
        getContext().getContentResolver().notifyChange(uri, null);

        // Return new URI with ID (of newly inserted row) appended to end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract ID from URI so we know which row to update.
                // Selection will be "_id=?" and selectionArgs will be a String array containing
                // actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);

        }

    }

    /**
     * Update pets in teh DB with the given content values. Apply changes to the rows specified
     * in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     * uri parameter used in lesson 4**
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present, check that the name value
        // is not null.
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }
        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present, check that the gender
        // value is valid.
        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present, check that the weight
        // value is valid.
        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        if (values.size() == 0) {
        // No need to check breed as any value is valid (including null).
        return 0;
        }

        // Otherwise, get writable database to update data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the given
        // URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the given
        // URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }
}