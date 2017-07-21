package text.downloadservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

/**
 * Created by liuwei on 17-7-19.
 */

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadBinder mdownloadBinder=new DownloadBinder();
    private DownloadListener listener=new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotiFicationManager().notify(1,getNotification("Download...",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask=null;
            stopForeground(true);
            getNotiFicationManager().notify(1,getNotification("Download Sucess",-1));
            Toast.makeText(DownloadService.this,"Download Success",Toast.LENGTH_LONG).show();
        }
        @Override
        public void onFailed() {
            downloadTask=null;
            stopForeground(true);
            getNotiFicationManager().notify(1,getNotification("Download Failed",-1));
            Toast.makeText(DownloadService.this,"Download Failed",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPause() {
            downloadTask=null;
            Toast.makeText(DownloadService.this,"Download Paused",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCanceled() {
            downloadTask=null;
            stopForeground(true);
            Toast.makeText(DownloadService.this,"Download Cancled",Toast.LENGTH_LONG).show();
        }
    };

    class DownloadBinder extends Binder{
        public void startDownload(String url){
            if (downloadTask==null){
                downloadUrl=url;
                downloadTask=new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("Dowloading...",0));
                Toast.makeText(DownloadService.this,"Downloading...",Toast.LENGTH_LONG).show();
            }
        }
        public void pauseDownload(){
            if (downloadTask!=null){
                downloadTask.pauseDowndload();
            }
        }
        public void cancelDownload(){
            if (downloadTask!=null){
                downloadTask.canceDowndload();
            }else {
                if (downloadUrl!=null){
                    String filename=downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String diectroy="/mnt/sdcard/Download";
                    File file=new File(diectroy+filename);
                    if(file.exists()){
                        file.delete();
                    }
                    getNotiFicationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this,"Download Cancled",Toast.LENGTH_LONG).show();
                }
            }

        }

    }

    private NotificationManager getNotiFicationManager() {
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    private Notification getNotification(String title,int progress){
        Intent intent=new Intent(this,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress>0){
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mdownloadBinder;
    }
}
