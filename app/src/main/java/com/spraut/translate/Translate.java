package com.spraut.translate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

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

    private LinearLayout beforeLay;//翻译之前的布局
    private NiceSpinner spLanguage;//语言选择下拉框
    private LinearLayout afterLay;//翻译之后的布局
    private TextView tvFrom;//翻译源语言
    private TextView tvTo;//翻译目标语言

    private EditText etContent;//输入框（要翻译的内容）
    private ImageView ivClearTx;//清空输入框按钮
    private TextView tvTranslation;//翻译

    private LinearLayout resultLay;//翻译结果布局
    private TextView tvResult;//翻译的结果
    private ImageView ivCopyTx;//复制翻译的结果

    private String fromLanguage = "auto";//目标语言
    private String toLanguage = "auto";//翻译语言

    private ClipboardManager myClipboard;//复制文本
    private ClipData myClip; //剪辑数据

    private String appId = "20220302001106832";//APP ID
    private String key = "HA1UmLX3HyfdPuJXOhuT";//秘钥

    //配置初始数据
    private List<String> data = new LinkedList<>(Arrays.asList(
            "自动检测语言", "中文 → 英文", "英文 → 中文",
            "中文 → 繁体中文", "中文 → 粤语", "中文 → 日语",
            "中文 → 韩语", "中文 → 法语", "中文 → 俄语",
            "中文 → 阿拉伯语", "中文 → 西班牙语 ", "中文 → 意大利语"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        initView();
    }//onCreate

    private void initView(){
        etContent=findViewById(R.id.et_content_translate);
        ivClearTx=findViewById(R.id.iv_clear_tx);
        resultLay=findViewById(R.id.result_lay);
        tvTranslation=findViewById(R.id.tv_translation);
        tvResult=findViewById(R.id.tv_result);

        //点击事件
        ivClearTx.setOnClickListener(this);
        tvTranslation.setOnClickListener(this);

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
                ivClearTx.setVisibility(View.VISIBLE);//显示清除按钮

                String content=etContent.getText().toString().trim();
                if(content.isEmpty()){
                    resultLay.setVisibility(View.GONE);
                    tvTranslation.setVisibility(View.VISIBLE);
                    ivClearTx.setVisibility(View.GONE);
                }
            }
        });
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_clear_tx://清空输入框
                etContent.setText("");
                ivClearTx.setVisibility(View.GONE);
                break;
            case R.id.tv_translation://翻译
                translate();
                break;
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
            tvTranslation.setText("翻译中...");
            tvTranslation.setEnabled(false);//不可更改，同样就无法点击
            String salt = num(1);//随机数
            //拼接一个字符串然后加密
            String spliceStr = appId + inputTx + salt + key;//根据百度要求 拼接
            String sign = stringToMD5(spliceStr);//将拼接好的字符串进行MD5加密   作为一个标识
            //异步Get请求访问网络
            asyncGet(inputTx, fromLanguage, toLanguage, salt, sign);
        } else {//为空
            showMsg("请输入要翻译的内容！");
        }
    }

    //异步Get请求
    private void asyncGet(String content, String fromType, String toType, String salt, String sign) {
        //通用翻译API HTTP地址：
        //http://api.fanyi.baidu.com/api/trans/vip/translate
        //通用翻译API HTTPS地址：
        //https://fanyi-api.baidu.com/api/trans/vip/translate

        String httpStr = "http://api.fanyi.baidu.com/api/trans/vip/translate";
        String httpsStr = "https://fanyi-api.baidu.com/api/trans/vip/translate";
        //拼接请求的地址
        String url = httpsStr +
                "?appid=" + appId + "&q=" + content + "&from=" + fromType + "&to=" +
                toType + "&salt=" + salt + "&sign=" + sign;
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
    }

    //回到主线程
    private void goToUIThread(final Object object, final int key) {
        //切换到主线程处理数据
        Translate.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTranslation.setText("翻译");
                tvTranslation.setEnabled(true);

                if (key == 0) {//异常返回
                    showMsg("异常信息：" + object.toString());
                    Log.e("MainActivity",object.toString());
                } else {//正常返回
                    //通过Gson 将 JSON字符串转为实体Bean
                    final TranslateResult result = new Gson().fromJson(object.toString(), TranslateResult.class);
                    tvTranslation.setVisibility(View.GONE);
                    //显示翻译的结果

                    if(result.getTrans_result().get(0).getDst() == null){
                        showMsg("数据为空");
                    }
                    tvResult.setText(result.getTrans_result().get(0).getDst());
                    resultLay.setVisibility(View.VISIBLE);
                    //翻译成功后的语言判断显示
                }
            }
        });
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //随机数
    public static String num(int a) {
        Random r = new Random(a);
        int ran1 = 0;
        for (int i = 0; i < 5; i++) {
            ran1 = r.nextInt(100);
            System.out.println(ran1);
        }
        return String.valueOf(ran1);
    }

    //MD5加密
    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }
}