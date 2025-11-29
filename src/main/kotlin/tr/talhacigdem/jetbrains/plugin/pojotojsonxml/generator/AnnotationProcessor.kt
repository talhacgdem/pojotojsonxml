package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.generator

import com.intellij.psi.PsiField

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
object AnnotationProcessor
{

    fun findJsonName(field: PsiField): String?
    {
        val ann = field.modifierList?.annotations?.firstOrNull {
            val q = it.qualifiedName
            q == "com.fasterxml.jackson.annotation.JsonProperty" || q == "org.codehaus.jackson.annotate.JsonProperty"
        } ?: return null

        val value = ann.findAttributeValue("value")?.text?.trim('"')
        return value
    }

    fun isJsonIgnored(field: PsiField): Boolean
    {
        return field.modifierList?.annotations?.any {
            val q = it.qualifiedName
            q == "com.fasterxml.jackson.annotation.JsonIgnore"
        } ?: false
    }

    fun findXmlName(field: PsiField): String?
    {
        val ann = field.modifierList?.annotations?.firstOrNull {
            val q = it.qualifiedName
            q == "jakarta.xml.bind.annotation.XmlElement" || q == "javax.xml.bind.annotation.XmlElement"
        } ?: return null
        val value = ann.findAttributeValue("name")?.text?.trim('"')
        return value
    }

    fun isXmlAttribute(field: PsiField): Boolean
    {
        return field.modifierList?.annotations?.any {
            val q = it.qualifiedName
            q == "jakarta.xml.bind.annotation.XmlAttribute" || q == "javax.xml.bind.annotation.XmlAttribute"
        } ?: false
    }
}
