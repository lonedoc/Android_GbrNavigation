package newVersion.utils

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex

class TileSources {

    val googleSat: OnlineTileSourceBase = object : XYTileSource(
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

    val googleHybrid: OnlineTileSourceBase = object : XYTileSource(
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

    val googleRoads: OnlineTileSourceBase = object : XYTileSource(
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
}
