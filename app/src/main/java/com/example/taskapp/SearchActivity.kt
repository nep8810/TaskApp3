package com.example.taskapp

import android.app.SearchManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import com.example.taskapp.R.id.app_bar_search

class SearchActivity : AppCompatActivity() {

    private var searchView: SearchView? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_bar_search, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(app_bar_search).actionView as SearchView
        val searchableInfo = searchManager.getSearchableInfo(componentName)
        searchView?.setSearchableInfo(searchableInfo)

        //isIconified = trueでSearchViewを閉じる
        searchView?.isIconified

        //入力された文字列のイベントリスナー
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //ユーザーによってボタンが押された時に呼ばれる
            override fun onQueryTextSubmit(query: String?): Boolean {
                finish()
                return true
            }
            //ユーザーによって文字列が変更された時に呼ばれる
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return true
    }

}
