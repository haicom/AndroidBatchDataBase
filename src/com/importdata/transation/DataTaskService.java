package com.importdata.transation;





import com.importdata.R;
import com.importdata.providers.ImportData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.app.Activity;
import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;


public class DataTaskService extends Service {
    public  static final String TAG = "DataTaskService";
    private static final String TAG_DATAS = "datas";
    private ServiceHandler mServiceHandler;
    private ArrayList<DataInfo> mDataInfo;
    private ArrayList<ContentProviderOperation> mOps;
    private Looper mServiceLooper;
    private int mResultCode;


    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        Log.d(TAG, "onCreate()");
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mDataInfo = new ArrayList<DataInfo>();
        mOps = new ArrayList<ContentProviderOperation>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
     // Temporarily removed for this duplicate message track down.
        mResultCode = intent != null ? intent.getIntExtra("result", 0) : 0;
        if (mResultCode != 0) {
            Log.v(TAG, "onStart: #" + startId + " mResultCode: " + mResultCode +
                    " = " + translateResultCode(mResultCode));
        }

        Log.v(TAG, "onStart: #" + startId + ": " + intent.getExtras() + " intent = " + intent);
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);

        return Service.START_NOT_STICKY;
    }

    private static String translateResultCode(int resultCode) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                return "Activity.RESULT_OK";
            default:
                return "Unknown error code";
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Handle incoming transaction requests. The incoming requests are
         * initiated
         */
        @Override
        public void handleMessage(Message msg) {
            int serviceId = msg.arg1;
            Intent intent = (Intent) msg.obj;
            Log.d(TAG, "handleMessage serviceId: " + serviceId 	+ " intent: " + intent);


            if (intent != null) {
                String action = intent.getAction();

                int error = intent.getIntExtra("errorCode", 0);

                Log.v(TAG, "handleMessage action: " + action + " error: " + error);
                if (ACTION_IMPORT_DATA.endsWith(action)) {
                    handleDataImport();
                } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                    handleDataImport();
                }
            }

            // NOTE: We MUST not call stopSelf() directly, since we need to
            // make sure the wake lock acquired by AlertReceiver is released.
            DataReceiver.finishStartingService(DataTaskService.this,  serviceId);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /***
     * 处理数据批量导入，查看数据有无更新如果更新的话则把所有数据批量添加到
     * ArrayList<ContentProviderOperation> 队列中,最后一次性批处理
     */
    public synchronized void handleDataImport() {
        //先判断是否数据有更新
        if ( loadAllDataUpdateInfo() ) {
            for (DataInfo dataInfo: mDataInfo) {
                String fileName = dataInfo.getName() + ".xml";
                Log.d(TAG, " handleDataImport fileName = " + fileName +
                        " dataInfo.getType() = " + dataInfo.getType());
                if ( CONTENT_INSERT.equals( dataInfo.getType() ) ) {
                    insertDataToDB(fileName, dataInfo);
                } else if ( CONTENT_UPDATE.equals( dataInfo.getType() ) ) {
                    updateDataToDB(fileName, dataInfo);
                } else if ( CONTENT_DELETE.equals( dataInfo.getType() ) ) {
                    deleteDataToDB(dataInfo);
                }
            }

            try {
                getContentResolver().applyBatch(ImportData.AUTHORITY, mOps);
                for (DataInfo dataInfo: mDataInfo) {
                    saveSharedPrefsVersion(dataInfo.getName(), dataInfo.getVersion());
                }
            } catch (Exception e) {
                // Log exception
                Log.e(TAG, "Exceptoin encoutered while inserting contact: " + e);
            }
        }

    }


    /**
     * Loads the all need to update database info  from an data_change.xml file.
     *
     *
     */
    private boolean loadAllDataUpdateInfo() {
        boolean loadData = false;
        try {
            XmlResourceParser parser = getResources().getXml(R.xml.data_change);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            beginDocument(parser, TAG_DATAS);

            final int depth = parser.getDepth();

            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG ||
                    parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                if (type != XmlPullParser.START_TAG) {
                    continue;
                }

                TypedArray a = obtainStyledAttributes(attrs, R.styleable.Data);
                String dataName = a.getString(R.styleable.Data_name);
                String dataType = a.getString(R.styleable.Data_type);
                String dataVersion = a.getString(R.styleable.Data_version);
                String dataObject = a.getString(R.styleable.Data_object);
                //查找版本是否已经更新，如果更新即放入mDataInfo内
                String prefsVerison = getSharedPrefsVersion(dataName);
                if ( dataVersion != null ) {
                    if (!dataVersion.equals(prefsVerison)) {
                        DataInfo dataInfo = new DataInfo();
                        dataInfo.setName(dataName);
                        dataInfo.setType(dataType);
                        dataInfo.setVersion(dataVersion);
                        dataInfo.setObject(dataObject);

                        mDataInfo.add(dataInfo);
                        loadData =true;
                    }
                }
                Log.d(TAG, " loadDataChangeXml dataName = " + dataName + " dataType = " + dataType
                        + " dataVersion = " + dataVersion + " dataObject = " + dataObject + " loadData = " + loadData);
                a.recycle();
            }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Got exception parsing favorites.", e);
        } catch (IOException e) {
            Log.w(TAG, "Got exception parsing favorites.", e);
        }

        return loadData;
    }

    /***
     * 从本地存储中从中获得的版本信息根据名称
     * @param dataName  通过名称获得版本信息
     * @return
     */
    private String getSharedPrefsVersion(String dataName) {
         SharedPreferences  sharedPreferences = getSharedPreferences(DATA_PREFS_SAVE, MODE_PRIVATE);
         String dataVersion = sharedPreferences.getString(dataName, null);
         Log.d(TAG, " getSharedPrefsVersion dataVersion = " + dataVersion);
         return dataVersion;
    }
    /***
     * 保存版本信息根据名称
     * @param dataName
     * @param dataVersion
     */
    private void saveSharedPrefsVersion(String dataName, String dataVersion) {
        SharedPreferences  sharedPreferences = getSharedPreferences(DATA_PREFS_SAVE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(dataName, dataVersion);
        editor.commit();
    }

    private void insertDataToDB(String fileName, DataInfo dataInfo) {
        String reader = "";
        InputStream inputStream = null;
        InputStreamReader inputreader = null;
        BufferedReader buffreader = null;

        if ( findFileName(fileName) ) {
            try {
                inputStream = getResources().getAssets().open(fileName);
                Log.d(TAG, " importInfoToData inputStream = " + inputStream);
                int lenght = inputStream.available();
                Log.d(TAG, " importInfoToData lenght = " + lenght);
                inputreader = new InputStreamReader(inputStream);
                buffreader = new BufferedReader(inputreader);
                DataRowModify dataRowModify  = getInstance(dataInfo.getObject());
                while ( (reader = buffreader.readLine() ) != null) {
                    Log.d(TAG, " importInfoToData reader = " + reader
                            + " dataInfo.getObject() = " + dataInfo.getObject());//
                    String[] rowData = reader.split(",");
                    if (dataRowModify != null) {
                        dataRowModify.buildOperation(rowData, mOps);
                    }
                }

                if (buffreader != null) {
                    buffreader.close();
                }

                if (inputreader != null) {
                    inputreader.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }

            } catch (Exception e) {
               e.printStackTrace();
            }
        }
    }

    /**
     * found all file from assets directory
     * @param fileName  need to match file name
     * @return
     */
    private boolean findFileName(String fileName) {
        String[] fileNames;
        boolean foundName = false;

        try {
            fileNames = getResources().getAssets().list("");
            for (String name: fileNames) {
                Log.d(TAG, "findFileName name = " + name);
                if (fileName.equals(name)) {
                    foundName = true;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "findFileName e.toString() = " + e.toString() );
        }

        Log.d(TAG, " findFileName foundName =  " + foundName) ;

        return foundName;
    }

    private void deleteDataToDB(DataInfo dataInfo) {
        try {
            DataRowModify dataRowModify  = getInstance(dataInfo.getObject());
            if (dataRowModify != null) {
                dataRowModify.buildOperation(null, mOps);
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    private void updateDataToDB(String fileName, DataInfo dataInfo) {
        String reader = "";
        InputStream inputStream = null;
        InputStreamReader inputreader = null;
        BufferedReader buffreader = null;

        try {
            getResources().getAssets().list("");
            inputStream = getResources().getAssets().open(fileName);
            Log.d(TAG, " importInfoToData inputStream = " + inputStream);
            int lenght = inputStream.available();
            Log.d(TAG, " importInfoToData lenght = " + lenght);
            inputreader = new InputStreamReader(inputStream);
            buffreader = new BufferedReader(inputreader);
            DataRowModify dataRowModify  = getInstance(dataInfo.getObject());

            if (dataRowModify != null) {
                dataRowModify.buildOperation(null, mOps);
            }
            while ( (reader = buffreader.readLine() ) != null) {
                Log.d(TAG, " importInfoToData reader = " + reader
                        + " dataInfo.getObject() = " + dataInfo.getObject());//
                String[] rowData = reader.split(",");
                if (dataRowModify != null) {
                    dataRowModify.buildOperation(rowData, mOps);
                }
            }

            if (buffreader != null) {
                buffreader.close();
            }

            if (inputreader != null) {
                inputreader.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    public static final void beginDocument(XmlPullParser parser, String firstElementName)
            throws XmlPullParserException, IOException
    {
        int type;
        while ((type=parser.next()) != parser.START_TAG
                   && type != parser.END_DOCUMENT) {
            ;
        }

        if (type != parser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    /***
     * 通过反射机制，获得每个类的对象，这些类都是DataRowModify子类
     * @param className 从xml中获得的类名称
     * @return
     */
    private  DataRowModify getInstance(String className){
        DataRowModify dataRow = null;
        try{
            dataRow = (DataRowModify)Class.forName(className).newInstance();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return dataRow;
    }

    public static final String ACTION_IMPORT_DATA = "com.importdata.transaction.IMPORT_DATA";
    public static final String DATA_PREFS_SAVE = "DataSave";

    /***
     * 批量插入数据，此部分内容保存在data_change.xml 文件内
     */
    public static final String CONTENT_INSERT = "insert";
    public static final String CONTENT_UPDATE = "update";
    public static final String CONTENT_DELETE = "delete";


}
