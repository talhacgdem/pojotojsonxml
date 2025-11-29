package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
@State(name = "PojoToJsonXmlSettings", storages = [Storage("pojoToJsonXml.xml")])
class PluginSettingsState : PersistentStateComponent<PluginSettingsState.State> {

    data class State(
        var includeNulls: Boolean = false,
        var defaultString: String = "sample",
        var defaultDate: String = "2025-01-01",
        var indentSize: Int = 2
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(): PluginSettingsState {
            val app = com.intellij.openapi.application.ApplicationManager.getApplication()
            return app.getService(PluginSettingsState::class.java)
                ?: error("PluginSettingsState service not available")
        }
    }
}
