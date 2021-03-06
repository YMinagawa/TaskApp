package jp.techacademy.yoshihiro.minagawa.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_TASK = "jp.techacademy.yoshihiro.minagawa.taskapp.TASK";

    private ListView mListView;
    private EditText mEditText;
    private TaskAdapter mTaskAdapter;

    private Realm mRealm;
    //RealmResultsはデータベースから取得した結果を保持する変数
    private RealmResults<Task> mTaskRealmResults;
    //RealmChangeListener : Realmのデータベースの追加や削除など変化があったときに呼び出される
    private RealmChangeListener mRealmChangeListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            filterList(mEditText.getText().toString());
            reloadListView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        //Realmの設定
        mRealm = Realm.getDefaultInstance();
        mTaskRealmResults = mRealm.where(Task.class).findAll();
        mTaskRealmResults.sort("date", Sort.DESCENDING);
        mRealm.addChangeListener(mRealmChangeListener);

        //ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mEditText = (EditText)findViewById(R.id.editText1);
        mListView = (ListView)findViewById(R.id.listView1);

        //ListView内のItemをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //入力・編集する画面に遷移させる
                Task task = (Task)parent.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task);
                startActivity(intent);
            }
        });

        //ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                //タスクを削除する
                final Task task = (Task)parent.getAdapter().getItem(position);
                //ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                   @Override
                   public void onClick(DialogInterface dialog, int which){
                       RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();
                       mRealm.beginTransaction();
                       results.clear();
                       mRealm.commitTransaction();

                       Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                       PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                            MainActivity.this,
                            task.getId(),
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                       );

                       AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                       alarmManager.cancel(resultPendingIntent);

                       reloadListView();
                   }
                });

                builder.setNegativeButton("CANCEL", null);
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;

            }
        });

        //EditTextにリスナー実装
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(mEditText.getText().toString());
                reloadListView();
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterList(mEditText.getText().toString());
                reloadListView();
            }
        });

        //タスクのデータが0の場合
        //addTaskForTestメソッドを呼び出して仮データを保存する
        if(mTaskRealmResults.size() == 0){
            addTaskForTest();
        }

        reloadListView();
    }

    private void reloadListView(){

        //後でTaskクラスに変更する
        ArrayList<Task> taskArrayList = new ArrayList<>();

        for(int i= 0; i < mTaskRealmResults.size(); i++){
            Task task = new Task();
            task.setId(mTaskRealmResults.get(i).getId());
            task.setTitle(mTaskRealmResults.get(i).getTitle());
            task.setContents(mTaskRealmResults.get(i).getContents());
            task.setCategory((mTaskRealmResults.get(i).getCategory()));
            task.setDate(mTaskRealmResults.get(i).getDate());

            taskArrayList.add(task);
        }

        mTaskAdapter.setTaskArrayList(taskArrayList);
        mTaskAdapter.setSearchWord(mEditText.getText().toString());
        mListView.setAdapter(mTaskAdapter);
        mTaskAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mRealm.close();
    }

    private void addTaskForTest(){
        Task task = new Task();
        task.setTitle("作業");
        task.setContents("プログラムを書いてPUSHする");
        task.setCategory("カテゴリーを入力する");
        task.setDate(new Date());
        task.setId(0);
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(task);
        mRealm.commitTransaction();
    }

    private void filterList(String searchWord){

        RealmQuery<Task> realmQuery = mRealm.where(Task.class);
        if(!searchWord.equals("")) {
            //半角スペースで文字列を分割
            String[] filterWords = searchWord.split(" ", 0);

            for (int i = 0; i < filterWords.length; i++) {
                if(i != 0){
                    realmQuery.or();
                }

                realmQuery.equalTo("category", filterWords[i]);
            }
        }

        mTaskRealmResults = realmQuery.findAll();

    }

}
