package text.downloadservice;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by liuwei on 17-7-19.
 */

public class DownloadTask extends AsyncTask<String,Integer,Integer> {
    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FAILED=1;
    public static final int TYPE_PAUSED=2;
    public static final int TYPE_CANCELED=3;
    private DownloadListener listener;
    private boolean isCanceled=false;
    private boolean isPaused=false;
    private int lastprogress;
    public DownloadTask(DownloadListener listener){
        this.listener=listener;
    }
    @Override
    protected Integer doInBackground(String... strings) {
        InputStream is = null;
        RandomAccessFile saveFile = null;
        FileOutputStream output;
        File file = null;
        try {
            long downloadLength = 0;
            String dolwnloadUrl = strings[0];
            String filename = dolwnloadUrl.substring(dolwnloadUrl.lastIndexOf("/"));//切出下载文件名
            String directroy = "/mnt/sdcard/Download";//规定下载路径
            file = new File(directroy + filename);
            URL url = new URL(dolwnloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();//
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            long contentlength = getContentLength(dolwnloadUrl);
            if (contentlength == 0) {
                return TYPE_FAILED;
            } else if (contentlength == downloadLength) {
                return TYPE_SUCCESS;
            }
            if (file.exists()){
                downloadLength=file.length();
            }
            saveFile = new RandomAccessFile(file, "rw");
            connection.setRequestProperty("RANGE", "bytes=" + downloadLength + "-");
            is = connection.getInputStream();
            saveFile.seek(downloadLength);
            byte[] b = new byte[1024];
            int count = 0;
            int len = -1;
            while ((len = is.read(b)) != -1) {
                if (isCanceled) {
                    return TYPE_CANCELED;
                } else if (isPaused) {
                    return TYPE_PAUSED;
                } else {
                    saveFile.write(b, 0, len);
                    count += len;
                    int prpgress = (int) (((count + downloadLength) / (float) contentlength) * 100);
                    publishProgress(prpgress);
                }
            }
            return TYPE_SUCCESS;
            } catch(Exception e){
                e.printStackTrace();
            }finally{
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (saveFile != null) {
                        saveFile.close();
                    }
                    if (isCanceled && file != null) {
                        file.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return TYPE_FAILED;
        }
    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSED:
                listener.onPause();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
            default:break;

        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress=values[0];
        if (progress>lastprogress){
            listener.onProgress(progress);
            lastprogress=progress;
        }
    }
    public void pauseDowndload(){
        isPaused=true;
    }
    public void canceDowndload(){
        isCanceled=true;
    }
    private long getContentLength(String dolwnloadUrl) {
        try {
            URL url=new URL(dolwnloadUrl);
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            long contentlength=connection.getContentLength();
            return contentlength;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
