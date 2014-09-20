package net.cupmanager.jangular.nodes;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.compiler.JangularCompilerUtils;
import net.cupmanager.jangular.compiler.templateloader.NoSuchScopeFieldException;
import net.cupmanager.jangular.exceptions.CompileExpressionException;
import net.cupmanager.jangular.exceptions.EvaluationException;
import net.cupmanager.jangular.injection.EvaluationContext;

import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class RepeatNode extends JangularNode {
	
	public static class RepeatNodeScope extends Scope {
		public void iterate(Scope parent, int i, Object object){}
		public void iterate(int i, Object object){}
	}
	
	
	private String varName;
	private Serializable listExpression;
	private JangularNode node;
	private ParserContext pc;
	private Class<? extends RepeatNodeScope> nodeScopeClass;
	private String listExpressionString;
	private String listVarName;

	public RepeatNode(String varName, Serializable listExpression, JangularNode node) {
		this.varName = varName;
		this.listExpression = listExpression;
		this.node = node;
	}
	
	public RepeatNode(String expression, JangularNode node) {
		String[] parts = expression.split(" in ");
		this.varName = parts[0].trim();
		
		this.listExpressionString = parts[1];
		
		int indexOfIf = listExpressionString.indexOf(" if ");
		if (indexOfIf > -1) {
			this.listVarName = listExpressionString.substring(0, indexOfIf);
			listExpressionString = "($ in " + listExpressionString + ")";
		} else {
			this.listVarName = listExpressionString;
		}
		listExpressionString = listExpressionString.replace(listVarName, "this."+listVarName);
		
		
		this.node = node;
	}
	
	private RepeatNode() {
	}
	@Override
	public synchronized void eval(Scope scope, StringBuilder sb, EvaluationContext context, EvaluationSession session) throws EvaluationException {
		try {
			Collection<?> list = (Collection<?>)MVEL.executeExpression(listExpression, scope);
			RepeatNodeScope nodeScope = nodeScopeClass.newInstance();
			if (list != null) {
				int i = 0;
				for (Object o : list) {
					
					if( i == 0 ){
						nodeScope.iterate(scope, i, o);
					} else {
						nodeScope.iterate(i, o);
					}
					
					session.eval(node, nodeScope, sb, context);
					i++;
				}
			}
		} catch (InstantiationException e) {
			throw new EvaluationException(node, e);
		} catch (IllegalAccessException e) {
			throw new EvaluationException(node, e);
		} 
	}

	@Override
	public Collection<String> getReferencedVariables() throws CompileExpressionException {
		Set<String> variables = new HashSet<String>();
		variables.addAll(node.getReferencedVariables());
		
		// if listExpression is like "this.base.years", add "base" to variables.
		int thisIndex = listExpressionString.indexOf("this.");
		String e = listExpressionString.substring(thisIndex+5);
		int idx = e.indexOf(".");
		if (idx < 0) idx=e.length();
		
		variables.add(e.substring(0, idx));
		
//		variables.addAll(pc.getInputs().keySet());
		variables.remove("$index");
		variables.remove(varName);
		variables.remove("$");
		
		return variables;
	}
	
	public static int repeatScopeSuffix = 0;
	
	
	@Override
	public void compileScope(Class<? extends Scope> parentScopeClass, 
			Class<? extends EvaluationContext> evaluationContextClass,
			CompilerSession session) throws NoSuchScopeFieldException, CompileExpressionException {
		
		Class<?> varType;
		
		try {
			ParserConfiguration conf = new ParserConfiguration();
			conf.setClassLoader(session.getClassLoader());
			this.pc = new ParserContext(conf);
			pc.withInput(varName, Iterable.class);
			pc.setStrictTypeEnforcement(true);
			pc.addInput("this", parentScopeClass);
			this.listExpression = MVEL.compileExpression("" + listExpressionString , pc);
			
			varType = MVEL.analyze("this." + listVarName + ".iterator().next()", pc);
		} catch (CompileException e ) {
			throw new CompileExpressionException(e);
		}
		
		String className = "RepeatScope" + (repeatScopeSuffix++);
		String parentClassName = parentScopeClass.getName().replace('.', '/');
		
		Class<? extends RepeatNodeScope> cl = createRepeatScopeClass(parentScopeClass, session, varType, className, parentClassName);
		
		this.nodeScopeClass = cl;
		
		node.compileScope(cl, evaluationContextClass, session);
	}

	private Class<? extends RepeatNodeScope> createRepeatScopeClass(
			Class<? extends Scope> parentScopeClass, CompilerSession session,
			Class<?> varType, String className, String parentClassName) throws NoSuchScopeFieldException, CompileExpressionException {
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;

		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null, RepeatNodeScope.class.getName().replace('.', '/'), null);

		fv = cw.visitField(Opcodes.ACC_PUBLIC, "$index", "I", null, null);
		fv.visitEnd();
		
		fv = cw.visitField(Opcodes.ACC_PUBLIC, varName, Type.getDescriptor(varType), null, null);
		fv.visitEnd();
		
		for (String field : getReferencedVariables()) {
			try {
				Type type = Type.getType(parentScopeClass.getField(field).getType());
				fv = cw.visitField(Opcodes.ACC_PUBLIC, field, type.getDescriptor(), null, null);
				fv.visitEnd();
			} catch (NoSuchFieldException e) {
				throw new NoSuchScopeFieldException(e);
			} catch (SecurityException e) {
				throw new NoSuchScopeFieldException(e);
			}
		}
		
		// CONSTRUCTOR
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, RepeatNodeScope.class.getName().replace('.', '/'), "<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		
		// ITERATE()
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "iterate", "(Lnet/cupmanager/jangular/Scope;ILjava/lang/Object;)V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitTypeInsn(Opcodes.CHECKCAST, parentClassName);
		mv.visitVarInsn(Opcodes.ASTORE, 4);
		
		for (String field : getReferencedVariables()) {
			try {
				Type type = Type.getType(parentScopeClass.getField(field).getType());
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ALOAD, 4);
				mv.visitFieldInsn(Opcodes.GETFIELD, parentClassName, field, type.getDescriptor());
				mv.visitFieldInsn(Opcodes.PUTFIELD, className, field, type.getDescriptor());
			} catch (NoSuchFieldException e) {
				throw new NoSuchScopeFieldException(e);
			} catch (SecurityException e) {
				throw new NoSuchScopeFieldException(e);
			}
		}
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ILOAD, 2);
		mv.visitFieldInsn(Opcodes.PUTFIELD, className, "$index", "I");
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 3);
		mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(varType));
		mv.visitFieldInsn(Opcodes.PUTFIELD, className, varName, Type.getDescriptor(varType));
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(2, 5);
		mv.visitEnd();
	
		cw.visitEnd();
		
		// ITERATE2()
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "iterate", "(ILjava/lang/Object;)V", null, null);
		mv.visitCode();
		
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ILOAD, 1);
		mv.visitFieldInsn(Opcodes.PUTFIELD, className, "$index", "I");
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(varType));
		mv.visitFieldInsn(Opcodes.PUTFIELD, className, varName, Type.getDescriptor(varType));
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
	
		cw.visitEnd();

		
		Class<? extends RepeatNodeScope> cl = JangularCompilerUtils.loadScopeClass(session.getClassLoader(), cw.toByteArray(), className);
		return cl;
	}


	@Override
	public JangularNode clone() {
		RepeatNode rn = new RepeatNode();
		rn.varName = varName;
		rn.listExpression = listExpression;
		rn.node = node.clone();
		rn.pc = pc;
		rn.nodeScopeClass = nodeScopeClass;
		rn.listExpressionString = listExpressionString;
		rn.listVarName = listVarName;
		return rn;
	}
}
