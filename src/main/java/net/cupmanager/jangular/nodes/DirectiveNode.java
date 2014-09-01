package net.cupmanager.jangular.nodes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.cupmanager.jangular.AbstractDirective;
import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.annotations.In;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.compiler.JangularCompilerUtils;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.expressions.CompiledExpression;
import net.cupmanager.jangular.injection.EvaluationContext;
import net.cupmanager.jangular.injection.Injector;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.common.base.Objects;

public class DirectiveNode extends JangularNode {
	
	public static abstract class DirectiveScopeValueCopier {
		public void copy(Scope scope, Object[] data, Scope parentScope) {
			
		}
	}
	
	private JangularNode node;
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
	private boolean transparent = false;
	private Injector injector;
	
	
	@Override
	public JangularNode clone() {
		DirectiveNode dn = new DirectiveNode();
		dn.node = node.clone();
		dn.directiveInstance = directiveInstance;
		dn.attrs = new HashMap<String, String>(attrs);
		dn.inExpressions = inExpressions;
		dn.variables = new HashSet<String>(variables);
		dn.nodeVariables = new ArrayList<String>(nodeVariables);
		dn.valueCopier = valueCopier;
		dn.hasDirectiveScope = hasDirectiveScope;
		dn.directiveScopeClass = directiveScopeClass;
		dn.transparent = transparent;
		dn.injector = injector;
		
		return dn;
	}
	
	
	private DirectiveNode() {}
	
	public DirectiveNode(AbstractDirective<?> directiveInstance, JangularNode node, Map<String, String> attrs) throws CompileExpressionException {
		this.directiveInstance = directiveInstance;
		this.node = node;
		
		this.variables = new HashSet<String>();
		this.attrs = attrs;
		
		this.transparent = attrs.containsKey("transparent");
		
		this.hasDirectiveScope = directiveInstance.getScopeClass() != null;
		
		for( String variable : attrs.keySet() ) {
			variables.addAll(CompiledExpression.getReferencedVariables(attrs.get(variable)));
		}
		nodeVariables = new ArrayList<String>(node.getReferencedVariables());
		if( transparent ){
			variables.addAll(nodeVariables);
		}
	}
	
