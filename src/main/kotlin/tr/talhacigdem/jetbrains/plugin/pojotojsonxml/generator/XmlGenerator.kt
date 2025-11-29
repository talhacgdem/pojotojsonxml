package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.generator

import tr.talhacigdem.jetbrains.plugin.pojotojsonxml.settings.PluginSettingsState

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
object XmlGenerator
{
    fun generate(
        input: GenerationInput,
        state: PluginSettingsState.State,
        useAnnotations: Boolean = true
    ): String
    {
        val indentUnit = " ".repeat(state.indentSize)

        val rootObj = TypeInfo.TObject(input.className, input.fields, xmlRootName = null)

        val rootName = if (useAnnotations)
        {
            rootObj.xmlRootName?.takeIf { it.isNotBlank() } ?: input.className
        } else
        {
            input.className
        }

        val content = buildObjectXml(
            elementName = rootName,
            obj = TypeInfo.TObject(input.className, input.fields, xmlRootName = rootName),
            state = state,
            useAnnotations = useAnnotations,
            indentUnit = indentUnit,
            depth = 0,
            visited = mutableSetOf()
        )

        return content
    }

    private fun buildObjectXml(
        elementName: String,
        obj: TypeInfo.TObject,
        state: PluginSettingsState.State,
        useAnnotations: Boolean,
        indentUnit: String,
        depth: Int,
        visited: MutableSet<String>
    ): String
    {
        val baseIndent = indentUnit.repeat(depth)

        val id = obj.className
        if (!visited.add(id) || depth > 6) return "$baseIndent<$elementName/>"

        val attrPairs = obj.fields
            .filter { if (useAnnotations) !it.jsonIgnored else true }
            .filter { useAnnotations && it.xmlAttribute }
            .map { f ->
                val attrName = if (useAnnotations) (f.xmlName ?: f.originalName) else f.originalName
                val attrVal = xmlScalarText(f.type, state)
                attrName to attrVal
            }

        val elementFields = obj.fields
            .filter { if (useAnnotations) !it.jsonIgnored else true }
            .filter { !(useAnnotations && it.xmlAttribute) }

        val attrsRendered = if (attrPairs.isNotEmpty())
        {
            " " + attrPairs.joinToString(" ") { "${it.first}=\"${escapeXml(it.second)}\"" }
        } else ""

        if (elementFields.isEmpty())
        {
            visited.remove(id)
            return "$baseIndent<$elementName$attrsRendered/>"
        }

        val inner = buildString {
            elementFields.forEach { f ->
                val childName = if (useAnnotations) (f.xmlName ?: f.originalName) else f.originalName
                append(renderXmlField(childName, f.type, state, useAnnotations, indentUnit, depth + 1, visited))
            }
        }

        visited.remove(id)
        return "$baseIndent<$elementName$attrsRendered>\n$inner$baseIndent</$elementName>\n"
    }

    private fun renderXmlField(
        name: String,
        type: TypeInfo,
        state: PluginSettingsState.State,
        useAnnotations: Boolean,
        indentUnit: String,
        depth: Int,
        visited: MutableSet<String>
    ): String
    {
        val indent = indentUnit.repeat(depth)
        val nextDepth = depth + 1

        return when (type)
        {
            TypeInfo.TBoolean, TypeInfo.TInt, TypeInfo.TLong, TypeInfo.TDouble,
            TypeInfo.TBigDecimal, TypeInfo.TString, TypeInfo.TUUID,
            TypeInfo.TLocalDate, TypeInfo.TLocalDateTime, is TypeInfo.TEnum ->
            {
                val v = xmlScalarText(type, state)
                "$indent<$name>${escapeXml(v)}</$name>\n"
            }

            is TypeInfo.TOptional ->
            {
                renderXmlField(name, type.wrapped, state, useAnnotations, indentUnit, depth, visited)
            }

            is TypeInfo.TList ->
            {
                val itemXml = renderXmlField("item", type.elementType, state, useAnnotations, indentUnit, nextDepth, visited)
                "$indent<$name>\n$itemXml$indent</$name>\n"
            }

            is TypeInfo.TSet ->
            {
                val itemXml = renderXmlField("item", type.elementType, state, useAnnotations, indentUnit, nextDepth, visited)
                "$indent<$name>\n$itemXml$indent</$name>\n"
            }

            is TypeInfo.TMap ->
            {
                val keyStr = when (type.keyType)
                {
                    is TypeInfo.TString -> "key"
                    is TypeInfo.TInt, is TypeInfo.TLong, is TypeInfo.TDouble -> "1"
                    is TypeInfo.TUUID -> "00000000-0000-0000-0000-000000000000"
                    else -> "key"
                }
                val valueXml = renderXmlField("value", type.valueType, state, useAnnotations, indentUnit, nextDepth, visited)
                "$indent<$name>\n$indent$indentUnit<entry key=\"${escapeXml(keyStr)}\">\n$valueXml$indent$indentUnit</entry>\n$indent</$name>\n"
            }

            is TypeInfo.TObject ->
            {
                buildObjectXml(
                    elementName = name,
                    obj = type,
                    state = state,
                    useAnnotations = useAnnotations,
                    indentUnit = indentUnit,
                    depth = depth,
                    visited = visited
                )
            }

            TypeInfo.TUnknown ->
            {
                val v = if (state.includeNulls) "" else state.defaultString
                "$indent<$name>${escapeXml(v)}</$name>\n"
            }
        }
    }

