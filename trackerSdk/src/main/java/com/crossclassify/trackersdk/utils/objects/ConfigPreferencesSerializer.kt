package com.crossclassify.trackersdk.utils.objects

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.codelab.android.datastore.ConfigPreferences
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

const val DATA_STORE_FILE_NAME = "config_pref.pb"

private val Context.userPreferencesStore: DataStore<ConfigPreferences> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = ConfigPreferencesSerializer
)

object ConfigPreferencesSerializer: Serializer<ConfigPreferences> {
        override val defaultValue: ConfigPreferences = ConfigPreferences.getDefaultInstance()
        override suspend fun readFrom(input: InputStream): ConfigPreferences {
            try {
                return ConfigPreferences.parseFrom(input)
            } catch (exception: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", exception)
            }
        }

        override suspend fun writeTo(t: ConfigPreferences, output: OutputStream) = t.writeTo(output)
}