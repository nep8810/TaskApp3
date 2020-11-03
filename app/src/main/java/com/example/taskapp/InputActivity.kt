package com.example.taskapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent

class InputActivity : AppCompatActivity() {

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null

    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false)
        timePickerDialog.show()
    }

    private val mOnDoneClickListener = View.OnClickListener {
        addTask()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        // ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        //setSupportActionBarメソッドにより、ツールバーをActionBarとして使えるように設定
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            //setDisplayHomeAsUpEnabledメソッドで、ActionBarに戻るボタンを表示している
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        val intent = intent
        //EXTRA_TASK から Task の id を取り出す
        //もし EXTRA_TASK が設定されていないと taskId には第二引数で指定している既定値 -1 が代入
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        //Task の id が taskId のものが検索され、findFirst() によって最初に見つかったインスタンスが返され、 mTask へ代入される
        // このとき、 taskId に -1 が入っていると、検索に引っかからず、 mTask には null が代入される
        // これは addTask で指定している、id が必ず 0 以上というアプリの仕様を利用している
        //taskId が -1 になるのはタスクを新規作成する場合を想定している（新規作成の場合は遷移元であるMainActivityから EXTRA_TASK は渡されない）
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()
        realm.close()

        if (mTask == null) {
            // 新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)
            category_edit_text.setText(mTask!!.category)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }

    private fun addTask() {
        val realm = Realm.getDefaultInstance()

        //Realmでデータを追加、削除など変更を行う場合はbeginTransactionメソッドを呼び出す
        realm.beginTransaction()

        if (mTask == null) {
            // 新規作成の場合
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mTask!!.id = identifier
        }

        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()
        val category = category_edit_text.text.toString()

        mTask!!.title = title
        mTask!!.contents = content
        mTask!!.category = category

        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.date = date

        //データの保存・更新はcopyToRealmOrUpdateメソッド
        realm.copyToRealmOrUpdate(mTask!!)
        //beginTransactionメソッドを使用した際は、最後にcommitTransactionメソッドを呼び出す必要がある
        realm.commitTransaction()

        realm.close()

        //まずTaskAlarmReceiverを起動するIntentを作成
        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        //そのExtraにタスクを設定 （TaskAlarmReceiverがブロードキャストを受け取った後、タスクのタイトルなどを表示する通知を発行するためにタスクの情報が必要になるため）
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)
        //PendingIntentを作成、第2引数にタスクのIDを指定（タスクを削除する際に指定したアラームも合わせて削除する必要あり）
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            //タスクを削除する際に指定したアラームも合わせて削除する必要
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        //AlarmManagerはActivityのgetSystemServiceメソッド（システムレベルのサービスを取得するためのメソッド）に引数ALARM_SERVICEを与えて取得する
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        //setメソッドの第一引数のRTC_WAKEUPは「UTC時間を指定する。画面スリープ中でもアラームを発行する。」という指定
        //第二引数でタスクの時間をUTC時間で指定
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
    }
}
