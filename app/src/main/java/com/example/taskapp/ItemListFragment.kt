package com.example.taskapp

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ItemListFragment : android.support.v4.app.Fragment() {

    private var mView: View? = null
    private var recyclerView: RecyclerView? = null
    private val items: MutableList<String> = mutableListOf()  //listOf()と違って変換可能
    private var adapter: ItemListAdapter? = null

    init {
        for (i in 1..100) {
            items.add("アイテム$i")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        mView = inflater!!.inflate(R.layout.activity_main, container, false)  //★
        //LinearLayoutManagerのインスタンスを生成
        val linerLayoutManager = LinearLayoutManager(mView!!.context)
        recyclerView = mView!!.findViewById(R.id.action_search) //★?
        recyclerView!!.layoutManager = linerLayoutManager

        //行間に区切り線の実装(RecyclerViewにはデフォルトでは区切りがないため)
        val dividerDecoration = DividerItemDecoration(recyclerView!!.context, linerLayoutManager.orientation)
        recyclerView!!.addItemDecoration(dividerDecoration)

        return mView!!
    }

    //onActivityCreated()は、Fragmentが属するActivityのonCreate()が完了した際に呼び出される
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = ItemListAdapter(mView!!.context, items)
        recyclerView!!.adapter = adapter
    }

    //検索文字を受け取ったら検索対象にフィルターをかけて更新
    fun searchRequest(text: String) {
        val adapter = adapter
        if (adapter != null) {
            adapter.data = items.filter { it.contains(text) }
            //notifyDataSetChanged()でリスト全体の更新
            adapter.notifyDataSetChanged()
        }
    }
}
