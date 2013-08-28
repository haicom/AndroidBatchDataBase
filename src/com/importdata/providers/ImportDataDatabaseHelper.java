package com.importdata.providers;

import com.importdata.providers.ImportData.Account;
import com.importdata.providers.ImportData.EnergyCost;
import com.importdata.providers.ImportData.HeatConsumption;
import com.importdata.providers.ImportData.HeatIngestion;
import com.importdata.providers.ImportData.Information;
import com.importdata.providers.ImportData.Ingestion;
import com.importdata.providers.ImportData.UnitSettings;
import com.importdata.providers.ImportData.WeightChange;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ImportDataDatabaseHelper extends SQLiteOpenHelper {
    private final static String TAG = "ImportDateDatabaseHelper";
    private static ImportDataDatabaseHelper sInstance = null;
    static final String DATABASE_NAME = "data.db";
    static final int DATABASE_VERSION = 2;

    public interface Views {
        public static final String COST_WEEKLY = "view_cost_weekly";
        public static final String COST_MONTHLY = "view_cost_monthly";
        public static final String COST_YEARLY = "view_cost_yearly";
        public static final String INGESTION_WEEKLY = "view_ingestion_weekly";
        public static final String INGESTION_MONTHLY = "view_ingestion_monthly";
        public static final String INGESTION_YEARLY = "view_ingestion_yearly";
        public static final String WEIGHT_WEEKLY = "view_weight_weekly";
        public static final String WEIGHT_MONTHLY = "view_weight_monthly";
        public static final String WEIGHT_YEARLY = "view_weight_yearly";
        public static final String BASE_INFO = "view_base_info";
    }

    private ImportDataDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Return a singleton helper for the combined Breezing health
     * database.
     */
    /* package */
    static synchronized ImportDataDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ImportDataDatabaseHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAccountTables(db);
        createInformationTables(db);
        createEnergyCostTables(db);
        createIngestionTables(db);
        createWeightChangeTables(db);
        createHeatConsumptionTables(db);
        createHeatIngestionTables(db);
        createUnitSettings(db);

    }


    private void createAccountTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ImportDataProvider.TABLE_ACCOUNT  + " ("
              +  Account._ID +  " INTEGER PRIMARY KEY, "
              +  Account.ACCOUNT_NAME + " TEXT NOT NULL , "
              +  Account.ACCOUNT_ID +  " INTEGER NOT NULL DEFAULT 0 , "
              +  Account.ACCOUNT_PASSWORD +  " TEXT NOT NULL " +
                   ");");
    }

    private void createInformationTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ImportDataProvider.TABLE_INFORMATION  + " ("
              +   Information._ID + " INTEGER PRIMARY KEY, "
              +   Information.ACCOUNT_ID + " INTEGER NOT NULL , "
              +   Information.GENDER +  " INTEGER NOT NULL , "
              +   Information.HEIGHT +  " FLOAT NOT NULL , "
              +   Information.BIRTHDAY + " INTEGER NOT NULL , "
              +   Information.CUSTOM + " INTEGER NOT NULL  " +
                   ");");
    }


    private void createEnergyCostTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ImportDataProvider.TABLE_COST  + " ("
                +  EnergyCost._ID + " INTEGER PRIMARY KEY, "
                +  EnergyCost.ACCOUNT_ID + " INTEGER NOT NULL , "
                +  EnergyCost.METABOLISM + " INTEGER NOT NULL , "
                +  EnergyCost.SPORT + " INTEGER NOT NULL , "
                +  EnergyCost.DIGEST + " INTEGER NOT NULL , "
                +  EnergyCost.TRAIN + " INTEGER NOT NULL , "
                +  EnergyCost.TOTAL_ENERGY + " INTEGER NOT NULL , "
                +  EnergyCost.ENERGY_COST_DATE + " INTEGER NOT NULL , "
                +  EnergyCost.DATE + " INTEGER NOT NULL , "
                +  EnergyCost.YEAR + " INTEGER NOT NULL , "
                +  EnergyCost.YEAR_MONTH + " INTEGER NOT NULL , "
                +  EnergyCost.YEAR_WEEK + " INTEGER NOT NULL " +
                   ");");
    }


    private void createIngestionTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ImportDataProvider.TABLE_INGESTION  + " ("
                  + Ingestion._ID + " INTEGER PRIMARY KEY, "
                  + Ingestion.ACCOUNT_ID + " INTEGER NOT NULL , "
                  + Ingestion.BREAKFAST + " INTEGER NOT NULL , "
                  + Ingestion.LUNCH + " INTEGER NOT NULL , "
                  + Ingestion.DINNER + " INTEGER NOT NULL , "
                  + Ingestion.ETC + " INTEGER NOT NULL , "
                  + Ingestion.TOTAL_INGESTION + " INTEGER NOT NULL , "
                  + Ingestion.DATE + " INTEGER NOT NULL , "
                  + Ingestion.YEAR + " INTEGER NOT NULL , "
                  + Ingestion.YEAR_MONTH + " INTEGER NOT NULL , "
                  + Ingestion.YEAR_WEEK + " INTEGER NOT NULL " +
                   ");");
    }



    private void createWeightChangeTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ImportDataProvider.TABLE_WEIGHT  + " ("
                 + WeightChange._ID + " INTEGER PRIMARY KEY, "
                 + WeightChange.ACCOUNT_ID + " INTEGER NOT NULL , "
                 + WeightChange.WEIGHT + " FLOAT NOT NULL , "
                 + WeightChange.EXPECTED_WEIGHT + " FLOAT NOT NULL , "
                 + WeightChange.DATE + " INTEGER NOT NULL , "
                 + WeightChange.YEAR + " INTEGER NOT NULL , "
                 + WeightChange.YEAR_MONTH + " INTEGER NOT NULL , "
                 + WeightChange.YEAR_WEEK + " INTEGER NOT NULL " +
                   ");");
    }




    private void createHeatConsumptionTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ImportDataProvider.TABLE_HEAT_CONSUMPTION  + " ("
                 +  HeatConsumption._ID + " INTEGER PRIMARY KEY , "
                 +  HeatConsumption.SPORT_TYPE + " TEXT NOT NULL , "
                 +  HeatConsumption.SPORT_LONG + " INTEGER , "
                 +  HeatConsumption.SPORT_STRENGTH + " TEXT NOT NULL, "
                 +  HeatConsumption.SPORT_DISTANCE + " INTEGER , "
                 +  HeatConsumption.SPORT_TIMES + " INTEGER , "
                 +  HeatConsumption.CALORIE + " INTEGER NOT NULL , "
                 +  HeatConsumption.DATE + " INTEGER NOT NULL " +
                   ");");
    }


    private void createHeatIngestionTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ImportDataProvider.TABLE_HEAT_INGESTION  + " ("
                   +  HeatIngestion._ID + " INTEGER PRIMARY KEY , "
                   +  HeatIngestion.FOOD_TYPE + " TEXT NOT NULL , "
                   +  HeatIngestion.FOOD_NAME + " TEXT NOT NULL , "
                   +  HeatIngestion.NAME_EXPRESS + " TEXT NOT NULL , "
                   +  HeatIngestion.PRIORITY + " INTEGER , "
                   +  HeatIngestion.FOOD_SIZE + " INTEGER NOT NULL , "
                   +  HeatIngestion.CALORIE + " INTEGER NOT NULL " +
                   ");");


    }


    private void createUnitSettings(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ImportDataProvider.TABLE_UNIT_SETTINGS  + " ("
                +  UnitSettings._ID + " INTEGER PRIMARY KEY , "
                +  UnitSettings.UNIT_TYPE + " TEXT NOT NULL , "
                +  UnitSettings.UNIT_NAME + " TEXT NOT NULL  " +
                ");");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, " onUpgrade oldVersion = " + oldVersion + " newVersion = " + newVersion);

        if (oldVersion == 1) {
            upgradeToVersion202(db);
        }
    }

    private void upgradeToVersion202(SQLiteDatabase db) {
        db.execSQL(
                "ALTER TABLE " + ImportDataProvider.TABLE_COST +
                " ADD " + EnergyCost.ENERGY_COST_DATE + " INTEGER;");
    }

}
