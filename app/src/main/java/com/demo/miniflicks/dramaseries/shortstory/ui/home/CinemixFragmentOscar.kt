package com.demo.miniflicks.dramaseries.shortstory.ui.home
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.demo.miniflicks.dramaseries.shortstory.base.BaseFragmentOscar
import com.demo.miniflicks.dramaseries.shortstory.base.gone
import com.demo.miniflicks.dramaseries.shortstory.base.loadImageWithProgress
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.model.MovieCardOscar
import com.demo.miniflicks.dramaseries.shortstory.model.TagModelOscar
import com.demo.miniflicks.dramaseries.shortstory.model.resolveName
import com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter.BannerAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter.NewCardAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter.SpotlightCardAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter.TagAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter.TrendingCardAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.movie_tag.MovieTagActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.movie_tag.NewReleaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.movie_tag.SpotlightActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.movie_tag.TrendingActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.play_video.PlayVideoActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.FragmentCinemixBinding
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.jvm.java
import kotlin.math.abs


class CinemixFragmentOscar : BaseFragmentOscar<FragmentCinemixBinding>() {

    // Delay duration between automatic slides in milliseconds
    private val autoScrollDelayMsOscar: Long = 8000L

    // Adapter for banner ViewPager2
    private lateinit var bannerAdapterOscar: BannerAdapterOscar
    private val viewModelOscar: MainViewModelOscar by activityViewModels()
    private val tagAdapterOscar = TagAdapterOscar()
    private val trendingAdapterOscar = TrendingCardAdapterOscar()
    private val releaseAdapterOscar = NewCardAdapterOscar()
    private val topRateAdapterOscar = SpotlightCardAdapterOscar()

    // Handler to schedule auto-scroll tasks
    private val autoScrollHandlerOscar: Handler = Handler(Looper.getMainLooper())

    // Flag indicating whether auto-scroll is actively running
    private var isAutoScrollRunningOscar: Boolean = false

    // Flag indicating whether banner ViewPager position is initialized
    private var isBannerPositionInitializedOscar: Boolean = false

    // Runnable task executed on each auto-scroll tick
    private val autoScrollRunnableOscar: Runnable = object : Runnable {
        override fun run() {
            // Check if fragment binding is still active
            val currentItemOscar = binding.vpBannerOscar.currentItem
            val nextItemOscar = currentItemOscar + 1
            binding.vpBannerOscar.setCurrentItem(nextItemOscar, true)

            // Schedule the next auto scroll tick
            autoScrollHandlerOscar.postDelayed(this, autoScrollDelayMsOscar)
        }
    }

    // Danh sách phim banner hiện tại
    private var currentBannerMoviesOscar: List<MovieCardOscar> = emptyList()

    // Callback lắng nghe lướt banner để cập nhật thông tin phim ở giữa
    private val pageChangeCallbackOscar = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            // Cập nhật tên phim và sự kiện nút Watch Now cho phim ở giữa
            updateBannerCenterInfoOscar(position)
        }
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                stopAutoScrollOscar()
            } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                startAutoScrollOscar()
            }
        }
    }

