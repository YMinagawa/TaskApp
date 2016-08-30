package jp.techacademy.yoshihiro.minagawa.taskapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class InputActivity extends AppCompatActivity {

    private int mYear, mMonth, mDay, mHour, mMinute;
    private Button mDateButton, mTimeButton;
    private EditText mTitleEdit, mContentEdit, mCategoryEdit;
    private Task mTask;
    private View.OnClickListener mOnDateClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(InputActivity.this,
                    new DatePickerDialog.OnDateSetListener(){
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            String dateString = mYear + "/" + String.format("%02d", (mMonth+1)) + "/" + String.format("%02d", mDay);
                            mDateButton.setText(dateString);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    };

    private View.OnClickListener mOnTimeClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener(){
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
                            mTimeButton.setText(timeString);

                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    };

    private View.OnClickListener mOnDoneClickListener =
            new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //finishメソッドを呼び出すことでinputActivityを閉じて前の画面に戻る
            addTask();
            finish();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        //ActionBarを設定する
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //UI部品の設定
        mDateButton = (Button)findViewById(R.id.date_button);
        mDateButton.setOnClickListener(mOnDateClickListener);
        mTimeButton = (Button)findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);
        mTitleEdit = (EditText)findViewById(R.id.title_edit_text);
        mContentEdit = (EditText)findViewById(R.id.content_edit_text);
        mCategoryEdit = (EditText)findViewById(R.id.category_edit_text);

        //MainActivityから回ってきたIntentのExtraからTaskを取り出す。
        //新規作成の場合は遷移元であるMainActivityからTaskは渡されないのでnullになる。
        //nullであれば現在時刻から時間を設定する。
        Intent intent = getIntent();
        mTask = (Task)intent.getSerializableExtra(MainActivity.EXTRA_TASK);

        if(mTask == null){
            //新規作成の場合
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
        }else {
            //更新の場合
            mTitleEdit.setText(mTask.getTitle());
            mContentEdit.setText(mTask.getContents());
            mCategoryEdit.setText(mTask.getCategory());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mTask.getDate());
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            String dateString = mYear + "/" + String.format("%02d", (mMonth+1)) + "/" + String.format("%02d", mDay);
            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);
        }

    }

    //
    //Realmオブジェクトを取得する。
    //mTaskがnullのときはTaskクラスを生成し保存されているタスクの中の最大のidの値に1を足したものを設定する
    //こーすることでユニークなIDを設定可能
    //そして色々な値をmTaskに設定し、データベースに保存する

    //Realmでデータを追加、削除など変更する場合はbeginTransactionメソッドを呼び出し、削除などの処理、
    //そして最後にcommitTransactionメソッドを呼び出す必要がある。
    private void addTask(){
        Realm realm = Realm.getDefaultInstance();

        if(mTask == null){
            //新規作成の場合
            mTask = new Task();
            RealmResults<Task> taskRealmResults
                    =realm.where(Task.class).findAll();

            int identifier;
            if(taskRealmResults.max("id") != null){
                identifier = taskRealmResults.max("id").intValue() + 1;
            }else{
                identifier = 0;
            }
            mTask.setId(identifier);

        }

        String title = mTitleEdit.getText().toString();
        String content = mContentEdit.getText().toString();
        String category = mCategoryEdit.getText().toString();

        mTask.setTitle(title);
        mTask.setContents(content);
        mTask.setCategory(category);
        GregorianCalendar calendar = new GregorianCalendar(mYear, mMonth, mDay, mHour,mMinute);
        Date date = calendar.getTime();
        mTask.setDate(date);

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(mTask);
        realm.commitTransaction();

        realm.close();

        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
        //タスクのタイトル等を表示する通知を発行するためにタスクの情報が必要。
        resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask);
        //PendingIntentを作成する。
        //第2引数にタスクのIDを指定。タスクを削除するときにアラームも合わせて削除するため。
        //PendingIntentを一意に識別するのにタスクのIDを設定する。
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                this,
                mTask.getId(),
                resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        //AlarmManageで指定した時間に任意の処理が可能
        //RTC_WAKEUPは「UTC時間を指定する。画面スリープ中でもアラームを発行する」
        //第二引数でタスクの時間をUTC時間で指定している。
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), resultPendingIntent);

    }

}
