package net.cupmanager.jangular.nodes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.cupmanager.jangular.AbstractDirective;
import net.cupmanager.jangular.JangularCompiler;
import net.cupmanager.jangular.JangularUtils;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.expressions.CompiledExpression;
import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.injection.Injector;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class DirectiveNode implements JangularNode {
	
	public static abstract class DirectiveScopeValueCopier {
		public void copy(Scope scope, Object[] data) {
			
		}
	}
	
	private CompositeNode node;
	private AbstractDirective directiveInstance;
	private Map<String, String> attrs;
	private CompiledExpression[] inExpressions;
	
	private Set<String> variables; 
	
	private List<String> nodeVariables;
//	private Object[] inValues;
	//private Method setMethod;
	
	// private Scope nodeScope;
	private DirectiveScopeValueCopier valueCopier;
	private boolean hasDirectiveScope;
	private Class<? extends Scope> directiveScopeClass;
	private Injector injector;
	
	
	public DirectiveNode(AbstractDirective directiveInstance, CompositeNode compositeNode, Map<String, String> attrs) {
		this.directiveInstance = directiveInstance;
		this.node = compositeNode;
		
		this.variables = new HashSet<String>();
		this.attrs = attrs;
		
		this.hasDirectiveScope = directiveInstance.getScopeClass() != null;
		
		for( String variable : attrs.keySet() ) {
			ParserContext pc = new ParserContext();
			MVEL.analyze(attrs.get(variable), pc);
			variables.addAll(pc.getInputs().keySet());
		}
	}
	
	private List<String> getDirectiveScopeIns() {
		Class<? extends Scope> directiveScopeClass = directiveInstance.getScopeClass();
		Field[] fields = directiveScopeClass.getFields();
		
		List<String> ins = new ArrayList<String>();
		for( Field f : fields ) {
			if( f.getAnnotation(In.class) != null ){
				ins.add(f.getName());
			}
		}
		return ins;
	}
	
	private List<Class<?>> getDirectiveScopeTypes() {
		Class<? extends Scope> directiveScopeClass = directiveInstance.getScopeClass();
		Field[] fields = directiveScopeClass.getFields();
		
		List<Class<?>> ins = new ArrayList<Class<?>>();
		for( Field f : fields ) {
			if( f.getAnnotation(In.class) != null ){
				ins.add(f.getType());
			}
		}
		return ins;
	}
	

	@Override
	public void eval(final Scope scope, StringBuilder sb, EvaluationContext context) {
		
		Object[] inValues = new Object[inExpressions.length];
		for (int i = 0; i < inValues.length; i++ ) {
			inValues[i] = inExpressions[i].eval(scope);
		}
		
		try {
			Scope nodeScope = directiveScopeClass.newInstance();
			valueCopier.copy(nodeScope,inValues);
			
			if( hasDirectiveScope ) {
				injector.inject(directiveInstance, context);
				directiveInstance.eval(nodeScope);
			}
			
			node.eval(nodeScope, sb, context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<String> getReferencedVariables() {
		nodeVariables = new ArrayList<String>(node.getReferencedVariables());
		return variables;
	}

	public static int directiveScopeSuffix = 0;
	
	@Override
	public void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			JangularCompiler compiler) throws Exception {

		List<String> fieldNames = null;
		List<Class<?>> fieldTypes = null;
		
		if (hasDirectiveScope) {
			directiveScopeClass = directiveInstance.getScopeClass();
			fieldNames = getDirectiveScopeIns();
			fieldTypes = getDirectiveScopeTypes();
		} else {
			directiveScopeClass = createDirectiveScopeClass();
			fieldNames = nodeVariables;
		}

		
		inExpressions = new CompiledExpression[fieldNames.size()];
		for( int i = 0; i < fieldNames.size(); i++ ){
			inExpressions[i] = CompiledExpression.compile(attrs.get(fieldNames.get(i)), parentScopeClass);
		}
		
		this.node.compileScope(directiveScopeClass, evaluationContextClass, compiler);
		
		Class<? extends DirectiveScopeValueCopier> valueCopierClass = createValueCopierClass(directiveScopeClass,fieldNames,fieldTypes);
		this.valueCopier = valueCopierClass.newInstance();
		
		
		Class<? extends Injector> injectorClass = Injector.createInjectorClass(directiveInstance.getClass(), evaluationContextClass);
		this.injector = injectorClass.newInstance();
	}
	
	
	private Class<? extends Scope> createDirectiveScopeClass() {
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;

		String className = "DirectiveScope" + directiveScopeSuffix++;
		
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null,
				Scope.class.getName().replace('.', '/'), null);
		
		/* FIELDS */
		for (String nodeVariable : nodeVariables) {
			fv = cw.visitField(Opcodes.ACC_PUBLIC, nodeVariable, "Ljava/lang/Object;",
					null, null);
			fv.visitEnd();
		}
		
		/* CONSTRUCTOR */
		
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Scope.class.getName().replace('.', '/'),
				"<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		
		
		cw.visitEnd();
		
		return JangularCompiler.loadScopeClass(cw.toByteArray(), className);
	}
	
	private Class<? extends DirectiveScopeValueCopier> createValueCopierClass(Class<? extends Scope> targetScopeClass, List<String> fieldNames, List<Class<?>> fieldTypes){
		
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		String className = "ValueCopier" + directiveScopeSuffix++;
		
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null,
				DirectiveScopeValueCopier.class.getName().replace('.', '/'), null);
		
		
		/* CONSTRUCTOR */
		
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, DirectiveScopeValueCopier.class.getName().replace('.', '/'),
				"<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		
		
		/* copy() */
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "copy",
				"(Lnet/cupmanager/jangular/Scope;[Ljava/lang/Object;)V", null, null);
		mv.visitCode();
		
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitTypeInsn(Opcodes.CHECKCAST, targetScopeClass.getName().replace('.', '/'));
		mv.visitVarInsn(Opcodes.ASTORE, 3);
		
		
		for (int i = 0; i < fieldNames.size(); i++) {
			mv.visitVarInsn(Opcodes.ALOAD, 3);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitIntInsn(Opcodes.BIPUSH, i);
			mv.visitInsn(Opcodes.AALOAD);
			if( fieldTypes != null ){
				JangularUtils.checkcast(fieldTypes.get(0), Object.class, mv);
				mv.visitFieldInsn(Opcodes.PUTFIELD, targetScopeClass.getName().replace('.', '/'), fieldNames.get(i),
						Type.getDescriptor(fieldTypes.get(i)));
			} else {
				mv.visitFieldInsn(Opcodes.PUTFIELD, targetScopeClass.getName().replace('.', '/'), fieldNames.get(i),
					"Ljava/lang/Object;");
			}
			
		}
		
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(3, 5);
		mv.visitEnd();
		
		cw.visitEnd();
		
		return JangularCompiler.loadScopeClass(cw.toByteArray(), className);
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
