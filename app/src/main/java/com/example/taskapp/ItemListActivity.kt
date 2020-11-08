package com.example.taskapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu


class ItemListActivity : AppCompatActivity() {

    companion object {
        private val TAG = ItemListActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  //★

        //FragmentはActivityのライフサイクル中に何度でも呼ばれることが可能なので最初だけ設定をするようにする
        if (savedInstanceState == null) {
            //使用するFragmentの作成
            val itemListFragment = ItemListFragment()
            //データベースでトランザクション(Fragmentの追加,削除,置換etcの情報処理)を開始
            //FragmentManagerは生成されたFragmentのインスタンスの状況を管理して、再度呼ばれると復元してくれる
            val transaction =supportFragmentManager.beginTransaction()
            //Fragmentを組み込む
            transaction.replace(R.id.container, itemListFragment)
            //上記の変更を反映
            transaction.commit()
        }
    }

    //MenuをActivity上に設置
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //参照するリソースは上記でリソースファイルに付けた名前と同じもの
        menuInflater.inflate(R.menu.menu_main, menu)  //★OK
        if (menu != null) {

            //SearchView.OnQueryTextListenerから入力欄の文字を取得
            val searchView = menu.findItem(R.id.action_search).actionView as SearchView  //★OK
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                //ユーザーが検索ボタンをクリックしたときに呼ばれる
                override fun onQueryTextSubmit(text: String?): Boolean {
                    Log.d(TAG, "submit text: $text")
                    return false
                }

                //ユーザーによって文字列が変更されたときに呼ばれる
                override fun onQueryTextChange(text: String?): Boolean {
                    Log.d(TAG, "change text: $text")
                    val itemListFragment = supportFragmentManager.findFragmentById(R.id.container)
                    if (itemListFragment is ItemListFragment && text != null) {
                        itemListFragment.searchRequest(text)
                    }
                    return false
                }
            })
        }
        //ActivityのもつMenuInflaterを取得し、リソースファイルをinflateしてmenuオブジェクトへ追加する
        return super.onCreateOptionsMenu(menu)
    }
}
