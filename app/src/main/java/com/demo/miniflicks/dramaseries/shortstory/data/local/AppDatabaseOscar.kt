package com.demo.miniflicks.dramaseries.shortstory.data.local
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.demo.miniflicks.dramaseries.shortstory.data.local.converter.ConvertersOscar
import com.demo.miniflicks.dramaseries.shortstory.data.local.dao.EpisodeDao
import com.demo.miniflicks.dramaseries.shortstory.data.local.dao.MovieCatalogDao
import com.demo.miniflicks.dramaseries.shortstory.model.CategoryEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.EpisodeEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.MovieCategoryCrossRefOscar
import com.demo.miniflicks.dramaseries.shortstory.model.MovieEntityOscar


// Nâng version lên 9 vì thêm trường lastPositionMs và durationMs vào bảng episodes
@Database(
    entities = [
        CategoryEntityOscar::class,
        MovieEntityOscar::class,
        MovieCategoryCrossRefOscar::class,
        EpisodeEntityOscar::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(ConvertersOscar::class)
abstract class AppDatabaseOscar : RoomDatabase() {
    abstract fun movieCatalogDaoOscar(): MovieCatalogDao
    abstract fun episodeDaoOscar(): EpisodeDao

    companion object {
        @Volatile
        private var INSTANCEOscar: AppDatabaseOscar? = null

        fun getInstanceOscar(contextOscar: Context): AppDatabaseOscar {
            if (INSTANCEOscar != null) {
                return INSTANCEOscar!!
            }
            synchronized(this) {
                if (INSTANCEOscar != null) {
                    return INSTANCEOscar!!
                }
                val instanceOscar = Room.databaseBuilder(
                    contextOscar.applicationContext,
                    AppDatabaseOscar::class.java,
                    "app_database_oscar"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCEOscar = instanceOscar
                return instanceOscar
            }
        }
    }
}