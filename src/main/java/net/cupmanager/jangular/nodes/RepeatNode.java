package net.cupmanager.jangular.nodes;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.compiler.JangularCompilerUtils;
import net.cupmanager.jangular.injection.EvaluationContext;

import org.mvel2.MVEL;
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
//	private Collection<String> nodeVariables;
	//private Method setMethod;
//	private RepeatNodeScope nodeScope;
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
	
	@Override
	public void eval(Scope scope, StringBuilder sb, EvaluationContext context) {
		try {
			List<?> list = (List<?>)MVEL.executeExpression(listExpression, scope);
			RepeatNodeScope nodeScope = nodeScopeClass.newInstance();
			if (list != null) {
				int i = 0;
				for (Object o : list) {
					
					if( i == 0 ){
					//	setMethod.invoke(nodeScope, parentScopeClass.cast(scope), i, (Object)o);
						nodeScope.iterate(scope, i, o);
					} else {
						nodeScope.iterate(i, o);
					}
					
					
					node.eval(nodeScope, sb, context);
					//sb.append(nodeScope);
					i++;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<String> getReferencedVariables() {
		Set<String> variables = new HashSet<String>();
		variables.addAll(node.getReferencedVariables());
		
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
			CompilerSession session) throws Exception {
		
		
		this.pc = ParserContext.create().withInput(varName, Iterable.class);
		pc.setStrictTypeEnforcement(true);
		pc.addInput("this", parentScopeClass);
		this.listExpression = MVEL.compileExpression("" + listExpressionString , pc);
		
		Class<?> varType = MVEL.analyze("this." + listVarName + ".get(0)", pc);
		
		String className = "RepeatScope" + (repeatScopeSuffix++);
		String parentClassName = parentScopeClass.getName().replace('.', '/');
		
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;

		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null, RepeatNodeScope.class.getName().replace('.', '/'), null);

		fv = cw.visitField(Opcodes.ACC_PUBLIC, "$index", "I", null, null);
		fv.visitEnd();
		
		fv = cw.visitField(Opcodes.ACC_PUBLIC, varName, Type.getDescriptor(varType), null, null);
		fv.visitEnd();
		
		for (String field : getReferencedVariables()) {
			Type type = Type.getType(parentScopeClass.getField(field).getType());
			fv = cw.visitField(Opcodes.ACC_PUBLIC, field, type.getDescriptor(), null, null);
			fv.visitEnd();
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
			Type type = Type.getType(parentScopeClass.getField(field).getType());
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 4);
			mv.visitFieldInsn(Opcodes.GETFIELD, parentClassName, field, type.getDescriptor());
			mv.visitFieldInsn(Opcodes.PUTFIELD, className, field, type.getDescriptor());
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

		/*
		public class RepeatScope {
			public int $index;
			public Object varName;
			public Object items;
			
			
			public void set(Parent parent, int $index, Object varName) {
				this.items = parent.items;
				this.$index = $index;
				this.varName = varName;
			}
		}
		 */
		
		Class<? extends RepeatNodeScope> cl = JangularCompilerUtils.loadScopeClass(session.getClassLoader(), cw.toByteArray(), className);
		
		//this.setMethod = cl.getMethod("set", parentScopeClass, int.class, Object.class);
//		this.nodeScope = cl.newInstance();
		this.nodeScopeClass = cl;
		
		
		node.compileScope(cl, evaluationContextClass, session);
	}

}
