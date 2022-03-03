package com.spraut.translate.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.spraut.translate.DataBase.Note;
import com.spraut.translate.DataBase.NoteDbOpenHelper;
import com.spraut.translate.R;

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

    private void bindTransViewHolder(TransViewHolder holder,int position){
        //解决显示错乱问题
        holder.mTvOrigin.setVisibility(View.VISIBLE);
        holder.mTvTrans.setVisibility(View.VISIBLE);
        holder.mViewMain.setVisibility(View.VISIBLE);
        holder.mViewSlideDelete.setVisibility(View.VISIBLE);

        Note note=mBeanList.get(position);

        holder.mTvOrigin.setText(note.getKeyword());
        holder.mTvTrans.setText(note.getValue());
        holder.mViewMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        holder.mViewSlideDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(note,position);
            }
        });
    }

    private void deleteItem(Note note,int position){
        int row=mNoteDbOpenHelper.deleteFromDbById(note.getId()+"");
        if(row>0){
            removeData(position);
        }
    }

    private void removeData(int position) {
        mBeanList.remove(position);
        //显示remove动画
        notifyItemRemoved(position);
        //删除后刷新被删的Item之后的Item，不要用notifyItemRangeRemoved，不然后面的Item会闪一下
        notifyItemRangeChanged(position,mBeanList.size()-position);
    }

    class TransViewHolder extends RecyclerView.ViewHolder{
        TextView mTvOrigin;
        TextView mTvTrans;
        CardView mCardView;
        CardView mCardViewSlide;
        RelativeLayout mViewMain;
        LinearLayout mViewSlideDelete;

        public TransViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mTvOrigin=itemView.findViewById(R.id.tv_item_origin);
            this.mTvTrans=itemView.findViewById(R.id.tv_item_trans);
            this.mViewMain=itemView.findViewById(R.id.view_item_main);
            this.mViewSlideDelete=itemView.findViewById(R.id.view_item_main_slide_delete);
        }
    }
}
