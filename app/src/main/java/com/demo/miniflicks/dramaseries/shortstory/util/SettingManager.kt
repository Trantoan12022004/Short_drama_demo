package com.demo.miniflicks.dramaseries.shortstory.util
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.demo.miniflicks.dramaseries.shortstory.base.gone
import com.demo.miniflicks.dramaseries.shortstory.dialog.rate.RatingDialogOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory


object SettingManager {
    const val linkPolicyOscar = "https://bamecoglobal.com/Privacy%20Policy%20asd085-drama.html"
    const val emailOscar = "email_cua_ban@gmail.com"
}

fun Activity.shareApp() {
    val intentShareOscar = Intent(Intent.ACTION_SEND)
    intentShareOscar.type = "text/plain"
    intentShareOscar.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.app_name))
    intentShareOscar.putExtra(
        Intent.EXTRA_TEXT,
        "Download application :https://play.google.com/store/apps/details?id=${this.packageName}"
    )
    this.startActivity(Intent.createChooser(intentShareOscar, "Share with"))
}

fun Activity.feedbackApp() {
    val uriTextOscar = """
                 mailto:${SettingManager.emailOscar}?subject=Feedback for ${this.getString(R.string.app_name)}
                 &body=${this.getString(R.string.app_name)}
                 Feedback: 
                 """.trimIndent()
    val uriOscar = Uri.parse(uriTextOscar)
    val sendIntentOscar = Intent(Intent.ACTION_SENDTO)
    sendIntentOscar.data = uriOscar
    try {
        this.startActivity(
            Intent.createChooser(
                sendIntentOscar, this.getString(R.string.Send_Email)
            )
        )
    } catch (_: Exception) {
        Toast.makeText(this, this.getString(R.string.feedback_failed), Toast.LENGTH_SHORT).show()
    }
}

fun Activity.rateApp(view: View?) {
    RatingDialogOscar(this, onSend = { rating ->
        val uriTextOscar = """
                 mailto:${SettingManager.emailOscar}?subject=Review for ${this.getString(R.string.app_name)}&body=${
            this.getString(
                R.string.app_name
            )
        }
                 Rate : $rating
                 Content: 
                 """.trimIndent()
        val uriOscar = Uri.parse(uriTextOscar)
        val sendIntentOscar = Intent(Intent.ACTION_SENDTO)
        sendIntentOscar.data = uriOscar
        try {
            view!!.gone()
            this.startActivity(
                Intent.createChooser(
                    sendIntentOscar, this.getString(R.string.Send_Email)
                )
            )
            SharedPreUtilsOscar.getInstance().forceRated(this)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                this, this.getString(R.string.app_review_failed), Toast.LENGTH_SHORT
            ).show()
        }
    }, onRate = {
        val managerOscar = ReviewManagerFactory.create(this)
        val requestOscar = managerOscar.requestReviewFlow()
        requestOscar.addOnCompleteListener { task: Task<ReviewInfo?> ->
            if (task.isSuccessful) {
                val reviewInfoOscar = task.result
                val flowOscar = managerOscar.launchReviewFlow(
                    this, reviewInfoOscar!!
                )
                flowOscar.addOnSuccessListener {
                    SharedPreUtilsOscar.getInstance().forceRated(this)
                    view!!.gone()
                }
            }
        }
    }).show()
}