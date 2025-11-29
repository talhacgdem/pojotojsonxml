package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.generator

import com.intellij.psi.*

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
object FieldTyping
{
    fun toTypeInfo(field: PsiField): TypeInfo
    {
        return toTypeInfo(field.type)
    }

    fun toTypeInfo(type: PsiType): TypeInfo
    {
        val normalized = type.canonicalText

        when (normalized)
        {
            "boolean" -> return TypeInfo.TBoolean
            "int" -> return TypeInfo.TInt
            "long" -> return TypeInfo.TLong
            "double" -> return TypeInfo.TDouble
            "java.lang.Boolean" -> return TypeInfo.TBoolean
            "java.lang.Integer" -> return TypeInfo.TInt
            "java.lang.Long" -> return TypeInfo.TLong
            "java.lang.Double" -> return TypeInfo.TDouble
            "java.lang.String" -> return TypeInfo.TString
            "java.math.BigDecimal" -> return TypeInfo.TBigDecimal
            "java.util.UUID" -> return TypeInfo.TUUID
            "java.time.LocalDate" -> return TypeInfo.TLocalDate
            "java.time.LocalDateTime" -> return TypeInfo.TLocalDateTime
        }

        if (normalized.startsWith("java.util.Optional<"))
        {
            val param = (type as? PsiClassType)?.parameters?.firstOrNull()
            val wrapped = param?.let { toTypeInfo(it) } ?: TypeInfo.TUnknown
            return TypeInfo.TOptional(wrapped)
        }

        if (normalized.startsWith("java.util.List<") || normalized.startsWith("kotlin.collections.List<"))
        {
            val param = (type as? PsiClassType)?.parameters?.firstOrNull()
            return TypeInfo.TList(param?.let { toTypeInfo(it) } ?: TypeInfo.TUnknown)
        }
        if (normalized.startsWith("java.util.Set<") || normalized.startsWith("kotlin.collections.Set<"))
        {
            val param = (type as? PsiClassType)?.parameters?.firstOrNull()
            return TypeInfo.TSet(param?.let { toTypeInfo(it) } ?: TypeInfo.TUnknown)
        }
        if (normalized.startsWith("java.util.Map<") || normalized.startsWith("kotlin.collections.Map<"))
        {
            val params = (type as? PsiClassType)?.parameters ?: emptyArray()
            val keyT = params.getOrNull(0)?.let { toTypeInfo(it) } ?: TypeInfo.TUnknown
            val valT = params.getOrNull(1)?.let { toTypeInfo(it) } ?: TypeInfo.TUnknown
            return TypeInfo.TMap(keyT, valT)
        }

        val cls = (type as? PsiClassType)?.resolve()
        if (cls != null)
        {
            if (cls.isEnum)
            {
                val constants = cls.fields.filterIsInstance<PsiEnumConstant>().map { it.name }
                return TypeInfo.TEnum(cls.name ?: "Enum", constants)
            }
            if (cls.isInterface) return TypeInfo.TUnknown

            val rootAnn = cls.modifierList?.annotations?.firstOrNull {
                val q = it.qualifiedName
                q == "jakarta.xml.bind.annotation.XmlRootElement" || q == "javax.xml.bind.annotation.XmlRootElement"
            }
            val xmlRootName = rootAnn?.findAttributeValue("name")?.text?.trim('"')

            val pojoFields = cls.allFields
                .filter { !it.hasModifierProperty(PsiModifier.STATIC) }
                .filter { !it.hasModifierProperty(PsiModifier.TRANSIENT) }
                .map { f ->
                    FieldDef(
                        originalName = f.name,
                        jsonName = AnnotationProcessor.findJsonName(f),
                        jsonIgnored = AnnotationProcessor.isJsonIgnored(f),
                        xmlName = AnnotationProcessor.findXmlName(f),
                        xmlAttribute = AnnotationProcessor.isXmlAttribute(f),
                        type = toTypeInfo(f.type)
                    )
                }

            return TypeInfo.TObject(
                className = cls.name ?: "Object",
                fields = pojoFields,
                xmlRootName = xmlRootName
            )
        }
        return TypeInfo.TUnknown
    }
}
