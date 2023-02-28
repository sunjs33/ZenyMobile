package userve.com.zenymobile;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    CameraPreview preview;
    Camera camera;
    Context ctx;

    private final static int PERMISSIONS_REQUEST_CODE = 100;
    // Camera.CameraInfo.CAMERA_FACING_FRONT or Camera.CameraInfo.CAMERA_FACING_BACK
    private final static int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;
    private AppCompatActivity mActivity;

    private LinearLayout layoutCapture;
    private LinearLayout layoutConfirm;
    private RelativeLayout imgLayout;

    private TitleBitmapButton btnFront;
    private TitleBitmapButton btnSide;
    private TitleBitmapButton btnBack;

    Button btnRotate;

    private ImageView ivImage;

    HashMap<String, String> modeMap = new HashMap<String, String>();

    public static void doRestart(Context c) {
        //http://stackoverflow.com/a/22345538
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted
                        // after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr =
                                (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application

                        Toast.makeText(c, "앱이 새로 시작 됩니다.", Toast.LENGTH_SHORT).show();
                        System.exit(0);
                    } else {
                        Log.e(TAG, "Was not able to restart application, " +
                                "mStartActivity null");
                    }
                } else {
                    Log.e(TAG, "Was not able to restart application, PM null");
                }
            } else {
                Log.e(TAG, "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Was not able to restart application");
        }
    }

    public void startCamera() {

        if ( preview == null ) {
            preview = new CameraPreview(this, (CameraSurfaceView) findViewById(R.id.surfaceView));
            preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            ((FrameLayout) findViewById(R.id.layout)).addView(preview);
            preview.setKeepScreenOn(true);

            /* 프리뷰 화면 눌렀을 때  사진을 찍음
            preview.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                }
            });*/
        }

        preview.setCamera(null);
        if (camera != null) {
            camera.release();
            camera = null;
        }

        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {

                camera = Camera.open(CAMERA_FACING);
                // camera orientation
                camera.setDisplayOrientation(setCameraDisplayOrientation(this, CAMERA_FACING,
                        camera));
                // get Camera parameters
                Camera.Parameters params = camera.getParameters();
                // picture image orientation
                params.setRotation(setCameraDisplayOrientation(this, CAMERA_FACING, camera));
                camera.startPreview();

            } catch (RuntimeException ex) {
                Toast.makeText(ctx, "camera_not_found " + ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                Log.d(TAG, "camera_not_found " + ex.getMessage().toString());
            }
        }

        preview.setCamera(camera);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        mActivity = this;

        //상태바 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        modeMap.put("100", "N");
        modeMap.put("200", "N");
        modeMap.put("300", "N");

        layoutCapture = (LinearLayout)findViewById(R.id.layoutCapture);
        layoutConfirm = (LinearLayout) findViewById(R.id.layoutConfirm);
        imgLayout = (RelativeLayout)findViewById(R.id.imgLayout);

        btnFront = (TitleBitmapButton)findViewById(R.id.btnFront);
        btnFront.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFront.setSelected(true);
                btnSide.setSelected(false);
                btnBack.setSelected(false);

                setMode("100");
            }
        });
        btnFront.setSelected(true);
        btnSide = (TitleBitmapButton)findViewById(R.id.btnSide);
        btnSide.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFront.setSelected(false);
                btnSide.setSelected(true);
                btnBack.setSelected(false);

                setMode("200");
            }
        });
        btnBack = (TitleBitmapButton)findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFront.setSelected(false);
                btnSide.setSelected(false);
                btnBack.setSelected(true);

                setMode("300");
            }
        });

        ivImage = (ImageView)findViewById(R.id.ivImage);


        Button button = (Button)findViewById(R.id.btnCapture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        Button btnGallery = (Button)findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        Button btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgLayout.setVisibility(View.GONE);
                ivImage.setImageBitmap(null);
                layoutCapture.setVisibility(View.VISIBLE);
                layoutConfirm.setVisibility(View.GONE);

                resetCam();
            }
        });

        Button btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgLayout.setVisibility(View.GONE);
                ivImage.setImageBitmap(null);
                btnRotate.setVisibility(View.GONE);
                dialog = ProgressDialog.show(CameraActivity.this,"","Uploading File...",true);
                new SaveImageTask().execute(getCurrentData());
            }
        });

        btnRotate = (Button)findViewById(R.id.btnRotate);
        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            rotateImg();
            }
        });


        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API 23 이상이면
                // 런타임 퍼미션 처리 필요

                int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);
                int hasWriteExternalStoragePermission =
                        ContextCompat.checkSelfPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if ( hasCameraPermission == PackageManager.PERMISSION_GRANTED
                        && hasWriteExternalStoragePermission==PackageManager.PERMISSION_GRANTED){
                    ;//이미 퍼미션을 가지고 있음
                }
                else {
                    //퍼미션 요청
                    ActivityCompat.requestPermissions( this,
                            new String[]{Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_CODE);
                }
            }
            else{
                ;
            }


        } else {
            Toast.makeText(CameraActivity.this, "Camera not supported",
                    Toast.LENGTH_LONG).show();
        }


    }

    String mode = "100";
    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return this.mode;
    }



    @Override
    protected void onResume() {
        super.onResume();

        startCamera();
    }



    @Override
    protected void onPause() {
        super.onPause();

        // Surface will be destroyed when we return, so stop the preview.
        if(camera != null) {
            // Call stopPreview() to stop updating the preview surface
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }

        ((FrameLayout) findViewById(R.id.layout)).removeView(preview);
        preview = null;

    }

    private void resetCam() {
        startCamera();
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };


    //참고 : http://stackoverflow.com/q/37135675
    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            //이미지의 너비와 높이 결정
            int w = camera.getParameters().getPictureSize().width;
            int h = camera.getParameters().getPictureSize().height;
