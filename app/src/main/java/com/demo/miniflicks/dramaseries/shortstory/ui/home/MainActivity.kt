package com.demo.miniflicks.dramaseries.shortstory.ui.home
import android.view.View
import com.b9ads.admob.bannerAds.collapsible.BannerGravity
import com.b9default.a1_common_utils.ad.AdCommon
import com.b9default.a1_common_utils.model_remote_config.ads.RemoteConfigAdNativeCollap
import com.b9utils.util.remoteconfig.RemoteConfigOrigin
import com.demo.miniflicks.dramaseries.shortstory.ads.IsNetWork
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.dialog.exit.ExitAppDialogOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.no_internet.NoInternetActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.search.SearchActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.widget.FluidBottomNavViewV2Oscar
import com.demo.miniflicks.dramaseries.shortstory.util.RemoteKey
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityMainBinding


class MainActivity : BaseActivityOscar<ActivityMainBinding>(ActivityMainBinding::inflate) {

    // Biến lưu tham chiếu đến dialog exit app để tránh mở lặp lại
    private var exitAppDialogOscar: ExitAppDialogOscar? = null

    override fun initView() {
                val nativeCollapModel = RemoteConfigOrigin.getConfigObject(
            this,
            RemoteKey.AD_NATIVE_COLLAP,
            RemoteConfigAdNativeCollap::class.java
        )

        AdCommon.loadAndShowNativeCollapse(
            lifecycle,
            RemoteKey.AD_NATIVE_COLLAP,
            binding.frAds,
            nativeCollapModel
        )

        AdCommon.loadAndShowAdBanner(
            lifecycle,
            RemoteKey.AD_NATIVE_COLLAP,
            binding.frBanner,
            BannerGravity.BOTTOM,
            nativeCollapModel?.banner
        )

        // 1. Cài đặt Adapter cho ViewPager2
        val adapterOscar: MainPagerAdapterOscar = MainPagerAdapterOscar(this)
        binding.viewPagerOscar.adapter = adapterOscar
        binding.viewPagerOscar.isUserInputEnabled = false // Khóa vuốt tay để điều hướng chuẩn theo nút bấm

        val bottomNavOscar: FluidBottomNavViewV2Oscar = binding.navBottomOscar

        // 2. Lắng nghe sự kiện click chuyển Tab
        bottomNavOscar.onTabSelectedListener = { position: Int ->
            if (position == 0) {
                // Tab Cinemix (vị trí 0 trên nav) -> Mở PlaylistFragmentOscar (vị trí 1 trong ViewPager)
                binding.viewPagerOscar.setCurrentItem(1, false)
                binding.viewTopOscar.root.visibility = View.GONE
            } else if (position == 1) {
                // Tab Home (vị trí 1 trên nav) -> Mở CinemixFragmentOscar (vị trí 0 trong ViewPager)
                binding.viewPagerOscar.setCurrentItem(0, false)
                binding.viewTopOscar.root.visibility = View.VISIBLE
            } else if (position == 2) {
                // Tab Settings (vị trí 2 trên nav) -> Mở SettingFragmentOscar (vị trí 2 trong ViewPager)
                binding.viewPagerOscar.setCurrentItem(2, false)
                binding.viewTopOscar.root.visibility = View.GONE
            }
        }

        // Mặc định chọn Tab Home (vị trí 1 trên nav - CinemixFragmentOscar) khi mở màn hình
        bottomNavOscar.selectTab(1, animate = false)
        binding.viewTopOscar.root.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Kiểm tra kết nối Internet khi màn hình MainActivity hiển thị
        checkInternetConnectionOscar()
    }

    /**
     * Kiểm tra trạng thái mạng, nếu không có mạng thì mở NoInternetActivityOscar
     */
    private fun checkInternetConnectionOscar() {
        val isConnectedOscar: Boolean = IsNetWork.haveNetworkConnection(this)
        if (!isConnectedOscar) {
            // Chuyển sang màn hình NoInternetActivityOscar
            startNextActivity(NoInternetActivityOscar::class.java, null)
        }
    }

    override fun bindView() {
        binding.viewTopOscar.btnSearchOscar.tap {
            startNextActivity(SearchActivityOscar::class.java, null)
        }
    }

    /**
     * Xử lý khi nhấn nút Back
     */
    override fun onBack() {
        // Kiểm tra vị trí tab hiện tại trên thanh điều hướng Bottom Navigation
        val currentTabOscar: Int = binding.navBottomOscar.selectedIndex
        if (currentTabOscar != 1) {
            // Nếu không ở tab Home (vị trí 1), chuyển về tab Home trước
            binding.navBottomOscar.selectTab(1, animate = true)
            return
        }

        // Kiểm tra nếu dialog exit đang hiển thị thì không mở lại
        val isDialogShowingOscar: Boolean = exitAppDialogOscar?.isShowing == true
        if (isDialogShowingOscar) {
            return
        }

        // Hiển thị dialog xác nhận thoát ứng dụng
        showExitDialogOscar()
    }

    /**
     * Khởi tạo và hiển thị Dialog xác nhận thoát app
     */
    private fun showExitDialogOscar() {
        // Khởi tạo dialog xác nhận thoát
        val dialogOscar: ExitAppDialogOscar = ExitAppDialogOscar(this, true) {
            // Khi người dùng bấm Yes -> đóng toàn bộ activity và thoát app
            finishAffinity()
        }

        // Lắng nghe sự kiện dismiss để giải phóng tham chiếu
        dialogOscar.setOnDismissListener {
            exitAppDialogOscar = null
        }

        exitAppDialogOscar = dialogOscar
        dialogOscar.show()
    }
}