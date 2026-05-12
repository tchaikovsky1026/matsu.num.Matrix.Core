/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

package matsu.num.matrix.core.mult;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.GeneralMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link BaseVectorAccessibleMatrix} のテスト.
 */
@RunWith(Enclosed.class)
final class BaseVectorAccessibleMatrixTest {

    public static class 行ベクトルからの生成に関するバリデーションテスト {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_サイズ_2_times_3_配列サイズ失敗パターン() {
            MatrixDimension dimension = MatrixDimension.rectangle(2, 3);
            Vector[] rowVectors = createRowVectors(10, VectorDimension.valueOf(3));

            // 失敗する
            BaseVectorAccessibleMatrix.fromRowVectors(dimension, rowVectors);
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_サイズ_2_times_3_ベクトル次元失敗パターン() {
            MatrixDimension dimension = MatrixDimension.rectangle(2, 3);
            Vector[] rowVectors = createRowVectors(2, VectorDimension.valueOf(10));

            // 失敗する
            BaseVectorAccessibleMatrix.fromRowVectors(dimension, rowVectors);
        }

        private static Vector[] createRowVectors(int length, VectorDimension vectorDimension) {
            Vector[] out = new Vector[length];
            for (int i = 0; i < out.length; i++) {
                out[i] = Vector.Builder.zeroBuilder(vectorDimension)
                        .build();
            }
            return out;
        }
    }

    public static class 列ベクトルからの生成に関するバリデーションテスト {

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_サイズ_2_times_3_配列サイズ失敗パターン() {
            MatrixDimension dimension = MatrixDimension.rectangle(2, 3);
            Vector[] columnVectors = createColumnVectors(10, VectorDimension.valueOf(2));

            // 成功する
            BaseVectorAccessibleMatrix.fromColumnVectors(dimension, columnVectors);
        }

        @Test(expected = MatrixFormatMismatchException.class)
        public void test_サイズ_2_times_3_ベクトル次元失敗パターン() {
            MatrixDimension dimension = MatrixDimension.rectangle(2, 3);
            Vector[] columnVectors = createColumnVectors(3, VectorDimension.valueOf(10));

            // 成功する
            BaseVectorAccessibleMatrix.fromColumnVectors(dimension, columnVectors);
        }

        private static Vector[] createColumnVectors(int length, VectorDimension vectorDimension) {
            Vector[] out = new Vector[length];
            for (int i = 0; i < out.length; i++) {
                out[i] = Vector.Builder.zeroBuilder(vectorDimension)
                        .build();
            }
            return out;
        }
    }

    @RunWith(Theories.class)
    public static class 生成された行列のテスト {

        @DataPoints
        public static MatrixDimension[] matrixDimensions = {
                MatrixDimension.rectangle(4, 2),
                MatrixDimension.rectangle(2, 4),
                MatrixDimension.rectangle(3, 3)
        };

        @Theory
        public void test_行生成での成分を検証(MatrixDimension matrixDimension) {
            var src = createRandomMatrix(matrixDimension);
            var matrix = BaseVectorAccessibleMatrix.fromRowVectors(
                    matrixDimension, createRowVectorsFrom(src));

            int row = matrixDimension.rowAsIntValue();
            int column = matrixDimension.columnAsIntValue();

            // 成分比較
            for (int j = 0; j < row; j++) {
                for (int k = 0; k < column; k++) {
                    double expected = src.valueAt(j, k);
                    assertThat(matrix.valueAt(j, k), is(expected));
                    assertThat(matrix.rowVectorAt(j).valueAt(k), is(expected));
                    assertThat(matrix.columnVectorAt(k).valueAt(j), is(expected));
                }
            }
        }

        @Theory
        public void test_列生成での成分を検証(MatrixDimension matrixDimension) {
            var src = createRandomMatrix(matrixDimension);
            var matrix = BaseVectorAccessibleMatrix.fromColumnVectors(
                    matrixDimension, createColumnVectorsFrom(src));

            int row = matrixDimension.rowAsIntValue();
            int column = matrixDimension.columnAsIntValue();

            // 成分比較
            for (int j = 0; j < row; j++) {
                for (int k = 0; k < column; k++) {
                    double expected = src.valueAt(j, k);
                    assertThat(matrix.valueAt(j, k), is(expected));
                    assertThat(matrix.rowVectorAt(j).valueAt(k), is(expected));
                    assertThat(matrix.columnVectorAt(k).valueAt(j), is(expected));
                }
            }
        }

        @Theory
        public void test_行列ベクトル積の検証(MatrixDimension matrixDimension) {
            int iteration = 10;
            for (int c = 0; c < iteration; c++) {
                var src = createRandomMatrix(matrixDimension);
                var matrix = BaseVectorAccessibleMatrix.fromRowVectors(
                        matrixDimension, createRowVectorsFrom(src));

                var vector = createRandomVector(matrixDimension.rightOperableVectorDimension());

                var expected = src.operate(vector);
                var result = matrix.operate(vector);

                var res = result.minus(expected);

                assertThat(res.normMax(), is(lessThan(1E-14)));
            }
        }

        @Theory
        public void test_転置行列ベクトル積の検証(MatrixDimension matrixDimension) {
            int iteration = 10;
            for (int c = 0; c < iteration; c++) {
                var src = createRandomMatrix(matrixDimension);
                var matrix = BaseVectorAccessibleMatrix.fromRowVectors(
                        matrixDimension, createRowVectorsFrom(src));

                var vector = createRandomVector(matrixDimension.leftOperableVectorDimension());

                var expected = src.operateTranspose(vector);
                var result = matrix.operateTranspose(vector);

                var res = result.minus(expected);

                assertThat(res.normMax(), is(lessThan(1E-14)));
            }
        }
    }

    /** 与えられた次元を持ち, -0.5から0.5の範囲のランダムな値の成分を持つ行列を生成する. */
    private static EntryReadableMatrix createRandomMatrix(MatrixDimension matrixDimension) {
        var b = GeneralMatrix.Builder.zero(matrixDimension);
        for (int j = 0; j < matrixDimension.rowAsIntValue(); j++) {
            for (int k = 0; k < matrixDimension.columnAsIntValue(); k++) {
                b.setValue(j, k, ThreadLocalRandom.current().nextDouble() - 0.5);
            }
        }
        return b.build();
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

    /** 与えられた行列から, 列ベクトルたちを抽出する. */
    private static Vector[] createColumnVectorsFrom(EntryReadableMatrix src) {
        MatrixDimension matrixDimension = src.matrixDimension();

        Vector[] out = new Vector[matrixDimension.columnAsIntValue()];
        VectorDimension vectorDimension = matrixDimension.leftOperableVectorDimension();
        for (int k = 0; k < out.length; k++) {
            double[] entry = new double[matrixDimension.rowAsIntValue()];
            for (int j = 0; j < entry.length; j++) {
                entry[j] = src.valueAt(j, k);
            }

            var b = Vector.Builder.zeroBuilder(vectorDimension);
            b.setEntryValue(entry);
            out[k] = b.build();
        }

        return out;
    }

    /** 与えられた次元を持ち, -0.5から0.5の範囲のランダムな値の成分を持つベクトルを生成する. */
    private static Vector createRandomVector(VectorDimension vectorDimension) {
        var b = Vector.Builder.zeroBuilder(vectorDimension);
        for (int i = 0; i < vectorDimension.intValue(); i++) {
            b.setValue(i, ThreadLocalRandom.current().nextDouble() - 0.5);
        }
        return b.build();
    }
}
