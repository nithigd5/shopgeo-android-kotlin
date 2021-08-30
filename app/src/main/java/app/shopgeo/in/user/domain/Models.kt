package app.shopgeo.`in`.user.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

//@ExperimentalSerializationApi
//data class Product(
//        var id : Int,
//
//        var Title : String,
//
//        var category : String,
//
//        var sub_category : String,
//
//        var Description : String?,
//
//        var Images : List<String>,
//
//        var Price : Double,
//
//        var MRP : Double,
//
//        var rating : Double,
//
//        var DateTime : Long,
//
//        var views : Int,
//
//        var Manufacturer : String?,
//
//        var vendor : String?,
//
//        var Size : List<String>?,
//
//        var Details :List<Detail>?
//)
@ExperimentalSerializationApi
@Serializable
data class Detail(
        var name : String,
        var value : String
)