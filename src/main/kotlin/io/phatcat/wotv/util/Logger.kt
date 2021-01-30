package io.phatcat.wotv.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object Log {

  // Verbose
  fun v(block: () -> String) = v(block())
  fun v(msg: String): Unit = logger().trace(msg)

  fun d(block: () -> String) = d(block())
  fun d(msg: String): Unit = logger().debug(msg)

  fun i(block: () -> String) = i(block())
  fun i(msg: String): Unit = logger().info(msg)

  fun w(block: () -> String) = w(block())
  fun w(msg: String): Unit = logger().warn(msg)

  fun e(block: () -> String) = e(block())
  fun e(msg: String): Unit = logger().error(msg)
}

//
// See: https://stackoverflow.com/questions/34416869/idiomatic-way-of-logging-in-kotlin
//

// Return logger for Java class, if companion object fix the name
fun <T : Any> logger(forClass: Class<T>): Logger {
  return LoggerFactory.getLogger(unwrapCompanionClass(forClass).name)
}

// unwrap companion class to enclosing class given a Java Class
fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
  return ofClass.enclosingClass?.takeIf {
    ofClass.enclosingClass.kotlin.objectInstance?.javaClass == ofClass
  } ?: ofClass
}

// unwrap companion class to enclosing class given a Kotlin Class
fun <T : Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> {
  return unwrapCompanionClass(ofClass.java).kotlin
}

// Return logger for Kotlin class
fun <T : Any> logger(forClass: KClass<T>): Logger {
  return logger(forClass.java)
}

// return logger from extended class (or the enclosing class)
fun <T : Any> T.logger(): Logger {
  return logger(this.javaClass)
}

// return a lazy logger property delegate for enclosing class
fun <R : Any> R.lazyLogger(): Lazy<Logger> {
  return lazy { logger(this.javaClass) }
}

// return a logger property delegate for enclosing class
fun <R : Any> R.injectLogger(): Lazy<Logger> {
  return lazyOf(logger(this.javaClass))
}

// marker interface and related extension (remove extension for Any.logger() in favour of this)
interface Loggable {}

fun Loggable.logger(): Logger = logger(this.javaClass)

// abstract base class to provide logging, intended for companion objects more than classes but works for either
abstract class WithLogging : Loggable {
  val LOG = logger()
}