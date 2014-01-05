import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class Asmtest {
	public static void main(String[] args) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(51, Opcodes.ACC_PUBLIC, "MaxTestClass", null, "java/lang/Object", null);
		
		MethodVisitor mv;
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		 mv.visitVarInsn(Opcodes.ALOAD, 0);
		 mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		 mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		
		
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "set", "(LLikeAScopeParent;Ljava/lang/String;)V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitFieldInsn(Opcodes.GETFIELD, "LikeAScopeParent", "name", "Ljava/lang/Object;");
		mv.visitFieldInsn(Opcodes.PUTFIELD, "MaxTestClass", "name", "Ljava/lang/Object;");
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitFieldInsn(Opcodes.GETFIELD, "LikeAScopeParent", "nr2", "Ljava/lang/Object;");
		mv.visitFieldInsn(Opcodes.PUTFIELD, "MaxTestClass", "nr2", "Ljava/lang/Object;");
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toUpperCase", "()Ljava/lang/String;");
		mv.visitFieldInsn(Opcodes.PUTFIELD, "MaxTestClass", "annan", "Ljava/lang/Object;");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
		
		
		FieldVisitor fv = cw.visitField(Opcodes.ACC_PUBLIC, "name", "Ljava/lang/Object;", null, "Max");
		fv.visitEnd();
		fv = cw.visitField(Opcodes.ACC_PUBLIC, "nr2", "Ljava/lang/Object;", null, "Max");
		fv.visitEnd();
		fv = cw.visitField(Opcodes.ACC_PUBLIC, "annan", "Ljava/lang/Object;", null, "Max");
		fv.visitEnd();
		cw.visitEnd();
		
		byte[] bytecode = cw.toByteArray();

		System.out.println(new String(bytecode));
		
		
		Class cl = loadClass(bytecode);
		System.out.println(cl);
		
		Object m;
		try {
			m = cl.newInstance();
			
			
			LikeAScopeParent parent = new LikeAScopeParent();
			parent.name = "Parent";
			parent.nr2 = 13;
			Method set = cl.getMethod("set", LikeAScopeParent.class, String.class);
			Object result = set.invoke(m, parent, "Svejsan");
			
			System.out.println( "Name:" + cl.getDeclaredField("name").get(m) );
			System.out.println( "Name:" + cl.getDeclaredField("nr2").get(m) );
			System.out.println( "Name:" + cl.getDeclaredField("annan").get(m) );
			
			System.out.println(m);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private static Class loadClass(byte[] b) {
		// override classDefine (as it is protected) and define the class.
		Class clazz = null;
		try {
			ClassLoader loader = ClassLoader.getSystemClassLoader();
			Class cls = Class.forName("java.lang.ClassLoader");
			java.lang.reflect.Method method = cls.getDeclaredMethod(
					"defineClass", new Class[] { String.class, byte[].class,
							int.class, int.class });

			// protected method invocaton
			method.setAccessible(true);
			try {
				Object[] args = new Object[] { "MaxTestClass", b, new Integer(0),
						new Integer(b.length) };
				clazz = (Class) method.invoke(loader, args);
			} finally {
				method.setAccessible(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return clazz;
	}

}