	private List<String> getDirectiveScopeInNames() {
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
	
	
	private List<Class<?>> getDirectiveScopeInTypes() {
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
	
	private static Field getFieldSafe(Class<?> c, String fieldName) {
		Field f = null;
		try {
			f = c.getField(fieldName);
		} catch (Exception e) {}
		return f;
	}
	
	public List<ScopeField> getFieldsForSubscope(Class<? extends Scope> superScopeClass, Class<? extends Scope> parentScopeClass) {
		int attrIndex = 0;
		List<ScopeField> list = new ArrayList<DirectiveNode.ScopeField>();
		
		if (transparent) {
			if (hasDirectiveScope) {
//				nodevariables som inte finns i scope och @in (värden från attribut i första hand, och i andra hand från parentscope)
				for (String s : nodeVariables) {
					boolean isInScope = getFieldSafe(superScopeClass, s) != null;
					if (!isInScope) {
						ScopeField f = new ScopeField();
						f.index = attrs.containsKey(s) ? attrIndex++ : -1;
						f.inScopeClass = false;
						f.name = s;
						f.parentType = attrs.containsKey(s) ? null : getFieldSafe(parentScopeClass, s).getType();
						f.type = f.parentType;
						list.add(f);
					}
				}
				
				Field[] fields = superScopeClass.getFields();
				for (Field field : fields) {
					if (field.getAnnotation(In.class) != null) {
						ScopeField f = new ScopeField();
						f.index = attrs.containsKey(field.getName()) ? attrIndex++ : -1;
						f.inScopeClass = true;
						f.name = field.getName();
						f.parentType = attrs.containsKey(field.getName()) ? null : getFieldSafe(parentScopeClass, field.getName()).getType();
						f.type = field.getType();
						list.add(f);
					}
				}
			} else {
				// nodevariables (värden från attribut i första hand, och i andra hand från parentscope)
				for (String s : nodeVariables) {
					ScopeField f = new ScopeField();
					f.index = attrs.containsKey(s) ? attrIndex++ : -1;
					f.inScopeClass = false;
					f.name = s;
					f.parentType = attrs.containsKey(s) ? null : getFieldSafe(parentScopeClass, s).getType();
					f.type = f.parentType;
					list.add(f);
				}
			}
			
		} else {
			if (hasDirectiveScope) {
//				@In (värden från attribut)
				Field[] fields = superScopeClass.getFields();
				for (Field field : fields) {
					if (field.getAnnotation(In.class) != null) {
						ScopeField f = new ScopeField();
						f.index = attrIndex++;
						f.inScopeClass = true;
						f.name = field.getName();
						f.parentType = null;
						f.type = field.getType();
						list.add(f);
					}
				}
			} else {
//				nodevariables  (värden från attribut)
				for (String s : nodeVariables) {
					ScopeField f = new ScopeField();
					f.index = attrIndex++;
					f.inScopeClass = false;
					f.name = s;
					f.parentType = null;
					f.type = null;
					list.add(f);
				}
			}
		}
		
		return list;
	}

	public static int directiveScopeSuffix = 0;
	
	@Override
	public void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			CompilerSession session) throws NoSuchScopeFieldException, CompileExpressionException {

//		List<String> inNames = null;
//		List<Class<?>> inTypes = null;
		
		List<ScopeField> fields = getFieldsForSubscope(directiveInstance.getScopeClass(), parentScopeClass);
		
		directiveScopeClass = createDirectiveScopeClass(session.getClassLoader(), Objects.firstNonNull(directiveInstance.getScopeClass(), Scope.class), parentScopeClass, session, fields);
		
//		if (hasDirectiveScope) {
//			directiveScopeClass = createDirectiveScopeClass(session.getClassLoader(),directiveInstance.getScopeClass(),parentScopeClass,session);
//			inNames = getDirectiveScopeInNames();
//			inTypes = getDirectiveScopeInTypes();
//		} else {
//			directiveScopeClass = createDirectiveScopeClass(session.getClassLoader(),Scope.class,parentScopeClass,session);
//			inNames = nodeVariables;
//		}

		List<ScopeField> attrFields = new ArrayList<ScopeField>();
		for (ScopeField field : fields) {
			if (field.index > -1) {
				attrFields.add(field);
			}
		}
		
		inExpressions = new CompiledExpression[attrFields.size()];
		for (ScopeField f : attrFields){
			inExpressions[f.index] = CompiledExpression.compile(attrs.get(f.name), parentScopeClass, session);
		}
		
		this.node.compileScope(directiveScopeClass, evaluationContextClass, session);
		
		Class<? extends DirectiveScopeValueCopier> valueCopierClass = createValueCopierClass(directiveScopeClass,parentScopeClass,
				fields,
				session.getClassLoader());
		try {
			this.valueCopier = valueCopierClass.newInstance();
				
			Class<? extends Injector> injectorClass = Injector.createInjectorClass(session, directiveInstance.getClass(), evaluationContextClass);
			this.injector = injectorClass.newInstance();
			
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	
	private static class ScopeField {
		public Class<?> type;
		public String name;
		public Class<?> parentType;
		public int index;
		public boolean inScopeClass;
	}
	
	
	private Class<? extends Scope> createDirectiveScopeClass(ClassLoader classLoader, 
			Class<? extends Scope> scopeClass, 
			Class<? extends Scope> parentScopeClass, 
			CompilerSession session, 
			List<ScopeField> fields) {
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;

		String className = "DirectiveScope" + directiveScopeSuffix++;
		
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null,
				Type.getInternalName(scopeClass), null);
		
		/* FIELDS */
		for (ScopeField field : fields) {
			if (!field.inScopeClass) {
				fv = cw.visitField(Opcodes.ACC_PUBLIC, 
						field.name, 
						field.type!=null ? Type.getDescriptor(field.type) : "Ljava/lang/Object;",
						null, null);
				fv.visitEnd();
			}
		}
		
//		for (String nodeVariable : nodeVariables) {
//			
//			fv = cw.visitField(Opcodes.ACC_PUBLIC, nodeVariable, "Ljava/lang/Object;",
//					null, null);
//			fv.visitEnd();
//		}
		
		/* CONSTRUCTOR */
		
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(scopeClass),
				"<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		cw.visitEnd();
		
		return JangularCompilerUtils.loadScopeClass(classLoader, cw.toByteArray(), className);
	}
	
	private Class<? extends DirectiveScopeValueCopier> createValueCopierClass(
			Class<? extends Scope> targetScopeClass, 
			Class<? extends Scope> parentScopeClass,
			List<ScopeField> fields, 
			ClassLoader classLoader){
		
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		String className = "DirectiveValueCopier" + directiveScopeSuffix++;
		
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
				"(Lnet/cupmanager/jangular/Scope;[Ljava/lang/Object;Lnet/cupmanager/jangular/Scope;)V", null, null);
		mv.visitCode();
		
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetScopeClass));
		mv.visitVarInsn(Opcodes.ASTORE, 4);
		
