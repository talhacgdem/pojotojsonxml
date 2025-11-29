package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.generator

import tr.talhacigdem.jetbrains.plugin.pojotojsonxml.settings.PluginSettingsState

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
object JsonGenerator
{
    fun generate(
        input: GenerationInput,
        state: PluginSettingsState.State,
        useAnnotations: Boolean = true
    ): String
    {
        return renderObjectFields(
            fields = input.fields,
            state = state,
            useAnnotations = useAnnotations,
            depth = 0
        )
    }

    private fun isMultilineBlock(s: String): Boolean =
        s.startsWith("{\n") || s.startsWith("[\n")

    private fun indentMultilineBlock(block: String, indent: String): String =
        block.lines().joinToString("\n") { line ->
            if (line.isEmpty()) line else indent + line.trimStart()
        }

    private fun renderMultilineAfterKey(
        key: String,
        valueBlock: String,
        keyIndent: String
    ): String
    {
        val lines = valueBlock.lines()
        if (lines.isEmpty()) return "$keyIndent\"$key\": $valueBlock"

        val first = "$keyIndent\"$key\": ${lines.first()}"
        val middle = if (lines.size > 2) lines.drop(1).dropLast(1).joinToString("\n") else ""
        val last = if (lines.size > 1) lines.last() else ""

        return buildString {
            append(first)
            if (middle.isNotEmpty())
            {
                append("\n")
                append(middle)
            }
            if (last.isNotEmpty())
            {
                append("\n")
                append(last)
            }
        }
    }

    private fun renderSingleElementArray(
        elementRendered: String,
        state: PluginSettingsState.State,
        depth: Int
    ): String
    {
        val baseIndent = " ".repeat(state.indentSize * depth)
        val innerIndent = " ".repeat(state.indentSize * (depth + 1))
        val elemOut = if (isMultilineBlock(elementRendered))
        {
            indentMultilineBlock(elementRendered, innerIndent)
        } else
        {
            "$innerIndent$elementRendered"
        }
        return "[\n$elemOut\n$baseIndent]"
    }

    private fun renderKvLine(
        key: String,
        renderedValue: String,
        state: PluginSettingsState.State,
        depth: Int
    ): String
    {
        val innerIndent = " ".repeat(state.indentSize * (depth + 1))
        return if (!isMultilineBlock(renderedValue))
        {
            "$innerIndent\"$key\": $renderedValue"
        } else
        {
            renderMultilineAfterKey(key, renderedValue, innerIndent)
        }
    }

    private fun renderObjectFields(
        fields: List<FieldDef>,
        state: PluginSettingsState.State,
        useAnnotations: Boolean,
        depth: Int,
        visited: MutableSet<String> = mutableSetOf()
    ): String
    {
        val baseIndent = " ".repeat(state.indentSize * depth)
        val kvLines = fields
            .filter { if (useAnnotations) !it.jsonIgnored else true }
            .map { f ->
                val key = if (useAnnotations) (f.jsonName ?: f.originalName) else f.originalName
                val value = jsonValueFor(f.type, state, useAnnotations, depth + 1, visited)
                renderKvLine(key, value, state, depth)
            }

        return buildString {
            append("{\n")
            append(kvLines.joinToString(",\n"))
            append("\n")
            append(baseIndent)
            append("}")
        }
    }

    private fun jsonValueFor(
        type: TypeInfo,
        state: PluginSettingsState.State,
        useAnnotations: Boolean,
        depth: Int,
        visited: MutableSet<String>
    ): String
    {
        if (depth > 16) return "null"

        return when (type)
        {
            TypeInfo.TBoolean -> if (state.includeNulls) "null" else "false"
            TypeInfo.TInt -> if (state.includeNulls) "null" else "0"
            TypeInfo.TLong -> if (state.includeNulls) "null" else "0"
            TypeInfo.TDouble -> if (state.includeNulls) "null" else "0.0"
            TypeInfo.TBigDecimal -> if (state.includeNulls) "null" else "\"0.00\""
            TypeInfo.TString -> if (state.includeNulls) "null" else "\"${state.defaultString}\""
            TypeInfo.TUUID -> if (state.includeNulls) "null" else "\"00000000-0000-0000-0000-000000000000\""
            TypeInfo.TLocalDate -> if (state.includeNulls) "null" else "\"${state.defaultDate}\""
            TypeInfo.TLocalDateTime -> if (state.includeNulls) "null" else "\"${state.defaultDate}T00:00:00\""

            is TypeInfo.TEnum -> if (state.includeNulls) "null" else "\"${type.constants.firstOrNull() ?: "UNKNOWN"}\""

            is TypeInfo.TOptional ->
                jsonValueFor(type.wrapped, state, useAnnotations, depth + 1, visited)

            is TypeInfo.TList ->
            {
                val elem = jsonValueFor(type.elementType, state, useAnnotations, depth + 1, visited)
                renderSingleElementArray(elem, state, depth)
            }

            is TypeInfo.TSet ->
            {
                val elem = jsonValueFor(type.elementType, state, useAnnotations, depth + 1, visited)
                renderSingleElementArray(elem, state, depth)
            }

            is TypeInfo.TMap ->
            {
                val baseIndent = " ".repeat(state.indentSize * depth)
                val innerIndent = " ".repeat(state.indentSize * (depth + 1))
                val keyStr = when (type.keyType)
                {
                    is TypeInfo.TString -> "\"key\""
                    is TypeInfo.TInt, is TypeInfo.TLong, is TypeInfo.TDouble -> "\"1\""
                    is TypeInfo.TUUID -> "\"00000000-0000-0000-0000-000000000000\""
                    else -> "\"key\""
                }
                val valStr = jsonValueFor(type.valueType, state, useAnnotations, depth + 1, visited)

                val renderedVal = if (isMultilineBlock(valStr))
                {
                    renderMultilineAfterKey(keyStr.trim('"'), valStr, innerIndent)
                } else
                {
                    "$innerIndent$keyStr: $valStr"
                }
                "{\n$renderedVal\n$baseIndent}"
            }

            is TypeInfo.TObject ->
            {
                val id = type.className
                if (!visited.add(id)) return "null"
                val rendered = renderObjectFields(
                    fields = type.fields,
                    state = state,
                    useAnnotations = useAnnotations,
                    depth = depth,
                    visited = visited
                )
                visited.remove(id)
                rendered
            }

            TypeInfo.TUnknown -> if (state.includeNulls) "null" else "\"${state.defaultString}\""
        }
    }
}
