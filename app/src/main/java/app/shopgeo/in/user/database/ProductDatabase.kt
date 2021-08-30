package app.shopgeo.`in`.user.database

import android.content.Context
import androidx.room.*
import app.shopgeo.`in`.user.domain.Detail
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Database(entities = [DatabaseProduct::class, Banners::class], version=4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ProductDatabase : RoomDatabase() {
    abstract  val productsDao : ProductsDao
    abstract  val bannersDao : BannersDao
    companion object {
        @Volatile
        private  var INSTANCE : ProductDatabase? = null

        @InternalCoroutinesApi
        fun getInstance(context: Context) : ProductDatabase {
            synchronized(this){
                var instance = INSTANCE
                if(instance==null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ProductDatabase::class.java,
                        "Products"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
class Converters{
    @TypeConverter
    fun fromList(value:List<String>?) =
        value?.let{
            Json.encodeToString(it)
        }

    @TypeConverter
    fun toList(value: String?) =
        value?.let{
            Json.decodeFromString<List<String>>(it)
        }

    @TypeConverter
    fun fromDetailList(value:List<Detail>?) =
        value?.let{
             Json.encodeToString(value)
        }

    @TypeConverter
    fun toDetailList(value:String?) =
        value?.let{
             Json.decodeFromString<List<Detail>>(value)
        }
}
