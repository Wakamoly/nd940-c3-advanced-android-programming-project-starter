package com.udacity.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.udacity.R
import com.udacity.models.NotificationModel
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var fileStatus: String
    private lateinit var fileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        receiveExtras()
        initButton()
    }

    private fun receiveExtras(){
        val extras = intent.extras
        extras?.let {
            // I believe Parcelable models to be a better alternative to passing strings or ints as intent-extra puts.
            val notificationModel: NotificationModel? = it.getParcelable("notification_model")
            notificationModel?.apply {
                fileStatus = status
                fileName = file_name
            }
        }
        initView()
    }

    private fun initView(){
        // "::_____.isInitialized" lets us know whether or not our lateinit variables have been initialized
        // in this case, by receiveExtras()
        if (::fileStatus.isInitialized){
            file_status_text.text = fileStatus
        }else{
            file_status_text.text = getString(R.string.notification_failure)
        }

        // Completely unnecessary, but for good fun.
        detail_motion.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                file_name_text.text = getString(R.string.aaaaaaaaaaaa)
            }
            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                if (::fileName.isInitialized){
                    file_name_text.text = fileName
                }else{
                    file_name_text.text = getString(R.string.notification_failure)
                }
            }
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {} // no implementation
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {} // no implementation
        })
    }

    // Finish the activity on button_okay click.
    private fun initButton() {
        button_okay.setOnClickListener {
            finish()
        }
    }

}
