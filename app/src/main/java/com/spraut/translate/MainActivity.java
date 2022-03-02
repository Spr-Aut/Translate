package com.spraut.translate;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private EditText editText;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button=findViewById(R.id.btn_main_translate);
        editText=findViewById(R.id.et_main_content);
        textView=findViewById(R.id.tv_main_result);
        textView.setText("123");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string=editText.getText().toString();
                BasicTranslate basicTranslate=new BasicTranslate();
                String url=basicTranslate.translate(string,"auto","auto");
                //异步Get请求
                OkHttpClient okHttpClient=new OkHttpClient();
                Request request=new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                Call call=okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        goToUIThread(e.toString(),0);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        goToUIThread(response.body().string(),1);
                    }
                });

            }
        });

    }//onCreate

    private void goToUIThread(final Object object,final int key) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(key==0){
                    textView.setText("网络请求错误");
                    textView.setTextColor(Color.RED);
                }else{
                    final TranslateResult result=new Gson().fromJson(object.toString(),TranslateResult.class);
                    textView.setText(result.getTrans_result().get(0).getDst());
                }
            }
        });
    }


}