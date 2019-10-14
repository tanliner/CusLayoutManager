package com.ltan.layoutmanager;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ltan.layoutmanager.MainActivity.ItemBean;

import java.util.ArrayList;

/**
 * My Application.com.ltan.myapplication
 *
 * @ClassName: MyAdapter
 * @Description:
 * @Author: tanlin
 * @Date: 2019-10-11
 * @Version: 1.0
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private static final String TAG = "ltan/MyAdapter";
    private static final boolean DEBUG = false;
    private ArrayList<ItemBean> datas;
    private Context mCtx;

    public MyAdapter(Context context) {
        datas = new ArrayList<>();
        mCtx = context;
    }

    public void updateTxts(ArrayList<ItemBean> beans) {
        datas.clear();
        datas.addAll(beans);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        if (DEBUG) {
            Log.d(TAG, "onCreateViewHolder: ");
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemBean item = datas.get(position);
        if (DEBUG) {
            Log.d(TAG, "onBindViewHolder: " + item + ", position:" + position);
        }
        Glide.with(mCtx)
                .load(item.iconUrl)
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.iconIv);
        holder.contentTv.setText(item.itemContent);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconIv = itemView.findViewById(R.id.iv_icon);
            contentTv = itemView.findViewById(R.id.tv_content);
        }

        public ImageView iconIv;
        public TextView contentTv;
    }
}
