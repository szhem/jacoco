/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergey Zhemzhitsky - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ScalaLoggingFilter}.
 */
public class ScalaLoggingFilterTest extends FilterTestBase {

	private final ScalaLoggingFilter filter = new ScalaLoggingFilter();

	@Before
	public void setUp() {
		context.getClassAnnotations()
				.add(ScalaFilter.SCALA_SIGNATURE_ANNOTATION);
	}

	/**
	 * import java.util.logging._
	 *
	 * object JavaUtilLogging {
	 *
	 *   val logger: Logger = Logger.getLogger(this.getClass.getName)
	 *
	 *   def main(args: Array[String]): Unit = {
	 *     print("Hello")
	 *     if (logger.isLoggable(Level.INFO)) {
	 *       logger.log(Level.INFO, "Hello World!")
	 *     }
	 *     print(" World")
	 *     if (logger.isLoggable(Level.FINE)) {
	 *       logger.log(Level.FINE, "Bye World!")
	 *     }
	 *     print("!")
	 *   }
	 * }
	 */
	@Test
	public void should_filter_jul() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"jul", "()V", null, null);

		AbstractInsnNode from;
		AbstractInsnNode to;
		final Range[] ranges = new Range[2];

		// print("Hello")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn("Hello");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		// logger.isLoggable(Level.INFO)
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Ljava/util/logging/Logger;", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "java/util/logging/Level", "INFO",
				"Ljava/util/logging/Level;");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/logging/Logger",
				"isLoggable", "(Ljava/util/logging/Level;)Z", false);

		// if (...) {
		//   logger.log(Level.INFO, "Hello World!")
		// }
		final Label lblInfo = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, lblInfo);

		from = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Ljava/util/logging/Logger;", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "java/util/logging/Level", "INFO",
				"Ljava/util/logging/Level;");
		m.visitLdcInsn("Hello World!");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/logging/Logger",
				"log", "(Ljava/util/logging/Level;Ljava/lang/String;)V", false);

		to = m.instructions.getLast();
		ranges[0] = new Range(from, to);

		m.visitLabel(lblInfo);

		// print(" World")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn(" World");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		// logger.isLoggable(Level.FINE)
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Ljava/util/logging/Logger;", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "java/util/logging/Level", "FINE",
				"Ljava/util/logging/Level;");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/logging/Logger",
				"isLoggable", "(Ljava/util/logging/Level;)Z", false);

		// if (...) {
		//   logger.log(Level.FINE, "Bye World!")
		// }
		final Label lblFine = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, lblFine);

		from = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Ljava/util/logging/Logger;", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "java/util/logging/Level", "FINE",
				"Ljava/util/logging/Level;");
		m.visitLdcInsn("Bye World!");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/logging/Logger",
				"log", "(Ljava/util/logging/Level;Ljava/lang/String;)V", false);

		to = m.instructions.getLast();
		ranges[1] = new Range(from, to);

		m.visitLabel(lblFine);

		// print("!")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn("!");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(ranges);
	}

	/**
	 * import org.slf4j._
	 *
	 * object Slf4jLogging {
	 *
	 *   val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)
	 *
	 *   def main(args: Array[String]): Unit = {
	 *     print("Hello")
	 *     if (logger.isInfoEnabled()) {
	 *       logger.info("Hello World!")
	 *     }
	 *     print(" World")
	 *     if (logger.isDebugEnabled()) {
	 *       logger.debug("Bye World!")
	 *     }
	 *     print("!")
	 *   }
	 * }
	 */
	@Test
	public void should_filter_slf4j() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"slf4j", "()V", null, null);

		AbstractInsnNode from;
		AbstractInsnNode to;
		final Range[] ranges = new Range[2];

		// print("Hello")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn("Hello");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		// logger.isInfoEnabled()
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Lorg/slf4j/Logger;", false);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/slf4j/Logger",
				"isInfoEnabled", "()Z", true);

		// if (...) {
		//   logger.info("Hello World!")
		// }
		final Label lblInfo = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, lblInfo);

		from = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Lorg/slf4j/Logger;", false);
		m.visitLdcInsn("Hello World!");
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/slf4j/Logger",
				"info", "(Ljava/lang/String;)V", true);

		to = m.instructions.getLast();
		ranges[0] = new Range(from, to);

		m.visitLabel(lblInfo);

		// print(" World")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn(" World");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		// logger.isDebugEnabled()
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Lorg/slf4j/Logger;", false);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/slf4j/Logger",
				"isDebugEnabled", "()Z", true);

		// if (...) {
		//   logger.debug("Bye World!")
		// }
		final Label lblDebug = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, lblDebug);

		from = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Lorg/slf4j/Logger;", false);
		m.visitLdcInsn("Bye World!");
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/slf4j/Logger",
				"debug", "(Ljava/lang/String;)V", true);

		to = m.instructions.getLast();
		ranges[1] = new Range(from, to);

		m.visitLabel(lblDebug);

		// print("!")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn("!");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(ranges);
	}


	/**
	 * import org.apache.log4j._
	 *
	 * object Log4jLogging {
	 *
	 *   val logger: Logger = Logger.getLogger(this.getClass.getName)
	 *
	 *   def main(args: Array[String]): Unit = {
	 *     print("Hello")
	 *     if (logger.isInfoEnabled()) {
	 *       logger.info("Hello World!")
	 *     }
	 *     print(" World")
	 *     if (logger.isEnabledFor(Priority.DEBUG)) {
	 *       logger.debug("Bye World!")
	 *     }
	 *     print("!")
	 *   }
	 *
	 * }
	 */
	@Test
	public void should_filter_log4j() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"log4j", "()V", null, null);

		AbstractInsnNode from;
		AbstractInsnNode to;
		final Range[] ranges = new Range[2];

		// print("Hello")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn("Hello");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		// logger.isInfoEnabled()
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Lorg/apache/log4j/Logger;", false);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/apache/log4j/Logger",
				"isInfoEnabled", "()Z", false);

		// if (...) {
		//   logger.info("Hello World!")
		// }
		final Label lblInfo = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, lblInfo);

		from = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Lorg/apache/log4j/Logger;", false);
		m.visitLdcInsn("Hello World!");
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/apache/log4j/Logger",
				"info", "(Ljava/lang/Object;)V", true);

		to = m.instructions.getLast();
		ranges[0] = new Range(from, to);

		m.visitLabel(lblInfo);

		// print(" World")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn(" World");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		// logger.isEnabledFor(Priority.DEBUG)
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Lorg/slf4j/Logger;", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "org/apache/log4j/Priority",
				"DEBUG", "Lorg/apache/log4j/Priority;");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/apache/log4j/Logger",
				"isEnabledFor", "(Lorg/apache/log4j/Priority;)Z", false);

		// if (...) {
		//   logger.debug("Bye World!")
		// }
		final Label lblDebug = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, lblDebug);

		from = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Lorg/apache/log4j/Logger;", false);
		m.visitLdcInsn("Bye World!");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/apache/log4j/Logger",
				"debug", "(Ljava/lang/Object;)V", false);

		to = m.instructions.getLast();
		ranges[1] = new Range(from, to);

		m.visitLabel(lblDebug);

		// print("!")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn("!");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(ranges);
	}

	/**
	 * import org.apache.logging.log4j._
	 * import org.apache.logging.log4j.scala._
	 *
	 * object Log4j2LoggingScala extends Logging {
	 *
	 *   def main(args: Array[String]): Unit = {
	 *     print("Hello")
	 *     logger.info("Hello World!")
	 *     print(" World")
	 *     logger(Level.DEBUG, "Bye World!")
	 *     print("!")
	 *   }
	 *
	 * }
	 */
	@Test
	public void should_filter_log4j2_scala() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"log4j", "()V", null, null);

		AbstractInsnNode from;
		AbstractInsnNode to;
		final Range[] ranges = new Range[2];

		// print("Hello")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn("Hello");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		// logger.isEnabled(Level.INFO)
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className,
				"logger", "()Lorg/apache/logging/log4j/spi/ExtendedLogger;",
				false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "org/apache/logging/log4j/Level",
				"INFO", "Lorg/apache/logging/log4j/Level;");
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"org/apache/logging/log4j/spi/ExtendedLogger", "isEnabled",
				"(Lorg/apache/logging/log4j/Level;)Z", true);


		// logger.info("Hello World!")
		final Label lblInfo = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, lblInfo);

		from = m.instructions.getLast();

		m.visitFieldInsn(Opcodes.GETSTATIC,
				"org/apache/logging/log4j/scala/Logger$", "MODULE$",
				"Lorg/apache/logging/log4j/scala/Logger$;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className, "logger",
				"()Lorg/apache/logging/log4j/spi/ExtendedLogger;", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "org/apache/logging/log4j/Level",
				"INFO", "Lorg/apache/logging/log4j/Level;");
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitLdcInsn("Hello World!");
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"org/apache/logging/log4j/scala/Logger$",
				"logMessage$extension1",
				"(Lorg/apache/logging/log4j/spi/ExtendedLogger;" +
						"Lorg/apache/logging/log4j/Level;" +
						"Lorg/apache/logging/log4j/Marker;Ljava/lang/CharSequence;" +
						"Ljava/lang/Throwable;)V", true);
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/runtime/BoxedUnit", "UNIT",
				"Lscala/runtime/BoxedUnit;");

		Label lblGoto = new Label();
		m.visitJumpInsn(Opcodes.GOTO, lblGoto);

		to = m.instructions.getLast();
		ranges[0] = new Range(from, to);
		m.visitLabel(lblInfo);

		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/runtime/BoxedUnit", "UNIT",
				"Lscala/runtime/BoxedUnit;");
		m.visitLabel(lblGoto);
		m.visitInsn(Opcodes.POP);

		// print(" World")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn(" World");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		// logger.isEnabled(Level.DEBUG)
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className, "logger",
				"()Lorg/apache/logging/log4j/spi/ExtendedLogger;", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "org/apache/logging/log4j/Level",
				"DEBUG", "Lorg/apache/logging/log4j/Level;");
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE,
				"org/apache/logging/log4j/spi/ExtendedLogger", "isEnabled",
				"(Lorg/apache/logging/log4j/Level;)Z", true);

		final Label lblDebug = new Label();
		m.visitJumpInsn(Opcodes.IFEQ, lblDebug);

		from = m.instructions.getLast();

		// logger.debug("Bye World!")
		m.visitFieldInsn(Opcodes.GETSTATIC,
				"org/apache/logging/log4j/scala/Logger$", "MODULE$",
				"Lorg/apache/logging/log4j/scala/Logger$;");
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className, "logger",
				"()Lorg/apache/logging/log4j/spi/ExtendedLogger;", false);
		m.visitFieldInsn(Opcodes.GETSTATIC, "org/apache/logging/log4j/Level",
				"DEBUG", "Lorg/apache/logging/log4j/Level;");
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitLdcInsn("Bye World!");
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"org/apache/logging/log4j/scala/Logger$",
				"logMessage$extension1",
				"(Lorg/apache/logging/log4j/spi/ExtendedLogger;" +
						"Lorg/apache/logging/log4j/Level;" +
						"Lorg/apache/logging/log4j/Marker;Ljava/lang/CharSequence;" +
						"Ljava/lang/Throwable;)V", true);
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/runtime/BoxedUnit", "UNIT",
				"Lscala/runtime/BoxedUnit;");

		lblGoto = new Label();
		m.visitJumpInsn(Opcodes.GOTO, lblGoto);

		to = m.instructions.getLast();
		ranges[1] = new Range(from, to);
		m.visitLabel(lblDebug);

		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/runtime/BoxedUnit", "UNIT",
				"Lscala/runtime/BoxedUnit;");
		m.visitLabel(lblGoto);
		m.visitInsn(Opcodes.POP);

		// print("!")
		m.visitFieldInsn(Opcodes.GETSTATIC, "scala/Predef$", "MODULE$",
				"Lscala/Predef$;");
		m.visitLdcInsn("!");
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "scala/Predef$", "print",
				"(Ljava/lang/Object;)V", false);

		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(ranges);
	}


}