//     Cập nhật tên phim và sự kiện click Watch Now cho phim đang ở tâm banner

    private fun updateBannerCenterInfoOscar(positionOscar: Int) {
        val listSizeOscar: Int = currentBannerMoviesOscar.size
        if (listSizeOscar <= 0) {
            return
        }
        val actualIndexOscar: Int = positionOscar % listSizeOscar
        val currentMovieOscar: MovieCardOscar = currentBannerMoviesOscar[actualIndexOscar]
        // Cập nhật tên phim
        binding.tvBannerMovieTitleOscar.text = currentMovieOscar.title
        // Gắn sự kiện phát phim khi click vào nút Watch Now bên dưới
        binding.btnBannerWatchNowOscar.tap {
            val bundleOscar = Bundle().apply {
                putLong("EXTRA_MOVIE_ID", currentMovieOscar.id)
                putString("EXTRA_MOVIE_TITLE", currentMovieOscar.title)
                putString("EXTRA_MOVIE_POSTER", currentMovieOscar.posterUrl)
            }
            startNextActivity(PlayVideoActivityOscar::class.java, bundleOscar)
        }
        // Cập nhật trạng thái hiển thị của layout_chip (chỉ số banner hiện tại)
        updateBannerChipIndicatorOscar(actualIndexOscar)
    }

    // Cập nhật icon cho 5 chip chỉ báo banner
    private fun updateBannerChipIndicatorOscar(selectedIndexOscar: Int) {
        val chipsOscar = listOf(
            binding.chip1Oscar,
            binding.chip2Oscar,
            binding.chip3Oscar,
            binding.chip4Oscar,
            binding.chip5Oscar
        )
        for (indexOscar in chipsOscar.indices) {
            val chipOscar = chipsOscar[indexOscar]
            if (indexOscar == selectedIndexOscar) {
                chipOscar.setImageResource(R.drawable.chip_s)
            } else {
                chipOscar.setImageResource(R.drawable.chip_sn)
            }
        }
    }

    // Gắn sự kiện click cho từng chip để cuộn mượt mà đến banner tương ứng
    private fun setupChipClickListenersOscar() {
        val chipsOscar = listOf(
            binding.chip1Oscar,
            binding.chip2Oscar,
            binding.chip3Oscar,
            binding.chip4Oscar,
            binding.chip5Oscar
        )
        for (targetIndexOscar in chipsOscar.indices) {
            chipsOscar[targetIndexOscar].tap {
                val listSizeOscar: Int = currentBannerMoviesOscar.size
                if (listSizeOscar > 0) {
                    val currentPosOscar: Int = binding.vpBannerOscar.currentItem
                    val currentActualOscar: Int = currentPosOscar % listSizeOscar
                    val diffOscar: Int = targetIndexOscar - currentActualOscar
                    val targetPosOscar: Int = currentPosOscar + diffOscar
                    binding.vpBannerOscar.setCurrentItem(targetPosOscar, true)
                }
            }
        }
    }
    private var tagListOscar: List<TagModelOscar> = emptyList()
    override fun setBinding(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        saveInstanceState: Bundle?
    ): FragmentCinemixBinding {
        val bindingInflatedOscar = FragmentCinemixBinding.inflate(inflater!!, container, false)
        return bindingInflatedOscar
    }
    override fun getData() {
        tagListOscar = createTagListOscar()
    }

    override fun initView() {
        binding.sectionVibesOscar.btnSeeAllOscar.gone()
        binding.sectionTrendingOscar.titleSectionOscar.text = getString(R.string.trending_now)
        binding.sectionReleaseOscar.titleSectionOscar.text = getString(R.string.new_release)
        binding.spotlightOscar.titleSectionOscar.text = getString(R.string.spotlight)

        // Initialize banner adapter
        setupBannerAdapterOscar()

        // Setup ViewPager2 carousel visual effects and transformers
        setupBannerViewPagerOscar()

        // Setup sự kiện click cho các chip chỉ báo banner
        setupChipClickListenersOscar()

        // Set ngôn ngữ hiện tại cho tagAdapter trước khi setup
        tagAdapterOscar.lang = SystemUtilOscar.getPreLanguage(requireContext())
        setupTagListAdapterOscar()
        setupTrendingAdapterOscar()
        setupReleaseAdapterOscar()
        setupSpotlightAdapterOscar()
    }

    override fun bindView() {
        // Quan sát dữ liệu phim từ ViewModel
        observeMovieOscar()

        // Xử lý click nút See All cho mục Trending Now
        binding.sectionTrendingOscar.btnSeeAllOscar.tap {
            val bundleOscar = Bundle()
            val categoryNameOscar = getString(R.string.trending_now)
            bundleOscar.putString("EXTRA_CATEGORY_NAME", categoryNameOscar)
            startNextActivity(TrendingActivityOscar::class.java, bundleOscar)
        }

        // Xử lý click nút See All cho mục New Release
        binding.sectionReleaseOscar.btnSeeAllOscar.tap {
            val bundleOscar = Bundle()
            val categoryNameOscar = getString(R.string.new_release)
            bundleOscar.putString("EXTRA_CATEGORY_NAME", categoryNameOscar)
            startNextActivity(NewReleaseActivityOscar::class.java, bundleOscar)
        }

        // Xử lý click nút See All cho mục Spotlight
        binding.spotlightOscar.btnSeeAllOscar.tap {
            val bundleOscar = Bundle()
            val categoryNameOscar = getString(R.string.spotlight)
            bundleOscar.putString("EXTRA_CATEGORY_NAME", categoryNameOscar)
            startNextActivity(SpotlightActivityOscar::class.java, bundleOscar)
        }
    }

    private fun observeMovieOscar(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModelOscar.bannerMovies.collect { movieCardsOscar ->
                        if (movieCardsOscar.isNotEmpty()) {
                            // Chỉ lấy 5 phim đầu tiên cho banner
                            val bannerMoviesOscar: List<MovieCardOscar> = movieCardsOscar.take(5)
                            currentBannerMoviesOscar = bannerMoviesOscar
                            bannerAdapterOscar.addListData(bannerMoviesOscar.toMutableList())
                            trendingAdapterOscar.addListData(movieCardsOscar.toMutableList())

                            // Chỉ căn giữa vị trí ở lần khởi tạo đầu tiên, không reset lại khi DB update
                            if (!isBannerPositionInitializedOscar) {
                                val listSizeOscar = bannerMoviesOscar.size
                                val halfMaxOscar = Int.MAX_VALUE / 2
                                val remainderOscar = halfMaxOscar % listSizeOscar
                                val startPositionOscar = halfMaxOscar - remainderOscar
                                binding.vpBannerOscar.setCurrentItem(startPositionOscar, false)
                                isBannerPositionInitializedOscar = true

                                // Cập nhật tên phim và nút Watch Now cho phim ở tâm
                                updateBannerCenterInfoOscar(startPositionOscar)
                            }

                            // Khôi phục hiệu ứng 3D CoverFlow cho các banner đang hiển thị
                            binding.vpBannerOscar.post {
                                binding.vpBannerOscar.requestTransform()
                            }
                        }
                    }
                }

                launch {
                    viewModelOscar.allMovies.collect { movieCardsOscar ->
                        if (movieCardsOscar.isNotEmpty()) {
                            releaseAdapterOscar.addListData(movieCardsOscar.toMutableList())
                        }
                    }
                }

                launch {
                    viewModelOscar.topRateMovies.collect { movieCardsOscar ->
                        if (movieCardsOscar.isNotEmpty()) {
                            topRateAdapterOscar.addListData(movieCardsOscar.toMutableList())
                        }
                    }
                }

                // Lắng nghe dữ liệu phim Movie Best (Editor's Choice)
                launch {
                    viewModelOscar.bestMovie.collect { movieOscar ->
                        if (movieOscar != null) {
                            bindMovieBestOscar(movieOscar)
                        }
                    }
                }

                launch {
                    viewModelOscar.allCategories.collect { categoriesOscar ->
                            tagAdapterOscar.addListData(categoriesOscar.toMutableList())
                        }
                    }

                // Collect trạng thái loading
                launch {
                    viewModelOscar.isLoading.collect { isLoadingOscar ->
                        // Kiểm tra xem banner đã có dữ liệu hiển thị chưa
                        val hasBannerDataOscar = bannerAdapterOscar.getActualListSize() > 0
                        if (hasBannerDataOscar) {
                            // Nếu đã có dữ liệu thì không bao giờ hiển thị lại skeleton
                            bannerAdapterOscar.showSkeleton(false)
                        } else {
                            // Chỉ hiển thị skeleton khi banner thực sự chưa có dữ liệu nào
                            bannerAdapterOscar.showSkeleton(isLoadingOscar)
                        }
                    }
                }
                // Collect lỗi
                launch {
                    viewModelOscar.errorMessage.collect { messageOscar ->
                        if (messageOscar != null) {
                            Toast.makeText(requireContext(), messageOscar, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    }

    private fun setupTagListAdapterOscar(){
        binding.rvTagsOscar.adapter = tagAdapterOscar
        tagAdapterOscar.onClick = { categoryOscar ->
            val bundleOscar = Bundle().apply {
                putLong("EXTRA_CATEGORY_ID", categoryOscar.id)
                // Resolve tên thể loại đúng ngôn ngữ khi truyền sang MovieTagActivityOscar
                val langOscar = SystemUtilOscar.getPreLanguage(requireContext())
                putString("EXTRA_CATEGORY_NAME", categoryOscar.resolveName(langOscar))
            }
            startNextActivity(MovieTagActivityOscar::class.java, bundleOscar)
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupTrendingAdapterOscar(){
        binding.rvTrendingsOscar.adapter = trendingAdapterOscar
        trendingAdapterOscar.onClick = { movieOscar ->
            val bundleOscar = Bundle().apply {
                putLong("EXTRA_MOVIE_ID", movieOscar.id)
                putString("EXTRA_MOVIE_TITLE", movieOscar.title)
                putString("EXTRA_MOVIE_POSTER", movieOscar.posterUrl)
            }
            startNextActivity(PlayVideoActivityOscar::class.java, bundleOscar)
        }
    }

    private fun setupReleaseAdapterOscar(){
        binding.rvReleaseOscar.adapter = releaseAdapterOscar
        releaseAdapterOscar.onClick = { movieOscar ->
            val bundleOscar = Bundle().apply {
                putLong("EXTRA_MOVIE_ID", movieOscar.id)
                putString("EXTRA_MOVIE_TITLE", movieOscar.title)
                putString("EXTRA_MOVIE_POSTER", movieOscar.posterUrl)
            }
            startNextActivity(PlayVideoActivityOscar::class.java, bundleOscar)
        }
    }

    private fun setupSpotlightAdapterOscar(){
        binding.rvSpotlightOscar.adapter = topRateAdapterOscar
        topRateAdapterOscar.onClick = { movieOscar ->
            val bundleOscar = Bundle().apply {
                putLong("EXTRA_MOVIE_ID", movieOscar.id)
                putString("EXTRA_MOVIE_TITLE", movieOscar.title)
                putString("EXTRA_MOVIE_POSTER", movieOscar.posterUrl)
            }
            startNextActivity(PlayVideoActivityOscar::class.java, bundleOscar)
        }
    }

    private fun createTagListOscar() : List<TagModelOscar> {
        val tagListOscar = listOf(
            TagModelOscar(1, "#Marriage"),
            TagModelOscar(2, "#Forbidden Love"),
            TagModelOscar(3, "#Celebrity"),
            TagModelOscar(4, "#Destiny"),
            TagModelOscar(5, "#Betrayal"),
            TagModelOscar(6, "#Workplace Romance"),
            TagModelOscar(7, "#Revenge"),
            TagModelOscar(8, "#Fantasy"),
            TagModelOscar(9, "#Sweet Love"),
            TagModelOscar(10, "#Action")
        )
        return tagListOscar

    }


    private fun setupBannerAdapterOscar() {
        bannerAdapterOscar = BannerAdapterOscar()

        // Handle item click
        bannerAdapterOscar.onItemClick = { movieOscar ->
            val bundleOscar = Bundle().apply {
                putLong("EXTRA_MOVIE_ID", movieOscar.id)
                putString("EXTRA_MOVIE_TITLE", movieOscar.title)
                putString("EXTRA_MOVIE_POSTER", movieOscar.posterUrl)
            }
            startNextActivity(PlayVideoActivityOscar::class.java, bundleOscar)
        }

        // Handle Watch Now button click
        bannerAdapterOscar.onWatchNowClick = { movieOscar ->
            val bundleOscar = Bundle().apply {
                putLong("EXTRA_MOVIE_ID", movieOscar.id)
                putString("EXTRA_MOVIE_TITLE", movieOscar.title)
                putString("EXTRA_MOVIE_POSTER", movieOscar.posterUrl)
            }
            startNextActivity(PlayVideoActivityOscar::class.java, bundleOscar)
        }

        // Xử lý Favorite button click trên Banner → toggle yêu thích cho phim, tự động
        // đồng bộ trạng thái yêu thích vào tập 1 của phim đó trong Room (xem MainViewModelOscar.toggleFavoriteMovie)
        bannerAdapterOscar.onFavoriteClick = { movieOscar ->
            viewModelOscar.toggleFavoriteMovie(movieOscar)
        }

        binding.vpBannerOscar.adapter = bannerAdapterOscar
    }

    private fun setupBannerViewPagerOscar() {
        val viewPagerOscar = binding.vpBannerOscar
        viewPagerOscar.offscreenPageLimit = 3
        viewPagerOscar.clipToPadding = false
        viewPagerOscar.clipChildren = false

        // 1. Lấy thông số màn hình để tính toán kích thước động theo tỉ lệ
        val displayMetricsOscar = resources.displayMetrics
        val screenWidthOscar: Int = displayMetricsOscar.widthPixels
        val densityOscar: Float = displayMetricsOscar.density

        // 2. Banner chính chiếm đúng 60% chiều rộng màn hình
        val mainCardWidthRatioOscar: Float = 0.60f
        val mainCardWidthOscar: Int = (screenWidthOscar * mainCardWidthRatioOscar).toInt()

        // 3. Tỉ lệ khung hình poster 2:3 (Chiều cao = 1.5 * Chiều rộng)
        val aspectRatioOscar: Float = 1.5f
        val bannerHeightOscar: Int = (mainCardWidthOscar * aspectRatioOscar).toInt()

        // 4. Khoảng đệm 2 bên để banner chính căn giữa màn hình (mỗi bên 20% màn hình)
        val sidePaddingPxOscar: Int = (screenWidthOscar - mainCardWidthOscar) / 2
        val childViewOscar: View? = viewPagerOscar.getChildAt(0)
        if (childViewOscar is RecyclerView) {
            childViewOscar.setPadding(sidePaddingPxOscar, 0, sidePaddingPxOscar, 0)
            childViewOscar.clipToPadding = false
            childViewOscar.clipChildren = false
            childViewOscar.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        // 5. Cập nhật chiều cao ViewPager2 theo đúng tỉ lệ 2:3
        val layoutParamsOscar = viewPagerOscar.layoutParams
        layoutParamsOscar.height = bannerHeightOscar
        viewPagerOscar.layoutParams = layoutParamsOscar

        // 6. Khoảng cách mong muốn giữa các banner: 12dp
        val desiredGapPxOscar: Float = 12f * densityOscar

        // 7. Tỉ lệ thu nhỏ của banner phụ: 200dp / 250dp = 0.80 (80%)
        val minScaleOscar: Float = 0.80f
        val scaleRangeOscar: Float = 1.0f - minScaleOscar // 0.20f

        // 8. Tiêu cự 3D chuẩn không méo hình
        val cameraDistanceValOscar: Float = densityOscar * 8000f
        val maxRotationAngleOscar: Float = 20f

        val compositeTransformerOscar = CompositePageTransformer()
        val coverFlowTransformerOscar = ViewPager2.PageTransformer { pageOscar: View, positionOscar: Float ->
            val absPositionOscar: Float = abs(positionOscar)
            var clampedPositionOscar: Float = absPositionOscar
            if (clampedPositionOscar > 1.0f) {
                clampedPositionOscar = 1.0f
            } else if (clampedPositionOscar < 0.0f) {
                clampedPositionOscar = 0.0f
            }

            // Gán cameraDistance
            pageOscar.cameraDistance = cameraDistanceValOscar

            // Thu nhỏ banner phụ về đúng tỉ lệ 80% (scale = 0.80)
            val scaleFactorOscar: Float = minScaleOscar + (1.0f - clampedPositionOscar) * scaleRangeOscar
            pageOscar.scaleX = scaleFactorOscar
            pageOscar.scaleY = scaleFactorOscar

            // Phân tầng Z: Card giữa luôn nổi trên các card bên cạnh
            pageOscar.translationZ = (1.0f - clampedPositionOscar) * 10f

            // Góc xoay 3D quanh trục Y nghiêng vào tâm
            pageOscar.pivotX = pageOscar.width * 0.5f
            pageOscar.pivotY = pageOscar.height * 0.5f
            pageOscar.rotationY = -positionOscar * maxRotationAngleOscar

            // Bù trừ khoảng cách để giữa các banner luôn cách nhau đúng 12dp
            val pageWidthOscar: Float = pageOscar.width.toFloat()
            val shrinkMarginOscar: Float = (pageWidthOscar * (1.0f - scaleFactorOscar)) / 2f
            val translationOffsetOscar: Float = shrinkMarginOscar - desiredGapPxOscar
            pageOscar.translationX = -positionOscar * translationOffsetOscar

            // Lớp phủ mờ đen: 0.0 khi ở tâm, tăng dần lên 0.50 khi ở 2 bên
            val dimOverlayOscar: View? = pageOscar.findViewById(R.id.viewDimOverlayOscar)
            if (dimOverlayOscar != null) {
                dimOverlayOscar.alpha = clampedPositionOscar * 0.50f
            }
        }

        compositeTransformerOscar.addTransformer(coverFlowTransformerOscar)
        viewPagerOscar.setPageTransformer(compositeTransformerOscar)
        viewPagerOscar.registerOnPageChangeCallback(pageChangeCallbackOscar)
    }



    private fun startAutoScrollOscar() {
        // Only start if not already running and adapter has items
        if (!isAutoScrollRunningOscar && bannerAdapterOscar.getActualListSize() > 1) {
            isAutoScrollRunningOscar = true
            autoScrollHandlerOscar.removeCallbacks(autoScrollRunnableOscar)
            autoScrollHandlerOscar.postDelayed(autoScrollRunnableOscar, autoScrollDelayMsOscar)
        }
    }

    private fun stopAutoScrollOscar() {
        if (isAutoScrollRunningOscar) {
            isAutoScrollRunningOscar = false
            autoScrollHandlerOscar.removeCallbacks(autoScrollRunnableOscar)
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume auto-scroll when fragment becomes visible
        startAutoScrollOscar()

        // Khôi phục lại hiệu ứng 3D, scale và khoảng cách cho ViewPager2 ngay khi quay lại màn hình
        binding.vpBannerOscar.post {
            binding.vpBannerOscar.requestTransform()
        }
    }

    override fun onPause() {
        super.onPause()
        // Pause auto-scroll when fragment is hidden/paused to prevent battery drain
        stopAutoScrollOscar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Reset cờ vị trí banner khi view bị hủy để lần sau view được tạo lại sẽ căn giữa đúng
        isBannerPositionInitializedOscar = false
        // Clean up callbacks and listeners to prevent memory leaks
        stopAutoScrollOscar()
        binding.vpBannerOscar.unregisterOnPageChangeCallback(pageChangeCallbackOscar)
    }

    /**
     * Hiển thị dữ liệu phim thật lên giao diện Movie Best (Editor's Choice)
     */
    private fun bindMovieBestOscar(movieOscar: MovieCardOscar) {
        // 1. Cập nhật tiêu đề phim thật
        binding.movieBestOscar.tvMovieTitleOscar.text = movieOscar.title

        // 2. Tải poster phim thật kèm hiệu ứng loading từ base extension loadImageWithProgress
        binding.movieBestOscar.ivCardMovieOscar.loadImageWithProgress(
            url = movieOscar.posterUrl,
            progressBar = binding.movieBestOscar.pbLoadingOscar,
            errorResId = R.drawable.card_demo
        )

        // 3. Bắt sự kiện click mở màn hình PlayVideoActivityOscar với dữ liệu phim thật
        binding.movieBestOscar.root.tap {
            val bundleOscar: Bundle = Bundle()
            bundleOscar.putLong("EXTRA_MOVIE_ID", movieOscar.id)
            bundleOscar.putString("EXTRA_MOVIE_TITLE", movieOscar.title)
            bundleOscar.putString("EXTRA_MOVIE_POSTER", movieOscar.posterUrl)
            startNextActivity(PlayVideoActivityOscar::class.java, bundleOscar)
        }
    }
}