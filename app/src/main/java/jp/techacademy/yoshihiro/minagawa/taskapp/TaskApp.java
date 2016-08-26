package jp.techacademy.yoshihiro.minagawa.taskapp;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by ym on 16/08/26.
 */
public class TaskApp extends Application {

    //RealmクラスのSetDefaultConfigurationメソッドで設定します。

    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }


}
