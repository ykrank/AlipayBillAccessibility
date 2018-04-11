package com.github.ykrank.alipaybillaccessibility

import android.content.Context
import android.content.SharedPreferences
import com.github.ykrank.androidtools.data.BasePreferences
import com.github.ykrank.androidtools.data.PreferenceDelegates


/**
 * A helper class for retrieving the general preferences from [SharedPreferences].
 */
class GeneralPreferencesImpl(context: Context, sharedPreferences: SharedPreferences) : BasePreferences(context, sharedPreferences), GeneralPreferences {
    override var lastTime: Long by PreferenceDelegates.long(
            mContext.getString(R.string.pref_key_last_time), 0L)
}

interface GeneralPreferences {
    var lastTime: Long
}

class GeneralPreferencesManager(private val mPreferencesProvider: GeneralPreferences) : GeneralPreferences by mPreferencesProvider