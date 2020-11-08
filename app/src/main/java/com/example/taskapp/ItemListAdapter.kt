package com.example.taskapp

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ItemListAdapter(private val context: Context,
                               var data: List<String>) : RecyclerView.Adapter<ItemListAdapter.ViewHolder>() {

    //onCreateViewHolder,getItemCount,onBindViewHolderを実装
    // Viewのinflate(膨らむ)、ViewHolderを作成し、返している
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        //support_simple_spinner_dropdown_itemはプルダウンで項目選択可
        return ViewHolder(inflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false))  //★？
    }

    //RecyclerViewで表示するアイテムの数を返す
    override fun getItemCount(): Int {
        return data.size
    }

    //onBindViewHolderでリストアイテムごとのデータ情報を保持する
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.title = data[position]  //nullであっても例外が発生しないようにする
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var titleTextView: TextView = itemView.findViewById(R.id.title)

        //リストアイテムに対応するデータを取得する
        var title: String
            get() = titleTextView.text.toString()
            set(value) {
                titleTextView.text = value
            }
    }

}
