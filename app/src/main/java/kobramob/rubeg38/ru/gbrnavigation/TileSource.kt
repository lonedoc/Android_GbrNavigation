package kobramob.rubeg38.ru.gbrnavigation

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex

class TileSource {

    val GoogleSat: OnlineTileSourceBase = object : XYTileSource(
        "Google-Sat",
        0,
        19,
        256,
        ".png",
        arrayOf("http://mt0.google.com", "http://mt1.google.com", "http://mt2.google.com", "http://mt3.google.com")
    ) {
        override fun getTileURLString(aTile: Long): String {
            return baseUrl + "/vt/lyrs=s&x=" + MapTileIndex.getX(aTile) + "&y=" + MapTileIndex.getY(aTile) + "&z=" + MapTileIndex.getZoom(aTile)
        }
    }

    val GoogleHybrid: OnlineTileSourceBase = object : XYTileSource(
        "Google-Hybrid",
        0,
        19,
        256,
        ".png",
        arrayOf("http://mt0.google.com", "http://mt1.google.com", "http://mt2.google.com", "http://mt3.google.com")
    ) {
        override fun getTileURLString(aTile: Long): String {
            return baseUrl + "/vt/lyrs=y&x=" + MapTileIndex.getX(aTile) + "&y=" + MapTileIndex.getY(aTile) + "&z=" + MapTileIndex.getZoom(aTile)
        }
    }

    val GoogleRoads: OnlineTileSourceBase = object : XYTileSource(
        "Google-Roads",
        0,
        19,
        256,
        ".png",
        arrayOf("http://mt0.google.com", "http://mt1.google.com", "http://mt2.google.com", "http://mt3.google.com")
    ) {
        override fun getTileURLString(aTile: Long): String {
            return baseUrl + "/vt/lyrs=m&x=" + MapTileIndex.getX(aTile) + "&y=" + MapTileIndex.getY(aTile) + "&z=" + MapTileIndex.getZoom(aTile)
        }
    }

    val YandexMaps: OnlineTileSourceBase = object : XYTileSource(
        "Yandex.Maps",
        0,
        19,
        256,
        ".png",
        arrayOf("http://vec01.maps.yandex.net/", "http://vec02.maps.yandex.net/", "http://vec03.maps.yandex.net/", "http://vec04.maps.yandex.net/")
    ) {
        override fun getTileURLString(aTile: Long): String {
            return baseUrl + "tiles?l=map&v=4.24.2&x=" + MapTileIndex.getX(aTile) + "&y=" + MapTileIndex.getY(aTile) + "&z=" + MapTileIndex.getZoom(aTile)
        }
    }
}
