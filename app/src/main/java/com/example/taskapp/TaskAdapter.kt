package com.example.taskapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

//BaseAdapterクラスを継承させる
class TaskAdapter(context: Context): BaseAdapter() {
    private val mLayoutInflater: LayoutInflater
    var taskList = mutableListOf<Task>() //可変リスト

    init {
        this.mLayoutInflater = LayoutInflater.from(context)
    }

        override fun getCount(): Int {  //getCountメソッドの実装:アイテム（データ）の数を返す
            return taskList.size
        }

        override fun getItem(position: Int): Any {  //getItemメソッドの実装:アイテム（データ）を返す
            return taskList[position]
        }

        override fun getItemId(position: Int): Long {  //getItemメソッドの実装:アイテム（データ）を返す
            return taskList[position].id.toLong()
        }


    //他のxmlリソースのViewを取り扱うための仕組みであるLayoutInflaterをプロパティとして定義しておき、コンストラクタを新規に追加して取得しておく
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //getViewメソッドの引数であるconvertViewがnullのときはLayoutInflaterを使ってsimple_list_item_2からViewを取得する(エルビス演算子?:は左辺がnullの時に右辺を返す)
        //nullかどうか判定を行っているのは、BaseAdapterにViewを再利用して描画する仕組みがあるため
        val view: View = convertView ?: mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null)

        val textView1 = view.findViewById<TextView>(android.R.id.text1)  //text1はタスク一覧画面のタイトルの部分
        val textView2 = view.findViewById<TextView>(android.R.id.text2)  //text2はタスク一覧画面の日時の部分

        //textView1にカテゴリもまとめて表示させる
        textView1.text = taskList[position].title.plus("《カテゴリ：").plus(taskList[position].category).plus("》")

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE)
        val date = taskList[position].date
        textView2.text = simpleDateFormat.format(date)

        return view
    }
  }