    private fun xmlScalarText(type: TypeInfo, state: PluginSettingsState.State): String =
        when (type)
        {
            TypeInfo.TBoolean -> if (state.includeNulls) "" else "false"
            TypeInfo.TInt -> if (state.includeNulls) "" else "0"
            TypeInfo.TLong -> if (state.includeNulls) "" else "0"
            TypeInfo.TDouble -> if (state.includeNulls) "" else "0.0"
            TypeInfo.TBigDecimal -> if (state.includeNulls) "" else "0.00"
            TypeInfo.TString -> if (state.includeNulls) "" else state.defaultString
            TypeInfo.TUUID -> if (state.includeNulls) "" else "00000000-0000-0000-0000-000000000000"
            TypeInfo.TLocalDate -> if (state.includeNulls) "" else state.defaultDate
            TypeInfo.TLocalDateTime -> if (state.includeNulls) "" else state.defaultDate + "T00:00:00"
            is TypeInfo.TEnum -> if (state.includeNulls) "" else (type.constants.firstOrNull() ?: "UNKNOWN")
            else -> ""
        }

    private fun escapeXml(s: String): String =
        s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")


    private fun xmlElement(
        name: String,
        type: TypeInfo,
        state: PluginSettingsState.State,
        useAnnotations: Boolean,
        indent: String,
        depth: Int,
        visited: MutableSet<String>,
        asAttribute: Boolean = false
    ): String
    {
        val curIndent = indent.repeat(depth)

        if (asAttribute)
        {
            val v = xmlScalarText(type, state)
            return "$curIndent<$name attr=\"$v\"></$name>\n"
        }

        return when (type)
        {
            TypeInfo.TBoolean, TypeInfo.TInt, TypeInfo.TLong, TypeInfo.TDouble,
            TypeInfo.TBigDecimal, TypeInfo.TString, TypeInfo.TUUID,
            TypeInfo.TLocalDate, TypeInfo.TLocalDateTime, is TypeInfo.TEnum ->
            {
                val v = xmlScalarText(type, state)
                "$curIndent<$name>$v</$name>\n"
            }

            is TypeInfo.TOptional ->
            {
                xmlElement(name, type.wrapped, state, useAnnotations, indent, depth, visited)
            }

            is TypeInfo.TList ->
            {
                val inner = xmlElement("item", type.elementType, state, useAnnotations, indent, depth + 1, visited)
                "$curIndent<$name>\n$inner$curIndent</$name>\n"
            }

            is TypeInfo.TSet ->
            {
                val inner = xmlElement("item", type.elementType, state, useAnnotations, indent, depth + 1, visited)
                "$curIndent<$name>\n$inner$curIndent</$name>\n"
            }

            is TypeInfo.TMap ->
            {
                val keyStr = when (type.keyType)
                {
                    is TypeInfo.TString -> "key"
                    is TypeInfo.TInt, is TypeInfo.TLong, is TypeInfo.TDouble -> "1"
                    is TypeInfo.TUUID -> "00000000-0000-0000-0000-000000000000"
                    else -> "key"
                }
                val valueXml = xmlElement("value", type.valueType, state, useAnnotations, indent, depth + 1, visited)
                "$curIndent<$name>\n$curIndent$indent<entry key=\"$keyStr\">\n$valueXml$curIndent$indent</entry>\n$curIndent</$name>\n"
            }

            is TypeInfo.TObject ->
            {
                val id = type.className
                if (!visited.add(id) || depth > 6) return "$curIndent<$name/>\n"
                val inner = buildString {
                    type.fields.forEach { f ->
                        if (useAnnotations && f.jsonIgnored) return@forEach
                        val childName = if (useAnnotations) (f.xmlName ?: f.originalName) else f.originalName
                        append(xmlElement(childName, f.type, state, useAnnotations, indent, depth + 1, visited, asAttribute = false))
                    }
                }
                visited.remove(id)
                "$curIndent<$name>\n$inner$curIndent</$name>\n"
            }

            TypeInfo.TUnknown -> "$curIndent<$name>${if (state.includeNulls) "" else state.defaultString}</$name>\n"
        }
    }
}