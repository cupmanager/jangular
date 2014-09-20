package net.cupmanager.jangular.expressions;

import java.lang.reflect.Field;
import java.util.Collection;

import net.cupmanager.jangular.Scope;
import net.cupmanager.jangular.compiler.CompilerSession;
import net.cupmanager.jangular.compiler.JangularCompilerUtils;
import net.cupmanager.jangular.exceptions.CompileExpressionException;

import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExecutableStatement;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public abstract class CompiledExpression {
	
	public abstract String evalToString(Scope scope);
	
	public abstract Object eval(Scope scope);
	
	
	public static Collection<String> getReferencedVariables(String expression) throws CompileExpressionException{
		ParserContext pc = new ParserContext();
		try {
			MVEL.analyze(expression, pc);
		} catch (CompileException e ) {
			throw new CompileExpressionException(e);
		}
		return pc.getInputs().keySet();
	}
	
	public static CompiledExpression compile(String expression, Class<? extends Scope> scopeClass, CompilerSession session) throws CompileExpressionException{
		
		ParserConfiguration conf = new ParserConfiguration();
		conf.setClassLoader(session.getClassLoader());
		ParserContext pc = new ParserContext(conf);
		
		expression = expression.trim();
		ExecutableStatement compiledExpression;
		try {
			pc.setStrictTypeEnforcement(true);
			pc.addInput("this", scopeClass);
			pc.addPackageImport("java.util");
			for (Field f : scopeClass.getFields()) {
				pc.addInput(f.getName(), f.getType());
			}
			compiledExpression = (ExecutableStatement)MVEL.compileExpression(expression, pc);
		} catch (CompileException e ) {
			throw new CompileExpressionException(e);
		}
		
		if( compiledExpression.isLiteralOnly() ) {
			Object value = MVEL.executeExpression(compiledExpression);
			return new ConstantExpression(value);
		}
		
		if( !expression.contains("[") && expression.matches("^([a-zA-z_][a-zA-z0-9_\\.]*)$") ) {
			try {
				return generateByteCode(session.getClassLoader(), scopeClass, expression);
			} catch (Exception e) {
				// Maybe some problem with types. That's okay, let MVEL handle it.
				e.printStackTrace();
				session.warn("CompiledExpression couldn't generate bytecode-class for expression \""+expression+"\". Caused by:\n" + e);
			}
		}
		
		return new VariableExpression(compiledExpression);
	}
	
	private static int compiledExpressionSuffix = 0;
	

	private static CompiledExpression generateByteCode(ClassLoader classLoader, Class<? extends Scope> scopeClass, String expression) throws NoSuchFieldException, SecurityException{
		
		String className = "CompiledExpression_" + scopeClass.getSimpleName() + "_" + (compiledExpressionSuffix++);
		String parentClassName = Type.getInternalName(scopeClass);
		
		String[] parts = expression.split("\\.");
		
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;
	
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null, Type.getInternalName(CompiledExpression.class), null);
	
		// CONSTRUCTOR
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(CompiledExpression.class), "<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		// EVAL()
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "evalToString", "(Lnet/cupmanager/jangular/Scope;)Ljava/lang/String;", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitTypeInsn(Opcodes.CHECKCAST, parentClassName);
		Class<?> lastType = scopeClass;
		for (String part : parts) {
			Class<?> type = lastType.getField(part).getType();
			mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(lastType), part, Type.getDescriptor(type));
			lastType = type;
		}
		box(lastType, mv);
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
		lastType = scopeClass;
		for (String part : parts) {
			Class<?> type = lastType.getField(part).getType();
			mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(lastType), part, Type.getDescriptor(type));
			lastType = type;
		}
		box(lastType, mv);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
	
		cw.visitEnd();
		
		
		Class<? extends CompiledExpression> cl = JangularCompilerUtils.loadScopeClass(classLoader, cw.toByteArray(), className);
		
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
