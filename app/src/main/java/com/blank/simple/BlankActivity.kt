package com.blank.simple

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.xc.blank.ChoiceBlankBean
import kotlinx.android.synthetic.main.activity_blank.blank_root_view
import kotlinx.android.synthetic.main.activity_blank.get_answer
import kotlinx.android.synthetic.main.activity_blank.tv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.StringBuilder

class BlankActivity : AppCompatActivity() {
    lateinit var launch: Job
    lateinit var path: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank)
        path = intent.getStringExtra("path")
        initData()
        get_answer.setOnClickListener {
            val answerResult = blank_root_view.getAnswerResult()
            val sb = StringBuilder()
            answerResult.forEach {
                if (!TextUtils.isEmpty(it.optValue)) {
                    sb.append("${it.optValue}；")
                } else {
                    sb.append("；")
                }
            }
            tv.text = sb.toString()
        }
    }

    private fun initData() {
        //最原始的协程,阻塞式的，需要在onDestroy的时候关掉协程
        launch = GlobalScope.launch(Dispatchers.IO) {
            val jsonData = getJsonData()
            withContext(Dispatchers.Main) {
                blank_root_view.setText(jsonData)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        launch.cancel()
    }

    private suspend fun getJsonData() = coroutineScope {
        var bean: ChoiceBlankBean? = null
        try {
            assets.open(path).use { inputStream ->
                bean = inputStream.reader().use { reader ->
                    val gsonToBean = GsonToBean(reader, ChoiceBlankBean::class.java)
                    gsonToBean
                }
                bean
            }
            bean
        } catch (e: Exception) {
            bean

        }
    }

}