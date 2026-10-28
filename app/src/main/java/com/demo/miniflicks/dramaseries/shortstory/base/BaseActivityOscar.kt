package com.demo.miniflicks.dramaseries.shortstory.base
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import com.demo.miniflicks.dramaseries.shortstory.R


abstract class BaseActivityOscar<VB : ViewBinding>(val bindingFactory: (LayoutInflater) -> VB) :
    AppCompatActivity() {

    protected val binding: VB by lazy { bindingFactory(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        SystemUtilOscar.setLocale(this)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        window.hideNavigation()
        window.hideStatusBar()

        getData()
        initView()
        bindView()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
    }

    open fun getData() {

    }

    open fun initView() {

    }

    open fun bindView() {

    }

    open fun reloadAds() {

    }


    fun startNextActivity(activity: Class<*>?, bundle: Bundle?) {
        var bundleOscar = bundle
        val intentOscar = Intent(this, activity)
        if (bundleOscar == null) {
            bundleOscar = Bundle()
        }
        intentOscar.putExtras(bundleOscar)
        resultLauncherOscar.launch(intentOscar)
        overridePendingTransition(R.anim.in_right, R.anim.out_left)
    }

    private var resultLauncherOscar =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                reloadAds()
            }
        }

    fun finishThisActivity() {
        finish()
        overridePendingTransition(R.anim.in_left, R.anim.out_right)
    }

    open fun onBack() {
        setResult(RESULT_OK)
        finishThisActivity()
    }
}