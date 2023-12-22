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
            GeneralMatrixBuilder builder = GeneralMatrixBuilder.zeroBuilder(dimension);
            int count = 0;
            for (int j = 0; j < dimension.rowAsIntValue(); j++) {
                for (int k = 0; k < dimension.columnAsIntValue(); k++) {
                    count++;
                    builder.setValue(j, k, count);
                }
            }
            Matrix originalMatrix = builder.build();
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
