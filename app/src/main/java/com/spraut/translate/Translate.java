package com.spraut.translate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.spraut.translate.DataBase.Note;
import com.spraut.translate.DataBase.NoteDbOpenHelper;

import org.angmarch.views.NiceSpinner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Translate extends AppCompatActivity implements View.OnClickListener{
    private EditText etContent;//输入框（要翻译的内容）
    private ImageView ivClearTx;//清空输入框按钮
    private TextView tvTranslation;//翻译
    private TextView tvAdd;//添加
    private TextView tvTest;

    private LinearLayout resultLay;//翻译结果布局
    private TextView tvResult;//翻译的结果

    private String fromLanguage = "auto";//目标语言
    private String toLanguage = "auto";//翻译语言

    private final static int STATE_BEFORE_INPUT=0;//输入前
    private final static int STATE_DURING_INPUT=1;//输入中
    private final static int STATE_DURING_TRANS=2;//翻译中
    private final static int STATE_AFTER_TRANS=3;//翻译后
    private final static int STATE_AFTER_ADD=4;//添加后
    private final static int STATE_INIT=5;//初始状态


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        setStatusBar();

        initView();
    }//onCreate

    private void initView(){
        etContent=findViewById(R.id.et_content_translate);
        ivClearTx=findViewById(R.id.iv_clear_tx);
        resultLay=findViewById(R.id.result_lay);
        tvTranslation=findViewById(R.id.tv_translation);
        tvResult=findViewById(R.id.tv_result);
        tvAdd=findViewById(R.id.tv_add);
        tvTest=findViewById(R.id.tv_test);

        //点击事件
        ivClearTx.setOnClickListener(this);
        tvTranslation.setOnClickListener(this);
        tvAdd.setOnClickListener(this);

        //输入监听
        editTextListener();
    }

    //输入监听
    private void editTextListener(){
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ivClearTx.setVisibility(View.VISIBLE);//显示清除按钮
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClearTx.setVisibility(View.VISIBLE);//显示清除按钮
            }

            @Override
            public void afterTextChanged(Editable s) {
                /*ivClearTx.setVisibility(View.VISIBLE);//显示清除按钮*/
                stateSwitch(STATE_DURING_INPUT);

                String content=etContent.getText().toString().trim();
                if(content.isEmpty()){
                    stateSwitch(STATE_BEFORE_INPUT);
                    /*resultLay.setVisibility(View.GONE);
                    tvTranslation.setVisibility(View.VISIBLE);
                    ivClearTx.setVisibility(View.GONE);
                    tvAdd.setVisibility(View.GONE);*/
                }
            }
        });
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_clear_tx://清空输入框
                /*etContent.setText("");
                ivClearTx.setVisibility(View.GONE);*/
                stateSwitch(STATE_INIT);
                break;
            case R.id.tv_translation://翻译
                translate();
                break;
            case R.id.tv_add:
                /*String test=tvResult.getText().toString()+etContent.getText().toString();
                tvTest.setText(test);*/
                String input=etContent.getText().toString();
                String output=tvResult.getText().toString();
                addData(input,output);
            default:
                break;
        }
    }

    //翻译
    private void translate(){
        //获取输入的内容
        String inputTx = etContent.getText().toString().trim();
        //判断输入内容是否为空
        if (!inputTx.isEmpty() || !"".equals(inputTx)) {//不为空
            /*tvTranslation.setText("翻译中...");
            tvTranslation.setEnabled(false);//不可更改，同样就无法点击*/
            stateSwitch(STATE_DURING_TRANS);

            BasicTranslate basicTranslate=new BasicTranslate();
            String url=basicTranslate.translate(inputTx,fromLanguage,toLanguage);

            OkHttpClient okHttpClient = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(url)
                    .get()//默认就是GET请求，可以不写
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    //异常返回
                    goToUIThread(e.toString(), 0);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //正常返回
                    goToUIThread(response.body().string(), 1);
                }
            });
        } else {//为空
            showMsg("请输入要翻译的内容！");
        }
    }

    //回到主线程
    private void goToUIThread(final Object object, final int key) {
        //切换到主线程处理数据
        Translate.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (key == 0) {//异常返回
                    showMsg("异常信息：" + object.toString());
                    Log.e("MainActivity",object.toString());
                    tvTranslation.setText("翻译");
                    tvTranslation.setEnabled(true);
                } else {//正常返回
                    showTransResult(object);
                }
            }
        });
    }

    //获取到翻译结果后的操作
    private void showTransResult(Object object){
        //通过Gson 将 JSON字符串转为实体Bean
        final TranslateResult result = new Gson().fromJson(object.toString(), TranslateResult.class);

        if(result.getTrans_result().get(0).getDst() == null){
            showMsg("数据为空");
        }else{
            tvResult.setText(result.getTrans_result().get(0).getDst());
            stateSwitch(STATE_AFTER_TRANS);
        }
    }

    //添加
    private void addData(String stringA,String stringB){
        if (!stringA.isEmpty() || !"".equals(stringA)){//非空
            if (judgeEnglish(stringA)){
                addToDB(stringA,stringB);//A是英语，B是汉语
            }else {
                addToDB(stringB,stringA);//B是英语，A是汉语
            }
        }else {//空
            showMsg("请输入要翻译的内容！");
        }
    }

    //判断是不是英语
    private boolean judgeEnglish(String string){
        char c=string.charAt(0);
        if((c>='a'&&c<='z')||(c>='A'&&c<='Z')){
            return true;
        }else{
            return false;
        }
    }

    //添加到数据库
    private void addToDB(String keyword,String value){
        NoteDbOpenHelper noteDbOpenHelper=new NoteDbOpenHelper(this);
        Note note=new Note();
        note.setKeyword(keyword);
        note.setValue(value);
        noteDbOpenHelper.insertData(note);
        stateSwitch(STATE_INIT);
        showMsg("添加成功");
    }

    //控件状态切换
    private void stateSwitch(int state){
        switch (state){
            case STATE_BEFORE_INPUT:
                tvResult.setText("");
                resultLay.setVisibility(View.GONE);//隐藏翻译结果
                tvTranslation.setVisibility(View.VISIBLE);//显示翻译按钮
                ivClearTx.setVisibility(View.GONE);//隐藏清除按钮
                tvAdd.setVisibility(View.GONE);//隐藏添加按钮
                break;
            case STATE_DURING_INPUT:
                resultLay.setVisibility(View.GONE);//隐藏翻译结果
                tvTranslation.setVisibility(View.VISIBLE);//显示翻译按钮
                ivClearTx.setVisibility(View.VISIBLE);//显示清除按钮
                tvAdd.setVisibility(View.GONE);//隐藏添加按钮
                break;
            case STATE_DURING_TRANS:
                tvTranslation.setText("翻译中...");
                tvTranslation.setEnabled(false);//不可更改，同样就无法点击
                resultLay.setVisibility(View.GONE);//隐藏翻译结果
                tvTranslation.setVisibility(View.VISIBLE);//显示翻译按钮
                ivClearTx.setVisibility(View.VISIBLE);//显示清除按钮
                tvAdd.setVisibility(View.GONE);//隐藏添加按钮
                break;
            case STATE_AFTER_TRANS:
                tvTranslation.setText("翻译");
                tvTranslation.setEnabled(true);
                resultLay.setVisibility(View.VISIBLE);//显示翻译结果
                tvTranslation.setVisibility(View.GONE);//隐藏翻译按钮
                tvAdd.setVisibility(View.VISIBLE);//显示添加按钮
                ivClearTx.setVisibility(View.VISIBLE);//显示清除按钮
                break;
            case STATE_AFTER_ADD:
                stateSwitch(STATE_INIT);
                showMsg("添加成功");
                break;
            case STATE_INIT:
                etContent.setText("");
                tvResult.setText("");
                resultLay.setVisibility(View.GONE);//隐藏翻译结果
                tvTranslation.setVisibility(View.VISIBLE);//显示翻译按钮
                ivClearTx.setVisibility(View.GONE);//隐藏清除按钮
                tvAdd.setVisibility(View.GONE);//隐藏添加按钮
                break;
            default:
                break;
        }
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void setStatusBar(){
        //适配MIUI，沉浸小横条和状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //状态栏文字显示为黑色
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        /*window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);*/
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

}