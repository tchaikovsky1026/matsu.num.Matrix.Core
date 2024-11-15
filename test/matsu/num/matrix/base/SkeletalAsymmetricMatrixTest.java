package matsu.num.matrix.base;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link SkeletalAsymmetricMatrix} クラスの(実装規約違反の)テスト.
 */
@RunWith(Enclosed.class)
final class SkeletalAsymmetricMatrixTest {

    public static class 実装規約違反に関するテスト {

        @Test(expected = AssertionError.class)
        public void test_Symmetricが付与されているならAE() {

            class TestMatrix extends SkeletalAsymmetricMatrix<Matrix> implements Symmetric {

                @Override
                public MatrixDimension matrixDimension() {
                    throw new AssertionError("呼ばれない");
                }

                @Override
                public Vector operate(Vector operand) {
                    throw new AssertionError("呼ばれない");
                }

                @Override
                public Vector operateTranspose(Vector operand) {
                    throw new AssertionError("呼ばれない");
                }

                @Override
                protected Matrix createTranspose() {
                    throw new AssertionError("呼ばれない");
                }
            }

            new TestMatrix();
        }
    }
}
