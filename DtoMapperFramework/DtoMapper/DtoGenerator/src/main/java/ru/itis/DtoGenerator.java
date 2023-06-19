package ru.itis;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.capitalize;


@Mojo(name= "dtoGenerator")
public class DtoGenerator extends AbstractMojo {
//    @Parameter(defaultValue = "${project}")
//    private  MavenProject project;

    public DtoGenerator() {
    }


//    public DtoGenerator(MavenProject project) {
//        this.project = project;
//    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            generateDto();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }




    public  void generateDto() throws IOException, ClassNotFoundException {
        String packageName = "ru";
        String superClassName = "java/lang/Object";
        Map<Class<?>, List<Field>> map = ProjectScanner.getDtoFields("ru.itis");
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        String directoryPath = "target/classes/ru/itis/dto";

        File directory = new File(directoryPath);

        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Directory created successfully.");
            } else {
                System.out.println("Failed to create the directory.");
            }
        } else {
            System.out.println("Directory already exists.");
        }

        for (Class<?> clazz : map.keySet()){
            String dtoClassName = clazz.getSimpleName() + "Dto";
            String dtoClassPath = "ru/itis/dto/" + clazz.getSimpleName() + "Dto";
            List<Field> fields = map.get(clazz);
            cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, dtoClassPath, null, superClassName, null);


            generateGetterAndSetter(fields, dtoClassPath, cw);
            generateDefaultConstructor(cw);
            generateToDto(clazz, fields, dtoClassPath, cw);
            generateToObjectMethod(cw, clazz, fields);
            generateEqualsMethod(cw, dtoClassName, fields);
            generateToStringMethod(cw, clazz, fields);
            generateHashCodeMethod(cw, clazz, fields);

            cw.visitEnd();
            byte[] bytecode = cw.toByteArray();
            String path = "target/classes/" + dtoClassPath + ".class";
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(bytecode);
            fos.close();
        }

    }

    private void generateGetterAndSetter(List<Field> fields, String dtoClassPath, ClassWriter cw){

        if (fields != null){
            for (Field f : fields){
                cw.visitField(Opcodes.ACC_PUBLIC,
                        f.getName(), f.getType().getName().replace(".", "/"), null, null);

                String fieldName = f.getName();
                String fieldType = Type.getType(f.getType()).getDescriptor();
                String getterName = "get" + capitalize(fieldName);
                String setterName = "set" + capitalize(fieldName);

                MethodVisitor getter = cw.visitMethod(Opcodes.ACC_PUBLIC, getterName, "()" + fieldType, null, null);
                getter.visitCode();
                getter.visitVarInsn(Opcodes.ALOAD, 0);
                getter.visitFieldInsn(Opcodes.GETFIELD, dtoClassPath, fieldName, fieldType);
                getter.visitInsn(Type.getType(f.getType()).getOpcode(Opcodes.IRETURN));
                getter.visitMaxs(1, 1);
                getter.visitEnd();

                MethodVisitor setter = cw.visitMethod(Opcodes.ACC_PUBLIC, setterName, "(" + fieldType + ")V", null, null);
                setter.visitCode();
                setter.visitVarInsn(Opcodes.ALOAD, 0);
                setter.visitVarInsn(Type.getType(f.getType()).getOpcode(Opcodes.ILOAD), 1);
                setter.visitFieldInsn(Opcodes.PUTFIELD, dtoClassPath, fieldName, fieldType);
                setter.visitInsn(Opcodes.RETURN);
                setter.visitMaxs(2, 2);
                setter.visitEnd();

            }
        }

    }

    private void generateDefaultConstructor(ClassWriter cw){
        MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
    }

    private void generateToDto(Class<?> clazz, List<Field> fields, String dtoClassPath, ClassWriter cw) throws IOException {
        String constructorDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(clazz));
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", constructorDescriptor, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        for (Field field : fields) {
            // Загрузка this на стек
            mv.visitVarInsn(Opcodes.ALOAD, 0);

            // Загрузка соответствующего поля из модели на стек
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.GETFIELD, Type.getDescriptor(clazz), field.getName(), Type.getDescriptor(field.getType()));

            // Установка значения поля в DTO-объекте
            mv.visitFieldInsn(Opcodes.PUTFIELD, Type.getObjectType(dtoClassPath).getDescriptor(), field.getName(), Type.getDescriptor(field.getType()));
        }

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(fields.size() + 1, fields.size() + 1);
        mv.visitEnd();
    }

    private void generateToObjectMethod(ClassWriter cw, Class<?> clazz, List<Field> fields) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "toObject", "()L" + clazz.getName().replace(".", "/") + ";", null, null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, clazz.getName().replace(".", "/"));
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, clazz.getName().replace(".", "/"), "<init>", "()V", false);

        for (Field field : fields) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, Type.getDescriptor(clazz), field.getName(), Type.getDescriptor(field.getType()));
            mv.visitFieldInsn(Opcodes.PUTFIELD, clazz.getName().replace(".", "/"), field.getName(), Type.getDescriptor(field.getType()));
        }

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(3, 1);
        mv.visitEnd();
    }

    private void generateToStringMethod(ClassWriter cw, Class<?> clazz, List<Field> fields) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
        mv.visitCode();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"").append(clazz.getSimpleName()).append("{\"");

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            stringBuilder.append("\"").append(field.getName()).append("=\"");
            stringBuilder.append(" + ").append("this.").append(field.getName());

            if (i < fields.size() - 1) {
                stringBuilder.append(" + \",\" + ");
            }
        }

        stringBuilder.append(" + \"}\"");

        mv.visitLdcInsn(stringBuilder.toString());
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }


    private void generateEqualsMethod(ClassWriter cw, String className, List<Field> fields) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
        mv.visitCode();

        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitJumpInsn(IF_ACMPNE, l1);

        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);

        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, className);
        mv.visitJumpInsn(IFEQ, l2);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, className);
        mv.visitVarInsn(ASTORE, 2);

        mv.visitInsn(ICONST_1);

        for (Field field : fields) {
            String fieldName = field.getName();
            String fieldDesc = Type.getDescriptor(field.getType());

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, fieldName, fieldDesc);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitFieldInsn(GETFIELD, className, fieldName, fieldDesc);

            if (field.getType().isPrimitive()) {
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Object.class), "equals", "(" + fieldDesc + fieldDesc + ")Z", false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Object.class), "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
            }

            mv.visitInsn(IAND);
        }

        mv.visitJumpInsn(IFEQ, l2);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, l3);

        mv.visitLabel(l2);
        mv.visitInsn(ICONST_0);

        mv.visitLabel(l3);
        mv.visitInsn(IRETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }



    private void generatePrimitiveEquals(MethodVisitor mv, Class<?> fieldType) {
        if (fieldType == float.class) {
            mv.visitInsn(Opcodes.FCMPG);
            Label label = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, label);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(label);
        } else if (fieldType == double.class) {
            mv.visitInsn(Opcodes.DCMPG);
            Label label = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, label);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(label);
        } else {
            mv.visitInsn(Opcodes.IXOR);
            mv.visitInsn(Opcodes.ICONST_M1);
            mv.visitInsn(Opcodes.IXOR);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IAND);
            Label label = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, label);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(label);
        }
    }

    private static void generateHashCodeMethod(ClassWriter cw, Class<?> clazz, List<Field> fields) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "hashCode", "()I", null, null);
        mv.visitCode();

        mv.visitIntInsn(Opcodes.BIPUSH, 31);
        mv.visitVarInsn(Opcodes.ISTORE, 1);

        for (Field field : fields) {
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, Type.getDescriptor(clazz), field.getName(), Type.getDescriptor(field.getType()));

            if (field.getType().isPrimitive()) {
                generatePrimitiveHashCode(mv, field.getType());
            } else {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Objects", "hashCode", "(Ljava/lang/Object;)I", false);
            }

            mv.visitInsn(Opcodes.IADD);
            mv.visitVarInsn(Opcodes.ISTORE, 1);
        }

        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitInsn(Opcodes.IRETURN);

        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private static void generatePrimitiveHashCode(MethodVisitor mv, Class<?> fieldType) {
        String fieldTypeDescriptor = getDescriptor(fieldType);

        if (fieldType == int.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "hashCode", "(" + fieldTypeDescriptor + ")I", false);
        } else if (fieldType == long.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "hashCode", "(" + fieldTypeDescriptor + ")I", false);
        } else if (fieldType == float.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "hashCode", "(" + fieldTypeDescriptor + ")I", false);
        } else if (fieldType == double.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "hashCode", "(" + fieldTypeDescriptor + ")I", false);
        } else if (fieldType == boolean.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "hashCode", "(" + fieldTypeDescriptor + ")I", false);
        } else if (fieldType == short.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "hashCode", "(" + fieldTypeDescriptor + ")I", false);
        } else if (fieldType == byte.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "hashCode", "(" + fieldTypeDescriptor + ")I", false);
        } else if (fieldType == char.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "hashCode", "(" + fieldTypeDescriptor + ")I", false);
        }
    }

    private static String getDescriptor(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == int.class) {
                return "I";
            } else if (clazz == long.class) {
                return "J";
            } else if (clazz == float.class) {
                return "F";
            } else if (clazz == double.class) {
                return "D";
            } else if (clazz == boolean.class) {
                return "Z";
            } else if (clazz == short.class) {
                return "S";
            } else if (clazz == byte.class) {
                return "B";
            } else if (clazz == char.class) {
                return "C";
            }
        }
        return "L" + clazz.getName().replace(".", "/") + ";";
    }


}
