/**
 * 
 */
package com.borunovv.common;

/**
 * @author borunovv
 * @date 26-01-2017
 */
public final class Assert {

    public static void isTrue(boolean condition, String msg) {
        if (! condition) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void notNull(Object obj, String msg) {
        if (obj == null) {
            throw new IllegalArgumentException(msg);
        }
    }
}
