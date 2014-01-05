package net.cupmanager.jangular.nodes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.cupmanager.jangular.AbstractController;
import net.cupmanager.jangular.Compiler;
import net.cupmanager.jangular.Scope;

import org.mvel2.util.ReflectionUtil;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ControllerNode implements JangularNode {
	
	public static abstract class ControllerScopeValueCopier {
		public void copy(Scope targetScope, Scope controllerScope, Scope parentScope) {
			
		}
	}
	
	private JangularNode node;
	private AbstractController controllerInstance;
	
	private List<String> nodeVariables;
	
	private ControllerScopeValueCopier valueCopier;
	private Class<? extends Scope> controllerScopeClass;
	private Class<? extends AbstractController> controllerClass;
	private Class<? extends Scope> dynamicControllerScopeClass;
	
	
	public ControllerNode(String controllerClassName, JangularNode node) {
		try {
			this.controllerClass = (Class<? extends AbstractController>) Class.forName(controllerClassName);
			this.node = node;
			this.controllerInstance = controllerClass.newInstance();
			this.controllerScopeClass = controllerInstance.getScopeClass();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

	private List<String> getScopeProvidedVariables() {
		Field[] fields = controllerScopeClass.getFields();
		
		List<String> ins = new ArrayList<String>();
		for( Field f : fields ) {
			ins.add(f.getName());
		}
		return ins;
	}
	
	private List<Class<?>> getScopeProvidedTypes() {
		Field[] fields = controllerScopeClass.getFields();
		
		List<Class<?>> ins = new ArrayList<Class<?>>();
		for( Field f : fields ) {
			ins.add(f.getType());
		}
		return ins;
	}
	

	public void eval(final Scope parentScope, StringBuilder sb) {
		
		try {
			Scope controllerScope = controllerScopeClass.newInstance();
			controllerInstance.eval(controllerScope);
			Scope nodeScope = dynamicControllerScopeClass.newInstance();
			valueCopier.copy(nodeScope, controllerScope, parentScope);
			
			node.eval(nodeScope, sb);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<String> getReferencedVariables() {
		nodeVariables = new ArrayList<String>(node.getReferencedVariables());
		return nodeVariables;
	}
	
	
	private static Field getFieldSafe(Class c, String fieldName) {
		Field f = null;
		try {
			f = c.getField(fieldName);
		} catch (Exception e) {}
		return f;
	}
	
	public static int controllerScopeSuffix = 0;
	
	public void compileScope(Class<? extends Scope> parentScopeClass) throws Exception {
		this.dynamicControllerScopeClass = createDynamicControllerScopeClass(controllerScopeClass, parentScopeClass);

		this.node.compileScope(dynamicControllerScopeClass);
		
		Class<? extends ControllerScopeValueCopier> valueCopierClass = createValueCopierClass(dynamicControllerScopeClass, controllerScopeClass, parentScopeClass);
		this.valueCopier = valueCopierClass.newInstance();
	}
	
	private Class<? extends Scope> createDynamicControllerScopeClass(
			Class<? extends Scope> controllerScopeClass, 
			Class<? extends Scope> parentScopeClass) {
		
		ArrayList<String> fieldsInDynamicClass = new ArrayList<String>(getReferencedVariables());
//		fieldsInDynamicClass.removeAll(getScopeProvidedVariables());
		
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;

		String className = "ControllerScope" + controllerScopeSuffix++;
		
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null,
				Type.getInternalName(Scope.class), null);
		
		/* FIELDS */
		for (String fieldName : fieldsInDynamicClass) {
			Field controllerField = getFieldSafe(controllerScopeClass, fieldName);
			Field parentField = getFieldSafe(parentScopeClass, fieldName);
			
			Class fieldType = Object.class;
			if (controllerField != null) {
				fieldType = controllerField.getType();
			} else {
				fieldType = parentField.getType();
			}
			
			fv = cw.visitField(Opcodes.ACC_PUBLIC, fieldName, Type.getDescriptor(fieldType), null, null);
			fv.visitEnd();
		}
		
		/* CONSTRUCTOR */
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Scope.class), "<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		cw.visitEnd();
		
		return Compiler.loadScopeClass(cw.toByteArray(), className);
	}
	
	private static Class<? extends ControllerScopeValueCopier> createValueCopierClass(
			Class<? extends Scope> targetScopeClass, 
			Class<? extends Scope> controllerScopeClass, 
			Class<? extends Scope> parentScopeClass) throws NoSuchFieldException, SecurityException{
		
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		String className = "ValueCopier" + controllerScopeSuffix++;
		
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null,
				Type.getInternalName(ControllerScopeValueCopier.class), null);
		
		
		/* CONSTRUCTOR */
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(ControllerScopeValueCopier.class), "<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		/* copy() */
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "copy",
				"(Lnet/cupmanager/jangular/Scope;Lnet/cupmanager/jangular/Scope;Lnet/cupmanager/jangular/Scope;)V", null, null);
		mv.visitCode();
		
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetScopeClass));
		mv.visitVarInsn(Opcodes.ASTORE, 4);
		
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(controllerScopeClass));
		mv.visitVarInsn(Opcodes.ASTORE, 5);
		
		mv.visitVarInsn(Opcodes.ALOAD, 3);
		mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(parentScopeClass));
		mv.visitVarInsn(Opcodes.ASTORE, 6);
		
		
		for (Field field : targetScopeClass.getFields()) {
			Field controllerField = getFieldSafe(controllerScopeClass, field.getName());
			if (controllerField != null) {
				mv.visitVarInsn(Opcodes.ALOAD, 4);
				mv.visitVarInsn(Opcodes.ALOAD, 5);
				mv.visitFieldInsn(Opcodes.GETFIELD, 
						Type.getInternalName(controllerScopeClass), 
						controllerField.getName(),
						Type.getDescriptor(controllerField.getType()));
				if( controllerField.getType().isPrimitive() ){
					unbox(controllerField.getType(), mv);
				} else {
					mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(controllerField.getType()));
				}
				mv.visitFieldInsn(Opcodes.PUTFIELD, 
						Type.getInternalName(targetScopeClass), 
						controllerField.getName(),
						Type.getDescriptor(controllerField.getType()));
				
			} else {
				Field parentField = getFieldSafe(parentScopeClass, field.getName());
				if (parentField != null) {
					mv.visitVarInsn(Opcodes.ALOAD, 4);
					mv.visitVarInsn(Opcodes.ALOAD, 6);
					mv.visitFieldInsn(Opcodes.GETFIELD, 
							Type.getInternalName(parentScopeClass), 
							parentField.getName(),
							Type.getDescriptor(parentField.getType()));
					if( parentField.getType().isPrimitive() ){
						unbox(parentField.getType(), mv);
					} else {
						mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(parentField.getType()));
					}
					mv.visitFieldInsn(Opcodes.PUTFIELD, 
							Type.getInternalName(targetScopeClass), 
							parentField.getName(),
							Type.getDescriptor(parentField.getType()));
				} else {
					throw new RuntimeException("Field wasnt in either controller/parent");
				}
			}
			
		}
		
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(3, 7);
		mv.visitEnd();
		
		cw.visitEnd();
		
		return Compiler.loadScopeClass(cw.toByteArray(), className);
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
