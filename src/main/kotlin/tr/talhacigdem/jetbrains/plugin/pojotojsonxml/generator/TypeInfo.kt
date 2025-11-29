package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.generator

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
sealed class TypeInfo
{
    object TBoolean : TypeInfo()
    object TInt : TypeInfo()
    object TLong : TypeInfo()
    object TDouble : TypeInfo()
    object TBigDecimal : TypeInfo()
    object TString : TypeInfo()
    object TUUID : TypeInfo()
    object TLocalDate : TypeInfo()
    object TLocalDateTime : TypeInfo()
    object TUnknown : TypeInfo()

    data class TEnum(val enumName: String, val constants: List<String>) : TypeInfo()
    data class TList(val elementType: TypeInfo) : TypeInfo()
    data class TSet(val elementType: TypeInfo) : TypeInfo()
    data class TMap(val keyType: TypeInfo, val valueType: TypeInfo) : TypeInfo()
    data class TOptional(val wrapped: TypeInfo) : TypeInfo()
    data class TObject(val className: String, val fields: List<FieldDef>, val xmlRootName: String? = null) : TypeInfo()
}