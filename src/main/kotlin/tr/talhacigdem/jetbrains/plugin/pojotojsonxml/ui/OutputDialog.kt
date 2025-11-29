package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.ui

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
class OutputDialog(
    project: Project?,
    private var json: String,
    private var xml: String,
    private val onRegenerate: (RegenerateRequest) -> RegenerateResult
) : DialogWrapper(project)
{

    private lateinit var jsonArea: JTextArea
    private lateinit var xmlArea: JTextArea
    private lateinit var useAnnotationsCheck: JCheckBox
    private lateinit var includeNullsCheck: JCheckBox
    private lateinit var indentSpinner: JSpinner

    init
    {
        init()
        title = "POJO → JSON / XML"
        setSize(900, 600)
    }

    override fun createCenterPanel(): JComponent
    {
        val root = JPanel(BorderLayout())

        val topBar = JPanel(BorderLayout())
        topBar.add(buildSettingsPanel(), BorderLayout.WEST)
        topBar.add(buildActionsPanel(), BorderLayout.EAST)

        val tabs = JTabbedPane()
        jsonArea = createTextArea(json)
        xmlArea = createTextArea(xml)
        tabs.addTab("JSON", JBScrollPane(jsonArea))
        tabs.addTab("XML", JBScrollPane(xmlArea))

        root.add(topBar, BorderLayout.NORTH)
        root.add(tabs, BorderLayout.CENTER)

        return root
    }

    private fun buildSettingsPanel(): JComponent
    {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            gridx = 0; gridy = 0
            anchor = GridBagConstraints.WEST
            insets = Insets(4, 4, 4, 8)
        }

        useAnnotationsCheck = JCheckBox("Use annotations", true)
        includeNullsCheck = JCheckBox("Include null fields", false)

        indentSpinner = JSpinner(SpinnerNumberModel(2, 0, 8, 1))

        panel.add(useAnnotationsCheck, gbc)
        gbc.gridx++
        panel.add(includeNullsCheck, gbc)
        gbc.gridx++
        panel.add(JLabel("Indent:"), gbc)
        gbc.gridx++
        panel.add(indentSpinner, gbc)
        gbc.gridx++
        val regenerateBtn = JButton("Regenerate")
        regenerateBtn.addActionListener {
            regenerate()
        }
        panel.add(regenerateBtn, gbc)

        return panel
    }

    private fun buildActionsPanel(): JComponent
    {
        val panel = JPanel(FlowLayout(FlowLayout.RIGHT, 8, 4))
        val copyJson = JButton("Copy JSON")
        val copyXml = JButton("Copy XML")
        val saveBtn = JButton("Save")

        copyJson.addActionListener {
            CopyPasteManager.getInstance().setContents(StringSelection(jsonArea.text))
        }
        copyXml.addActionListener {
            CopyPasteManager.getInstance().setContents(StringSelection(xmlArea.text))
        }
        saveBtn.addActionListener { saveToFile() }

        panel.add(copyJson)
        panel.add(copyXml)
        panel.add(saveBtn)
        return panel
    }

    private fun createTextArea(content: String): JTextArea
    {
        return JTextArea(content).apply {
            isEditable = false
            font = Font(Font.MONOSPACED, Font.PLAIN, 13)
            lineWrap = false
            wrapStyleWord = false
        }
    }

    private fun regenerate()
    {
        val req = RegenerateRequest(
            useAnnotations = useAnnotationsCheck.isSelected,
            includeNulls = includeNullsCheck.isSelected,
            indentSize = (indentSpinner.value as Int)
        )
        val result = onRegenerate(req)
        json = result.json
        xml = result.xml
        jsonArea.text = json
        xmlArea.text = xml
    }

    private fun saveToFile()
    {
        val chooser = JFileChooser().apply {
            dialogTitle = "JSON/XML Save"
            isMultiSelectionEnabled = false
            fileFilter = FileNameExtensionFilter("JSON & XML", "json", "xml")
        }
        val ret = chooser.showSaveDialog(contentPanel)
        if (ret == JFileChooser.APPROVE_OPTION)
        {
            val file = chooser.selectedFile
            val name = file.name.lowercase()
            val toWrite =
                when
                {
                    name.endsWith(".json") -> json
                    name.endsWith(".xml") -> xml
                    else ->
                    {
                        val base = file.absolutePath
                        File("$base.json").writeText(json)
                        File("$base.xml").writeText(xml)
                        return
                    }
                }
            file.writeText(toWrite)
        }
    }
}

data class RegenerateRequest(
    val useAnnotations: Boolean,
    val includeNulls: Boolean,
    val indentSize: Int
)

data class RegenerateResult(
    val json: String,
    val xml: String
)
