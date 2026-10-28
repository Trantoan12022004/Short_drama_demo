package com.demo.miniflicks.dramaseries.shortstory.ui.play_video
import android.net.ConnectivityManager
import android.net.Network
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.miniflicks.dramaseries.shortstory.ads.IsNetWork
import com.demo.miniflicks.dramaseries.shortstory.api.FilmDetailResponseOscar
import com.demo.miniflicks.dramaseries.shortstory.api.SegmentResponseOscar
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.gone
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.base.visible
import com.demo.miniflicks.dramaseries.shortstory.dialog.SelectLanguageOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.no_internet.NoInternetActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.play_video.adapter.EpisodeFeedAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.play_video.adapter.EpisodeNumberAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.play_video.model.PlayVideoMapper
import com.demo.miniflicks.dramaseries.shortstory.ui.play_video.player.PlayerCacheProvider
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityPlayVideoBinding
import kotlinx.coroutines.launch
import java.util.Locale


private const val TAG_PLAY_VIDEOOscar = "DEBUG_PLAYER"
private const val TAG_SUBTITLEOscar = "DEBUG_SUBTITLE"
@OptIn(UnstableApi::class)
class PlayVideoActivityOscar : BaseActivityOscar<ActivityPlayVideoBinding>(ActivityPlayVideoBinding::inflate) {

    private val viewModelOscar: PlayVideoViewModelOscar by viewModels()

    private var movieIdOscar: Long = -1L
    private var initialEpisodeIdOscar: Long = -1L

    private var moviePosterOscar: String = ""
    private var movieTitleOscar: String = ""
    // Callback theo dõi trạng thái kết nối mạng theo thời gian thực
    private var networkCallbackOscar: ConnectivityManager.NetworkCallback? = null


    // Quản lý hiển thị icon Play/Pause ở giữa màn hình
    private val playPauseIconHandlerOscar = Handler(Looper.getMainLooper())
    private val hidePlayPauseRunnableOscar = Runnable {
        hideCenterPlayControlsOscar()
    }

    // Quản lý tự động ẩn glassToolbar và bottomOverlay sau 7s khi phát video
    private val overlayHideHandlerOscar = Handler(Looper.getMainLooper())
    private val hideOverlayRunnableOscar = Runnable {
        hideControlsOverlayOscar()
    }

    // Pool 3 ExoPlayer dùng chung cho feed video
    private lateinit var playerPoolOscar: List<ExoPlayer>
    private lateinit var feedAdapterOscar: EpisodeFeedAdapterOscar
    private val episodeNumberAdapterOscar = EpisodeNumberAdapterOscar()

    private var currentSnapPositionOscar: Int = 0

    // Cờ đánh dấu đã load chi tiết phim lần đầu tiên hay chưa
    private var isFirstDetailLoadOscar: Boolean = true

    // Trạng thái phát đang chọn, được áp lại cho player mới mỗi khi đổi tập
    private var subtitleEnabledOscar: Boolean = false
    private var currentSpeedOscar: Float = 1.0f
    private var currentQualityHeightOscar: Int = 1080

    // Vòng lặp cập nhật SeekBar theo tiến trình phát thực tế
    private val progressHandlerOscar = Handler(Looper.getMainLooper())
    private var isUserSeekingOscar = false

    // Ngưỡng thời gian phát để tính 1 tập là "đã xem gần đây"
    private val recentlyWatchedThresholdMsOscar: Long = 3_000L
    // Vị trí tập đang được theo dõi thời gian xem trong phiên hiện tại
    private var trackedEpisodePositionOscar: Int = -1
    // Đánh dấu tập tại trackedEpisodePosition đã được ghi "đã xem" trong phiên hiện tại chưa
    private var trackedEpisodeMarkedOscar: Boolean = false

    private val progressRunnableOscar = object : Runnable {
        override fun run() {
            updateSeekBarProgressOscar()
            checkRecentlyWatchedThresholdOscar()
            progressHandlerOscar.postDelayed(this, 500L)
        }
    }

    override fun getData() {
        val bundleOscar = intent.extras
        if (bundleOscar != null) {
            movieIdOscar = bundleOscar.getLong("EXTRA_MOVIE_ID", -1L)
            initialEpisodeIdOscar = bundleOscar.getLong("EXTRA_EPISODE_ID", -1L)
            moviePosterOscar = bundleOscar.getString("EXTRA_MOVIE_POSTER", "")
            movieTitleOscar = bundleOscar.getString("EXTRA_MOVIE_TITLE", "")
        }
    }

    override fun onStart() {
        super.onStart()
        // Đăng ký lắng nghe sự kiện mạng thời gian thực
        registerNetworkCallbackOscar()
    }

    override fun initView() {
        binding.toolbarTitleOscar.text = movieTitleOscar
        setupPlayerPoolOscar()
        setupEpisodeFeedOscar()
        setupEpisodeNumberGridOscar()
        setupSeekBarOscar()
        observeViewModelOscar()

        if (moviePosterOscar.isNotEmpty()) {
            applyScreenBackgroundOscar(moviePosterOscar)
        }

        if (movieIdOscar > 0) {
            viewModelOscar.loadFilmDetail(movieIdOscar)
        }
    }

    override fun bindView() {
        binding.btnBackOscar.tap {
            onBack()
        }
        binding.btnFavoriteOscar.tap {
            val episodeIdOscar = getCurrentEpisodeIdOscar()
            if (episodeIdOscar != null && episodeIdOscar > 0L) {
                viewModelOscar.toggleFavorite(episodeIdOscar)
            }
        }

        // Không dùng tap() (có scale animation) vì ẩn parent LiquidGlass ngay lúc đang animate sẽ crash native
        binding.btnEpisodeListOscar.tap {
            toggleGroupOscar(binding.bottomEpisodeOscar)
        }

        binding.ivCenterPlayOscar.tap {
            togglePlayPauseOscar()
        }

        // Bấm nút tua lùi 10 giây
        binding.btnReplay10Oscar.tap {
            replay10SecondsOscar()
        }

        // Bấm nút tua tới 10 giây
        binding.btnForward10Oscar.tap {
            forward10SecondsOscar()
        }

        binding.btnSpeedOscar.tap {
            toggleGroupOscar(binding.groupSpeedOptionsOscar)
        }

        binding.btnQualityOscar.tap {
            toggleGroupOscar(binding.groupQualityOptionsOscar)
        }

        binding.btnSubtitleOscar.tap {
            onSubtitleButtonClickedOscar()
        }

        setupSpeedOptionClicksOscar()
        setupQualityOptionClicksOscar()

        episodeNumberAdapterOscar.onEpisodeClick = { item ->
            closeAllOptionGroupsOscar()
            playEpisodeOscar(item.number, smoothScrollOscar = false)
        }
    }

