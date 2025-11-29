package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.generator

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
data class GenerationInput(
    val className: String,
    val fields: List<FieldDef>,
    val xmlRootName: String? = null
)