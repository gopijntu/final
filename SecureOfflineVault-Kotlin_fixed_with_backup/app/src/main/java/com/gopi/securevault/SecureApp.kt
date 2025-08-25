package com.gopi.securevault

import android.app.Application
import net.sqlcipher.database.SQLiteDatabase

class SecureApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Load SQLCipher native libraries
        SQLiteDatabase.loadLibs(this)
    }
}
