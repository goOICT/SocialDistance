package ai.kun.socialdistancealarm.ui.settings

import ai.kun.socialdistancealarm.R
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class SettingsActivityFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }
}