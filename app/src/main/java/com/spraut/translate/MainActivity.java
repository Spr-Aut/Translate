package com.spraut.translate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.spraut.translate.Adapter.TransAdapter;
import com.spraut.translate.DataBase.Note;
import com.spraut.translate.DataBase.NoteDbOpenHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Button btnAdd;
    private EditText editText;
    private TextView textView;
    private TransAdapter mTransAdapter;
    private RecyclerView mRecyclerView;
    private List<Note> mNotes;
    private NoteDbOpenHelper mNoteDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setStatusBar();
        initView();
        initData();
        initEvent();

    }//onCreate

    protected void onResume(){
        super.onResume();
        refreshDataFromDB();
    }

    private void refreshDataFromDB() {
        mNotes=getDataFromDB();
        mTransAdapter.refreshData(mNotes);
    }

    private List<Note> getDataFromDB(){
        return mNoteDbOpenHelper.queryAllFromDb();
    }

    private void initView(){
        //添加按钮
        btnAdd=findViewById(R.id.btn_main_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Translate.class);
                startActivity(intent);
            }
        });

        mRecyclerView=findViewById(R.id.rc_main);
    }

    private void initData(){
        mNotes=new ArrayList<>();
        mNoteDbOpenHelper=new NoteDbOpenHelper(this);
    }

    private void initEvent(){
        mTransAdapter=new TransAdapter(this,mNotes);
        mRecyclerView.setAdapter(mTransAdapter);

        RecyclerView.LayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mTransAdapter.setViewType(TransAdapter.TYPE_LINEAR_LAYOUT);
        mTransAdapter.notifyDataSetChanged();
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