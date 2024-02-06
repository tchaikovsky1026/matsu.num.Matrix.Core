package matsu.num.matrix.base.nlsf;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.GeneralMatrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.SymmetricMatrix;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.validation.MatrixNotSymmetricException;

/**
 * {@link ModifiedCholeskyPivotingExecutor}クラスのテスト.
 * 
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
public class ModifiedCholeskyPivotingExecutorTest {

    public static final Class<?> TEST_CLASS = ModifiedCholeskyPivotingExecutor.class;

    public static class 生成に関する {

        @Test(expected = MatrixNotSymmetricException.class)
        public void test_非対称行列は使用できないMNSEx() {
            ModifiedCholeskyPivotingExecutor.instance().apply(
                    GeneralMatrix.Builder.zero(MatrixDimension.square(3)).build());
        }
    }

    public static class 特異行列での振る舞い検証 {

        private EntryReadableMatrix matrix;

        @Before
        public void before_行列の準備() {
            //特異行列である
            /*
             * 0 2 0 0
             * 2 5 0 0
             * 0 0 1 -1
             * 0 0 -1 1
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(4));
            builder.setValue(0, 0, 0);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 5);
            builder.setValue(2, 2, 1);
            builder.setValue(3, 2, -1);
            builder.setValue(3, 3, 1);
            matrix = builder.build();
        }

        @Test
        public void test_行列分解の失敗() {
            Optional<? extends LUTypeSolver> cho = ModifiedCholeskyPivotingExecutor.instance().apply(matrix);
            assertThat(cho.isEmpty(), is(true));
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元6_パターン1 {

        private EntryReadableMatrix matrix;
        private LUTypeSolver mcp;

        @Before
        public void before_次元6の正方行列のソルバを用意する() {
            /*
             * 1 3 6 10 15 21
             * 3 2 5 9 14 20
             * 6 5 4 8 13 19
             * 10 9 8 7 12 18
             * 15 14 13 12 11 17
             * 21 20 19 18 17 16
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(6));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 1, 2);
            builder.setValue(1, 0, 3);
            builder.setValue(2, 2, 4);
            builder.setValue(2, 1, 5);
            builder.setValue(2, 0, 6);
            builder.setValue(3, 3, 7);
            builder.setValue(3, 2, 8);
            builder.setValue(3, 1, 9);
            builder.setValue(3, 0, 10);
            builder.setValue(4, 4, 11);
            builder.setValue(4, 3, 12);
            builder.setValue(4, 2, 13);
            builder.setValue(4, 1, 14);
            builder.setValue(4, 0, 15);
            builder.setValue(5, 5, 16);
            builder.setValue(5, 4, 17);
            builder.setValue(5, 3, 18);
            builder.setValue(5, 2, 19);
            builder.setValue(5, 1, 20);
            builder.setValue(5, 0, 21);
            matrix = builder.build();
            mcp = ModifiedCholeskyPivotingExecutor.instance().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(mcp.determinant(), is(closeTo(-43074.0, 1E-9)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(mcp.logAbsDeterminant(), is(closeTo(Math.log(43074.0), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(mcp.signOfDeterminant(), is(-1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(mcp.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }

        @Test
        public void test_逆行列生成の実装に関する() {

            //注意:このテストは実装の詳細に依存している

            //逆行列の複数回の呼び出しは同一インスタンスを返す
            assertThat(mcp.inverse() == mcp.inverse(), is(true));
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元6_パターン2 {

        private EntryReadableMatrix matrix;
        private LUTypeSolver mcp;

        @Before
        public void before_次元6の正方行列のソルバを用意する() {
            /*
             * 1 2 4 7 11 16
             * 2 3 5 8 12 17
             * 4 5 6 9 13 18
             * 7 8 9 10 14 19
             * 11 12 13 14 15 20
             * 16 17 18 19 20 21
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(6));
            builder.setValue(0, 0, 1);
            builder.setValue(1, 0, 2);
            builder.setValue(1, 1, 3);
            builder.setValue(2, 0, 4);
            builder.setValue(2, 1, 5);
            builder.setValue(2, 2, 6);
            builder.setValue(3, 0, 7);
            builder.setValue(3, 1, 8);
            builder.setValue(3, 2, 9);
            builder.setValue(3, 3, 10);
            builder.setValue(4, 0, 11);
            builder.setValue(4, 1, 12);
            builder.setValue(4, 2, 13);
            builder.setValue(4, 3, 14);
            builder.setValue(4, 4, 15);
            builder.setValue(5, 0, 16);
            builder.setValue(5, 1, 17);
            builder.setValue(5, 2, 18);
            builder.setValue(5, 3, 19);
            builder.setValue(5, 4, 20);
            builder.setValue(5, 5, 21);
            matrix = builder.build();
            mcp = ModifiedCholeskyPivotingExecutor.instance().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(mcp.determinant(), is(closeTo(-24.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(mcp.logAbsDeterminant(), is(closeTo(Math.log(24), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(mcp.signOfDeterminant(), is(-1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(mcp.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元6_パターン3 {

        private EntryReadableMatrix matrix;
        private LUTypeSolver mcp;

        @Before
        public void before_次元6の正方行列のソルバを用意する() {
            /*
             * 0 2 4 7 11 16
             * 2 0 5 8 12 17
             * 4 5 0 0 13 18
             * 7 8 0 0 0 19
             * 11 12 13 0 0 0
             * 16 17 18 19 0 0
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(6));
            builder.setValue(1, 0, 2);
            builder.setValue(2, 0, 4);
            builder.setValue(2, 1, 5);
            builder.setValue(3, 0, 7);
            builder.setValue(3, 1, 8);
            builder.setValue(4, 0, 11);
            builder.setValue(4, 1, 12);
            builder.setValue(4, 2, 13);
            builder.setValue(5, 0, 16);
            builder.setValue(5, 1, 17);
            builder.setValue(5, 2, 18);
            builder.setValue(5, 3, 19);
            matrix = builder.build();
            mcp = ModifiedCholeskyPivotingExecutor.instance().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(mcp.determinant(), is(closeTo(1786236.0, 1E-8)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(mcp.logAbsDeterminant(), is(closeTo(Math.log(1786236.0), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(mcp.signOfDeterminant(), is(1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(mcp.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }
    }

    public static class 行列分解と逆行列ベクトル積_次元1 {

        private EntryReadableMatrix matrix;
        private LUTypeSolver mcp;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
             * 2
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(1));
            builder.setValue(0, 0, 2);
            matrix = builder.build();
            mcp = ModifiedCholeskyPivotingExecutor.instance().apply(matrix).get();
        }

        @Test
        public void test_行列式の計算() {
            assertThat(mcp.determinant(), is(closeTo(2.0, 1E-12)));
        }

        @Test
        public void test_行列式の対数absの計算() {
            assertThat(mcp.logAbsDeterminant(), is(closeTo(Math.log(2.0), 1E-12)));
        }

        @Test
        public void test_行列式の符号の計算() {
            assertThat(mcp.signOfDeterminant(), is(1));
        }

        @Test
        public void test_逆行列ベクトル積() {
            for (int i = 0; i < matrix.matrixDimension().columnAsIntValue(); i++) {
                Vector.Builder builder =
                        Vector.Builder.zeroBuilder(matrix.matrixDimension().rightOperableVectorDimension());
                builder.setValue(i, 1d);
                Vector v = builder.build();

                Vector res = matrix.operate(mcp.inverse().operate(v)).minus(v);
                assertThat(res.normMax(), is(lessThan(1E-12)));
            }
        }
    }
    
    public static class toString表示 {

        private ModifiedCholeskyPivotingExecutor executor = ModifiedCholeskyPivotingExecutor.instance();
        private LUTypeSolver mcp;

        @Before
        public void before_次元1の正方行列のソルバを用意する() {
            /*
             * 2
             */
            SymmetricMatrix.Builder builder = SymmetricMatrix.Builder.zero(MatrixDimension.square(1));
            builder.setValue(0, 0, 2);
            EntryReadableMatrix em = builder.build();
            mcp = executor.apply(em).get();
        }

        @Test
        public void test_toString表示() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(executor);
            System.out.println(mcp);
            System.out.println();
        }
    }

}
