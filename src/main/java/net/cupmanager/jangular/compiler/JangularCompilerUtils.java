package net.cupmanager.jangular.compiler;

import java.lang.reflect.Field;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class JangularCompilerUtils {
	
	@SuppressWarnings("unchecked")
	public static <T> Class<T> loadScopeClass(ClassLoader classLoader, byte[] b, String className) {
		// override classDefine (as it is protected) and define the class.
		Class<T> clazz = null;
		try {
			Class<?> cls = Class.forName("java.lang.ClassLoader");
			java.lang.reflect.Method method = cls.getDeclaredMethod(
					"defineClass", new Class[] { String.class, byte[].class, int.class, int.class });

			// protected method invocaton
			method.setAccessible(true);
			try {
				Object[] args = new Object[] { className, b, new Integer(0),
						new Integer(b.length) };
				clazz = (Class<T>) method.invoke(classLoader, args);
			} finally {
				method.setAccessible(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return clazz;
	}
	
	public static void checkcast(Field toField, Field fromField, MethodVisitor mv) {
		checkcast(toField.getType(), fromField.getType(), mv);
	}
	
	public static void checkcast(Class<?> toFieldClass, Class<?> fromFieldClass, MethodVisitor mv) {
		if( toFieldClass.isPrimitive() ){
			if( !fromFieldClass.isPrimitive() ){
				unbox(toFieldClass, mv);
			}
		} else {
			if( fromFieldClass.isPrimitive() ){
				box(fromFieldClass, mv);
			} 
			mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(toFieldClass));
		}
	}
	
	private static void box(Class<?> varType, MethodVisitor mv) {
		if (varType.isPrimitive()) {
			Type varTypeType = Type.getType(varType);
			switch (varTypeType.getSort()) {
			case Type.BOOLEAN:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
				break;
			case Type.BYTE:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
				break;
			case Type.CHAR:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
				break;
			case Type.SHORT:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
				break;
			case Type.INT:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
				break;
			case Type.FLOAT:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
				break;
			case Type.LONG:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
				break;
			case Type.DOUBLE:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
				break;
			}
		}
	}

	private static void unbox(Class<?> fieldType, MethodVisitor mv) {
		Type type = Type.getType(fieldType);
		switch (type.getSort()) {
		case Type.BOOLEAN:
			mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
			break;
		case Type.BYTE:
			mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
			break;
		case Type.CHAR:
			mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
			break;
		case Type.SHORT:
			mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
			break;
		case Type.INT:
			mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
			break;
		case Type.FLOAT:
			mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
			break;
		case Type.LONG:
			mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
			break;
		case Type.DOUBLE:
			mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
			break;
		case Type.ARRAY:
			mv.visitTypeInsn(Opcodes.CHECKCAST, type.getDescriptor());
			break;
		case Type.OBJECT:
			mv.visitTypeInsn(Opcodes.CHECKCAST, type.getInternalName());
			break;
		}
	}
}