		mv.visitVarInsn(Opcodes.ALOAD, 3);
		mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(parentScopeClass));
		mv.visitVarInsn(Opcodes.ASTORE, 5);
		
		/**
		 * 1: targetScope
		 * 2: attributvärden[]
		 * 3. parentScope
		 * 4. targetScope typecastat
		 * 5. parentScope typecastat
		 */
		
		for (ScopeField field : fields) {
			mv.visitVarInsn(Opcodes.ALOAD, 4); // Ladda target
			
			if (field.index > -1) {
				mv.visitVarInsn(Opcodes.ALOAD, 2); // Ladda attributvärden
				mv.visitIntInsn(Opcodes.BIPUSH, field.index); // Ladda specifikt attributvärde
				mv.visitInsn(Opcodes.AALOAD);
				
				if (field.type != null) {
					JangularCompilerUtils.checkcast(field.type, Object.class, mv);
					mv.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(targetScopeClass), field.name,
							Type.getDescriptor(field.type));
				} else {
					mv.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(targetScopeClass), field.name,
							"Ljava/lang/Object;");
				}
			} else {
				mv.visitVarInsn(Opcodes.ALOAD, 5); // Ladda parent
				
				mv.visitFieldInsn(Opcodes.GETFIELD, 
						Type.getInternalName(parentScopeClass), 
						field.name,
						Type.getDescriptor(field.parentType));
				
				JangularCompilerUtils.checkcast(field.type, field.parentType, mv);
				
				mv.visitFieldInsn(Opcodes.PUTFIELD, 
						Type.getInternalName(targetScopeClass), 
						field.name,
						Type.getDescriptor(field.type));
			}
		}
		
//		
//		for (int i = 0; i < fieldNames.size(); i++) {
//			mv.visitVarInsn(Opcodes.ALOAD, 4); // Ladda target
//			mv.visitVarInsn(Opcodes.ALOAD, 2); // Ladda attributvärden
//			mv.visitIntInsn(Opcodes.BIPUSH, i);
//			mv.visitInsn(Opcodes.AALOAD);
//			if( fieldTypes != null ){
//				JangularCompilerUtils.checkcast(fieldTypes.get(0), Object.class, mv);
//				mv.visitFieldInsn(Opcodes.PUTFIELD, targetScopeClass.getName().replace('.', '/'), fieldNames.get(i),
//						Type.getDescriptor(fieldTypes.get(i)));
//			} else {
//				mv.visitFieldInsn(Opcodes.PUTFIELD, targetScopeClass.getName().replace('.', '/'), fieldNames.get(i),
//					"Ljava/lang/Object;");
//			}
//			
//		}
//		
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(4, 6);
		mv.visitEnd();
		
		cw.visitEnd();
		
		return JangularCompilerUtils.loadScopeClass(classLoader, cw.toByteArray(), className);
	}
	
	
	
	
	
	
	
	
	
	

	@Override
	public Collection<String> getReferencedVariables() throws CompileExpressionException {
//		nodeVariables = new ArrayList<String>(node.getReferencedVariables());
		return variables;
	}


	@Override
	public void eval(final Scope scope, StringBuilder sb, EvaluationContext context) 
			throws EvaluationException {
		
		Object[] inValues = new Object[inExpressions.length];
		for (int i = 0; i < inValues.length; i++ ) {
			inValues[i] = inExpressions[i].eval(scope);
		}
		
		try {
			Scope nodeScope = directiveScopeClass.newInstance();
			valueCopier.copy(nodeScope,inValues,scope);
			
			if( hasDirectiveScope ) {
				injector.inject(directiveInstance, context);
				directiveInstance.eval(nodeScope);
			}
			
			node.eval(nodeScope, sb, context);
		} catch (InstantiationException e) {
			throw new EvaluationException(this, e);
		} catch (IllegalAccessException e) {
			throw new EvaluationException(this, e);
		} 
	}

}
