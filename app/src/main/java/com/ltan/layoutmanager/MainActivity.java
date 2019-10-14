package com.ltan.layoutmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRv;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mRv = findViewById(R.id.id_rv_layout);
        // LinearLayoutManager manager = new LinearLayoutManager(this);
        // MyLayoutManager manager = new MyLayoutManager(this);
        CustomLayoutManager manager = new CustomLayoutManager(this);
        // CustomLayoutManager manager = new CustomLayoutManager(this, true);
        mRv.setLayoutManager(manager);
        adapter = new MyAdapter(this);
        // adapter.setHasStableIds(true);
        adapter.updateTxts(initDate());
        mRv.setAdapter(adapter);
    }

    private ArrayList<ItemBean> initDate() {
        ArrayList<ItemBean> datas = new ArrayList<>();
        for (int i = 0; i < 22; i++) {
            ItemBean itemBean = new ItemBean();
            itemBean.iconUrl = "http://placehold.it/120x120&text=image" + (i + 1);
            itemBean.itemContent = "this is item " + (i + 1);
            datas.add(itemBean);
        }
        return datas;
    }

    public static class ItemBean {
        public String iconUrl;
        public String itemContent;

        @NonNull
        @Override
        public String toString() {
            return "url:" + iconUrl + ", content:" + itemContent;
        }
    }
}
