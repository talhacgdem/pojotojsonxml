package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JTextField
import javax.swing.BoxLayout

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
class PluginSettingsConfigurable : Configurable {

    private val panel = JPanel()
    private val includeNulls = JCheckBox("Include null fields")
    private val defaultString = JTextField()
    private val defaultDate = JTextField()
    private val indentSize = JTextField()

    init {
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.add(includeNulls)
        panel.add(JLabel("Default string value:"))
        panel.add(defaultString)
        panel.add(JLabel("Default date value:"))
        panel.add(defaultDate)
        panel.add(JLabel("Indent size:"))
        panel.add(indentSize)
    }

    override fun createComponent(): JComponent
    {
        val state = PluginSettingsState.getInstance().state
        includeNulls.isSelected = state.includeNulls
        defaultString.text = state.defaultString
        defaultDate.text = state.defaultDate
        indentSize.text = state.indentSize.toString()
        return panel
    }

    override fun isModified(): Boolean {
        val state = PluginSettingsState.getInstance().state
        return (state.includeNulls != includeNulls.isSelected) ||
                (state.defaultString != defaultString.text) ||
                (state.defaultDate != defaultDate.text) ||
                (state.indentSize.toString() != indentSize.text)
    }

    override fun apply() {
        val state = PluginSettingsState.getInstance().state
        state.includeNulls = includeNulls.isSelected
        state.defaultString = defaultString.text
        state.defaultDate = defaultDate.text
        state.indentSize = indentSize.text.toIntOrNull() ?: state.indentSize
    }

    override fun getDisplayName(): String = "POJO to JSON/XML"
}
