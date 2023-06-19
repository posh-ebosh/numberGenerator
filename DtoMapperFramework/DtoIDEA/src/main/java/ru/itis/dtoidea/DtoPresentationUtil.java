package ru.itis.dtoidea;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.FontPreferences;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.presentation.java.ClassPresentationUtil;
import com.intellij.ui.JBColor;

public class DtoPresentationUtil {

    public static String getDtoClassPresentation(PsiElement element) {
        StringBuilder stringBuilder = new StringBuilder();

        if (element instanceof PsiFile) {
            PsiFile file = (PsiFile) element;
            stringBuilder.append(file.getName());
        } else {
            stringBuilder.append(ClassPresentationUtil.getNameForClass(element));
        }

        return stringBuilder.toString();
    }

    public static void styleDtoClassPresentation(Editor editor, PsiElement element) {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        TextAttributesKey defaultAttributesKey = ClassPresentationUtil.getNameAttributesKey(element);
        TextAttributes defaultAttributes = scheme.getAttributes(defaultAttributesKey);
        TextAttributes styledAttributes = new TextAttributes(defaultAttributes);

        styledAttributes.setFontType(FontPreferences.BOLD);
        styledAttributes.setForegroundColor(JBColor.BLUE);

        MarkupModel markupModel = editor.getMarkupModel();
        markupModel.addRangeHighlighter(
                element.getTextRange().getStartOffset(),
                element.getTextRange().getEndOffset(),
                0,
                styledAttributes,
                0,
                null
        );
    }
}

