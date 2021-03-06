package com.spraut.translate.Adapter;

import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.spraut.translate.DataBase.Note;
import com.spraut.translate.DataBase.NoteDbOpenHelper;
import com.spraut.translate.R;
import com.spraut.translate.Translate;

import java.util.List;

public class TransAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Note>mBeanList;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private NoteDbOpenHelper mNoteDbOpenHelper;
    private int viewType;
    public static final int TYPE_LINEAR_LAYOUT = 0;
    public static final int TYPE_GRID_LAYOUT = 1;

    public TransAdapter(Context mContext,List<Note>mBeanList){
        this.mBeanList=mBeanList;
        this.mContext=mContext;
        mLayoutInflater=LayoutInflater.from(mContext);
        mNoteDbOpenHelper=new NoteDbOpenHelper(mContext);
    }

    public void setViewType(int viewType){
        this.viewType=viewType;
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==TYPE_LINEAR_LAYOUT){
            View view=mLayoutInflater.inflate(R.layout.item_main,parent,false);
            TransViewHolder myTransViewHolder=new TransViewHolder(view);
            return myTransViewHolder;
        }else if (viewType==TYPE_GRID_LAYOUT){
            return null;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder==null){
            return;
        }
        if (holder instanceof TransViewHolder){
            bindTransViewHolder((TransViewHolder)holder,position);
        }
    }

    @Override
    public int getItemCount() {
        return mBeanList.size();
    }

    public void refreshData(List<Note> notes){
        this.mBeanList=notes;
        notifyDataSetChanged();
    }

    //??????
    private void bindTransViewHolder(TransViewHolder holder,int position){
        //????????????????????????
        holder.mTvOrigin.setVisibility(View.VISIBLE);
        holder.mTvTrans.setVisibility(View.VISIBLE);
        holder.mViewMain.setVisibility(View.VISIBLE);
        holder.mViewSlideDelete.setVisibility(View.VISIBLE);
        holder.mViewSlideMore.setVisibility(View.VISIBLE);

        Note note=mBeanList.get(position);

        holder.mTvOrigin.setText(note.getKeyword());
        holder.mTvTrans.setText(note.getValue());
        holder.mViewMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        holder.mViewSlideDelete.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                shake();
                deleteItem(note,position);
            }
        });
        holder.mViewSlideMore.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                shake();
                showMsg("???????????????????????????");
            }
        });
    }

    private void deleteItem(Note note,int position){
        int row=mNoteDbOpenHelper.deleteFromDbById(note.getId()+"");
        if(row>0){
            removeData(position);
            showMsg("?????? "+note.getKeyword());
        }
    }

    private void removeData(int position) {
        mBeanList.remove(position);
        //??????remove??????
        notifyItemRemoved(position);
        //????????????????????????Item?????????Item????????????notifyItemRangeRemoved??????????????????Item????????????
        notifyItemRangeChanged(position,mBeanList.size()-position);
    }

    //????????????
    class TransViewHolder extends RecyclerView.ViewHolder{
        TextView mTvOrigin;
        TextView mTvTrans;
        CardView mCardView;
        CardView mCardViewSlide;
        RelativeLayout mViewMain;
        LinearLayout mViewSlideDelete;
        LinearLayout mViewSlideMore;

        public TransViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mTvOrigin=itemView.findViewById(R.id.tv_item_origin);
            this.mTvTrans=itemView.findViewById(R.id.tv_item_trans);
            this.mViewMain=itemView.findViewById(R.id.view_item_main);
            this.mViewSlideDelete=itemView.findViewById(R.id.view_item_main_slide_delete);
            this.mViewSlideMore=itemView.findViewById(R.id.view_item_main_slide_more);
        }
    }

    private void showMsg(String msg) {
        Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM,0,0);
        toast.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void shake(){
        //??????
        Vibrator vibrator=(Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
    }
}
