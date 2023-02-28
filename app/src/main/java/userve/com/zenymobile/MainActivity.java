package userve.com.zenymobile;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    public static WebView webview = null;
    public static MyProgressDialog webviewDialog;

    private final static int PERMISSIONS_REQUEST_CODE = 100;



    public static final String TAG = "MainActivity";



    /**
     * SharedPreferences instance
     */
    SharedPreferences pref;

    /**
     * SharedPreferences.Editor instance
     */
    SharedPreferences.Editor edit;

    public static void doRestart(Context c) {
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
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr =
                                (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        webview = (WebView)findViewById(R.id.webview);

        webview.setWebViewClient(new WebViewClientClass());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.setVerticalScrollBarEnabled(false);
        webview.setHorizontalFadingEdgeEnabled(false);
        webview.setHorizontalScrollBarEnabled(false);

        webview.getSettings().setDisplayZoomControls(false);


        //webview.setScrollContainer(false);
        webview.setBackgroundColor(0);

        webview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            // chromium, enable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);



        webviewDialog = new MyProgressDialog(this);
        webviewDialog = MyProgressDialog.show(MainActivity.this, "", "", true, true, null);


        TriphosInterface tri = new TriphosInterface();
        tri.TriphosInterface_init(webview, this,webviewDialog);


        webview.setWebChromeClient(new WebChromeClient()
        {
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result)
            {
                //Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                //result.confirm();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(message).setTitle("알림")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                result.confirm();
                            }
                        });

                builder.create().show();

                return true;
            }
            public boolean onJsConfirm (WebView view, String url, String message, final JsResult result)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(message).setTitle("확인")
                        .setCancelable(false)
                        .setPositiveButton("예", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                result.confirm();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                result.cancel();
                            }
                        });

                builder.create().show();
                return true;
            }
        });

        CookieManager.getInstance().setAcceptCookie(true);

        webview.getSettings().setUseWideViewPort(false);

        webview.getSettings().setDefaultTextEncodingName("utf-8");
        webview.loadUrl(WebConnect.url);


        CookieSyncManager.createInstance(this);

        pref = getSharedPreferences("settings", 0);
        edit = pref.edit();

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
                    checkAppVer();
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
                checkAppVer();
            }


        } else {
            Toast.makeText(MainActivity.this, "Camera not supported",
                    Toast.LENGTH_LONG).show();
        }

    }

    /**
     * More info this method can be found at
     * http://developer.android.com/training/camera/photobasics.html
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }
//
    private class WebViewClientClass extends WebViewClient {

        String beforeUrl = "";
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {

            // TODO Auto-generated method stub
            final JsResult finalRes = result;
            new AlertDialog.Builder(view.getContext())
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finalRes.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {


            String bodyAnalUrl = WebConnect.url+"UserBodyAnalysis.do?";

            Log.d(TAG, "aaaaaaaaaaaaa : " + url);

            if(url.startsWith(bodyAnalUrl)) {
                view.requestFocus(View.FOCUS_UP);
                view.getSettings().setBuiltInZoomControls(true);
                view.getSettings().setSupportZoom(true);
            } else {
                view.requestFocus(View.FOCUS_DOWN);
                view.getSettings().setBuiltInZoomControls(false);
                view.getSettings().setSupportZoom(false);

            }


            if (url != null && (url.startsWith("intent:"))
                    || url.contains("market://")
                    || url.contains("vguard")
                    || url.contains("droidxantivirus")
                    || url.contains("v3mobile")
                    || url.contains(".apk")
                    || url.contains("mvaccine")
                    || url.contains("smartwall://")
                    || url.contains("nidlogin://")
                    || url.contains("http://m.ahnlab.com/kr/site/download")
                    ) {
                try {

                    Intent intent = null;

                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    } catch (URISyntaxException ex) {
                        Log.e("Browser", "Bad URI " + url + ":" + ex.getMessage());
                        return false;
                    }
                    //chrome 버젼 방식
                    if (url.startsWith("intent") || url.startsWith("lottesmartpay://") || url.startsWith("kb-acp://"))
                    {
                        // 앱설치 체크를 합니다.
                        if (getPackageManager().resolveActivity(intent, 0) == null) {
                            String packagename = intent.getPackage();
                            if (packagename != null) {
                                Uri uri = Uri.parse("market://search?q=pname:" + packagename);
                                intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                                return true;
                            }
                        }
                        //구동방식은 PG:쇼핑몰에서 결정하세요.

                        int runType=1;

                        if (runType == 1) {
                            Uri uri = Uri.parse(intent.getDataString());
                            intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        } else {
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            intent.setComponent(null);
                            try {
                                if (startActivityIfNeeded(intent, -1)) {
                                    return true;
                                }
                            } catch (ActivityNotFoundException ex) {
                                return false;
                            }
                        }
                    } else { // 구 방식
                        Uri uri = Uri.parse(url);
                        intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                    return true;
                } catch (ActivityNotFoundException e) {
                    Log.e("error ===>", e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            } else if(url.startsWith("app://cameraApplication")) {
                Log.d(TAG,"log. : " + url);
                String hp = url.substring((url.indexOf("userId=")+7), url.indexOf("&seq="));
                String seq = url.substring(url.indexOf("&seq=")+5, url.length());
                Log.d(TAG,"log. : " + hp);
                Log.d(TAG,"log. : " + seq);
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.putExtra("hp", hp);
                intent.putExtra("seq", seq);
                startActivityForResult(intent, 0);
                return true;
            } else if(url.startsWith("app://briefingApplication")) {

                Intent intent = new Intent(getApplicationContext(), BriefingActivity.class);

                startActivityForResult(intent, 1);
                return true;
            } else if(url.startsWith("app://itemInfoApplication")) {

                Intent intent = new Intent(getApplicationContext(), itemInfoApplication.class);

                startActivityForResult(intent, 1);
                return true;
            }
            else {
                Log.d(TAG, "ddddddddd : " + url);
                Log.d(TAG, "eeeeeeeee : " + WebConnect.url+"UserNewSizeForm.do");
                Map<String, String> extraHeaders = new HashMap<String, String>();
                if(url.startsWith(WebConnect.url+"UserNewSizeForm.do")
                        && beforeUrl.startsWith(WebConnect.url+"UserInsertForm.do")) {
                    extraHeaders.put("Refzerer", beforeUrl);
                } else if(url.startsWith(WebConnect.url+"ScheduleInsertForm.do")
                        && beforeUrl.startsWith(WebConnect.url+"UserManager.do")) {
                    extraHeaders.put("Referer", beforeUrl);
                }
                view.loadUrl(url, extraHeaders);

                return true;
            }

        }


        @Override
        public void onPageFinished(WebView view, String url)
        {

            try {
                String bodyAnalUrl = WebConnect.url+"UserBodyAnalysis.do?";


                if(url.startsWith(bodyAnalUrl)) {
                    view.requestFocus(View.FOCUS_UP);

                    view.getSettings().setBuiltInZoomControls(true);
                    view.getSettings().setSupportZoom(true);
                } else {

                    view.requestFocus(View.FOCUS_DOWN);

                    view.getSettings().setBuiltInZoomControls(false);
                    view.getSettings().setSupportZoom(false);

                }


                CookieSyncManager.getInstance().sync();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(MainActivity.webviewDialog != null)
            {
                WebBackForwardList mWebBackForwardList = webview.copyBackForwardList();
                if (mWebBackForwardList.getCurrentIndex() > 0) {
                    beforeUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()).getUrl();
                }


                MainActivity.webviewDialog.dismiss();

            } else {
                Log.d(TAG, "bbbbbbbbbbb : " + url);

            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if((keyCode == KeyEvent.KEYCODE_BACK))
        {
            String backUrl = webview.getUrl();

            String loginUrl = WebConnect.url+"LoginForm.do";
            String userUrl = WebConnect.url+"UserManager.do";

            String UserNewSizeForm = WebConnect.url+"UserNewSizeForm.do";
            String UserSizeHistory = WebConnect.url+"UserSizeHistory.do";
            String UserSizeList = WebConnect.url+"UserSizeList.do";




            Log.d(TAG,"backUrl : " + backUrl);
            if(backUrl.equals(userUrl)
                    || backUrl.equals(loginUrl)
                    )
            {
                showExitDialog();
            } else if(backUrl.startsWith(UserNewSizeForm)) {
                String[] urls = backUrl.split("\\?");
                String[] params = urls[1].split("&");
                Map<String, String> map = new HashMap<String, String>();
                for (String param : params)
                {
                    String name = param.split("=")[0];
                    String value = param.split("=")[1];
                    map.put(name, value);
                }




                String hp = map.get("inputPhone");
                Log.d(TAG,"newsize!! " + hp);
                webview.loadUrl(WebConnect.url + "UserSizeHistory.do?inputPhone="+hp);
            } else if(backUrl.startsWith(UserSizeHistory)) {
                Log.d(TAG,"sizehistory!!");
                webview.loadUrl(WebConnect.url + "UserSizeList.do");
            } else if(backUrl.startsWith(UserSizeList)) {
                Log.d(TAG,"sizehistory!!");
                webview.loadUrl(WebConnect.url + "UserManager.do");
            }

            else
            {
                webview.goBack();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }


    public AlertDialog.Builder exitDialog;
    /**
     * Exit Confrim(종료) Dialog 띄우기
     */
    public void showExitDialog() {

        exitDialog = new AlertDialog.Builder(this);
        exitDialog.setTitle(getResources().getString(R.string.exitdialog_title));
        exitDialog.setMessage(getResources().getString(R.string.exitdialog_comment));
        exitDialog.setPositiveButton(getResources().getString(R.string.yes_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        edit.putBoolean("FIRST_OPEN", true);
                        edit.commit();


                        finishFlag = true;
                        finish();
                    }
                });
        exitDialog.setNegativeButton(getResources().getString(R.string.no_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        exitDialog.show();
    }

    boolean finishFlag = false;
    @Override
    protected void onDestroy() {
        if(finishFlag) {
            ActivityManager aManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
            aManager.restartPackage(getPackageName());
            CookieSyncManager.getInstance().stopSync();
            Log.d(TAG, "onDestroy called");
            System.exit(0);
        }
        super.onDestroy();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
        Log.d(TAG, "onresume called");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        CookieSyncManager.getInstance().stopSync();

        Log.d(TAG, "onPause called");
    }



    /**
     * 앱 버전 확인
     */
    public void checkAppVer() {
        Log.d(TAG, "checkAppVer() called");
        showProgressDialog();
        progressDialog.setMessage(getResources().getString(R.string.check_filever));

        String version = "0";
        try {
            version = getPackageManager().getPackageInfo(getApplicationInfo().packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }


        checkAppVerTask task = new checkAppVerTask(version);
        task.execute();
    }

    public ProgressDialog progressDialog;
    /**
     * Progress Dialog 띄우기
     */
    public void showProgressDialog() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public static final int UPDATE_CONFIRM = 5001;
    public static final int RE_UPDATE_CONFIRM = 5002;
    /**
     * Dialog 등록
     */
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder;
        switch (id) {
            case UPDATE_CONFIRM:
                builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle(getResources().getString(R.string.updatedialog_title));
                builder.setMessage(getResources().getString(R.string.updatedialog_comment01));
                builder.setPositiveButton(getResources().getString(R.string.yes_btn),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                fileDownTask task = new fileDownTask(getUpdateFileName());
                                task.execute();
                            }
                        });
                builder.setNegativeButton(getResources().getString(R.string.no_btn),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                return builder.create();

            case RE_UPDATE_CONFIRM:
                builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle(getResources().getString(R.string.updatedialog_title));
                builder.setMessage(getResources().getString(R.string.file_exception));
                builder.setPositiveButton(getResources().getString(R.string.yes_btn),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                fileDownTask task = new fileDownTask(getUpdateFileName());
                                task.execute();
                            }
                        });
                builder.setNegativeButton(getResources().getString(R.string.no_btn),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                return builder.create();


        }

        return null;
    }

    /**
     * 앱 버전 확인 TASK
     * @author cha
     *
     */
    private class checkAppVerTask extends AsyncTask<String, Integer, Long> {
        String returnData[] = new String[2];
        String appVer;

        public checkAppVerTask(String appVer) {
            super();
            this.appVer = appVer;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(String... strData) {

            returnData = checkAppVer(this.appVer);

            return 0L;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Long result) {
            try {
                if(progressDialog != null) {
                    progressDialog.dismiss();
                }

                if(returnData != null) {
                    if(returnData[0].equals("N")) {

                    } else {
                        setUpdateFileName(returnData[1]);
                        showDialog(UPDATE_CONFIRM);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /**
     * 업데이트 파일 체크
     * @param fileName
     * @return
     */
    public String[] checkAppVer(String fileName) {
        Log.d(TAG, "checkAppVer() CALL");
        String[] returnData = new String[2];

        // 단순 연결
        URL url = null;
        // 연결 관련 정보도 제공
        URLConnection connection;
        HttpURLConnection httpConnection = null;
        try {
            url = new URL(WebConnect.checkAppVer + "?fileName=" + fileName);
            Log.d(TAG, WebConnect.checkAppVer + "?fileName=" + fileName);
            // 연결 열기
            connection = url.openConnection();
            connection.setConnectTimeout(600000);
            connection.setReadTimeout(600000);
            // 200: Success, 404: File Not Found
            httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();

            Log.d(TAG, "responseCode" + responseCode);

            // 서버에서 에러코드 응답

            if (responseCode == HttpURLConnection.HTTP_OK) { // 200

                // 한글 처리를 위해 InputStreamReader를 UTF-8 인코딩으로 감싼다.
                StringBuilder html = new StringBuilder();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                httpConnection.getInputStream(), "UTF-8"));
                while (true) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    html.append(line + '\n');
                }
                br.close();

                returnData = parsUpdateYN(html);
            }
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "checkAppVer() Error : " + e);
            return null;
        } catch (Exception e) {
            Log.d(TAG, "checkAppVer() Error : " + e);
            return null;
        }
        return returnData;
    }

    public String[] parsUpdateYN(StringBuilder html) {
        String[] returnData = new String[2];

        String htmlStr = html.toString();
        JSONObject json = null;

        try {
            json = new JSONObject(htmlStr);
            returnData[0] = json.getString("updateYN");
            returnData[1] = json.getString("fileName");
            Log.d(TAG, "returnData[0] : " + returnData[0]);
            Log.d(TAG, "returnData[1] : " + returnData[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnData;
    }

    String fileName = "";
    public void setUpdateFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUpdateFileName() {
        return this.fileName;
    }

    private class fileDownTask extends AsyncTask<Integer, Integer, Integer> {
        String mobileFileName = "";
        boolean isExist = false;


        String FileDownloadURL = WebConnect.url + "file/android/";

        String sdFolder = Environment.getExternalStorageDirectory().getAbsolutePath();
        String path = "";

        File targetFile = null;

        long maxLength = 0;

        public fileDownTask(String fileName) {
            super();
            mobileFileName = fileName;

            path = sdFolder+"/"+"userve.com.zenymobile/apk/" + mobileFileName;
            targetFile = new File(path);
        }

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(getResources().getString(R.string.progress_filedown));
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.show();

            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            // 파일 존재 여부 확인
            if (!targetFile.exists()) {
                File targetParentFile = targetFile.getParentFile();
                if (targetParentFile != null) {
                    targetParentFile.mkdirs();
                }

                try {
                    URL url = new URL(FileDownloadURL + mobileFileName);
//		        	Log.d(TAG, "FileDownloadURL : " + url.toString());
                    HttpURLConnection conn= (HttpURLConnection)url.openConnection();

                    int len = conn.getContentLength();
                    maxLength = len;

                    InputStream is = conn.getInputStream();
                    DataInputStream dis = new DataInputStream(is);

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(targetFile);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }

                    if (fos != null) {
                        byte[] data = new byte[1024];
                        int count = 0;
                        long totalSize = 0;
                        while(true) {
                            try {
                                count = dis.read(data, 0, 1024);
                                if (count > 0) {
                                    fos.write(data, 0, count);
                                    totalSize = totalSize + count;
                                    int calc = (int)((totalSize / (float)len) * 100);
                                    publishProgress(calc);
                                } else {
                                    break;
                                }
                            } catch(Exception ex) {
                                ex.printStackTrace();
                                break;
                            }
                        }

                        isExist = targetFile.exists();

                        try {
                            dis.close();
                            fos.close();
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "target file outputstream is null.");
                    }


                    is.close();
                    fos.close();
                    conn.disconnect();
                } catch (Exception e) {
                    progressDialog.dismiss();
                    isExist = false;
                    Log.d(TAG, "fileDownload() Error : " + e);
                }
            } else {
                try {
                    URL url = new URL(FileDownloadURL + mobileFileName);
                    Log.d(TAG, "FileDownloadURL : " + url.toString());
                    HttpURLConnection conn= (HttpURLConnection)url.openConnection();

                    int len = conn.getContentLength();
                    maxLength = len;

                    conn.disconnect();
                    progressDialog.dismiss();
                    isExist = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if(isExist) {
                    if(maxLength == targetFile.length()) {
                        installUpdateFile(mobileFileName);
                    } else {
                        deleteDownloadfile(targetFile);
                        isExist = false;
                    }
                } else {
                    deleteDownloadfile(targetFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }

    }

    public void installUpdateFile(String fileName) {
        try {
            dismissProgressDialog();

            String sdFolder = Environment.getExternalStorageDirectory().getAbsolutePath();
            String path = sdFolder+"/"+"userve.com.zenymobile/apk/" + fileName;
            File apkFile = new File(path);
            boolean flag = apkFile.exists();

            if(flag) {
                if(progressDialog != null) {
                    progressDialog.dismiss();
                }


                if (Build.VERSION.SDK_INT <= 23) {
                    //단말기 OS버전이 젤라빈 버전 보다 작을때.....처리 코드
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(apkFile),"application/vnd.android.package-archive");
                    startActivity(intent);
                }
                else{
                    //젤라빈 버전 이상일때.....처리 코드
                    Log.d(TAG,"test : " + getApplicationContext().getApplicationContext().getPackageName());
                    Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", apkFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    List<ResolveInfo> resInfoList = getApplicationContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        getApplicationContext().grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    getApplicationContext().startActivity(intent);
                }


            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.file_notfound), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void deleteDownloadfile(File targetFile) {
        File file = targetFile;
        try{
            file.delete();
        }catch(Exception e){
            System.err.println(System.err);
            System.exit(-1);
        }
        showDialog(RE_UPDATE_CONFIRM);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            checkAppVer();
                        } else {


                        }
                    }
                }


                break;

            default:
                break;
        }
    }

    /**
     * Create file example.
     */
    private void writeFile() {

        try {

            String sdFolder = Environment.getExternalStorageDirectory().getAbsolutePath();
            String path = "";

            File targetFile = null;
            path = sdFolder+"/"+"userve.com.zenymobile/apk/";
            targetFile = new File(path);

            if(!targetFile.exists()) {
                File targetParentFile = targetFile.getParentFile();
                if (targetParentFile != null) {
                    targetParentFile.mkdirs();
                }
            }

            checkAppVer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            this.webview.reload();
        }
    }



}
