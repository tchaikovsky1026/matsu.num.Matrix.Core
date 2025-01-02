package matsu.num.matrix.core.validation.constant;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;

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
