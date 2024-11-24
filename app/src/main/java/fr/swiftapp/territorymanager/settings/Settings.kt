package fr.swiftapp.territorymanager.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private object PreferencesKeys {
    val NAMES = stringPreferencesKey("names")
    val API_URL = stringPreferencesKey("api_url")
}

suspend fun getNameList(context: Context): String {
    return context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NAMES] ?: ""
    }.firstOrNull() ?: ""
}

fun getNameListAsFlow(context: Context): Flow<String?> {
    return context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NAMES] ?: ""
    }
}

suspend fun updateNamesList(context: Context, names: String) {
    context.dataStore.edit { settings ->
        settings[PreferencesKeys.NAMES] = names
    }
}

suspend fun getApiUrl(context: Context): String {
    return context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.API_URL] ?: ""
    }.firstOrNull() ?: ""
}

fun getApiUrlAsFlow(context: Context): Flow<String?> {
    return context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.API_URL] ?: ""
    }
}

suspend fun updateApiUrl(context: Context, url: String) {
    context.dataStore.edit { settings ->
        settings[PreferencesKeys.API_URL] = url
    }
}
