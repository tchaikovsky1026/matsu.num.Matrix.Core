package matsu.num.matrix.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link Matrix}インターフェースのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class MatrixTest {

    public static class toStringの表示 {

        private List<Matrix> matrixs;

        @Before
        public void before_長方形行列の生成() {
            if (Objects.isNull(matrixs)) {
                matrixs = new ArrayList<>();
            }
            MatrixDimension dimension = MatrixDimension.rectangle(3, 4);
            Matrix originalMatrix = GeneralMatrixBuilder.zeroBuilder(dimension)
                    .setValue(0, 0, 1).setValue(0, 1, 2).setValue(0, 2, 3).setValue(0, 3, 4)
                    .setValue(1, 0, 5).setValue(1, 1, 6).setValue(1, 2, 7).setValue(1, 3, 8)
                    .setValue(2, 0, 9).setValue(2, 1, 10).setValue(2, 2, 11).setValue(2, 3, 12)
                    .build();
            matrixs.add(originalMatrix);
        }

        @Test
        public void test_toString表示_Matrix提供() {
            System.out.println(Matrix.class.getName() + ":");
            matrixs.stream()
                    .forEach(m -> {
                        System.out.println(Matrix.toString(m));
                        System.out.println(Matrix.toString(m, "X", "YY"));
                    });
            System.out.println();
        }
    }

}
