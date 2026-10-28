package com.demo.miniflicks.dramaseries.shortstory.ui.play_video.player
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File


@UnstableApi
object  PlayerCacheProvider {

    private const val MAX_CACHE_SIZE_BYTESOscar: Long = 300L * 1024L * 1024L
    private var cacheInstanceOscar: SimpleCache? = null

    @OptIn(UnstableApi::class)
    @Synchronized
    fun getCache(context: Context): SimpleCache {
        val existingCacheOscar = cacheInstanceOscar
        if (existingCacheOscar != null) {
            return existingCacheOscar
        }
        val cacheDirectoryOscar = File(context.applicationContext.cacheDir, "video_cache")
        val evictorOscar = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTESOscar)
        val databaseProviderOscar = StandaloneDatabaseProvider(context.applicationContext)
        val newCacheOscar = SimpleCache(cacheDirectoryOscar, evictorOscar, databaseProviderOscar)
        cacheInstanceOscar = newCacheOscar
        return newCacheOscar
    }
    @OptIn(UnstableApi::class)
    fun buildCacheDataSourceFactory(context: Context): CacheDataSource.Factory {
        val httpDataSourceFactoryOscar = DefaultHttpDataSource.Factory()
        val factoryOscar = CacheDataSource.Factory()
        factoryOscar.setCache(getCache(context))
        factoryOscar.setUpstreamDataSourceFactory(httpDataSourceFactoryOscar)
        factoryOscar.setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        return factoryOscar
    }
}