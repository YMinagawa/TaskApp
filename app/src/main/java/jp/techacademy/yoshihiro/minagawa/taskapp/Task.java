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
    private String category; //カテゴリー
    private Date date; //日時

    //idをプライマリーキーとして設定する
    @PrimaryKey
    private int id;

    //タイトル
    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    //内容
    public String getContents(){
        return contents;
    }

    public void setContents(String contents){
        this.contents = contents;
    }

    //カテゴリー
    public String getCategory(){
        return category;
    }

    public void setCategory(String category){
        this.category = category;
    }

    //日付
    public Date getDate(){
        return date;
    }

    public void setDate(Date date){
        this.date = date;
    }

    //ID
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

}
