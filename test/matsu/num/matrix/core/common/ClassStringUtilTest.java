/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.common;

import static matsu.num.matrix.core.common.ClassStringUtil.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link ClassStringUtil} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class ClassStringUtilTest {

    public static final Class<?> TEST_CLASS = ClassStringUtil.class;

    public static class classStringの表示 {

        @Test
        public void test_表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println("null: " + getClassString(null));
            System.out.println("Outer: " + getClassString(new ClassStringUtilTest()));
            System.out.println("staticNest: " + getClassString(new StaticNest.StaticNestNest()));
            System.out.println("Inner: " + getClassString(new ClassStringUtilTest().new Inner()));
            System.out.println("Local: " + getClassString(StaticNest.getLocal()));
            System.out.println("Anonymous: " + getClassString(StaticNest.getAnonymous()));
            System.out.println("String[]: " + getClassString(new String[1]));
            System.out.println("int[]: " + getClassString(new int[1]));
            System.out.println("int[][]: " + getClassString(new int[1][1]));
            System.out.println();
        }
    }

    private static class StaticNest {

        static Object getLocal() {
            class Local {
            }
            return new Local();
        }

        static Object getAnonymous() {
            return new Object() {
                @Override
                public String toString() {
                    return "Anonymous";
                }
            };
        }

        private static class StaticNestNest {
        }
    }

    private class Inner {
    }
}
