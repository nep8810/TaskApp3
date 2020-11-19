package com.example.taskapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.Sort
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.SearchManager
import android.content.Context
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.widget.Toast
import io.realm.Case


const val EXTRA_TASK = "com.example.taskapp.TASK"
var taskList = mutableListOf<Task>() //可変リスト

class MainActivity : AppCompatActivity() {
    //Realmクラスを保持するmRealmを定義
    //lateinitで宣言することで初期化タイミングをonCreate()まで遅延させている
    //変数をlateinit宣言することにより、non-null な初期化済みの変数として参照することができるようになる
    private lateinit var mRealm: Realm
    //RealmChangeListenerクラスのmRealmListenerはRealmのデータベースに追加や削除など変化があった場合に呼ばれるリスナー
    private val mRealmListener = object : RealmChangeListener<Realm> {
        //onChangeメソッドをoverrideしてreloadListViewメソッドを呼び出す
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //＋ボタンを押したときの処理
        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)
        }

        // Realmの設定
        //getDefaultInstanceメソッドでRealmクラスのobjectを取得➡このメソッド使用した場合、closeメソッドで終了させる必要がある
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            //Intentによってクラスの垣根を超えてTaskのidを渡したり(putExtra())受け取ったり(putExtra())できる。
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        reloadListView()
    }

    private fun reloadListView() {
        // Realmデータベースから「全てのデータを取得して新しい日時順に並べた結果」を取得
        // findAll で全てのTaskデータを取得して、sortで"date" （日時）を Sort.DESCENDING （降順）で並べ替えた結果を返す
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        // 上記の結果をmRealm.copyFromRealm(taskRealmResults) でコピーしてTaskList としてセットする
        // Realmのデータベースから取得した内容をAdapterなど別の場所で使う場合は直接渡すのではなくコピーして渡す必要がある
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプタにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }


    private var searchView: SearchView? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.app_bar_search, menu)
    val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
    searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
    val searchableInfo = searchManager.getSearchableInfo(componentName)
    searchView?.setSearchableInfo(searchableInfo)

    //isIconified = trueでSearchViewを閉じる
    searchView?.isIconified

    //入力された文字列のイベントリスナー
    searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        //ユーザーによってボタンが押された時に呼ばれる
        override fun onQueryTextSubmit(query: String?): Boolean {

            val CATEGORY = query!!
            //インスタンス作成
            val mRealm = Realm.getDefaultInstance()

            if (R.menu.app_bar_search!= null && !R.menu.app_bar_search.equals("")) {
                //全取得＆絞り込み
                //equalTo➡containsで部分一致に
                //Case.INSENSITIVEで大文字小文字の区別をしない
                val taskRealmResults = mRealm.where(Task::class.java).contains("category", CATEGORY, Case.INSENSITIVE).findAll().sort("category",Sort.DESCENDING)


                //上記の結果をtaskListとしてセットする
                mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

                // TaskのListView用のアダプタに渡す
                listView1.adapter = mTaskAdapter

                // 表示を更新するために、アダプタにデータが変更されたことを知らせる
                mTaskAdapter.notifyDataSetChanged()
            }else if (query.isEmpty()){
                val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

                mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

                listView1.adapter = mTaskAdapter

                mTaskAdapter.notifyDataSetChanged()

            }else{
                Toast.makeText(applicationContext, "入力されたカテゴリは見つかりませんでした。", Toast.LENGTH_SHORT).show()
                finish()
            }
            return true
        }

        //ユーザーによって文字列が変更された時に呼ばれる
        override fun onQueryTextChange(newText: String?): Boolean {

            val CATEGORY = newText!!
            //インスタンス作成
            val mRealm = Realm.getDefaultInstance()

            if (R.menu.app_bar_search!= null && !R.menu.app_bar_search.equals("")) {
                //全取得＆絞り込み
                val taskRealmResults = mRealm.where(Task::class.java).contains("category", CATEGORY,Case.INSENSITIVE).findAll().sort("category",Sort.DESCENDING)

                mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

                // TaskのListView用のアダプタに渡す
                listView1.adapter = mTaskAdapter

                // 表示を更新するために、アダプタにデータが変更されたことを知らせる
                mTaskAdapter.notifyDataSetChanged()
            }else if (R.menu.app_bar_search.equals("")){ //query.equals("")が記述できないため

                val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

                mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

                listView1.adapter = mTaskAdapter

                mTaskAdapter.notifyDataSetChanged()

            }else{
                Toast.makeText(applicationContext, "入力されたカテゴリは見つかりませんでした。", Toast.LENGTH_SHORT).show()
                finish()
            }
            return true
        }
    })
    return true

 }

}