//            Toast.makeText(CameraActivity.this, ""+w, Toast.LENGTH_SHORT).show();//4656
//            Toast.makeText(CameraActivity.this, ""+h, Toast.LENGTH_SHORT).show();//3492


            int orientation = setCameraDisplayOrientation(CameraActivity.this,
                    CAMERA_FACING, camera);

            //byte array를 bitmap으로 변환
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeByteArray( data, 0, data.length, options);
            //int w = bitmap.getWidth();
            //int h = bitmap.getHeight();

            //이미지를 디바이스 방향으로 회전
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bitmap =  Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

            //bitmap을 byte array로 변환
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] currentData = stream.toByteArray();

            setCurrentData(currentData);

            layoutCapture.setVisibility(View.GONE);
            layoutConfirm.setVisibility(View.VISIBLE);
            //파일로 저장
            //new SaveImageTask().execute(getCurrentData());
            //resetCam();




            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };

    byte[] currentData;
    public void setCurrentData(byte[] currentData) {
        this.currentData = currentData;
    }

    Bitmap bit = null;
    public void setTempBitMap(Bitmap bit) {
        this.bit = bit;
    }

    public Bitmap getTempBitMap() {
        return this.bit;
    }

    public byte[] getCurrentData() {
        return this.currentData;
    }

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/zeny");
                dir.mkdirs();

                final String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to "
                        + outFile.getAbsolutePath());

                refreshGallery(outFile);
                Log.d(TAG,"저장");
                setImageFileStr(outFile.getAbsolutePath());
                setImageFileStrNm(fileName);
                // Toast.makeText(getApplicationContext(),"저장", Toast.LENGTH_LONG).show();;


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //creating new thread to handle Http Operations
                        uploadFile(getImageFileStr());
                    }
                }).start();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }

    }

    String image = "";
    public void setImageFileStr(String image) {
        this.image = image;
    }

    public String getImageFileStr() {
        return this.image;
    }

    String imageName = "";
    public void setImageFileStrNm(String imageName) {
        this.imageName = imageName;
    }

    public String getImageFileStrNm() {
        return this.imageName;
    }

    /**
     *
     * @param activity
     * @param cameraId  Camera.CameraInfo.CAMERA_FACING_FRONT,
     *                    Camera.CameraInfo.CAMERA_FACING_BACK
     * @param camera
     *
     * Camera Orientation
     * reference by https://developer.android.com/reference/android/hardware/Camera.html
     */
    public static int setCameraDisplayOrientation(Activity activity,
                                                  int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length > 0) {

            int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            int hasWriteExternalStoragePermission =
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if ( hasCameraPermission == PackageManager.PERMISSION_GRANTED
                    && hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED ){

                //이미 퍼미션을 가지고 있음
                doRestart(this);
            }
            else{
                checkPermissions();
            }
        }

    }


    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int hasWriteExternalStoragePermission =
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

        boolean cameraRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA);
        boolean writeExternalStorageRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);


        if ( (hasCameraPermission == PackageManager.PERMISSION_DENIED && cameraRationale)
                || (hasWriteExternalStoragePermission== PackageManager.PERMISSION_DENIED
                && writeExternalStorageRationale))
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");

        else if ( (hasCameraPermission == PackageManager.PERMISSION_DENIED && !cameraRationale)
                || (hasWriteExternalStoragePermission== PackageManager.PERMISSION_DENIED
                && !writeExternalStorageRationale))
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");

        else if ( hasCameraPermission == PackageManager.PERMISSION_GRANTED
                || hasWriteExternalStoragePermission== PackageManager.PERMISSION_GRANTED ) {
            doRestart(this);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //퍼미션 요청
                ActivityCompat.requestPermissions( CameraActivity.this,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CODE);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    ProgressDialog dialog;
    public int uploadFile(final String selectedFilePath){
        Log.d("TAG", "uploadFile called");
        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        String successYn = "N";

        int result = RESULT_OK;

        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];

        if (!selectedFile.isFile()){
            dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("TTT", "Source File Doesn't Exist: " + selectedFilePath);
                    // tvFileName.setText("Source File Doesn't Exist: " + selectedFilePath);
                }
            });
            return 0;
        }else{
            try{
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(WebConnect.uploadUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file",selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);


                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0){
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // tvFileName.setText("File Upload completed.\n\n You can see the uploaded file here: \n\n" + "http://coderefer.com/extras/uploads/"+ fileName);
                        }
                    });

                    // 한글 처리를 위해 InputStreamReader를 UTF-8 인코딩으로 감싼다.
                    StringBuilder html = new StringBuilder();
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream(), "UTF-8"));
                    while (true) {
                        String line = br.readLine();
                        if (line == null)
                            break;
                        html.append(line + '\n');
                    }
                    br.close();

                    JSONObject json = new JSONObject(html.toString());
                    successYn = json.getString("successYn");
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();



            } catch (FileNotFoundException e) {
                result = RESULT_CANCELED;
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraActivity.this,"File Not Found",Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                result = RESULT_CANCELED;
                e.printStackTrace();
                // Toast.makeText(CameraActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                result = RESULT_CANCELED;
                e.printStackTrace();
                // Toast.makeText(CameraActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                result = RESULT_CANCELED;
                e.printStackTrace();
            }



            if(successYn.equals("Y")) {
                saveBodyImage task = new saveBodyImage(getImageFileStrNm());
                task.execute();

            } else {
                dialog.dismiss();
            }


            return serverResponseCode;
        }

    }

    /**
     * 이미지 정보 저장
     * @author cha
     *
     */
    private class saveBodyImage extends AsyncTask<String, Integer, Long> {

        String successYn = "N";
        String hp = "";
        String seq = "";
        String imagePath = "";

        public saveBodyImage(String imagePath) {
            super();

            Intent intent = getIntent();
            String hp = intent.getStringExtra("hp");
            String seq = intent.getStringExtra("seq");
            Log.d(TAG,"log. : " + hp);
            Log.d(TAG,"log. : " + seq);

            this.hp = hp;
            this.seq = seq;
            //imagePath = imagePath.substring(imagePath.indexOf("zeny/")+5, imagePath.length());
            this.imagePath = imagePath;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(String... strData) {
            WebConnect wc = new WebConnect(getApplicationContext());
            JSONObject obj = new JSONObject();
            try {
                obj.put("hp", this.hp);
                obj.put("seq", this.seq);
                obj.put("imagePath", this.imagePath);
                obj.put("mode", getMode());

            } catch (Exception e) {
                e.printStackTrace();
            }

            successYn = wc.saveBodyImage(obj);

            return 0L;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Long result) {
            try {
                if(!successYn.equals("0")) {
                    dialog.dismiss();
                    setResult(RESULT_OK);
                    // 종료
                    layoutCapture.setVisibility(View.VISIBLE);
                    layoutConfirm.setVisibility(View.GONE);
                    checkMode();
                    resetCam();
                } else {
                    dialog.dismiss();
                }


            } catch (Exception e) {
                dialog.dismiss();
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public void checkMode() {
        boolean flag = true;

        if(getMode().equals("100")) {
            btnFront.setBackgroundBitmap(R.drawable.normal_btn_normal2, R.drawable.normal_btn_clicked);
        } else if(getMode().equals("200")) {
            btnSide.setBackgroundBitmap(R.drawable.normal_btn_normal2, R.drawable.normal_btn_clicked);
        } else if(getMode().equals("300")) {
            btnBack.setBackgroundBitmap(R.drawable.normal_btn_normal2, R.drawable.normal_btn_clicked);
        }

        String cameraYn = modeMap.get(getMode());
        if(cameraYn.equals("N")) {
            modeMap.put(getMode(), "Y");
        }

        Iterator<String> iter = modeMap.keySet().iterator();
        while(iter.hasNext()) {
            String key = iter.next();
            String selectedMap = modeMap.get(key);

            if(selectedMap.equals("N")) {
                flag = false;
                setMode(key);
                if(key.equals("100")) {
                    btnFront.setSelected(true);
                    btnSide.setSelected(false);
                    btnBack.setSelected(false);
                } else if(key.equals("200")) {
                    btnFront.setSelected(false);
                    btnSide.setSelected(true);
                    btnBack.setSelected(false);
                } else if(key.equals("300")) {
                    btnFront.setSelected(false);
                    btnSide.setSelected(false);
                    btnBack.setSelected(true);
                }
                break;
            }
        }

        if(flag) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            imgLayout.setVisibility(View.VISIBLE);
            btnRotate.setVisibility(View.VISIBLE);
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                setTempBitMap(selectedImage);

                //bitmap을 byte array로 변환
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                byte[] currentData = stream.toByteArray();

                setCurrentData(currentData);


                ivImage.setImageBitmap(selectedImage);



                layoutCapture.setVisibility(View.GONE);
                layoutConfirm.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(getApplicationContext(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    public void rotateImg() {
        Bitmap selectedImage = getTempBitMap();

        Matrix rotateMatrix = new Matrix();
        rotateMatrix.postRotate(90); //-360~360



        selectedImage = Bitmap.createBitmap(selectedImage, 0, 0,
                selectedImage.getWidth(), selectedImage.getHeight(), rotateMatrix, false);

        setTempBitMap(selectedImage);

        //bitmap을 byte array로 변환
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] currentData = stream.toByteArray();

        setCurrentData(currentData);


        ivImage.setImageBitmap(selectedImage);

        layoutCapture.setVisibility(View.GONE);
        layoutConfirm.setVisibility(View.VISIBLE);
    }
}