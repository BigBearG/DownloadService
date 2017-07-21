package text.downloadservice;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnstartD,mBtnpauseD,mBtncancleD;
    private DownloadService.DownloadBinder downloadBinder;
    private EditText mEdtdownload;
    private ServiceConnection  connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder=(DownloadService.DownloadBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnstartD=(Button)findViewById(R.id.start_download);
        mBtnpauseD=(Button)findViewById(R.id.pause_download);
        mBtncancleD=(Button)findViewById(R.id.cancel_download);
        mBtnstartD.setOnClickListener(this);
        mBtnpauseD.setOnClickListener(this);
        mBtncancleD.setOnClickListener(this);
        mEdtdownload=(EditText)findViewById(R.id.download);
        Intent intent=new Intent(this,DownloadService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    @Override
    public void onClick(View view) {
        if (downloadBinder==null){
            Toast.makeText(MainActivity.this,"哈哈",Toast.LENGTH_LONG).show();
            return;
        }
        switch (view.getId()){
            case R.id.start_download:
                String url=mEdtdownload.getText().toString().trim();
                downloadBinder.startDownload(url);
                break;
            case R.id.pause_download:
                downloadBinder.pauseDownload();
                break;
            case R.id.cancel_download:
                downloadBinder.cancelDownload();
                break;
            default:break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:if (grantResults.length>0&&grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"拒绝将无法使用",Toast.LENGTH_LONG).show();
                finish();
            }
            break;
            default:break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