    private fun setupPlayerPoolOscar() {
        val cacheDataSourceFactoryOscar = PlayerCacheProvider.buildCacheDataSourceFactory(this)
        // DefaultMediaSourceFactory mới merge được sidecar SRT/VTT vào HLS
        val mediaSourceFactoryOscar = DefaultMediaSourceFactory(cacheDataSourceFactoryOscar)

        val poolSizeOscar = 3
        val builtPlayersOscar = mutableListOf<ExoPlayer>()
        var indexOscar = 0
        while (indexOscar < poolSizeOscar) {
            val trackSelectorOscar = DefaultTrackSelector(this)
            // Tắt text track mặc định, chỉ bật khi user nhấn btnSubtitle
            val paramsBuilderOscar = trackSelectorOscar.buildUponParameters()
            paramsBuilderOscar.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            trackSelectorOscar.parameters = paramsBuilderOscar.build()

            // Truyền mediaSourceFactory để subtitle pipeline đồng bộ giữa source và renderer
            val playerOscar = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactoryOscar)
                .setTrackSelector(trackSelectorOscar)
                .build()
            builtPlayersOscar.add(playerOscar)
            indexOscar = indexOscar + 1
        }
        playerPoolOscar = builtPlayersOscar

        feedAdapterOscar = EpisodeFeedAdapterOscar(playerPoolOscar, mediaSourceFactoryOscar)
        feedAdapterOscar.onSubtitleCue = { cueTextOscar ->
            updateSubtitleOverlayOscar(cueTextOscar)
        }
        feedAdapterOscar.onVideoTap = {
            onVideoTapActionOscar()
        }
        feedAdapterOscar.onVideoEnded = { positionOscar ->
            handleVideoEndedOscar(positionOscar)
        }
    }

    private fun setupEpisodeFeedOscar() {
        val layoutManagerOscar = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvEpisodesOscar.layoutManager = layoutManagerOscar
        binding.rvEpisodesOscar.adapter = feedAdapterOscar

        val snapHelperOscar = PagerSnapHelper()
        snapHelperOscar.attachToRecyclerView(binding.rvEpisodesOscar)

        binding.rvEpisodesOscar.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snapViewOscar = snapHelperOscar.findSnapView(layoutManagerOscar)
                    if (snapViewOscar != null) {
                        val positionOscar = layoutManagerOscar.getPosition(snapViewOscar)
                        if (positionOscar != currentSnapPositionOscar) {
                            currentSnapPositionOscar = positionOscar
                            feedAdapterOscar.playAt(positionOscar)
                            applyAllPlaybackSettingsOscar()
                            viewModelOscar.setCurrentEpisodeIndex(positionOscar)
                            episodeNumberAdapterOscar.currentIndex = positionOscar
                            updateEpisodeInfoTextOscar(positionOscar)
                            updateFavoriteIconOscar()
                            hideCenterPlayIconOscar()
                            showControlsOverlayOscar()
                        }
                    }
                }
            }
        })
    }

    private fun setupEpisodeNumberGridOscar() {
        binding.rvEpisodeNumbersOscar.adapter = episodeNumberAdapterOscar
    }

    // Giới hạn chiều cao lưới tập: RecyclerView wrap_content với nhiều tập sẽ cao quá,
    // LiquidGlass capture bitmap lớn dễ native crash và app tự mở lại
    private fun applyEpisodeGridMaxHeightOscar(itemCountOscar: Int) {
        val columnCountOscar = 6
        val maxVisibleRowsOscar = 5
        val rowCountOscar = (itemCountOscar + columnCountOscar - 1) / columnCountOscar
        val densityOscar = resources.displayMetrics.density
        val estimatedRowHeightOscar = (40f * densityOscar).toInt()
        val layoutParamsOscar = binding.rvEpisodeNumbersOscar.layoutParams
        if (rowCountOscar > maxVisibleRowsOscar) {
            layoutParamsOscar.height = estimatedRowHeightOscar * maxVisibleRowsOscar
        } else {
            layoutParamsOscar.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        binding.rvEpisodeNumbersOscar.layoutParams = layoutParamsOscar
    }

    private fun observeViewModelOscar() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModelOscar.filmDetail.collect { detailOscar ->
                        if (detailOscar != null) {
                            onFilmDetailLoadedOscar(detailOscar)
                        }
                    }
                }

                // Icon tim tự cập nhật mỗi khi danh sách yêu thích trong Room thay đổi
                launch {
                    viewModelOscar.favorites.collect {
                        updateFavoriteIconOscar()
                    }
                }
            }
        }
    }

    private fun applyScreenBackgroundOscar(urlOscar: String?) {

        Glide.with(this)
            .load(urlOscar)
            .error(R.drawable.card_demo)
            .centerCrop()
            .into(binding.ivScreenBackgroundOscar)
    }

    private fun onFilmDetailLoadedOscar(detailOscar: FilmDetailResponseOscar) {
        val episodeListOscar = detailOscar.episodes
        if (episodeListOscar != null) {
            feedAdapterOscar.addListData(episodeListOscar.toMutableList())
        }

        val numberListOscar = PlayVideoMapper.buildEpisodeNumberList(detailOscar)
        episodeNumberAdapterOscar.addListData(numberListOscar.toMutableList())
        applyEpisodeGridMaxHeightOscar(numberListOscar.size)

        if (isFirstDetailLoadOscar) {
            isFirstDetailLoadOscar = false

            // Xác định vị trí tập cần phát ban đầu
            var targetPositionOscar = 0
            if (initialEpisodeIdOscar > 0L && episodeListOscar != null) {
                var indexOscar = 0
                while (indexOscar < episodeListOscar.size) {
                    val segmentOscar = episodeListOscar[indexOscar]
                    if (segmentOscar.id == initialEpisodeIdOscar) {
                        targetPositionOscar = indexOscar
                        break
                    }
                    indexOscar = indexOscar + 1
                }
            }

            // Cập nhật trạng thái tập đang chọn
            currentSnapPositionOscar = targetPositionOscar
            viewModelOscar.setCurrentEpisodeIndex(targetPositionOscar)
            episodeNumberAdapterOscar.currentIndex = targetPositionOscar
            updateEpisodeInfoTextOscar(targetPositionOscar)
            updateFavoriteIconOscar()
            hideCenterPlayIconOscar()

            // Cuộn feed đến đúng tập phim
            binding.rvEpisodesOscar.scrollToPosition(targetPositionOscar)

            // Bắt đầu phát video sau khi RecyclerView hoàn thành layout
            binding.rvEpisodesOscar.post {
                if (isDestroyed || isFinishing) {
                    return@post
                }
                feedAdapterOscar.playAt(targetPositionOscar)
                applyAllPlaybackSettingsOscar()
                showControlsOverlayOscar()
            }
        } else {
            // Khi API cập nhật thêm các tập mới về trong lúc đang xem:
            // Cập nhật lại text số tập và highlight tập hiện tại mà không phát lại video
            updateEpisodeInfoTextOscar(currentSnapPositionOscar)
            updateFavoriteIconOscar()
            episodeNumberAdapterOscar.currentIndex = currentSnapPositionOscar
        }
    }


    private fun updateEpisodeInfoTextOscar(positionOscar: Int) {
        val detailOscar = viewModelOscar.filmDetail.value
        var totalOscar = 0
        if (detailOscar != null && detailOscar.totalEpisode != null) {
            totalOscar = detailOscar.totalEpisode
        }
        val textOscar = getString(R.string.episode_format, positionOscar + 1, totalOscar)
        binding.tvEpisodeNameIndexOscar.text = textOscar
    }

    private fun toggleGroupOscar(groupOscar: View) {
        if (groupOscar.visibility == View.VISIBLE) {
            closeAllOptionGroupsOscar()
        } else {
            openOptionGroupOscar(groupOscar)
        }
    }

    private fun openOptionGroupOscar(groupOscar: View) {
        cancelHideControlsOverlayOscar()
        binding.groupSpeedOptionsOscar.visibility = View.GONE
        binding.groupQualityOptionsOscar.visibility = View.GONE

        if (groupOscar == binding.bottomEpisodeOscar) {
            cancelOverlayButtonAnimationOscar()
            // Ẩn overlay trước, đợi 1 frame rồi mới hiện panel tập
            // để LiquidGlass không snapshot overlay đang bị detach (gây native crash)
            binding.bottomOverlayOscar.visibility = View.GONE
            binding.root.post {
                if (isDestroyed || isFinishing) {
                    return@post
                }
                binding.bottomEpisodeOscar.visibility = View.VISIBLE
            }
        } else {
            binding.bottomEpisodeOscar.visibility = View.GONE
            binding.bottomOverlayOscar.visibility = View.VISIBLE
            binding.groupEpisodeInfoOscar.gone()
            groupOscar.visibility = View.VISIBLE
        }
    }

    // Hủy scale animation của nút trong overlay trước khi ẩn parent LiquidGlass
    private fun cancelOverlayButtonAnimationOscar() {
        binding.btnEpisodeListOscar.animate().cancel()
        binding.btnEpisodeListOscar.scaleX = 1f
        binding.btnEpisodeListOscar.scaleY = 1f
        binding.btnEpisodeListOscar.alpha = 1f
        binding.btnEpisodeListOscar.translationY = 0f
    }

    // Đóng toàn bộ panel (tốc độ/chất lượng/chọn tập) và đưa UI về trạng thái mặc định
    private fun closeAllOptionGroupsOscar() {
        binding.groupSpeedOptionsOscar.visibility = View.GONE
        binding.groupQualityOptionsOscar.visibility = View.GONE
        binding.bottomEpisodeOscar.visibility = View.GONE
        binding.bottomOverlayOscar.visibility = View.VISIBLE
        binding.groupEpisodeInfoOscar.visible()
        scheduleHideControlsOverlayOscar()
    }

    private fun setupSpeedOptionClicksOscar() {
        val speedOptionsOscar = listOf(
            Pair(binding.btnSpeed1xOscar, 1.0f),
            Pair(binding.btnSpeed125xOscar, 1.25f),
            Pair(binding.btnSpeed15xOscar, 1.5f),
            Pair(binding.btnSpeed20xOscar, 2.0f)
        )

        for (optionOscar in speedOptionsOscar) {
            val textViewOscar = optionOscar.first
            val speedValueOscar = optionOscar.second
            textViewOscar.tap {
                applyPlaybackSpeedOscar(speedValueOscar)
                highlightSelectedOptionOscar(textViewOscar, speedOptionsOscar.map { it.first })
                closeAllOptionGroupsOscar()
            }
        }

        // Highlight màu lựa chọn tốc độ mặc định ban đầu
        highlightSpeedOptionOscar(currentSpeedOscar, speedOptionsOscar)
    }

    private fun applyPlaybackSpeedOscar(speedOscar: Float) {
        currentSpeedOscar = speedOscar
        val currentPlayerOscar = getCurrentPlayerOscar()
        if (currentPlayerOscar != null) {
            currentPlayerOscar.setPlaybackSpeed(speedOscar)
        }
    }

    private fun setupQualityOptionClicksOscar() {
        val qualityOptionsOscar = listOf(
            Pair(binding.btn2kOscar, 1440),
            Pair(binding.btnFullhdOscar, 1080),
            Pair(binding.btnHdOscar, 720),
            Pair(binding.btn540Oscar, 540),
            Pair(binding.btn360Oscar, 360)
        )

        for (optionOscar in qualityOptionsOscar) {
            val textViewOscar = optionOscar.first
            val heightOscar = optionOscar.second
            textViewOscar.tap {
                applyQualityOscar(heightOscar)
                highlightSelectedOptionOscar(textViewOscar, qualityOptionsOscar.map { it.first })
                closeAllOptionGroupsOscar()
            }
        }

        // Highlight màu lựa chọn chất lượng mặc định ban đầu
        highlightQualityOptionOscar(currentQualityHeightOscar, qualityOptionsOscar)
    }

    private fun applyQualityOscar(heightOscar: Int) {
        currentQualityHeightOscar = heightOscar
        val currentPlayerOscar = getCurrentPlayerOscar()
        if (currentPlayerOscar != null) {
            val trackSelectorOscar = currentPlayerOscar.trackSelector
            if (trackSelectorOscar is DefaultTrackSelector) {
                val parametersOscar = trackSelectorOscar.parameters
                    .buildUpon()
                    .setMaxVideoSize(Int.MAX_VALUE, heightOscar)
                    .setForceHighestSupportedBitrate(true)
                    .build()
                trackSelectorOscar.parameters = parametersOscar
            }
        }
    }

    private fun highlightSelectedOptionOscar(
        selectedOscar: TextView,
        allOscar: List<TextView>
    ) {
        for (textViewOscar in allOscar) {
            if (textViewOscar == selectedOscar) {
                textViewOscar.setTextColor(ContextCompat.getColor(this, R.color.Orange_50))
            } else {
                textViewOscar.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }
    }

    // Đánh dấu highlight cho tốc độ tương ứng với selectedSpeed
    private fun highlightSpeedOptionOscar(
        selectedSpeedOscar: Float,
        speedOptionsOscar: List<Pair<TextView, Float>>
    ) {
        val allTextViewsOscar = mutableListOf<TextView>()
        var targetTextViewOscar: TextView? = null
        for (optionOscar in speedOptionsOscar) {
            val textViewOscar = optionOscar.first
            val speedValueOscar = optionOscar.second
            allTextViewsOscar.add(textViewOscar)
            if (speedValueOscar == selectedSpeedOscar) {
                targetTextViewOscar = textViewOscar
            }
        }
        if (targetTextViewOscar != null) {
            highlightSelectedOptionOscar(targetTextViewOscar, allTextViewsOscar)
        }
    }

    // Đánh dấu highlight cho chất lượng tương ứng với selectedHeight
    private fun highlightQualityOptionOscar(
        selectedHeightOscar: Int,
        qualityOptionsOscar: List<Pair<TextView, Int>>
    ) {
        val allTextViewsOscar = mutableListOf<TextView>()
        var targetTextViewOscar: TextView? = null
        for (optionOscar in qualityOptionsOscar) {
            val textViewOscar = optionOscar.first
            val heightValueOscar = optionOscar.second
            allTextViewsOscar.add(textViewOscar)
            if (heightValueOscar == selectedHeightOscar) {
                targetTextViewOscar = textViewOscar
            }
        }
        if (targetTextViewOscar != null) {
            highlightSelectedOptionOscar(targetTextViewOscar, allTextViewsOscar)
        }
    }

    /**
     * Xử lý sự kiện khi ấn nút Subtitle
     */
    private fun onSubtitleButtonClickedOscar() {
        val hasSubtitleOscar = hasMovieSubtitleOscar()
        if (hasSubtitleOscar) {
            // Phim có hỗ trợ phụ đề -> Mở dialog chọn ngôn ngữ
            val dialogOscar = SelectLanguageOscar(
                context = this,
                isSubtitleEnabledOscar = subtitleEnabledOscar,
                onSelectOscar = { isSelectedOscar ->
                    subtitleEnabledOscar = isSelectedOscar
                    Log.d(
                        TAG_SUBTITLEOscar,
                        "=== User chọn Subtitle từ Dialog: subtitleEnabled=$subtitleEnabledOscar ==="
                    )
                    updateSubtitleButtonIconOscar()
                    applySubtitleStateOscar()
                    if (!subtitleEnabledOscar) {
                        clearSubtitleOverlayOscar()
                    }
                }
            )
            dialogOscar.show()
        } else {
            // Phim không hỗ trợ phụ đề -> Hiển thị Toast thông báo
            Toast.makeText(
                this,
                getString(R.string.no_subtitles_available),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Kiểm tra xem phim hoặc tập hiện tại có phụ đề hay không
     */
    private fun hasMovieSubtitleOscar(): Boolean {
        val detailOscar = viewModelOscar.filmDetail.value
        if (detailOscar != null && detailOscar.subtitle == true) {
            return true
        }
        val currentSegmentOscar = feedAdapterOscar.getSegmentAt(currentSnapPositionOscar)
        if (currentEpisodeHasSubtitleOscar(currentSegmentOscar)) {
            return true
        }
        val episodeListOscar = detailOscar?.episodes
        if (!episodeListOscar.isNullOrEmpty()) {
            for (segmentOscar in episodeListOscar) {
                if (currentEpisodeHasSubtitleOscar(segmentOscar)) {
                    return true
                }
            }
        }
        return false
    }

    // Đổi icon nút phụ đề giữa trạng thái bật (ic_subtitles_select) và tắt (ic_subtitles)
    private fun updateSubtitleButtonIconOscar() {
        if (subtitleEnabledOscar) {
            binding.btnSubtitleOscar.setImageResource(R.drawable.ic_subtitles_select)
        } else {
            binding.btnSubtitleOscar.setImageResource(R.drawable.ic_subtitles)
        }
    }

    // Gọi lại toàn bộ setting (speed, quality, subtitle) mỗi khi đổi tập,
    // vì mỗi ExoPlayer trong pool có DefaultTrackSelector riêng
    private fun applyAllPlaybackSettingsOscar() {
        applyPlaybackSpeedOscar(currentSpeedOscar)
        applyQualityOscar(currentQualityHeightOscar)
        clearSubtitleOverlayOscar()
        // Đồng bộ icon nút subtitle và state khi đổi tập
        updateSubtitleButtonIconOscar()
        applySubtitleStateOscar()
    }

    private fun applySubtitleStateOscar() {
        val currentPlayerOscar = getCurrentPlayerOscar()
        if (currentPlayerOscar == null) {
            Log.w(TAG_SUBTITLEOscar, "applySubtitleState: currentPlayer đang null tại vị trí $currentSnapPositionOscar")
            return
        }
        val trackSelectorOscar = currentPlayerOscar.trackSelector
        if (trackSelectorOscar !is DefaultTrackSelector) {
            Log.w(TAG_SUBTITLEOscar, "applySubtitleState: trackSelector không phải DefaultTrackSelector")
            return
        }

        val segmentOscar = feedAdapterOscar.getSegmentAt(currentSnapPositionOscar)
        val hasSubtitleOscar = currentEpisodeHasSubtitleOscar(segmentOscar)
        val builderOscar = trackSelectorOscar.parameters.buildUpon()
        if (subtitleEnabledOscar && hasSubtitleOscar) {
            val languageCodeOscar = pickSubtitleLanguageOscar(segmentOscar)
            Log.d(
                TAG_SUBTITLEOscar,
                "=== [GIAI ĐOẠN 2] BẬT Text Track: Tập $currentSnapPositionOscar | Ngôn ngữ ưu tiên: $languageCodeOscar ==="
            )
            builderOscar.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            builderOscar.setSelectUndeterminedTextLanguage(true)
            if (languageCodeOscar != null) {
                builderOscar.setPreferredTextLanguage(languageCodeOscar)
            }
        } else {
            Log.d(
                TAG_SUBTITLEOscar,
                "=== [GIAI ĐOẠN 2] TẮT Text Track: Tập $currentSnapPositionOscar | subtitleEnabled=$subtitleEnabledOscar, hasSubtitle=$hasSubtitleOscar ==="
            )
            builderOscar.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            clearSubtitleOverlayOscar()
        }
        trackSelectorOscar.parameters = builderOscar.build()

        // Force seek tại vị trí hiện tại để TextRenderer nhận lại subtitle data
        if (subtitleEnabledOscar && hasSubtitleOscar) {
            val currentPosOscar = currentPlayerOscar.currentPosition
            Log.d(TAG_SUBTITLEOscar, "Force seek tại vị trí ${currentPosOscar}ms để refresh TextRenderer")
            currentPlayerOscar.seekTo(currentPosOscar)
        }

        Log.d(
            TAG_PLAY_VIDEOOscar,
            "applySubtitleState enabled=$subtitleEnabledOscar hasSubtitle=$hasSubtitleOscar position=$currentSnapPositionOscar"
        )
    }

    private fun currentEpisodeHasSubtitleOscar(segmentOscar: SegmentResponseOscar?): Boolean {
        if (segmentOscar == null) {
            return false
        }
        val subtitlesOscar = segmentOscar.subtitles
        if (subtitlesOscar.isNullOrEmpty()) {
            return false
        }
        var hasValueOscar = false
        for (subtitleOscar in subtitlesOscar) {
            if (!subtitleOscar.value.isNullOrBlank()) {
                hasValueOscar = true
            }
        }
        return hasValueOscar
    }

    private fun updateSubtitleOverlayOscar(cueTextOscar: String) {
        if (!subtitleEnabledOscar) {
            Log.d(TAG_SUBTITLEOscar, "[GIAI ĐOẠN 4] Bỏ qua hiển thị UI vì subtitleEnabled = false")
            clearSubtitleOverlayOscar()
            return
        }
        if (cueTextOscar.isEmpty()) {
            Log.d(TAG_SUBTITLEOscar, "[GIAI ĐOẠN 4] Xóa UI Overlay (cueText rỗng)")
            clearSubtitleOverlayOscar()
        } else {
            Log.d(TAG_SUBTITLEOscar, "[GIAI ĐOẠN 4] Cập nhật UI Subtitle TextView: \"$cueTextOscar\"")
            binding.tvSubOscar.text = cueTextOscar
            binding.subTitleOscar.visible()
        }
    }

    private fun clearSubtitleOverlayOscar() {
        binding.tvSubOscar.text = ""
        binding.subTitleOscar.gone()
    }

    // Ưu tiên phụ đề trùng ngôn ngữ máy, nếu không có thì lấy ngôn ngữ đầu tiên có sẵn
    private fun pickSubtitleLanguageOscar(segmentOscar: SegmentResponseOscar?): String? {
        if (segmentOscar == null) {
            return null
        }
        val subtitlesOscar = segmentOscar.subtitles
        if (subtitlesOscar.isNullOrEmpty()) {
            return null
        }

        val deviceLanguageOscar = Locale.getDefault().language
        for (subtitleOscar in subtitlesOscar) {
            if (subtitleOscar.languageCode == deviceLanguageOscar) {
                return deviceLanguageOscar
            }
        }
        return subtitlesOscar[0].languageCode
    }

    private fun setupSeekBarOscar() {
        binding.seekBarOscar.max = 1000

        binding.seekBarOscar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val currentPlayerOscar = getCurrentPlayerOscar()
                    if (currentPlayerOscar != null) {
                        val durationOscar = currentPlayerOscar.duration
                        if (durationOscar > 0) {
                            val positionMsOscar = durationOscar * progress / seekBar.max
                            binding.tvCurrentTimeOscar.text = formatTimeOscar(positionMsOscar)
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isUserSeekingOscar = true
                cancelHideControlsOverlayOscar()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val currentPlayerOscar = getCurrentPlayerOscar()
                if (currentPlayerOscar != null) {
                    val durationOscar = currentPlayerOscar.duration
                    if (durationOscar > 0) {
                        val targetPositionMsOscar = durationOscar * seekBar.progress / seekBar.max
                        currentPlayerOscar.seekTo(targetPositionMsOscar)
                    }
                }
                isUserSeekingOscar = false
                scheduleHideControlsOverlayOscar()
            }
        })
    }

    private fun updateSeekBarProgressOscar() {
        if (isUserSeekingOscar) {
            return
        }

        val currentPlayerOscar = getCurrentPlayerOscar()
        if (currentPlayerOscar == null) {
            return
        }

        val durationOscar = currentPlayerOscar.duration
        if (durationOscar <= 0) {
            return
        }

        val currentPositionOscar = currentPlayerOscar.currentPosition
        val progressOscar = (currentPositionOscar * binding.seekBarOscar.max / durationOscar).toInt()
        binding.seekBarOscar.progress = progressOscar
        binding.tvCurrentTimeOscar.text = formatTimeOscar(currentPositionOscar)
        binding.tvTotalTimeOscar.text = formatTimeOscar(durationOscar)
    }

    // Kiểm tra tập đang phát đã vượt ngưỡng "đã xem" (3 giây) hay chưa, nếu có thì đánh dấu 1 lần
    // duy nhất cho mỗi lượt xem (không ghi DB liên tục mỗi tick 500ms của progressRunnable)
    private fun checkRecentlyWatchedThresholdOscar() {
        if (trackedEpisodePositionOscar != currentSnapPositionOscar) {
            trackedEpisodePositionOscar = currentSnapPositionOscar
            trackedEpisodeMarkedOscar = false
        }
        if (trackedEpisodeMarkedOscar) {
            return
        }
        val currentPlayerOscar = getCurrentPlayerOscar()
        if (currentPlayerOscar == null) {
            return
        }
        if (currentPlayerOscar.currentPosition >= recentlyWatchedThresholdMsOscar) {
            trackedEpisodeMarkedOscar = true
            markCurrentEpisodeAsWatchedOscar(currentSnapPositionOscar)
        }
    }

    // Đánh dấu tập tại vị trí `position` là "đã xem gần đây" trong Room DB. Metadata đầy đủ
    // (title/poster/totalEpisode...) đã được PlayVideoViewModelOscar cache sẵn khi tải chi tiết phim,
    // nên ở đây chỉ cần xác định đúng episodeId rồi cập nhật lastWatchedAt.
    private fun markCurrentEpisodeAsWatchedOscar(positionOscar: Int) {
        val detailOscar = viewModelOscar.filmDetail.value
        val episodeListOscar = detailOscar?.episodes
        val currentSegmentOscar: SegmentResponseOscar?
        if (!episodeListOscar.isNullOrEmpty() && positionOscar >= 0 && positionOscar < episodeListOscar.size) {
            currentSegmentOscar = episodeListOscar[positionOscar]
        } else {
            currentSegmentOscar = null
        }

        val episodeIdOscar = currentSegmentOscar?.id
        if (episodeIdOscar == null || episodeIdOscar <= 0L) {
            Log.w(TAG_PLAY_VIDEOOscar, "Không có episodeId hợp lệ để đánh dấu đã xem tại vị trí $positionOscar")
            return
        }

        val playerOscar = getCurrentPlayerOscar()
        val currentDurationOscar: Long
        val currentPositionOscar: Long
        if (playerOscar != null) {
            currentDurationOscar = playerOscar.duration
            currentPositionOscar = playerOscar.currentPosition
        } else {
            currentDurationOscar = 0L
            currentPositionOscar = 0L
        }
        viewModelOscar.updateWatchProgress(episodeIdOscar, currentPositionOscar, currentDurationOscar)
    }

    private fun formatTimeOscar(millisOscar: Long): String {
        val totalSecondsOscar = millisOscar / 1000
        val minutesOscar = totalSecondsOscar / 60
        val secondsOscar = totalSecondsOscar % 60
        return String.format(Locale.US, "%02d:%02d", minutesOscar, secondsOscar)
    }

    private fun getCurrentPlayerOscar(): ExoPlayer? {
        return feedAdapterOscar.getPlayerForPosition(currentSnapPositionOscar)
    }

    private fun getCurrentEpisodeIdOscar(): Long? {
        val segmentOscar = feedAdapterOscar.getSegmentAt(currentSnapPositionOscar)
        return segmentOscar?.id
    }

    // Cập nhật icon tim theo trạng thái yêu thích của tập đang xem (currentSnapPosition)
    private fun updateFavoriteIconOscar() {
        val episodeIdOscar = getCurrentEpisodeIdOscar()
        val favoriteListOscar = viewModelOscar.favorites.value
        var isFavoriteOscar = false
        for (episodeOscar in favoriteListOscar) {
            if (episodeOscar.episodeId == episodeIdOscar) {
                isFavoriteOscar = true
                break
            }
        }
        if (isFavoriteOscar) {
            binding.btnFavoriteOscar.setImageResource(R.drawable.boxicons_heart_filled_sn)
        } else {
            binding.btnFavoriteOscar.setImageResource(R.drawable.boxicons_heart_filled)
        }
    }

    /**
     * Xử lý sự kiện khi tap vào màn hình video
     */
    private fun onVideoTapActionOscar() {
        val isEpisodePanelOpenOscar = binding.bottomEpisodeOscar.visibility == View.VISIBLE
        val isSpeedPanelOpenOscar = binding.groupSpeedOptionsOscar.visibility == View.VISIBLE
        val isQualityPanelOpenOscar = binding.groupQualityOptionsOscar.visibility == View.VISIBLE
        if (isEpisodePanelOpenOscar || isSpeedPanelOpenOscar || isQualityPanelOpenOscar) {
            closeAllOptionGroupsOscar()
            return
        }

        val isToolbarHiddenOscar = binding.glassToolbarOscar.visibility != View.VISIBLE
        val isCenterHiddenOscar = binding.ivCenterPlayOscar.visibility != View.VISIBLE

        if (isToolbarHiddenOscar || isCenterHiddenOscar) {
            // Controls đang ẩn: Hiện toàn bộ controls lên, video vẫn tiếp tục phát bình thường (KHÔNG pause)
            showControlsOverlayOscar()
        } else {
            // Controls đang hiện: Chạm vào màn hình để ẩn controls đi, video vẫn tiếp tục phát bình thường
            hideControlsOverlayOscar()
            cancelHideControlsOverlayOscar()
        }
    }

    // Hiển thị toolbar, bottomOverlay và cụm nút giữa, đồng thời đặt lịch tự động ẩn sau 7s nếu video đang phát
    private fun showControlsOverlayOscar() {
        binding.glassToolbarOscar.visible()
        binding.bottomOverlayOscar.visible()
        showCenterPlayControlsOscar()

        // Cập nhật đúng icon Play / Pause cho nút ở giữa
        val currentPlayerOscar = getCurrentPlayerOscar()
        if (currentPlayerOscar != null) {
            if (currentPlayerOscar.playWhenReady) {
                // Đang phát -> Hiện icon Pause
                binding.ivPausePlayOscar.setImageResource(R.drawable.ic_pause)
            } else {
                // Đang tạm dừng -> Hiện icon Play
                binding.ivPausePlayOscar.setImageResource(R.drawable.play_arrow1)
            }
        }

        scheduleHideControlsOverlayOscar()
    }

    // Ẩn toolbar, bottomOverlay và cụm nút giữa
    private fun hideControlsOverlayOscar() {
        val isEpisodeOpenOscar = binding.bottomEpisodeOscar.visibility == View.VISIBLE
        val isSpeedOpenOscar = binding.groupSpeedOptionsOscar.visibility == View.VISIBLE
        val isQualityOpenOscar = binding.groupQualityOptionsOscar.visibility == View.VISIBLE
        if (isEpisodeOpenOscar || isSpeedOpenOscar || isQualityOpenOscar || isUserSeekingOscar) {
            return
        }

        val currentPlayerOscar = getCurrentPlayerOscar()
        // Nếu video đang tạm dừng (pause), không tự động ẩn controls để người dùng quan sát nút Play
        if (currentPlayerOscar != null) {
            if (!currentPlayerOscar.playWhenReady) {
                return
            }
        }

        binding.glassToolbarOscar.gone()
        binding.bottomOverlayOscar.gone()
        hideCenterPlayControlsOscar()
    }

    // Đặt lịch ẩn sau 7s nếu video đang phát
    private fun scheduleHideControlsOverlayOscar() {
        cancelHideControlsOverlayOscar()
        val currentPlayerOscar = getCurrentPlayerOscar()
        if (currentPlayerOscar != null) {
            if (currentPlayerOscar.playWhenReady) {
                overlayHideHandlerOscar.postDelayed(hideOverlayRunnableOscar, 7000L)
            }
        }
    }

    // Hủy lịch ẩn
    private fun cancelHideControlsOverlayOscar() {
        overlayHideHandlerOscar.removeCallbacks(hideOverlayRunnableOscar)
    }

    private fun togglePlayPauseOscar() {
        // Đang mở panel chọn tập / tốc độ / chất lượng thì bấm vào nút chỉ đóng panel
        val isEpisodePanelOpenOscar = binding.bottomEpisodeOscar.visibility == View.VISIBLE
        val isSpeedPanelOpenOscar = binding.groupSpeedOptionsOscar.visibility == View.VISIBLE
        val isQualityPanelOpenOscar = binding.groupQualityOptionsOscar.visibility == View.VISIBLE
        if (isEpisodePanelOpenOscar || isSpeedPanelOpenOscar || isQualityPanelOpenOscar) {
            closeAllOptionGroupsOscar()
            return
        }

        val currentPlayerOscar = getCurrentPlayerOscar()
        if (currentPlayerOscar == null) {
            return
        }

        val segmentOscar = feedAdapterOscar.getSegmentAt(currentSnapPositionOscar)
        if (segmentOscar == null || segmentOscar.url.isNullOrBlank()) {
            return
        }

        if (currentPlayerOscar.playWhenReady) {
            // Tạm dừng video: Đổi icon sang Play, giữ controls hiện và hủy lịch tự ẩn
            currentPlayerOscar.playWhenReady = false
            binding.ivPausePlayOscar.setImageResource(R.drawable.play_arrow1)
            showControlsOverlayOscar()
            cancelHideControlsOverlayOscar()
        } else {
            // Tiếp tục phát: Nếu video đã kết thúc thì seek về đầu
            if (currentPlayerOscar.playbackState == Player.STATE_ENDED) {
                currentPlayerOscar.seekTo(0)
            }
            currentPlayerOscar.playWhenReady = true
            binding.ivPausePlayOscar.setImageResource(R.drawable.ic_pause)
            showControlsOverlayOscar()
            scheduleHideControlsOverlayOscar()
        }
    }

    /**
     * Xử lý sự kiện khi video của một tập phát xong
     */
    private fun handleVideoEndedOscar(positionOscar: Int) {
        val totalEpisodesOscar = feedAdapterOscar.itemCount
        val nextPositionOscar = positionOscar + 1

        if (nextPositionOscar < totalEpisodesOscar) {
            // Chưa phải tập cuối cùng: Tự động nhảy sang tập kế tiếp
            playEpisodeOscar(nextPositionOscar, smoothScrollOscar = true)
        } else {
            // Là tập cuối cùng (ví dụ 50/50): Tạm dừng video và hiển thị icon Play
            val currentPlayerOscar = getCurrentPlayerOscar()
            if (currentPlayerOscar != null) {
                currentPlayerOscar.seekTo(0)
                currentPlayerOscar.playWhenReady = false
            }
            showCenterPlayControlsOscar()
            binding.ivPausePlayOscar.setImageResource(R.drawable.play_arrow1)
            binding.seekBarOscar.progress = 0
            binding.tvCurrentTimeOscar.text = formatTimeOscar(0)
            binding.glassToolbarOscar.visible()
            binding.bottomOverlayOscar.visible()
            cancelHideControlsOverlayOscar()
        }
    }

    /**
     * Chuyển đến tập chỉ định và bắt đầu phát video
     */
    private fun playEpisodeOscar(positionOscar: Int, smoothScrollOscar: Boolean = false) {
        if (positionOscar < 0 || positionOscar >= feedAdapterOscar.itemCount) {
            return
        }
        if (smoothScrollOscar) {
            binding.rvEpisodesOscar.smoothScrollToPosition(positionOscar)
        } else {
            binding.rvEpisodesOscar.scrollToPosition(positionOscar)
        }
        currentSnapPositionOscar = positionOscar
        viewModelOscar.setCurrentEpisodeIndex(positionOscar)
        episodeNumberAdapterOscar.currentIndex = positionOscar
        updateEpisodeInfoTextOscar(positionOscar)
        updateFavoriteIconOscar()
        hideCenterPlayControlsOscar()

        // Đợi RecyclerView cập nhật layout rồi mới phát video
        binding.rvEpisodesOscar.post {
            if (isDestroyed || isFinishing) {
                return@post
            }
            feedAdapterOscar.playAt(positionOscar)
            applyAllPlaybackSettingsOscar()
            showControlsOverlayOscar()
        }
    }

    /**
     * Hiển thị cụm nút điều khiển ở giữa màn hình (Play/Pause, Replay 10s, Forward 10s)
     */
    private fun showCenterPlayControlsOscar() {
        binding.ivCenterPlayOscar.visible()
        binding.btnReplay10Oscar.visible()
        binding.btnForward10Oscar.visible()
    }

    /**
     * Ẩn cụm nút điều khiển ở giữa màn hình (Play/Pause, Replay 10s, Forward 10s)
     */
    private fun hideCenterPlayControlsOscar() {
        binding.ivCenterPlayOscar.gone()
        binding.btnReplay10Oscar.gone()
        binding.btnForward10Oscar.gone()
    }

    private fun hideCenterPlayIconOscar() {
        hideCenterPlayControlsOscar()
    }

    /**
     * Tua lùi 10 giây
     */
    private fun replay10SecondsOscar() {
        val currentPlayerOscar = getCurrentPlayerOscar()
        if (currentPlayerOscar == null) {
            return
        }

        // Lấy vị trí phát hiện tại và trừ đi 10 giây (10.000 ms)
        val currentPositionOscar = currentPlayerOscar.currentPosition
        var targetPositionOscar = currentPositionOscar - 10_000L
        if (targetPositionOscar < 0L) {
            targetPositionOscar = 0L
        }

        // Seek đến vị trí mới
        currentPlayerOscar.seekTo(targetPositionOscar)
        // Cập nhật lại thanh SeekBar
        updateSeekBarProgressOscar()

        // Nếu video đang phát thì gia hạn thời gian hiển thị controls
        if (currentPlayerOscar.playWhenReady) {
            scheduleHideControlsOverlayOscar()
        }
    }

    /**
     * Tua tới 10 giây
     */
    private fun forward10SecondsOscar() {
        val currentPlayerOscar = getCurrentPlayerOscar()
        if (currentPlayerOscar == null) {
            return
        }

        // Lấy vị trí phát hiện tại và cộng thêm 10 giây (10.000 ms)
        val currentPositionOscar = currentPlayerOscar.currentPosition
        val durationOscar = currentPlayerOscar.duration
        var targetPositionOscar = currentPositionOscar + 10_000L
        if (durationOscar > 0L) {
            if (targetPositionOscar > durationOscar) {
                targetPositionOscar = durationOscar
            }
        }

        // Seek đến vị trí mới
        currentPlayerOscar.seekTo(targetPositionOscar)
        // Cập nhật lại thanh SeekBar
        updateSeekBarProgressOscar()

        // Nếu video đang phát thì gia hạn thời gian hiển thị controls
        if (currentPlayerOscar.playWhenReady) {
            scheduleHideControlsOverlayOscar()
        }
    }


    override fun onStop() {
        super.onStop()
        // Hủy đăng ký lắng nghe mạng khi màn hình không còn hiển thị
        unregisterNetworkCallback()
    }

    override fun onResume() {
        super.onResume()
        progressHandlerOscar.post(progressRunnableOscar)
        // Kiểm tra kết nối mạng khi quay lại màn hình
        checkInternetConnectionOscar()
        if (feedAdapterOscar.hasData()) {
            binding.rvEpisodesOscar.post {
                if (isDestroyed || isFinishing) {
                    return@post
                }
                feedAdapterOscar.resumeAt(currentSnapPositionOscar)
            }
        }
    }

    /**
     * Đăng ký NetworkCallback để phát hiện mất mạng ngay trong lúc đang phát video
     */
    private fun registerNetworkCallbackOscar() {
        val connectivityManagerOscar = getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManagerOscar == null) {
            return
        }
        val callbackOscar = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                handleNetworkLostOscar()
            }
            override fun onUnavailable() {
                super.onUnavailable()
                handleNetworkLostOscar()
            }
        }
        networkCallbackOscar = callbackOscar
        try {
            connectivityManagerOscar.registerDefaultNetworkCallback(callbackOscar)
        } catch (exceptionOscar: Exception) {
            // Xử lý ngoại lệ nếu thiết bị không hỗ trợ hoặc lỗi đăng ký
        }
    }
    /**
     * Xử lý khi mất mạng: chuyển sang NoInternetActivityOscar ngay lập tức
     */
    private fun handleNetworkLostOscar() {
        val isConnectedOscar = IsNetWork.haveNetworkConnection(this@PlayVideoActivityOscar)
        if (!isConnectedOscar) {
            runOnUiThread {
                val isActivityAliveOscar = !isFinishing && !isDestroyed
                if (isActivityAliveOscar) {
                    startNextActivity(NoInternetActivityOscar::class.java, null)
                }
            }
        }
    }
    /**
     * Hủy đăng ký callback mạng an toàn
     */
    private fun unregisterNetworkCallback() {
        val callbackOscar = networkCallbackOscar
        if (callbackOscar != null) {
            val connectivityManagerOscar = getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
            try {
                connectivityManagerOscar?.unregisterNetworkCallback(callbackOscar)
            } catch (exceptionOscar: Exception) {
                // Tránh crash khi callback chưa được đăng ký
            }
            networkCallbackOscar = null
        }
    }
    /**
     * Kiểm tra trạng thái mạng, nếu không có mạng thì mở NoInternetActivityOscar
     */
    private fun checkInternetConnectionOscar() {
        val isConnectedOscar = IsNetWork.haveNetworkConnection(this)
        if (!isConnectedOscar) {
            // Chuyển sang màn hình NoInternetActivityOscar
            startNextActivity(NoInternetActivityOscar::class.java, null)
        }
    }

    override fun onPause() {
        super.onPause()
        cancelHideControlsOverlayOscar()
        // Dừng tất cả player khi thoát khỏi màn hình
        feedAdapterOscar.pauseAll()
        // Hiển thị icon play để user biết video đang bị pause
        showCenterPlayControlsOscar()
        binding.ivPausePlayOscar.setImageResource(R.drawable.play_arrow1)
        progressHandlerOscar.removeCallbacks(progressRunnableOscar)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelHideControlsOverlayOscar()
        progressHandlerOscar.removeCallbacks(progressRunnableOscar)
        feedAdapterOscar.releaseAll()
    }
}