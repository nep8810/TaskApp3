package com.example.taskapp

import java.io.Serializable
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

//Serializableインターフェイスを実装することで生成したObjectをシリアライズすることができるようになる
//シリアライズとはデータを丸ごとファイルに保存したり、TaskAppでいうと別のActivityに渡すことができるようにすること
//open修飾子を付けるのは、Realmが内部的にTaskを継承したクラスを作成して利用するため
open class Task : RealmObject(), Serializable {
    var title: String = ""      // タイトル
    var contents: String = ""   // 内容
    var category: String = ""   // カテゴリ
    var date: Date = Date()     // 日時

    // id をPrimaryKeyとして設定
    // @PrimaryKeyはRealmがプライマリーキーと判断するために必要なもの
    //PrimaryKeyとはデーターベースの一つのテーブルの中でデータを唯一的に確かめるための値
    @PrimaryKey
    var id: Int = 0
}
