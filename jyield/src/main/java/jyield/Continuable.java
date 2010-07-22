package jyield;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a method for runtime instrumentation.
 * Methods marked with this annotation should return Iterable<T>, Enumerable<T>, Iterator<T> or Continuation. 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Continuable {
	boolean autoJoin() default false;

}
