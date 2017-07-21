package text.downloadservice;

/**
 * Created by liuwei on 17-7-19.
 */

public interface DownloadListener {
    void onProgress(int progress);//下载进度
    void onSuccess();//下载成功
    void onFailed();//下载失败
    void onPause();;//下载暂时
    void onCanceled();//下载关闭


}
