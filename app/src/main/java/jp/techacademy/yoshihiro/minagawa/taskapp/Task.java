package jp.techacademy.yoshihiro.minagawa.taskapp;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ym on 16/08/26.
 */
public class Task extends RealmObject implements Serializable{

    //implements Serializable
    //シリアライズすることでデータを丸ごとファイルに保存

    private String title; //タイトル
    private String contents; //内容
    private Date date; //日時

    //idをプライマリーキーとして設定する
    @PrimaryKey
    private int id;

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getContents(){
        return contents;
    }

    public void setContents(String contents){
        this.contents = contents;
    }

    public Date getDate(){
        return date;
    }

    public void setDate(Date date){
        this.date = date;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

}
