package com.demo.miniflicks.dramaseries.shortstory

import android.content.Intent
import android.util.Log
import com.b9ads.admob.AdsHelper
import com.b9ads.admob.B9AdApplication
import com.b9ads.admob.interstitialAds.InterManager
import com.b9ads.fan.interstitialAds.InterFanManager
import com.b9default.a1_common_utils.data.RemoteConfigManager
import com.b9default.a1_common_utils.model_remote_config.app.RemoteConfigAppAllModel
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenWelcomebackModel
import com.b9default.a8_app_utils.SharePrefUtils
import com.b9default.ui.ads.NativeFullActivity
import com.b9default.ui.no_internet.NoInternetActivity
import com.b9default.ui.splash.SplashActivity
import com.b9default.ui.welcomeback.WelcomeBackActivity
import com.b9utils.util.remoteconfig.RemoteConfigOrigin
import com.demo.miniflicks.dramaseries.shortstory.data.local.repository.SyncRepositoryOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.no_internet.NoInternetActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.util.SharedPreUtilsOscar
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.demo.miniflicks.dramaseries.shortstory.BuildConfig
import com.demo.miniflicks.dramaseries.shortstory.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyApplicationOscar : B9AdApplication() {

    private val tagOscar = "MyApplicationTag"

    // Scope cấp ứng dụng không bị huỷ khi các Activity bị finish
    private val applicationScopeOscar = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAGOscar = "MyApplication"
    }

    override fun onCreate() {
        super.onCreate()

        // Khởi tạo SharedPreferences của app và của thư viện B9
        SharedPreUtilsOscar.init(this)
        SharePrefUtils.init(this)

        // Khởi tạo Firebase
        Firebase.initialize(this)

        // Khởi tạo AdMob SDK
        MobileAds.initialize(this)

        // Khởi tạo Facebook Audience Network
        AudienceNetworkAds.initialize(this)
        AdSettings.setTestMode(BuildConfig.DEBUG)
        AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE)

        // Cấu hình Activity hiển thị Native Full và chữ ký SHA256
        InterFanManager.setNativeFullActivity(NativeFullActivity::class.java.name)
        InterManager.setNativeFullActivity(NativeFullActivity::class.java.name)
        AdsHelper.setOriginalSignature(BuildConfig.KEYSTORE_SHA256)

        // Gọi đồng bộ catalog và episodes ngay từ khi khởi động app
        syncCatalogOnAppStartOscar()
    }

    // Khởi chạy đồng bộ dữ liệu phim ngầm từ API lưu vào Room DB
    private fun syncCatalogOnAppStartOscar() {
        applicationScopeOscar.launch {
            try {
                val codeLangOscar = SystemUtilOscar.getPreLanguage(this@MyApplicationOscar)
                val syncRepositoryOscar = SyncRepositoryOscar(this@MyApplicationOscar)
                val resultOscar = syncRepositoryOscar.syncIfNeededOscar(preferredLangOscar = codeLangOscar)
                if (resultOscar.isSuccess) {
                    Log.d(TAGOscar, "Đồng bộ dữ liệu khi khởi động app thành công")
                } else {
                    val exceptionOscar = resultOscar.exceptionOrNull()
                    val messageOscar = exceptionOscar?.message
                    Log.e(TAGOscar, "Đồng bộ dữ liệu khi khởi động app thất bại: $messageOscar")
                }
            } catch (eOscar: Exception) {
                Log.e(TAGOscar, "Lỗi xảy ra trong quá trình đồng bộ nền", eOscar)
            }
        }
    }

    // Ghi đè phương thức lấy Adjust App Token
    override fun getAppTokenAdjust(): String {
        return getString(R.string.adjust_token)
    }

    // Ghi đè phương thức lấy Adjust Event Token
    override fun getTokenEventAdjust(): String {
        return getString(R.string.adjust_token_event)
    }

    // Ghi đè phương thức lấy Facebook App ID
    override fun getFacebookId(): String {
        return getString(R.string.facebook_app_id)
    }

    // Xử lý hiển thị màn hình WelcomeBack khi quay lại ứng dụng
    override fun showWelcombackScreen() {
        if (currentActivity is SplashActivity) {
            return
        }
        if (currentActivity is WelcomeBackActivity) {
            return
        }
        if (currentActivity is NoInternetActivity) {
            return
        }
        if (currentActivity is NoInternetActivityOscar) {
            return
        }

        val remoteConfigScreenWelcomebackModelOscar = RemoteConfigOrigin.getConfigObject(
            this@MyApplicationOscar,
            RemoteConfigManager.getKeyScreenWelcomeback(),
            RemoteConfigScreenWelcomebackModel::class.java
        )
        if (remoteConfigScreenWelcomebackModelOscar == null) {
            return
        }

        val remoteConfigAppAllModelOscar = RemoteConfigOrigin.getConfigObject(
            this@MyApplicationOscar,
            RemoteConfigManager.getKeyAppConfig(),
            RemoteConfigAppAllModel::class.java
        )
        if (remoteConfigAppAllModelOscar == null) {
            return
        }

        if (remoteConfigAppAllModelOscar.isShowWellcomebackScreen == false) {
            return
        }

        // Kiểm tra danh sách các màn hình không hiển thị WelcomeBack
        val notShowRawOscar = remoteConfigScreenWelcomebackModelOscar.notShowScreen ?: ""
        val notShowScreenListOscar: List<String> = notShowRawOscar.lowercase().split(",")
        var screenNameOscar = currentActivity?.javaClass?.simpleName?.lowercase()
        screenNameOscar = screenNameOscar?.replace("activityoscar", "")
        screenNameOscar = screenNameOscar?.replace("activity", "")

        if (screenNameOscar in notShowScreenListOscar) {
            Log.v(tagOscar, "not show welcome back")
            return
        }

        val intentOscar = Intent(this@MyApplicationOscar, WelcomeBackActivity::class.java)
        intentOscar.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intentOscar)
    }
}