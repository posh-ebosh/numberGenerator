package ru.itis;

import org.apache.maven.plugins.annotations.Mojo;
import org.reflections.Reflections;
import ru.itis.annotations.DtoMapping;
import ru.itis.annotations.Ignore;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;


public class ProjectScanner {

    public static Map<Class<?>, List<Field>> getDtoFields(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> dtoClasses = reflections.getTypesAnnotatedWith(DtoMapping.class);
        Map<Class<?>, List<Field>> result = new HashMap<>();

        for (Class<?> dtoClass : dtoClasses) {
            List<Field> convertibleFields = new ArrayList<>();

            for (Field field : dtoClass.getDeclaredFields()) {
                Annotation[] annotations = field.getAnnotations();
                if (annotations.length!=0){
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType() != Ignore.class) {
                            convertibleFields.add(field);
                            break;
                        }
                    }
                }else {
                    convertibleFields.add(field);
                }

            }
            result.put(dtoClass, convertibleFields);
        }
        return result;
    }
}

