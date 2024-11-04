package matsu.num.matrix.base;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * {@link VectorDimension}クラスのテスト.
 *
 * @author Matsuura Y.
 */
@RunWith(Enclosed.class)
final class VectorDimensionTest {

    public static final Class<?> TEST_CLASS = VectorDimension.class;

    public static class 生成に関するテスト {

        @Test(expected = IllegalArgumentException.class)
        public void test_引数0はIAEx() {
            VectorDimension.valueOf(0);
        }

        @Test
        public void test_引数1は成功() {
            VectorDimension.valueOf(1);
        }
    }

    public static class 等価性に関するテスト {

        private VectorDimension dimension_1;
        private VectorDimension dimension_10000;

        @Before
        public void before_次元1を生成する() {
            dimension_1 = VectorDimension.valueOf(1);
        }

        @Before
        public void before_次元10000を生成する() {
            dimension_10000 = VectorDimension.valueOf(10000);
        }

        @Test
        public void test_次元2は1でない() {
            assertThat(VectorDimension.valueOf(2), is(not(dimension_1)));
        }

        @Test
        public void test_次元1は1である() {
            assertThat(VectorDimension.valueOf(1), is(dimension_1));
        }

        @Test
        public void test_次元10000は10000である() {
            assertThat(VectorDimension.valueOf(10000), is(dimension_10000));
        }

        @Test
        public void test_次元1と1はハッシュコードが等しい() {
            assertThat(VectorDimension.valueOf(1).hashCode(), is(dimension_1.hashCode()));
        }

        @Test
        public void test_次元10000と10000はハッシュコードが等しい() {
            assertThat(VectorDimension.valueOf(10000).hashCode(), is(dimension_10000.hashCode()));
        }
    }

    public static class toString表示 {

        @Test
        public void test_toString() {
            System.out.println(TEST_CLASS.getName());
            System.out.println(VectorDimension.valueOf(3));
            System.out.println();
        }
    }
}
