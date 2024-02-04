package matsu.num.matrix.base;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link MatrixDimension}クラスのテスト.
 *
 * @author Matsuura, Y.
 */
@RunWith(Enclosed.class)
public class MatrixDimensionTest {

    public static class 生成に関する {

        @Test(expected = IllegalArgumentException.class)
        public void test_行が0でIAEx() {
            MatrixDimension.rectangle(0, 1);
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_列が0でIAEx() {
            MatrixDimension.rectangle(1, 0);
        }
    }

    public static class 形状に関する {

        @Test
        public void test_正方形次元() {
            MatrixDimension square = MatrixDimension.rectangle(3, 3);
            assertThat(square.isSquare(), is(true));
        }

        @Test
        public void test_縦長次元() {
            MatrixDimension square = MatrixDimension.rectangle(4, 3);
            assertThat(square.isVertical(), is(true));
        }

        @Test
        public void test_横長次元() {
            MatrixDimension square = MatrixDimension.rectangle(3, 4);
            assertThat(square.isHorizontal(), is(true));
        }
    }

    public static class ベクトル演算可能性に関する {

        private MatrixDimension md;

        @Before
        public void before_次元4_2を作成する() {
            md = MatrixDimension.rectangle(4, 2);
        }

        @Test
        public void test_右から次元2が演算可能() {
            assertThat(md.rightOperable(VectorDimension.valueOf(2)), is(true));
            assertThat(md.rightOperable(VectorDimension.valueOf(4)), is(false));
        }

        @Test
        public void test_左から次元4が演算可能() {
            assertThat(md.leftOperable(VectorDimension.valueOf(4)), is(true));
            assertThat(md.leftOperable(VectorDimension.valueOf(2)), is(false));
        }
    }

    public static class 転置に関する {

        @Test
        public void test_3_4の転置は4_3() {
            MatrixDimension dimension = MatrixDimension.rectangle(3, 4);
            //遅延初期化の可能性があるため2回実行
            assertThat(dimension.transpose(), is(MatrixDimension.rectangle(4, 3)));
            assertThat(dimension.transpose(), is(MatrixDimension.rectangle(4, 3)));
        }

        @Test
        public void test_3_4の転置の転置は3_4() {
            MatrixDimension dimension = MatrixDimension.rectangle(3, 4);
            //遅延初期化の可能性があるため2回実行
            assertThat(dimension.transpose().transpose(), is(MatrixDimension.rectangle(3, 4)));
            assertThat(dimension.transpose().transpose(), is(MatrixDimension.rectangle(3, 4)));
        }

        @Test
        public void test_4_4の転置は4_4() {
            MatrixDimension dimension = MatrixDimension.square(4);
            //遅延初期化の可能性があるため2回実行
            assertThat(dimension.transpose(), is(MatrixDimension.square(4)));
            assertThat(dimension.transpose(), is(MatrixDimension.square(4)));
        }

        @Test
        public void test_4_4の転置の転置は4_4() {
            MatrixDimension dimension = MatrixDimension.square(4);
            //遅延初期化の可能性があるため2回実行
            assertThat(dimension.transpose().transpose(), is(MatrixDimension.square(4)));
            assertThat(dimension.transpose().transpose(), is(MatrixDimension.square(4)));
        }
    }
}
