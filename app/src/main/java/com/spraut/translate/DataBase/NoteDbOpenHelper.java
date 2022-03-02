package com.spraut.translate.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NoteDbOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME="noteSQLite.db";
    private static final String TABLE_NAME_NOTE="note";

    /*
    private String keyword;
    private String value;
    private String id;
    */

    private static final String CREATE_TABLE_SQL="create table " + TABLE_NAME_NOTE +" (id integer primary key autoincrement, keyword text, value text)";


    public NoteDbOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertData(Note note){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues values=new ContentValues();

        values.put("keyword",note.getKeyword());
        values.put("value",note.getValue());

        return db.insert(TABLE_NAME_NOTE,null,values);
    }

    public int deleteFromDbById(String id){
        SQLiteDatabase db=getWritableDatabase();
        return db.delete(TABLE_NAME_NOTE,"id like ?",new String[]{id});
    }

    public List<Note>queryAllFromDb(){
        SQLiteDatabase db=getWritableDatabase();
        List<Note>noteList=new ArrayList<>();

        Cursor cursor=db.query(TABLE_NAME_NOTE,null,null,null,null,null,null);
        if (cursor!=null){
            while (cursor.moveToNext()){
                String keyword=cursor.getString(cursor.getColumnIndex("keyword"));
                String value=cursor.getString(cursor.getColumnIndex("value"));
                String id=cursor.getString(cursor.getColumnIndex("id"));

                Note note=new Note();
                note.setKeyword(keyword);
                note.setValue(value);
                note.setId(id);

                noteList.add(note);
            }
            cursor.close();
        }
        return noteList;
    }
}
