package com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.demo.miniflicks.dramaseries.shortstory.base.BaseAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.base.loadImageWithProgress
import com.demo.miniflicks.dramaseries.shortstory.model.WatchedEpisodeOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemPlaylistBinding


class PlaylistAdapterOscar : BaseAdapterOscar<WatchedEpisodeOscar, ItemPlaylistBinding>() {

    var onItemClick: ((WatchedEpisodeOscar) -> Unit)? = null
    var onFavoriteClick: ((WatchedEpisodeOscar) -> Unit)? = null

    override fun setBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemPlaylistBinding {
        return ItemPlaylistBinding.inflate(inflater, parent, false)
    }

    override fun addListData(newList: MutableList<WatchedEpisodeOscar>) {
        listData.clear()
        listData.addAll(newList)
        notifyDataSetChanged()
    }

    override fun setData(binding: ItemPlaylistBinding, item: WatchedEpisodeOscar, layoutPosition: Int) {
        // Hiển thị ảnh poster / thumbnail của tập phim kèm ProgressBar xoay tròn loading độc lập
        binding.ivCardMovieOscar.loadImageWithProgress(
            url = item.posterUrl,
            progressBar = binding.pbLoadingOscar,
            errorResId = R.drawable.card_demo
        )

        // Hiển thị tên phim
        binding.tvMovieTitleOscar.text = item.movieTitle

        // Hiển thị lượt xem (format K/M)
        binding.tvCountViewOscar.text = formatViewCountOscar(item.viewCount)

        // Hiển thị số tập: "episodeNumber/totalEpisode"
        val episodeTextOscar: String
        if (item.totalEpisode > 0) {
            episodeTextOscar = "${item.episodeNumber}/${item.totalEpisode}"
        } else {
            episodeTextOscar = "${item.episodeNumber}"
        }
        binding.tvCountOscar.text = episodeTextOscar

        // Icon yêu thích
        val favoriteIconOscar = if (item.isFavorite) {
            R.drawable.ic_favorite_border_select  // icon đã thích
        } else {
            R.drawable.ic_favorite_border  // icon chưa thích
        }
        binding.ivFavoriteOscar.setImageResource(favoriteIconOscar)
    }

    override fun onCLick(binding: ItemPlaylistBinding, item: WatchedEpisodeOscar, layoutPosition: Int) {
        // Click vào item → mở PlayVideoActivityOscar với đúng tập phim
        binding.root.setOnClickListener {
            onItemClick?.invoke(item)
        }
        // Click vào nút favorite → toggle trạng thái yêu thích của tập phim
        binding.ivFavoriteOscar.setOnClickListener {
            onFavoriteClick?.invoke(item)
        }
    }

    private fun formatViewCountOscar(countOscar: Long): String {
        if (countOscar >= 1_000_000) {
            val formattedOscar = countOscar / 1_000_000.0
            return String.format("%.1fM", formattedOscar)
        }
        if (countOscar >= 1_000) {
            val formattedOscar = countOscar / 1_000.0
            return String.format("%.1fK", formattedOscar)
        }
        return countOscar.toString()
    }
}