package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class PetContract {

    // Prevent someone from accidentally instantiating the class
    private PetContract() {}

    /**
     * Content authority = name for Content Provider akin to relationship between
     * domain and it's website. Use package name for app as string since it's unique
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";

    /*
     Use CONTENT_AUTHORITY to create URI base apps will use to contact content provider
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*
     Possible path for looking at pet data
     */
    public static final String PATH_PETS = "pets";



    // Inner class defining the constant values for the database table
    public static final class PetEntry implements BaseColumns {

        // Content URI to access pet data in provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         * CURSOR_DIR_BASE_TYPE maps to constant "vnd.android.cursor.dir"
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         * CURSOR_ITEM_BASE_TYPE maps to constant "vnd.android.cursor.item"
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        // Name of the database table for pets
        public final static String TABLE_NAME = "pets";

        /**
         * 5 columns: _id, name, breed, gender, weight. String = datatype of constants, not
         *          the individual attributes stored in columns.
          */
        // Unique ID number for pet (only used in DB table)
        public final static String _ID = BaseColumns._ID; // Type: INTEGER
        public final static String COLUMN_PET_NAME = "name"; // Type: TEXT
        public final static String COLUMN_PET_BREED = "breed"; // Type: TEXT
        public final static String COLUMN_PET_GENDER = "gender"; // Type: INTEGER
        public final static String COLUMN_PET_WEIGHT = "weight"; // Type: INTEGER

        // Gender constants
        public final static int GENDER_UNKNOWN = 0;
        public final static int GENDER_MALE = 1;
        public final static int GENDER_FEMALE = 2;

        /**
         * Returns whether or not the given gender is {@link #GENDER_UNKNOWN}, {@link #GENDER_MALE},
         * or {@link #GENDER_FEMALE}.
         */
        public static boolean isValidGender(int gender) {
            return gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE;
        }
    }
}
