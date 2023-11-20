package fr.swiftapp.territorymanager.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private object PreferencesKeys {
    val NAMES = stringPreferencesKey("names")
}

suspend fun getNameList(context: Context): String {
    return context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NAMES] ?: ""
    }.first().toString()
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