package net.tactware.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.tactware.worldweaver.domain.ActiveContext
import net.tactware.worldweaver.domain.ActiveContextRepository
import java.util.prefs.Preferences

internal class PreferenceActiveContextRepository : ActiveContextRepository {
    private val preferences = Preferences.userRoot().node(PREF_NODE)
    private val state = MutableStateFlow(read())

    override fun observe(): Flow<ActiveContext> = state.asStateFlow()

    override fun get(): ActiveContext = state.value

    override fun setActiveWorldId(worldId: String?) {
        write(WORLD_KEY, worldId)
        state.value = state.value.copy(activeWorldId = worldId)
    }

    override fun setActiveCampaignId(campaignId: String?) {
        write(CAMPAIGN_KEY, campaignId)
        state.value = state.value.copy(activeCampaignId = campaignId)
    }

    override fun setActiveSessionId(sessionId: String?) {
        write(SESSION_KEY, sessionId)
        state.value = state.value.copy(activeSessionId = sessionId)
    }

    private fun read(): ActiveContext {
        return ActiveContext(
            activeWorldId = readValue(WORLD_KEY),
            activeCampaignId = readValue(CAMPAIGN_KEY),
            activeSessionId = readValue(SESSION_KEY),
        )
    }

    private fun readValue(key: String): String? {
        val stored = preferences.get(key, "")
        return stored.takeIf { it.isNotBlank() }
    }

    private fun write(key: String, value: String?) {
        if (value.isNullOrBlank()) {
            preferences.remove(key)
        } else {
            preferences.put(key, value)
        }
    }

    private companion object {
        const val PREF_NODE = "net.tactware.worldweaver"
        const val WORLD_KEY = "active_world_id"
        const val CAMPAIGN_KEY = "active_campaign_id"
        const val SESSION_KEY = "active_session_id"
    }
}
