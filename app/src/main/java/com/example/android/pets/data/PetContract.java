package com.example.android.pets.data;

import android.provider.BaseColumns;

public final class PetContract {

    // Prevent someone from accidentally instantiating the class
    private PetContract() {}

    // Inner class defining the constant values for the database table
    public static final class PetEntry implements BaseColumns {

        // Name of the database table for pets
        public final static String TABLE_NAME = "pets";

        // 5 columns: _id, name, breed, gender, weight. String = datatype of constants, not
        // the individual attributes stored in columns
        public final static String _ID = BaseColumns._ID; // Type: INTEGER
        public final static String COLUMN_PET_NAME = "name"; // Type: TEXT
        public final static String COLUMN_PET_BREED = "breed"; // Type: TEXT
        public final static String COLUMN_PET_GENDER = "gender"; // Type: INTEGER
        public final static String COLUMN_PET_WEIGHT = "weight"; // Type: INTEGER

        // Gender constants
        public final static int GENDER_UNKNOWN = 0;
        public final static int GENDER_MALE = 1;
        public final static int GENDER_FEMALE = 2;
    }
}
