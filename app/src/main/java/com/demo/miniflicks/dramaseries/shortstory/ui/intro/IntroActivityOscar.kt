package com.demo.miniflicks.dramaseries.shortstory.ui.intro
import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tapNoHandle
import com.demo.miniflicks.dramaseries.shortstory.model.IntroModelOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.intro.adapter.IntroAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.permission.PermissionActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityIntroOldBinding
import kotlin.math.abs


class IntroActivityOscar : BaseActivityOscar<ActivityIntroOldBinding>(ActivityIntroOldBinding::inflate) {

    // Danh sách các ImageView hiển thị chấm tròn indicator
    private var dotsOscar: Array<ImageView>? = null
    // Danh sách dữ liệu các trang Intro
    private var listIntroOscar: ArrayList<IntroModelOscar> = ArrayList()

    override fun initView() {
        // Khởi tạo mảng indicator với đầy đủ 4 chấm tròn
        dotsOscar = arrayOf(
            binding.ivCircle01Oscar,
            binding.ivCircle02Oscar,
            binding.ivCircle03Oscar,
            binding.ivCircle04Oscar
        )

        // Thêm dữ liệu 4 trang Intro
        listIntroOscar.add(
            IntroModelOscar(
//                R.string.title_intro_01, R.string.content_intro_01, R.drawable.img_intro_1
            )
        )
        listIntroOscar.add(
            IntroModelOscar(
//                R.string.title_intro_02, R.string.content_intro_02, R.drawable.img_intro_2
            )
        )
        listIntroOscar.add(
            IntroModelOscar(
//                R.string.title_intro_03, R.string.content_intro_03, R.drawable.img_intro_3
            )
        )
        listIntroOscar.add(
            IntroModelOscar(
//                R.string.title_intro_04, R.string.content_intro_04, R.drawable.img_intro_4
            )
        )

        // Gán adapter cho ViewPager2
        val introAdapterOscar = IntroAdapterOscar()
        introAdapterOscar.addListData(listIntroOscar)
        binding.viewPager2Oscar.adapter = introAdapterOscar
    }

    override fun bindView() {
        // Thiết lập cấu hình hiển thị cho ViewPager2
        binding.viewPager2Oscar.clipToPadding = false
        binding.viewPager2Oscar.clipChildren = false
        binding.viewPager2Oscar.offscreenPageLimit = 3
        binding.viewPager2Oscar.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_ALWAYS

        val compositePageTransformerOscar = CompositePageTransformer()
        compositePageTransformerOscar.addTransformer(MarginPageTransformer(100))
        compositePageTransformerOscar.addTransformer { page, position ->
            val rOscar = 1 - abs(position)
            page.scaleY = 0.8f + rOscar * 0.2f
            val absPositionOscar = abs(position)
            page.alpha = 1.0f - (1.0f - 0.3f) * absPositionOscar
        }
        binding.viewPager2Oscar.setPageTransformer(compositePageTransformerOscar)

        // Lắng nghe sự kiện đổi trang trên ViewPager2
        binding.viewPager2Oscar.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Cập nhật trạng thái indicator theo vị trí hiện tại
                changeContentInitOscar(position)

                // Kiểm tra xem có phải trang cuối cùng (trang thứ 4 - index 3) hay không
                val lastIndexOscar = listIntroOscar.size - 1
                if (position == lastIndexOscar) {
                    binding.btnNextOscar.tapNoHandle {
                        startNextActivityOscar()
                    }
                } else {
                    binding.btnNextOscar.tapNoHandle {
                        val nextPositionOscar = binding.viewPager2Oscar.currentItem + 1
                        binding.viewPager2Oscar.currentItem = nextPositionOscar
                    }
                }
            }
        })
    }

    private fun startNextActivityOscar() {
        // Chuyển sang màn hình xin quyền
        startNextActivity(PermissionActivityOscar::class.java, null)
        finish()
    }

    // Cập nhật trạng thái chọn/chưa chọn của các chấm tròn indicator
    private fun changeContentInitOscar(positionOscar: Int) {
        val dotsArrayOscar = dotsOscar
        if (dotsArrayOscar != null) {
            val totalDotsOscar = dotsArrayOscar.size
            for (iOscar in 0 until totalDotsOscar) {
                val dotImageViewOscar = dotsArrayOscar[iOscar]
                if (iOscar == positionOscar) {
                    dotImageViewOscar.setImageResource(R.drawable.ic_intro_s)
                } else {
                    dotImageViewOscar.setImageResource(R.drawable.ic_intro_sn)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        changeContentInitOscar(binding.viewPager2Oscar.currentItem)
    }

    override fun onBack() {
        finishAffinity()
    }
}