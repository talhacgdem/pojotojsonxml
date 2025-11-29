package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiJavaFile
import tr.talhacigdem.jetbrains.plugin.pojotojsonxml.generator.*
import tr.talhacigdem.jetbrains.plugin.pojotojsonxml.ui.OutputDialog
import tr.talhacigdem.jetbrains.plugin.pojotojsonxml.ui.RegenerateResult

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
class GenerateJsonXmlAction : AnAction()
{

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent)
    {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)

        val visible = psiFile is PsiJavaFile && psiFile.classes.isNotEmpty()
        e.presentation.isEnabledAndVisible = visible
    }

    override fun actionPerformed(e: AnActionEvent)
    {
        val project = e.project ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) as? PsiJavaFile ?: run {
            Messages.showErrorDialog(project, "This action works only on Java files.", "Error")
            return
        }

        object : Task.Backgroundable(project, "Generating JSON / XML from POJO", false)
        {
            override fun run(indicator: ProgressIndicator)
            {
                try
                {
                    val generationInput = ApplicationManager.getApplication().runReadAction<GenerationInput?> {
                        val classes = psiFile.classes.toList()
                        if (classes.isEmpty()) return@runReadAction null

                        val firstClass = classes.first()
                        val fields = FieldExtractor.extractFields(firstClass)

                        val fieldDefs = fields.map { f ->
                            FieldDef(
                                originalName = f.name,
                                jsonName = AnnotationProcessor.findJsonName(f),
                                jsonIgnored = AnnotationProcessor.isJsonIgnored(f),
                                xmlName = AnnotationProcessor.findXmlName(f),
                                xmlAttribute = AnnotationProcessor.isXmlAttribute(f),
                                type = FieldTyping.toTypeInfo(f)
                            )
                        }

                        GenerationInput(
                            className = firstClass.name ?: "Root",
                            fields = fieldDefs
                        )
                    }

                    if (generationInput == null)
                    {
                        showInfo(project, "No Java classes found in file.")
                        return
                    }

                    val settingsService = tr.talhacigdem.jetbrains.plugin.pojotojsonxml.settings.PluginSettingsState.getInstance()
                    val state = settingsService.state

                    val json = JsonGenerator.generate(generationInput, state)
                    val xml = XmlGenerator.generate(generationInput, state)

                    ApplicationManager.getApplication().invokeLater {
                        OutputDialog(
                            project,
                            json = json,
                            xml = xml,
                            onRegenerate = { req ->
                                val tempState = tr.talhacigdem.jetbrains.plugin.pojotojsonxml.settings.PluginSettingsState.State(
                                    includeNulls = req.includeNulls,
                                    defaultString = tr.talhacigdem.jetbrains.plugin.pojotojsonxml.settings.PluginSettingsState.getInstance().state.defaultString,
                                    defaultDate = tr.talhacigdem.jetbrains.plugin.pojotojsonxml.settings.PluginSettingsState.getInstance().state.defaultDate,
                                    indentSize = req.indentSize
                                )

                                val newJson = JsonGenerator.generate(generationInput, tempState, useAnnotations = req.useAnnotations)
                                val newXml = XmlGenerator.generate(generationInput, tempState, useAnnotations = req.useAnnotations)

                                RegenerateResult(json = newJson, xml = newXml)
                            }
                        ).show()
                    }
                } catch (ex: Exception)
                {
                    showError(project, "Generation failed: ${ex.message}")
                }
            }
        }.queue()
    }

    @Suppress("SameParameterValue")
    private fun showInfo(project: Project, msg: String)
    {
        ApplicationManager.getApplication().invokeLater {
            Messages.showInfoMessage(project, msg, "POJO to JSON/XML")
        }
    }

    private fun showError(project: Project, msg: String)
    {
        ApplicationManager.getApplication().invokeLater {
            Messages.showErrorDialog(project, msg, "POJO to JSON/XML")
        }
    }
}
