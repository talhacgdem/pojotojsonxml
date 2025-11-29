package tr.talhacigdem.jetbrains.plugin.pojotojsonxml.generator

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField

/**
 * @author MuhammedTalha.Cigdem on 2025-11-27
 */
object FieldExtractor
{
    fun extractFields(psiClass: PsiClass): List<PsiField> =
        psiClass.allFields
            .filter { !it.hasModifierProperty("static") }
            .filter { !it.hasModifierProperty("transient") }
}
