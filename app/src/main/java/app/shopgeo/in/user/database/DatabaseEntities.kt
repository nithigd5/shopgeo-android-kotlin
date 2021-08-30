package app.shopgeo.`in`.user.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.shopgeo.`in`.user.domain.Detail
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel

@Entity(tableName= "Products")
data class DatabaseProduct(
    @PrimaryKey( autoGenerate = true)
        var id : Int,

    @ColumnInfo(name="Title")
        var Title : String,

    @ColumnInfo(name="Category")
        var category : String,

    @ColumnInfo(name="sub_category")
        var sub_category : String,

    @ColumnInfo(name="Description")
        var Description : String?,

    @ColumnInfo(name="Images")
        var Images : List<String>,

    @ColumnInfo(name="Price")
        var Price : Double,

    @ColumnInfo(name="MRP")
        var MRP : Double,

    @ColumnInfo(name="Rating")
        var rating : Double,

    @ColumnInfo(name="Date")
        var DateTime : Long,

    @ColumnInfo(name="views")
        var views : Int,

    @ColumnInfo(name="Manufacturer")
        var Manufacturer : String?,

    @ColumnInfo(name="vendor")
        var vendor : String?,

    @ColumnInfo(name="Size")
        var Size : List<String>?,

    @ColumnInfo(name="Details")
        var Details : List<Detail>?,

    @ColumnInfo(name="in_stock")
        var in_stock : Int?,
)
//@ExperimentalSerializationApi
//fun List<DatabaseProduct>.asDomainModel(): List<Product>{
//        return map{
//                Product(
//                        id = it.id,
//                        Title = it.Title,
//                        sub_category =  it.sub_category,
//                        category =  it.category,
//                        Description =  it.Description,
//                        Images = it.Images,
//                        Price = it.Price,
//                        DateTime = it.DateTime,
//                        views = it.views,
//                        rating = it.rating,
//                        MRP = it.MRP,
//                        Details = it.Details,
//                        Manufacturer = it.Manufacturer,
//                        vendor = it.vendor,
//                        Size = it.Size
//                )
//        }
//}
//@ExperimentalSerializationApi
//fun DatabaseProduct.asDomainModel() : Product{
//        return  Product(
//                id = id,
//                Title = Title,
//                sub_category =  sub_category,
//                category =  category,
//                Images = Images,
//                Price = Price,
//                DateTime = DateTime,
//                views = views,
//                MRP = MRP,
//                rating = rating,
//                Description = Description,
//                vendor = vendor,
//                Manufacturer = Manufacturer,
//                Details = Details,
//                Size = Size
//        )
//}
@Entity(tableName="banners")
data class Banners(
        @PrimaryKey( autoGenerate = true)
        var id : Int,

        var image : String,

        var image_type : String,

        var title : String,
        
        var image_content_type : String,

        var extra : String?
)

fun List<Banners>.asSlideMode(): List<SlideModel> {
        return map {
                SlideModel(
                        imageUrl = it.image,
                        scaleType = ScaleTypes.FIT
                )
        }
}

