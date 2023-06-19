package ru.itis.dtoidea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

public class ShowDTOAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String dtoCode = generateDtoCode();
        Messages.showMessageDialog(dtoCode, "DTO Code", Messages.getInformationIcon());
    }

    public static String generateDtoCode(TypeElement modelElement) {
        StringBuilder stringBuilder = new StringBuilder();

        String modelName = modelElement.getSimpleName().toString();
        String dtoName = modelName + "Dto";

        stringBuilder.append("public class ")
                .append(dtoName)
                .append(" {\n\n");

        List<Element> fields = getAllFields(modelElement);

        for (Element field : fields) {
            String fieldName = field.getSimpleName().toString();
            String fieldType = field.asType().toString();

            stringBuilder.append("    private ")
                    .append(fieldType)
                    .append(" ")
                    .append(fieldName)
                    .append(";\n");

            stringBuilder.append("\n    public ")
                    .append(fieldType)
                    .append(" get")
                    .append(fieldName.substring(0, 1).toUpperCase())
                    .append(fieldName.substring(1))
                    .append("() {\n")
                    .append("        return ")
                    .append(fieldName)
                    .append(";\n")
                    .append("    }\n");

            stringBuilder.append("\n    public void set")
                    .append(fieldName.substring(0, 1).toUpperCase())
                    .append(fieldName.substring(1))
                    .append("(")
                    .append(fieldType)
                    .append(" ")
                    .append(fieldName)
                    .append(") {\n")
                    .append("        this.")
                    .append(fieldName)
                    .append(" = ")
                    .append(fieldName)
                    .append(";\n")
                    .append("    }\n");
        }

        stringBuilder.append("}");

        return stringBuilder.toString();
    }

    private static List<Element> getAllFields(TypeElement modelElement) {
        List<Element> fields = new ArrayList<>();
        Types typeUtils = modelElement.getEnclosingElement().getEnclosingElement().getProcessingEnvironment().getTypeUtils();

        for (Element element : ElementFilter.fieldsIn(modelElement.getEnclosedElements())) {
            if (!element.getModifiers().contains(javax.lang.model.element.Modifier.STATIC)) {
                fields.add(element);
            }
        }

        TypeMirror superclass = modelElement.getSuperclass();
        if (superclass instanceof DeclaredType) {
            Element superElement = ((DeclaredType) superclass).asElement();
            if (superElement instanceof TypeElement) {
                fields.addAll(getAllFields((TypeElement) superElement));
            }
        }

        return fields;
    }

}
