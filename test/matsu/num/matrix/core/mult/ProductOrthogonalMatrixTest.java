/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

package matsu.num.matrix.core.mult;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.UnitMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;

/**
 * {@link ProductOrthogonalMatrix} のテスト.
 */
@RunWith(Enclosed.class)
final class ProductOrthogonalMatrixTest {

    public static class 転置行列に関する {

        private ProductOrthogonalMatrix matrix;

        @Before
        public void before_行列の用意() {
            var dimension = MatrixDimension.square(4);
            matrix = ProductOrthogonalMatrix.of(
                    BaseVectorAccessibleMatrix
                            .fromRowVectors(
                                    dimension,
                                    createRowVectorsFrom(UnitMatrix.matrixOf(dimension))));
        }

        @Test
        public void test_転置の転置は自分自身() {
            assertThat(matrix.transpose().transpose(), is(matrix));
        }

        @Test
        public void test_逆の逆は自分自身() {
            assertThat(matrix.inverse().get().inverse().get(), is(matrix));
        }

        @Test
        public void test_転置転置転置は転置() {
            assertThat(matrix.transpose().transpose().transpose(), is(matrix.transpose()));
        }

        @Test
        public void test_逆逆逆は逆() {
            assertThat(
                    matrix.inverse().get().inverse().get().inverse().get(),
                    is(matrix.inverse().get()));
        }

        @Test
        public void test_転置の行ベクトルは自身の列ベクトル() {
            for (int k = 0; k < matrix.matrixDimension().columnAsIntValue(); k++) {
                assertThat(matrix.transpose().rowVectorAt(k), is(matrix.columnVectorAt(k)));
            }
        }

        @Test
        public void test_転置の列ベクトルは自身の行ベクトル() {
            for (int j = 0; j < matrix.matrixDimension().rowAsIntValue(); j++) {
                assertThat(matrix.transpose().columnVectorAt(j), is(matrix.rowVectorAt(j)));
            }
        }
    }

    /** 与えられた行列から, 行ベクトルたちを抽出する. */
    private static Vector[] createRowVectorsFrom(EntryReadableMatrix src) {
        MatrixDimension matrixDimension = src.matrixDimension();

        Vector[] out = new Vector[matrixDimension.rowAsIntValue()];
        VectorDimension vectorDimension = matrixDimension.rightOperableVectorDimension();
        for (int j = 0; j < out.length; j++) {
            double[] entry = new double[matrixDimension.columnAsIntValue()];
            for (int k = 0; k < entry.length; k++) {
                entry[k] = src.valueAt(j, k);
            }

            var b = Vector.Builder.zeroBuilder(vectorDimension);
            b.setEntryValue(entry);
            out[j] = b.build();
        }

        return out;
    }
}
