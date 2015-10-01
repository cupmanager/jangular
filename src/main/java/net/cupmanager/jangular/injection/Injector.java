package net.cupmanager.jangular.injection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.cupmanager.jangular.annotations.Context;
import net.cupmanager.jangular.annotations.Provides;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.compiler.JangularCompilerUtils;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public abstract class Injector {
	public void inject(Object target, EvaluationContext evaluationContext) {}
	
	
	
	
	
	
	
	
	
	private static AtomicInteger injectorClassSuffix = new AtomicInteger();
	public static Class<? extends Injector> createInjectorClass(
			CompilerSession session, 
			Class<?> targetClass, 
			Class<? extends EvaluationContext> evaluationContextClass) {
		
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		String className = "Injector" + injectorClassSuffix.incrementAndGet();
		
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null,
				Type.getInternalName(Injector.class), null);
		
		
		/* CONSTRUCTOR */
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Injector.class), "<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		
		/* inject() */
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "inject",
				"(Ljava/lang/Object;Lnet/cupmanager/jangular/injection/EvaluationContext;)V", null, null);
		mv.visitCode();
		
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetClass));
		mv.visitVarInsn(Opcodes.ASTORE, 3);
		
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(evaluationContextClass));
		mv.visitVarInsn(Opcodes.ASTORE, 4);
		
		List<InjectableField> fields = Injector.getInjectableFields(targetClass, session);
		
		for (InjectableField field : fields) {
			// What's the field name in the evaluation context?
			String sourceFieldName = Injector.getProvidedFieldName(evaluationContextClass, field);
			if (sourceFieldName != null) {
				Type type = Type.getType(field.type);
				mv.visitVarInsn(Opcodes.ALOAD, 3);
				mv.visitVarInsn(Opcodes.ALOAD, 4);
				
				mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(evaluationContextClass), sourceFieldName, type.getDescriptor());
				mv.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(targetClass), field.name, type.getDescriptor());
			} else {
				session.warn(String.format("The context class %s has no @Provides for [%s with context '%s']. "+
							"Will not inject '%s' in %s",
							evaluationContextClass, field.type, field.context,
							field.name, targetClass));
			}
		}
		
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(3, 5);
		mv.visitEnd();
		
		cw.visitEnd();
		
		
		return JangularCompilerUtils.loadScopeClass(session.getClassLoader(), cw.toByteArray(), className);
	}
	
	
	
	

	private static List<InjectableField> getInjectableFields(Class<?> c, CompilerSession session) {
		List<InjectableField> fields = new ArrayList<InjectableField>();
		for (Field field : c.getDeclaredFields()) {
			Context injectAnnotation = field.getAnnotation(Context.class);
			if (injectAnnotation != null) {
				if (Modifier.isPublic(field.getModifiers())) {
					InjectableField injectableField = new InjectableField(field.getName(), injectAnnotation.value(), field.getType());
					fields.add(injectableField);
				} else {
					session.warn("The field '"+field.getName()+"' in "+c.getCanonicalName()+" has @Context but is not public!");
				}
			} 
		}
		return fields;
	}
	
	private static class InjectableField {
		public String name;
		public String context;
		public Class<?> type;
		
		public InjectableField(String name, String context, Class<?> type) {
			this.name = name;
			this.context = context;
			this.type = type;
		}
	}

	private static String getProvidedFieldName(Class<? extends EvaluationContext> contextClass, InjectableField injectableField) {
		for (Field field : contextClass.getFields()) {
			Provides providesAnnotation = field.getAnnotation(Provides.class);
			if (providesAnnotation != null) {
				if (providesAnnotation.value().equals(injectableField.context)) {
					if (field.getType().isAssignableFrom(injectableField.type)) {
						return field.getName();
					}
				}
			}
		}
		return null;
	}
	
	
}
