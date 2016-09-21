package com.example.luchunyang.okhttp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private OkHttpClient httpClient;
    private String BAIDU = "http://www.baidu.com";
    private String LIUYAN = "http://news.xinhuanet.com/ent/2013-06/29/11130124930049_91d.jpg";
    private TextView tv;
    private ImageView iv;
    private ExecutorService pool;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.tv);
        iv = (ImageView) findViewById(R.id.iv);
        pool = Executors.newFixedThreadPool(4);

        httpClient = new OkHttpClient();
    }

    public void getString(View view) {

        Request request = new Request.Builder().url(BAIDU).build();
        Call call = httpClient.newCall(request);
        //异步的方式去执行请求
        httpRequest(call, handler, new StringCallBack(){
            @Override
            public void call(String s) {
                tv.setText(s);
            }
        });
    }

    public void postString(View view) {
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("search", "李鸿章");

        RequestBody body = builder.build();

        Request request = new Request.Builder().url(BAIDU).post(body).build();
        final Call call = httpClient.newCall(request);
//        httpRequest(call,handler,"string");
    }

    public void getImage(View view) {
        Request request = new Request.Builder().url(LIUYAN).build();
        Call call = httpClient.newCall(request);
        httpRequest(call,handler,new ImageCallBack(){
            @Override
            public void call(Bitmap bitmap) {
                iv.setImageBitmap(bitmap);
            }
        });
    }


    private void httpRequest(final Call call, final Handler handler, final HttpCallBack callBack) {

        pool.execute(new Runnable() {
            @Override
            public void run() {
                //异步的方式去执行请求
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(final Response response) throws IOException {
                        if (response.isSuccessful()) {
                            if (callBack instanceof StringCallBack) {
                                //希望获得返回的字符串，可以通过response.body().string()获取
//                              //如果希望获得返回的二进制字节数组，则调用response.body().bytes()
//                              //如果你想拿到返回的inputStream，则调用response.body().byteStream() 看到这个最起码能意识到一点，这里支持大文件下载Ò
                                final String s = response.body().string();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callBack.call(s);
                                    }
                                });
                            }else if(callBack instanceof ImageCallBack){

                                final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callBack.call(bitmap);
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }



    private interface HttpCallBack<T>{
        public void call(T t);
    }

    private class StringCallBack implements HttpCallBack<String> {
        @Override
        public void call(String s) {

        }
    }

    private class ImageCallBack implements HttpCallBack<Bitmap>{

        @Override
        public void call(Bitmap bitmap) {

        }
    }
}
