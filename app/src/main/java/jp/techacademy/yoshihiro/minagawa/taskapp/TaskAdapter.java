package jp.techacademy.yoshihiro.minagawa.taskapp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * Created by ym on 16/08/25.
 */
public class TaskAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;
    private ArrayList<Task> mTaskArrayList;
    private String mSearchWord;

    public TaskAdapter(Context context){
        //LayoutInflaterを使えば動的にレイアウトxmlからViewを生成することが可能
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setTaskArrayList(ArrayList<Task> taskArrayList){
        mTaskArrayList = taskArrayList;
    }

    public void setSearchWord(String searchWord){
        mSearchWord = searchWord;
    }

    @Override
    public int getCount() {
        return mTaskArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTaskArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null);
        }

        TextView textView1 = (TextView)convertView.findViewById(android.R.id.text1);
        TextView textView2 = (TextView)convertView.findViewById(android.R.id.text2);

        //タスクのgetTitleメソッドでTitleを入手してsetする
        textView1.setText(mTaskArrayList.get(position).getTitle());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE);
        Date date = mTaskArrayList.get(position).getDate();
        textView2.setText(simpleDateFormat.format(date));


        return convertView;
    }


}
