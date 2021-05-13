package tech.jhavidit.remindme.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityTimeReminderBinding
import tech.jhavidit.remindme.service.AlarmService

class TimeReminderActivity : AppCompatActivity() {
    private lateinit var binding : ActivityTimeReminderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_time_reminder)
        binding.dismiss.setOnClickListener{
            val intentService = Intent(applicationContext, AlarmService::class.java)
            applicationContext.stopService(intentService)
            finish()
        }
        if(intent.extras?.getString("dismiss") == "dismiss")
        {
            val intentService = Intent(applicationContext, AlarmService::class.java)
            applicationContext.stopService(intentService)
            finish()
        }
    }
}