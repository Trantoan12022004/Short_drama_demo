package com.demo.miniflicks.dramaseries.shortstory.ui.home.no_internet
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.demo.miniflicks.dramaseries.shortstory.ads.IsNetWork
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityNoInternetOldBinding


class NoInternetActivityOscar : BaseActivityOscar<ActivityNoInternetOldBinding>(
    ActivityNoInternetOldBinding::inflate) {

    override fun bindView() {
        // Bắt sự kiện khi người dùng nhấn nút Thử lại (Try Again)
        binding.btnTryAgainOscar.tap {
            handleTryAgainOscar()
        }
    }

    override fun onResume() {
        super.onResume()
        // Kiểm tra kết nối mạng khi người dùng quay lại từ màn hình cài đặt
        val hasInternetOscar = IsNetWork.haveNetworkConnection(this)
        if (hasInternetOscar) {
            // Đã có internet -> Đóng màn hình này để quay về MainActivity
            finishThisActivity()
        }
    }

    /**
     * Xử lý sự kiện khi ấn Try Again
     */
    private fun handleTryAgainOscar() {
        // Kiểm tra trạng thái mạng hiện tại
        val hasInternetOscar = IsNetWork.haveNetworkConnection(this)
        if (hasInternetOscar) {
            // Nếu đã có mạng thì quay về màn hình trước (MainActivity)
            finishThisActivity()
        } else {
            // Nếu chưa có mạng thì mở cài đặt mạng để người dùng bật
            openNetworkSettingsOscar()
        }
    }

    /**
     * Mở màn hình cài đặt mạng hệ thống (Wi-Fi / Dữ liệu di động)
     */
    private fun openNetworkSettingsOscar() {
        val networkIntentOscar: Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ mở Internet Connectivity Panel tiện lợi
            networkIntentOscar = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        } else {
            // Android cũ hơn mở cài đặt Wi-Fi
            networkIntentOscar = Intent(Settings.ACTION_WIFI_SETTINGS)
        }

        try {
            startActivity(networkIntentOscar)
        } catch (exceptionOscar: Exception) {
            // Dự phòng mở cài đặt kết nối không dây chung nếu thiết bị không hỗ trợ intent trên
            val fallbackIntentOscar = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            try {
                startActivity(fallbackIntentOscar)
            } catch (eOscar: Exception) {
                // Xử lý khi không thể mở cài đặt
            }
        }
    }

    override fun onBack() {
        // Thoát ứng dụng nếu người dùng bấm back khi không có kết nối internet
        finishAffinity()
    }
}