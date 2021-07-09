/**
 * Code from https://github.com/adrianwalker/multiline-string
 * Based on Adrian Walker's blog posts:
 *  -http://www.adrianwalker.org/2011/12/java-multiline-string.html
 *  -http://www.adrianwalker.org/2018/08/java-910-multiline-string.html
 */
package org.adrianwalker.multilinestring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Multiline {
}
