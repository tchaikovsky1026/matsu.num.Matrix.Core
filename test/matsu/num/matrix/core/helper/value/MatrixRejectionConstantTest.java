/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package matsu.num.matrix.core.helper.value;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link MatrixRejectionConstant} クラスのテスト.
 */
@RunWith(Enclosed.class)
final class MatrixRejectionConstantTest {

    public static final Class<?> TEST_CLASS = MatrixRejectionConstant.class;

    public static final class toString表示 {

        @Test
        public void test_toString() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(MatrixRejectionConstant.REJECTED_BY_NOT_SQUARE.get().getException("text"));
            System.out.println(MatrixRejectionConstant.REJECTED_BY_NOT_SYMMETRIC.get().getException("text"));
            System.out.println(MatrixRejectionConstant.REJECTED_BY_TOO_MANY_ELEMENTS.get().getException("text"));
            System.out.println();
        }
    }
}
