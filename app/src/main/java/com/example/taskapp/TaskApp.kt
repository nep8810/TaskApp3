package com.example.taskapp

import android.app.Application
import io.realm.Realm

//Applicationクラスを継承しただけではこのクラスは使われることはない➡AndroidManifest.xmlにandroid:name=".TaskApp"を追記
class TaskApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this) //Realmを初期化(デフォルト設定を使用する場合にこのように記述)
    }
}
