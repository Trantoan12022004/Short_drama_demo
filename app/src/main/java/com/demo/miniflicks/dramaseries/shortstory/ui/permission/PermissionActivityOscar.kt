package com.demo.miniflicks.dramaseries.shortstory.ui.permission
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.CompoundButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.b9default.a1_common_utils.ad.AdCommon
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.ui.age.AgeConfirmActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.MainActivity
import com.demo.miniflicks.dramaseries.shortstory.util.GlobalData
import com.demo.miniflicks.dramaseries.shortstory.util.PermissionManager
import com.demo.miniflicks.dramaseries.shortstory.util.RemoteKey
import com.demo.miniflicks.dramaseries.shortstory.util.SharedPreUtilsOscar
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityPermissionBinding


class PermissionActivityOscar :
    BaseActivityOscar<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {

    companion object {
        private const val MAX_PERMISSION_REQUEST_CLICKSOscar = 2
    }

    private val sharedPreUtilsOscar = SharedPreUtilsOscar.getInstance()

    private var notificationClickCountOscar = 0
    private var memoryClickCountOscar = 0
    private var writeClickCountOscar = 0

    // Cờ đánh dấu đang cập nhật trạng thái Switch từ code để tránh kích hoạt listener lặp vô tận
    private var isUpdatingSwitchStateOscar = false

    private val requestPermissionLauncherOscar =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            updateSwitchStatesOscar()
        }

    override fun initView() {
        // Lấy số lần đã bấm yêu cầu quyền từ SharedPreferences
        notificationClickCountOscar = sharedPreUtilsOscar.getNotificationPermissionClickCount(this)
        memoryClickCountOscar = sharedPreUtilsOscar.getMemoryPermissionClickCount(this)
        // Cập nhật trạng thái hiển thị của các switch
        updateSwitchStatesOscar()
        AdCommon.loadAndShowAdNativeAdvance(
            lifecycle, RemoteKey.AD_NATIVE_PERMISSION, binding.frAds
        )
    }



    override fun bindView() {
        // Xử lý sự kiện khi bấm nút Tiếp tục
        binding.tvContinueOscar.tap {
            goToMainOscar()
        }

        // Lắng nghe sự kiện click và vuốt switch thông báo
        bindPermissionRowOscar(
            switchOscar = binding.swNotificationOscar,
            rowLayoutOscar = binding.llNotificationOscar,
            checkGrantedOscar = {
                val isGrantedOscar = PermissionManager.checkNotificationPermission(this)
                isGrantedOscar
            },
            onRequestOscar = {
                handleNotificationPermissionOscar()
            }
        )

        // Lắng nghe sự kiện click và vuốt switch bộ nhớ
        bindPermissionRowOscar(
            switchOscar = binding.swMemoryOscar,
            rowLayoutOscar = binding.llMemoryOscar,
            checkGrantedOscar = {
                val isGrantedOscar = PermissionManager.checkReadPermission(this)
                isGrantedOscar
            },
            onRequestOscar = {
                handleMemoryPermissionOscar()
            }
        )

        // Lắng nghe sự kiện click và vuốt switch ghi cài đặt
        bindPermissionRowOscar(
            switchOscar = binding.swWriteOscar,
            rowLayoutOscar = binding.llWriteOscar,
            checkGrantedOscar = {
                val isGrantedOscar = PermissionManager.checkWritePermission(this)
                isGrantedOscar
            },
            onRequestOscar = {
                handleWritePermissionOscar()
            }
        )
    }

    private fun bindPermissionRowOscar(
        switchOscar: CompoundButton,
        rowLayoutOscar: View,
        checkGrantedOscar: () -> Boolean,
        onRequestOscar: () -> Unit
    ) {
        // Lắng nghe sự kiện thay đổi trạng thái (bắt được cả khi click và khi vuốt/gạt switch)
        switchOscar.setOnCheckedChangeListener { buttonViewOscar, _ ->
            // Nếu đang cập nhật từ code thì bỏ qua không xử lý
            if (isUpdatingSwitchStateOscar) {
                return@setOnCheckedChangeListener
            }

            val isGrantedOscar = checkGrantedOscar()
            if (!isGrantedOscar) {
                // Nếu chưa được cấp quyền thì giữ switch ở trạng thái tắt cho đến khi cấp quyền thành công
                isUpdatingSwitchStateOscar = true
                buttonViewOscar.isChecked = false
                isUpdatingSwitchStateOscar = false

                // Gọi hàm yêu cầu quyền
                onRequestOscar()
            } else {
                // Nếu đã được cấp quyền thì giữ switch luôn bật
                isUpdatingSwitchStateOscar = true
                buttonViewOscar.isChecked = true
                isUpdatingSwitchStateOscar = false
            }
        }

        // Xử lý khi người dùng bấm vào toàn bộ dòng item
        rowLayoutOscar.setOnClickListener {
            val isGrantedOscar = checkGrantedOscar()
            if (!isGrantedOscar) {
                onRequestOscar()
            }
        }
    }

    private fun getReadPermissionOscar(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_AUDIO
        } else {
            return Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private fun handleNotificationPermissionOscar() {
        val isGrantedOscar = PermissionManager.checkNotificationPermission(this)
        if (!isGrantedOscar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (notificationClickCountOscar < MAX_PERMISSION_REQUEST_CLICKSOscar) {
                    notificationClickCountOscar++
                    sharedPreUtilsOscar.setNotificationPermissionClickCount(this, notificationClickCountOscar)
                    requestPermissionLauncherOscar.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    openNotificationSettingsOscar()
                }
            } else {
                openNotificationSettingsOscar()
            }
        }
    }

    private fun handleMemoryPermissionOscar() {
        val isGrantedOscar = PermissionManager.checkReadPermission(this)
        if (!isGrantedOscar) {
            if (memoryClickCountOscar < MAX_PERMISSION_REQUEST_CLICKSOscar) {
                memoryClickCountOscar++
                sharedPreUtilsOscar.setMemoryPermissionClickCount(this, memoryClickCountOscar)
                val permissionOscar = getReadPermissionOscar()
                requestPermissionLauncherOscar.launch(permissionOscar)
            } else {
                openAppSettingsOscar()
            }
        }
    }

    private fun handleWritePermissionOscar() {
        val isGrantedOscar = PermissionManager.checkWritePermission(this)
        if (!isGrantedOscar) {
//            RingtoneHelper.openWriteSettings(this)
        }
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật lại trạng thái switch khi quay lại màn hình
        updateSwitchStatesOscar()
    }

    private fun updateSwitchStatesOscar() {
        isUpdatingSwitchStateOscar = true

        val isNotificationGrantedOscar = PermissionManager.checkNotificationPermission(this)
        updateSwitchStateOscar(
            switchOscar = binding.swNotificationOscar,
            isGrantedOscar = isNotificationGrantedOscar
        )

        val isMemoryGrantedOscar = PermissionManager.checkReadPermission(this)
        updateSwitchStateOscar(
            switchOscar = binding.swMemoryOscar,
            isGrantedOscar = isMemoryGrantedOscar
        )

        val isWriteGrantedOscar = PermissionManager.checkWritePermission(this)
        updateSwitchStateOscar(
            switchOscar = binding.swWriteOscar,
            isGrantedOscar = isWriteGrantedOscar
        )

        isUpdatingSwitchStateOscar = false
    }

    private fun updateSwitchStateOscar(switchOscar: CompoundButton, isGrantedOscar: Boolean) {
        switchOscar.isChecked = isGrantedOscar
        if (isGrantedOscar) {
            switchOscar.isEnabled = false
        } else {
            switchOscar.isEnabled = true
        }
    }

    private fun openNotificationSettingsOscar() {
        val intentOscar = Intent()
        intentOscar.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intentOscar.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intentOscar)
    }

    private fun openAppSettingsOscar() {
        val intentOscar = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uriOscar = Uri.fromParts("package", packageName, null)
        intentOscar.data = uriOscar
        startActivity(intentOscar)
    }

    @OptIn(UnstableApi::class)
    private fun goToMainOscar() {
        // Kiểm tra trạng thái xác nhận độ tuổi
        val isAgeConfirmedOscar = sharedPreUtilsOscar.getBoolean(GlobalData.IS_AGE_CONFIRMOscar, false)
        if (isAgeConfirmedOscar) {
            // Đã xác nhận -> Đi thẳng vào MainActivity
            startNextActivity(MainActivity::class.java, null)
        } else {
            // Chưa xác nhận -> Mở màn hình AgeConfirmActivityOscar
            startNextActivity(AgeConfirmActivityOscar::class.java, null)
        }
        finishAffinity()
    }

    override fun onBack() {
        finishAffinity()
    }
}