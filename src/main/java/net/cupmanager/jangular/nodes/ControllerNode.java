package net.cupmanager.jangular.nodes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.cupmanager.jangular.AbstractController;
import net.cupmanager.jangular.JangularClassLoader;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.compiler.JangularCompilerUtils;
import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.injection.Injector;

import org.apache.commons.lang.ClassUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ControllerNode extends JangularNode {
	
	public static abstract class ControllerScopeValueCopier {
		public void copy(Scope targetScope, /*Scope controllerScope,*/ Scope parentScope) {
			
		}
	}
	
	private JangularNode node;
	//private AbstractController controllerInstance;
	
	private ControllerScopeValueCopier valueCopier;
	private Class<? extends Scope> controllerScopeClass;
	private Class<? extends AbstractController<?>> controllerClass;
	private Class<? extends Scope> dynamicControllerScopeClass;
	private Injector injector;
	
	
	@SuppressWarnings("unchecked") 
	public ControllerNode(String controllerClassName, JangularNode node) {
		try {
			this.controllerClass = (Class<? extends AbstractController<?>>) Class.forName(controllerClassName);
			this.node = node;
			//this.controllerInstance = controllerClass.newInstance();
			this.controllerScopeClass = AbstractController.getScopeClass(controllerClass);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

	@Override
	public void eval(final Scope parentScope, StringBuilder sb, EvaluationContext context) {
		
		try {
			AbstractController controllerInstance = controllerClass.newInstance();
			injector.inject(controllerInstance, context);
			
			//Scope controllerScope = controllerScopeClass.newInstance();
			
			Scope nodeScope = dynamicControllerScopeClass.newInstance();
			valueCopier.copy(nodeScope, parentScope);
			
			controllerInstance.eval(nodeScope);
			
			node.eval(nodeScope, sb, context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<String> getControllerScopeIns() {

		Field[] fields = controllerScopeClass.getFields();
		
		List<String> ins = new ArrayList<String>();
		for( Field f : fields ) {
			if( f.getAnnotation(In.class) != null ){
				ins.add(f.getName());
			}
		}
		return ins;
	}
	
	private List<String> getControllerScopeFields() {
		
		Field[] fields = controllerScopeClass.getFields();
		
		List<String> names = new ArrayList<String>();
		for( Field f : fields ) {
			names.add(f.getName());
		}
		return names;
	}

	@Override
	public Collection<String> getReferencedVariables() {
		Set<String> names = new HashSet<String>(node.getReferencedVariables());
		
		names.removeAll(getControllerScopeFields());
		
		// All ins are required
		names.addAll(getControllerScopeIns());
		return names;
	}
	
	
	private static Field getFieldSafe(Class<?> c, String fieldName) {
		Field f = null;
		try {
			f = c.getField(fieldName);
		} catch (Exception e) {}
		return f;
	}
	
	public static int controllerScopeSuffix = 0;
	
	@Override
	public void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			CompilerSession session) throws Exception {
		this.dynamicControllerScopeClass = createDynamicControllerScopeClass(controllerScopeClass, parentScopeClass, session);

		
		this.node.compileScope(dynamicControllerScopeClass, evaluationContextClass, session);
		
		Class<? extends ControllerScopeValueCopier> valueCopierClass = 
				createValueCopierClass(getReferencedVariables(),dynamicControllerScopeClass, parentScopeClass, 
						session.getClassLoader());
		this.valueCopier = valueCopierClass.newInstance();
		

		Class<? extends Injector> injectorClass = Injector.createInjectorClass(session.getClassLoader(), controllerClass, evaluationContextClass);
		this.injector = injectorClass.newInstance();
	}
	
	private Class<? extends Scope> createDynamicControllerScopeClass(
			Class<? extends Scope> controllerScopeClass, 
			Class<? extends Scope> parentScopeClass, 
			CompilerSession session) {
		
		ArrayList<String> fieldsInDynamicClass = new ArrayList<String>(getReferencedVariables());
		
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;

		String className = "ControllerScope" + controllerScopeSuffix++;
		
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null,
				Type.getInternalName(controllerScopeClass), null);
		
		/* FIELDS */
		for (String fieldName : fieldsInDynamicClass) {
			Field controllerField = getFieldSafe(controllerScopeClass, fieldName);
			Field parentField = getFieldSafe(parentScopeClass, fieldName);
			
			if (controllerField != null) {
				// The field already exists in the controllerScope
				// Just make sure that it is availiable if necessary
				
				Class<?> fieldType = controllerField.getType();
				fieldType = ClassUtils.primitiveToWrapper(fieldType);
				
				if( controllerField.getAnnotation(In.class) != null ){
					if( parentField == null ) {
						throw new RuntimeException(String.format(
							"The @In-field %s in %s is not availiable in the parent scope (%s)!",
							fieldName, controllerScopeClass.getName(), parentScopeClass.getName()));
					} 
				}
				
				if( parentField != null ) {
					session.assertCasts(controllerField, parentField);
				} 
			} else {
				Class<?> fieldType = parentField.getType();
				
				fv = cw.visitField(Opcodes.ACC_PUBLIC, fieldName, Type.getDescriptor(fieldType), null, null);
				fv.visitEnd();
			}
		}
		
		/* CONSTRUCTOR */
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(controllerScopeClass), "<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		cw.visitEnd();
		
		return JangularCompilerUtils.loadScopeClass(session.getClassLoader(), cw.toByteArray(), className);
	}
	
	private static Class<? extends ControllerScopeValueCopier> createValueCopierClass(
			Collection<String> fieldNames,
			Class<? extends Scope> targetScopeClass, 
			Class<? extends Scope> parentScopeClass,
			JangularClassLoader classLoader) throws NoSuchFieldException, SecurityException{
		
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		String className = "ControllerValueCopier" + controllerScopeSuffix++;
		
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
		
		/* copy(Scope target, Scope parent) */
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "copy",
				"(Lnet/cupmanager/jangular/Scope;Lnet/cupmanager/jangular/Scope;)V", null, null);
		mv.visitCode();
		
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetScopeClass));
		mv.visitVarInsn(Opcodes.ASTORE, 3);
		
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(parentScopeClass));
		mv.visitVarInsn(Opcodes.ASTORE, 4);
		
		for (String fieldName : fieldNames) {
			Field targetField = getFieldSafe(targetScopeClass, fieldName);
			Field parentField = getFieldSafe(parentScopeClass, fieldName);
			
			mv.visitVarInsn(Opcodes.ALOAD, 3);
			mv.visitVarInsn(Opcodes.ALOAD, 4);
			mv.visitFieldInsn(Opcodes.GETFIELD, 
					Type.getInternalName(parentScopeClass), 
					fieldName,
					Type.getDescriptor(parentField.getType()));
			
			JangularCompilerUtils.checkcast(targetField, parentField, mv);
			
			mv.visitFieldInsn(Opcodes.PUTFIELD, 
					Type.getInternalName(targetScopeClass), 
					fieldName,
					Type.getDescriptor(targetField.getType()));	
		}
		
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(3, 6);
		mv.visitEnd();
		
		cw.visitEnd();
		
		return JangularCompilerUtils.loadScopeClass(classLoader, cw.toByteArray(), className);
	}

}
