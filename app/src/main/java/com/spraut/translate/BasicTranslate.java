package com.spraut.translate;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BasicTranslate {
    private String appId = "20220302001106832";//APP ID
    private String key = "HA1UmLX3HyfdPuJXOhuT";//秘钥

    public String translate(String inputTx,String fromType,String toType){
        String salt = num(1);//随机数
        //拼接一个字符串然后加密
        String spliceStr = appId + inputTx + salt + key;//根据百度要求 拼接
        String sign = stringToMD5(spliceStr);//将拼接好的字符串进行MD5加密   作为一个标识

        //通用翻译API HTTP地址：
        //http://api.fanyi.baidu.com/api/trans/vip/translate
        //通用翻译API HTTPS地址：
        //https://fanyi-api.baidu.com/api/trans/vip/translate

        String httpStr = "http://api.fanyi.baidu.com/api/trans/vip/translate";
        String httpsStr = "https://fanyi-api.baidu.com/api/trans/vip/translate";
        //拼接请求的地址
        String url = httpsStr +
                "?appid=" + appId + "&q=" + inputTx + "&from=" + fromType + "&to=" +
                toType + "&salt=" + salt + "&sign=" + sign;

        return url;
    }

    //同步Get请求
    private String syncGet(String content, String fromType, String toType, String salt, String sign) throws IOException {
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
        final Request request=new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call=okHttpClient.newCall(request);
        String res="";
        try{
            Response response=call.execute();
            TranslateResult result=new Gson().fromJson(response.body().string(),TranslateResult.class);
            res=result.getTrans_result().get(0).getDst();
        }catch (IOException e){
            e.printStackTrace();
        }
        return res;
    }

    //随机数
    public String num(int a) {
        Random r = new Random(a);
        int ran1 = 0;
        for (int i = 0; i < 5; i++) {
            ran1 = r.nextInt(100);
            System.out.println(ran1);
        }
        return String.valueOf(ran1);
    }

    //MD5加密
    public String stringToMD5(String string) {
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
