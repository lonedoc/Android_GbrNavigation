package kobramob.rubeg38.ru.gbrnavigation.resource

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal class DataBase(context: Context) : SQLiteOpenHelper(context, "gbrDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table StatusList (" + "status text" + ")")
        db.execSQL("create table RouteServerList (" + "ip text" + ")")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}