package userve.com.zenymobile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;



public class WebConnect {

    public Context mContext = null;

    public static final String TAG = "WebConnect";

    /** 3G 네트워크 사용 허용 여부 검사 */
    public boolean OnOff_3G = false;

    /** 기본 URL */

    public static final String url = "http://121.78.131.33:8081/";
    //public static final String url = "http://192.168.31.142:8080/";

    public static final String uploadUrl = url+"Upload.do";

    public static final String saveBodyImageUrl = url+"UserBodyImageUpdate.do";

    /** 파일 이름 체크 URL */
    public static final String checkAppVer = url + "checkAppVer.do";





    public WebConnect(Context mContext) {
        this.mContext = mContext;
    }


    /**
     * saveBodyImage
     * @param jsonObj
     * @return
     */
    public String saveBodyImage(JSONObject jsonObj) {
        Log.d(TAG, "saveBodyImage() CALL");
        String returnData = "N";

        try {
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(saveBodyImageUrl).openConnection();

            httpConnection.setConnectTimeout(60000);
            httpConnection.setReadTimeout(60000);
            httpConnection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(httpConnection.getOutputStream(), "UTF-8");
            wr.write(jsonObj.toString());
            wr.flush();

            int responseCode = httpConnection.getResponseCode();

            // 서버에서 에러코드 응답
            Log.d(TAG, "responseCode" + responseCode);
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

                JSONObject json = new JSONObject(html.toString());
                returnData = json.getString("successYn");
            }
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "loadMemoData() Error : " + e);
            return null;
        } catch (Exception e) {
            Log.d(TAG, "loadMemoData() Error : " + e);
        }
        return returnData;
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
            url = new URL(checkAppVer + "?fileName=" + fileName);
            Log.d(TAG, checkAppVer + "?fileName=" + fileName);
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

                JsonParser jp = new JsonParser();
                returnData = jp.parsUpdateYN(html);
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

}
