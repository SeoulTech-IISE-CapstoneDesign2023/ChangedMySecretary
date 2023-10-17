package com.design.model.location

data class Location(
    val searchPoiInfo : SearchPoiInfo
)

data class SearchPoiInfo (
    val count: String,
    val page: String,
    val pois: Pois,
    val totalCount: String
)

data class Pois(
    val poi: List<Poi>
)

data class Poi(
    val frontLat: String,
    val frontLon: String,
    val id: String,
    val name: String,
    val newAddressList: NewAddressList,
    val noorLat: String,
    val noorLon: String,
    val roadName: String,
)

data class NewAddressList(
    val newAddress: List<NewAddres>
)

data class NewAddres(
    val centerLat: String,
    val centerLon: String,
    val frontLat: String,
    val frontLon: String,
    val fullAddressRoad: String,
    val roadId: String,
    val roadName: String
)
