package com.example.taskapp

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

//Applicationクラスを継承しただけではこのクラスは使われることはない➡AndroidManifest.xmlにandroid:name=".TaskApp"を追記
class TaskApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this) //Realmを初期化(デフォルト設定を使用する場合にこのように記述)

        //RealmConfigurationにて.deleteRealmIfMigrationNeeded()を設定
        //➡ もともとあったデータを全て削除してくれるようになる。
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(realmConfig)
    }
}
