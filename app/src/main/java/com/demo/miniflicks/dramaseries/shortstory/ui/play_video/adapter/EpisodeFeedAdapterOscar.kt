package com.demo.miniflicks.dramaseries.shortstory.ui.play_video.adapter
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import com.bumptech.glide.Glide
import com.demo.miniflicks.dramaseries.shortstory.api.SegmentResponseOscar
import com.demo.miniflicks.dramaseries.shortstory.base.BaseAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemPlayVideoBinding
import java.util.Locale
import kotlin.collections.iterator


private const val TAG_PLAYEROscar = "DEBUG_PLAYER"
private const val TAG_SUBTITLEOscar = "DEBUG_SUBTITLE"

@OptIn(UnstableApi::class)
class EpisodeFeedAdapterOscar(
    private val playersOscar: List<ExoPlayer>,
    private val mediaSourceFactoryOscar: MediaSource.Factory
) : BaseAdapterOscar<SegmentResponseOscar, ItemPlayVideoBinding>() {

    private val boundHoldersOscar: MutableMap<Int, ViewHolderOscar> = mutableMapOf()
    private var playingPositionOscar: Int = -1

    var onSubtitleCue: ((String) -> Unit)? = null
    var onVideoTap: (() -> Unit)? = null
    var onVideoEnded: ((Int) -> Unit)? = null

    init {
        for (player in playersOscar) {
            player.addListener(buildDiagnosticListenerOscar(player))
        }
    }

    override fun setBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemPlayVideoBinding {
        val bindingOscar = ItemPlayVideoBinding.inflate(inflater, parent, false)
        val subtitleViewOscar = bindingOscar.playerViewOscar.subtitleView
        if (subtitleViewOscar != null) {
            subtitleViewOscar.visibility = View.GONE
        }
        return bindingOscar
    }

    override fun addListData(newList: MutableList<SegmentResponseOscar>) {
        listData.clear()
        listData.addAll(newList)
        notifyDataSetChanged()
    }

    override fun setData(
        binding: ItemPlayVideoBinding,
        item: SegmentResponseOscar,
        layoutPosition: Int
    ) {
        bindThumbnailOscar(binding, item)
    }

    override fun onCLick(
        binding: ItemPlayVideoBinding,
        item: SegmentResponseOscar,
        layoutPosition: Int
    ) {
        binding.playerViewOscar.isClickable = false
        binding.playerViewOscar.isFocusable = false
        binding.touchOverlayOscar.setOnClickListener {
            val listenerOscar = onVideoTap
            if (listenerOscar != null) {
                listenerOscar()
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolderOscar, position: Int) {
        super.onBindViewHolder(holder, position)
        boundHoldersOscar[position] = holder
        attachPlayerToVisibleHolderOscar(position)
        // playAt thường chạy trước khi holder bind (mở từ fragment / scrollToPosition)
        if (position == playingPositionOscar) {
            syncPlayingThumbnailOscar()
        }
    }

    override fun onViewRecycled(holder: ViewHolderOscar) {
        holder.binding.ivThumbnailOscar.visibility = View.VISIBLE
        holder.binding.pbLoadingOscar.visibility = View.GONE
        holder.binding.playerViewOscar.player = null
        var positionToRemoveOscar: Int? = null
        for (entry in boundHoldersOscar) {
            if (entry.value == holder) {
                positionToRemoveOscar = entry.key
            }
        }
        if (positionToRemoveOscar != null) {
            boundHoldersOscar.remove(positionToRemoveOscar)
        }
        super.onViewRecycled(holder)
    }

    fun playAt(position: Int) {
        if (position < 0 || position >= listData.size) {
            return
        }

        playingPositionOscar = position
        val poolSizeOscar = playersOscar.size
        val playIndexOscar = position % poolSizeOscar
        val preloadIndexOscar = (position + 1) % poolSizeOscar
        val idleIndexOscar = (position + 2) % poolSizeOscar

        val playPlayerOscar = playersOscar[playIndexOscar]
        val preloadPlayerOscar = playersOscar[preloadIndexOscar]
        val idlePlayerOscar = playersOscar[idleIndexOscar]

        Log.d(TAG_PLAYEROscar, "──────── tập " + (position + 1) + " ────────")

        // Máy thứ 3: bỏ video cũ, sẵn sàng vòng sau
        resetPlayerOscar(idlePlayerOscar)

        // Máy 1: phát tập hiện tại (nếu vừa preload thì không load lại)
        bindPlayerToEpisodeOscar(playPlayerOscar, listData[position], true)
        attachPlayerToVisibleHolderOscar(position)
        syncPlayingThumbnailOscar()


        // Chỉ một máy có tiếng
        var indexOscar = 0
        while (indexOscar < playersOscar.size) {
            if (playersOscar[indexOscar] != playPlayerOscar) {
                playersOscar[indexOscar].playWhenReady = false
            }
            indexOscar = indexOscar + 1
        }

        // Máy 2: preload tập kế
        val nextPositionOscar = position + 1
        if (nextPositionOscar < listData.size) {
            bindPlayerToEpisodeOscar(preloadPlayerOscar, listData[nextPositionOscar], false)
            attachPlayerToVisibleHolderOscar(nextPositionOscar)
        } else {
            resetPlayerOscar(preloadPlayerOscar)
        }

        logBoardOscar(playIndexOscar, preloadIndexOscar, idleIndexOscar, position)
    }

    fun pauseAll() {
        for (player in playersOscar) {
            player.playWhenReady = false
        }
    }

    // Re-attach player vào holder hiện tại ở trạng thái pause
    // Dùng khi vào lại Activity sau khi thoát ra (onResume)
    fun resumeAt(position: Int) {
        if (position < 0 || position >= listData.size) {
            return
        }
        // Cập nhật lại playingPosition để các hàm attach hoạt động đúng
        playingPositionOscar = position

        // Re-attach player vào holder đang hiển thị
        attachPlayerToVisibleHolderOscar(position)

        // Đồng bộ trạng thái thumbnail với player
        syncPlayingThumbnailOscar()

        // Giữ trạng thái pause, KHÔNG tự động phát lại
    }

    fun hasData(): Boolean {
        return listData.isNotEmpty()
    }

    fun releaseAll() {
        for (player in playersOscar) {
            player.release()
        }
        boundHoldersOscar.clear()
    }

    fun getSegmentAt(position: Int): SegmentResponseOscar? {
        if (position < 0 || position >= listData.size) {
            return null
        }
        return listData[position]
    }

    fun getPlayerForPosition(position: Int): ExoPlayer? {
        if (position != playingPositionOscar) {
            return null
        }
        val poolSizeOscar = playersOscar.size
        if (poolSizeOscar == 0) {
            return null
        }
        return playersOscar[position % poolSizeOscar]
    }

    // play = true → phát; false → chỉ preload
    private fun bindPlayerToEpisodeOscar(
        playerOscar: ExoPlayer,
        segmentOscar: SegmentResponseOscar,
        playOscar: Boolean
    ) {
        val currentTagOscar = playerOscar.currentMediaItem?.mediaId
        val sameMediaOscar: Boolean
        if (currentTagOscar == segmentOscar.id.toString()) {
            sameMediaOscar = true
        } else {
            sameMediaOscar = false
        }

        val actionOscar: String
        if (playOscar) {
            actionOscar = "phát   "
        } else {
            actionOscar = "preload"
        }

        if (sameMediaOscar) {
            Log.d(TAG_PLAYEROscar, actionOscar + " " + playerLabelOscar(playerOscar) + " (đã có media)")
        } else {
            Log.d(
                TAG_PLAYEROscar,
                actionOscar + " " + playerLabelOscar(playerOscar) + " " + shortUrlOscar(segmentOscar.url)
            )
            val mediaItemOscar = buildMediaItemOscar(segmentOscar)
            val mediaSourceOscar = mediaSourceFactoryOscar.createMediaSource(mediaItemOscar)
            playerOscar.setMediaSource(mediaSourceOscar)
            playerOscar.prepare()
        }

        playerOscar.playWhenReady = playOscar
    }

    private fun resetPlayerOscar(playerOscar: ExoPlayer) {
        Log.d(TAG_PLAYEROscar, "reset  " + playerLabelOscar(playerOscar) + " (rảnh)")
        detachPlayerFromAllHoldersOscar(playerOscar)
        playerOscar.playWhenReady = false
        playerOscar.stop()
        playerOscar.clearMediaItems()
    }

    private fun attachPlayerToVisibleHolderOscar(positionOscar: Int) {
        val holderOscar = boundHoldersOscar[positionOscar]
        if (holderOscar == null) {
            return
        }
        if (playingPositionOscar < 0) {
            return
        }
        val poolSizeOscar = playersOscar.size
        val playIndexOscar = playingPositionOscar % poolSizeOscar
        val preloadIndexOscar = (playingPositionOscar + 1) % poolSizeOscar
        if (positionOscar == playingPositionOscar) {
            holderOscar.binding.playerViewOscar.player = playersOscar[playIndexOscar]
        } else if (positionOscar == playingPositionOscar + 1) {
            holderOscar.binding.playerViewOscar.player = playersOscar[preloadIndexOscar]
        }
    }

    private fun detachPlayerFromAllHoldersOscar(playerOscar: ExoPlayer) {
        for (entryOscar in boundHoldersOscar) {
            val playerViewOscar = entryOscar.value.binding.playerViewOscar
            if (playerViewOscar.player == playerOscar) {
                playerViewOscar.player = null
                entryOscar.value.binding.ivThumbnailOscar.visibility = View.VISIBLE
                entryOscar.value.binding.pbLoadingOscar.visibility = View.GONE
            }
        }
    }

    private fun playerLabelOscar(playerOscar: ExoPlayer): String {
        var indexOscar = 0
        var foundIndexOscar = -1
        while (indexOscar < playersOscar.size) {
            if (playersOscar[indexOscar] === playerOscar) {
                foundIndexOscar = indexOscar
                break
            }
            indexOscar = indexOscar + 1
        }
        if (foundIndexOscar >= 0) {
            return "P" + foundIndexOscar
        }
        return "P?"
    }

    private fun shortUrlOscar(urlOscar: String?): String {
        if (urlOscar.isNullOrBlank()) {
            return "(không có url)"
        }
        val markerOscar = "/Episode_"
        val markerIndexOscar = urlOscar.indexOf(markerOscar)
        if (markerIndexOscar >= 0) {
            return urlOscar.substring(markerIndexOscar + 1)
        }
        return urlOscar
    }

    private fun logBoardOscar(playIndexOscar: Int, preloadIndexOscar: Int, idleIndexOscar: Int, positionOscar: Int) {
        val nextPositionOscar = positionOscar + 1
        val messageOscar =
            "bảng máy:  P" + playIndexOscar + "=tập " + (positionOscar + 1) + "(phát)" +
                    "  P" + preloadIndexOscar + "=tập " + (nextPositionOscar + 1) + "(preload)" +
                    "  P" + idleIndexOscar + "=rảnh"
        Log.d(TAG_PLAYEROscar, messageOscar)
    }

    private fun bindThumbnailOscar(bindingOscar: ItemPlayVideoBinding, segmentOscar: SegmentResponseOscar) {
        val thumbnailListOscar = segmentOscar.thumbnails.orEmpty()

        Log.d("DEBUG_PLAYER", "===== THUMBNAILS =====")
        Log.d("DEBUG_PLAYER", "Count: ${thumbnailListOscar.size}")

        thumbnailListOscar.forEachIndexed { indexOscar, thumbnailOscar ->
            Log.d("DEBUG_PLAYER", "[$indexOscar] $thumbnailOscar")
        }
        bindingOscar.ivThumbnailOscar.visibility = View.VISIBLE
        bindingOscar.pbLoadingOscar.visibility = View.GONE
        var thumbnailUrlOscar: String? = null
        if (thumbnailListOscar != null && thumbnailListOscar.isNotEmpty()) {
            thumbnailUrlOscar = thumbnailListOscar[0].value
        }
        if (!thumbnailUrlOscar.isNullOrBlank()) {
            Glide.with(bindingOscar.ivThumbnailOscar.context)
                .load(thumbnailUrlOscar)
                .into(bindingOscar.ivThumbnailOscar)
        }
        if (!segmentOscar.url.isNullOrBlank()) {
            bindingOscar.groupLockedOscar.visibility = View.GONE
        } else {
            bindingOscar.groupLockedOscar.visibility = View.VISIBLE
        }
    }

    private fun hideThumbnailAtOscar(positionOscar: Int) {
        val holderOscar = boundHoldersOscar[positionOscar]
        if (holderOscar == null) {
            return
        }
        holderOscar.binding.ivThumbnailOscar.visibility = View.GONE
        holderOscar.binding.pbLoadingOscar.visibility = View.GONE
    }

    private fun showThumbnailAtOscar(positionOscar: Int) {
        val holderOscar = boundHoldersOscar[positionOscar]
        if (holderOscar == null) {
            return
        }
        holderOscar.binding.ivThumbnailOscar.visibility = View.VISIBLE

        val segmentOscar = getSegmentAt(positionOscar)
        val canPlayOscar: Boolean
        if (segmentOscar != null && !segmentOscar.url.isNullOrBlank()) {
            canPlayOscar = true
        } else {
            canPlayOscar = false
        }

        if (positionOscar == playingPositionOscar && canPlayOscar) {
            holderOscar.binding.pbLoadingOscar.visibility = View.VISIBLE
            holderOscar.binding.pbLoadingOscar.isIndeterminate = true
            holderOscar.binding.pbLoadingOscar.bringToFront()
        } else {
            holderOscar.binding.pbLoadingOscar.visibility = View.GONE
        }
    }

    // Ẩn poster khi đã có hình video; không thì giữ poster
    private fun syncPlayingThumbnailOscar() {
        if (playingPositionOscar < 0) {
            return
        }
        val poolSizeOscar = playersOscar.size
        val playPlayerOscar = playersOscar[playingPositionOscar % poolSizeOscar]
        val isReadyOscar = playPlayerOscar.playbackState == Player.STATE_READY
        if (isReadyOscar) {
            hideThumbnailAtOscar(playingPositionOscar)
        } else {
            showThumbnailAtOscar(playingPositionOscar)
        }
    }

    private fun buildMediaItemOscar(segmentOscar: SegmentResponseOscar): MediaItem {
        val subtitleConfigsOscar = mutableListOf<MediaItem.SubtitleConfiguration>()
        val subtitleListOscar = segmentOscar.subtitles

        // Log giai đoạn 1: Lấy dữ liệu subtitle từ API cho tập
        val episodeIdOscar = segmentOscar.id
        Log.d(TAG_SUBTITLEOscar, "=== [GIAI ĐOẠN 1] Khởi tạo MediaItem cho tập: $episodeIdOscar ===")

        if (subtitleListOscar != null) {
            val totalSubtitlesOscar = subtitleListOscar.size
            Log.d(TAG_SUBTITLEOscar, "[GIAI ĐOẠN 1] Số lượng subtitle từ API: $totalSubtitlesOscar")
            var indexOscar = 0
            while (indexOscar < subtitleListOscar.size) {
                val subtitleOscar = subtitleListOscar[indexOscar]
                val subtitleValueOscar = subtitleOscar.value
                val languageCodeOscar = subtitleOscar.languageCode
                val languageNameOscar = subtitleOscar.languageName

                if (!subtitleValueOscar.isNullOrBlank()) {
                    val mimeTypeOscar = resolveSubtitleMimeTypeOscar(subtitleValueOscar)
                    Log.d(
                        TAG_SUBTITLEOscar,
                        "[GIAI ĐOẠN 1] [$indexOscar] Subtitle: Lang=$languageCodeOscar ($languageNameOscar) | MimeType=$mimeTypeOscar | URL=$subtitleValueOscar"
                    )
                    val configOscar = MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitleValueOscar))
                        .setMimeType(mimeTypeOscar)
                        .setLanguage(languageCodeOscar)
                        .setLabel(languageNameOscar)
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build()
                    subtitleConfigsOscar.add(configOscar)
                } else {
                    Log.w(TAG_SUBTITLEOscar, "[GIAI ĐOẠN 1] [$indexOscar] Subtitle bị rỗng URL! Lang=$languageCodeOscar")
                }
                indexOscar = indexOscar + 1
            }
        } else {
            Log.w(TAG_SUBTITLEOscar, "[GIAI ĐOẠN 1] Tập $episodeIdOscar không có danh sách subtitles từ API.")
        }
        val builderOscar = MediaItem.Builder()
        builderOscar.setMediaId(segmentOscar.id.toString())
        builderOscar.setUri(segmentOscar.url)
        builderOscar.setMimeType(MimeTypes.APPLICATION_M3U8)
        builderOscar.setSubtitleConfigurations(subtitleConfigsOscar)
        return builderOscar.build()
    }

    private fun resolveSubtitleMimeTypeOscar(urlOscar: String): String {
        val lowerUrlOscar = urlOscar.lowercase(Locale.US)
        if (lowerUrlOscar.contains(".vtt")) {
            return MimeTypes.TEXT_VTT
        }
        return MimeTypes.APPLICATION_SUBRIP
    }

    private fun buildDiagnosticListenerOscar(playerOscar: ExoPlayer): Player.Listener {
        return object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG_PLAYEROscar, playerLabelOscar(playerOscar) + " | LỖI " + error.errorCodeName, error)
                Log.e(TAG_SUBTITLEOscar, playerLabelOscar(playerOscar) + " | LỖI Player: " + error.errorCodeName, error)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playingPositionOscar < 0) {
                    return
                }
                val playPlayerOscar = playersOscar[playingPositionOscar % playersOscar.size]
                if (playerOscar != playPlayerOscar) {
                    return
                }

                // Khi phát hết video → gọi callback thông báo cho Activity xử lý
                if (playbackState == Player.STATE_ENDED) {
                    val callbackOscar = onVideoEnded
                    if (callbackOscar != null) {
                        callbackOscar(playingPositionOscar)
                    }
                    return
                }

                syncPlayingThumbnailOscar()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                val eventOscar: String
                if (isPlaying) {
                    eventOscar = "đang phát"
                } else {
                    eventOscar = "dừng"
                }
                Log.d(TAG_PLAYEROscar, playerLabelOscar(playerOscar) + " | " + eventOscar)
            }

            override fun onRenderedFirstFrame() {
                Log.d(TAG_PLAYEROscar, playerLabelOscar(playerOscar) + " | hiện hình")
                if (playingPositionOscar < 0) {
                    return
                }
                val playPlayerOscar = playersOscar[playingPositionOscar % playersOscar.size]
                if (playerOscar != playPlayerOscar) {
                    return
                }
                hideThumbnailAtOscar(playingPositionOscar)
            }

            override fun onTracksChanged(tracks: Tracks) {
                val playerTagOscar = playerLabelOscar(playerOscar)
                Log.d(TAG_SUBTITLEOscar, "=== [GIAI ĐOẠN 2] Tracks thay đổi trên $playerTagOscar ===")
                val groupsOscar = tracks.groups
                var groupIndexOscar = 0
                while (groupIndexOscar < groupsOscar.size) {
                    val groupOscar = groupsOscar[groupIndexOscar]
                    val trackTypeOscar = groupOscar.type
                    if (trackTypeOscar == C.TRACK_TYPE_TEXT) {
                        val mediaTrackGroupOscar = groupOscar.mediaTrackGroup
                        var trackIndexOscar = 0
                        while (trackIndexOscar < mediaTrackGroupOscar.length) {
                            val formatOscar = mediaTrackGroupOscar.getFormat(trackIndexOscar)
                            val isTrackSelectedOscar = groupOscar.isTrackSelected(trackIndexOscar)
                            Log.d(
                                TAG_SUBTITLEOscar,
                                "[GIAI ĐOẠN 2] $playerTagOscar Text Track: Lang=${formatOscar.language} | Label=${formatOscar.label} | Mime=${formatOscar.sampleMimeType} | Selected=$isTrackSelectedOscar"
                            )
                            trackIndexOscar = trackIndexOscar + 1
                        }
                    }
                    groupIndexOscar = groupIndexOscar + 1
                }
            }

            override fun onCues(cueGroup: CueGroup) {
                if (playingPositionOscar < 0) {
                    return
                }
                val playPlayerOscar = playersOscar[playingPositionOscar % playersOscar.size]
                if (playerOscar != playPlayerOscar) {
                    return
                }
                val cuesOscar = cueGroup.cues
                val playerTagOscar = playerLabelOscar(playerOscar)
                val currentPositionMsOscar = playerOscar.currentPosition

                val textBuilderOscar = StringBuilder()
                var cueIndexOscar = 0
                while (cueIndexOscar < cuesOscar.size) {
                    val cueTextOscar = cuesOscar[cueIndexOscar].text
                    if (cueTextOscar != null && cueTextOscar.isNotEmpty()) {
                        if (textBuilderOscar.isNotEmpty()) {
                            textBuilderOscar.append("\n")
                        }
                        textBuilderOscar.append(cueTextOscar)
                    }
                    cueIndexOscar = cueIndexOscar + 1
                }
                val finalCueTextOscar = textBuilderOscar.toString()
                if (finalCueTextOscar.isNotEmpty()) {
                    Log.d(
                        TAG_SUBTITLEOscar,
                        "=== [GIAI ĐOẠN 3] $playerTagOscar Cues tại ${currentPositionMsOscar}ms: \"$finalCueTextOscar\""
                    )
                } else {
                    Log.d(
                        TAG_SUBTITLEOscar,
                        "=== [GIAI ĐOẠN 3] $playerTagOscar Cues rỗng (ẩn sub) tại ${currentPositionMsOscar}ms"
                    )
                }

                val callbackOscar = onSubtitleCue
                if (callbackOscar != null) {
                    callbackOscar(finalCueTextOscar)
                }
            }
        }
    }
}