package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.generator

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
data class FieldDef(
    val originalName: String,
    val jsonName: String?,
    val jsonIgnored: Boolean,
    val xmlName: String?,
    val xmlAttribute: Boolean,
    val type: TypeInfo
)