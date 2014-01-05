package net.cupmanager.jangular.expressions;

import java.util.Collection;

import net.cupmanager.jangular.JangularCompiler;
import net.cupmanager.jangular.Scope;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExecutableStatement;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public abstract class CompiledExpression {
	
	public abstract String evalToString(Scope scope);
	
	public abstract Object eval(Scope scope);
	
	
	public static Collection<String> getReferencedVariables(String expression){
		ParserContext pc = new ParserContext();
		MVEL.analyze(expression, pc);
		return pc.getInputs().keySet();
	}
	
	public static CompiledExpression compile(String expression, Class<? extends Scope> scopeClass){
		
		ParserContext pc = new ParserContext();
		
		expression = expression.trim();
		
		ExecutableStatement compiledExpression = (ExecutableStatement)MVEL.compileExpression(expression, pc);
		
		if( compiledExpression.isLiteralOnly() ) {
			Object value = MVEL.executeExpression(compiledExpression);
			return new ConstantExpression(value);
		}
		
		if( expression.matches("([a-zA-z_][a-zA-z0-9_]*)") ) {
			return generateByteCode(scopeClass, expression);
		}
		
		return new VariableExpression(compiledExpression);
	}
	
	private static int compiledExpressionSuffix = 0;
	
	private static CompiledExpression generateByteCode(Class<? extends Scope> scopeClass, String varName){
		
		String className = "CompiledExpression_" + scopeClass.getSimpleName() + (compiledExpressionSuffix++);
		String parentClassName = scopeClass.getName().replace('.', '/');
		Class<?> varType = null;
		
		try {
			varType = scopeClass.getField(varName).getType();
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null, CompiledExpression.class.getName().replace('.', '/'), null);

		// CONSTRUCTOR
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CompiledExpression.class.getName().replace('.', '/'), "<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		// EVAL()
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "evalToString", "(Lnet/cupmanager/jangular/Scope;)Ljava/lang/String;", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitTypeInsn(Opcodes.CHECKCAST, parentClassName);
		mv.visitFieldInsn(Opcodes.GETFIELD, parentClassName, varName, Type.getDescriptor(varType));
		box(varType, mv);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
	
		cw.visitEnd();
		
		// EVAL()
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "eval", "(Lnet/cupmanager/jangular/Scope;)Ljava/lang/Object;", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitTypeInsn(Opcodes.CHECKCAST, parentClassName);
		mv.visitFieldInsn(Opcodes.GETFIELD, parentClassName, varName, Type.getDescriptor(varType));
		box(varType, mv);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
	
		cw.visitEnd();
		
				
		Class<? extends CompiledExpression> cl = JangularCompiler.loadScopeClass(cw.toByteArray(), className);
		
		try {
			return cl.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
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
	
}
