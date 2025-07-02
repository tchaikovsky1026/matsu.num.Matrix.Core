/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.6.26
 */
package matsu.num.matrix.core.helper.value;

import matsu.num.matrix.core.BandMatrixDimension;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 行列のバリデーションに関するサポートを提供するクラス.
 * 
 * @author Matsuura Y.
 */
public final class MatrixValidationSupport {

    private MatrixValidationSupport() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * (r,c)が行列内かどうかを判定する.
     * 
     * @param matrixDimension dimension
     * @param row row
     * @param column column
     * @throws IndexOutOfBoundsException out of matrix
     * @throws NullPointerException null
     */
    public static void validateIndexInMatrix(
            MatrixDimension matrixDimension, int row, int column) {
        if (!(matrixDimension.isValidIndexes(row, column))) {
            throw new IndexOutOfBoundsException(
                    "out of matrix: matrix: %s, (row, column) = (%s, %s)"
                            .formatted(matrixDimension, row, column));
        }
    }

    /**
     * 帯行列について, (r,c)が帯内かどうかを判定する.
     * 
     * @param bandMatrixDimension dimension
     * @param row row
     * @param column column
     * @throws IndexOutOfBoundsException out of band or matrix
     * @throws NullPointerException null
     */
    public static void validateIndexInMatrixAndBand(
            BandMatrixDimension bandMatrixDimension, int row, int column) {

        switch (BandDimensionPositionState.positionStateAt(row, column, bandMatrixDimension)) {
            case DIAGONAL, LOWER_BAND, UPPER_BAND:
                return;
            case OUT_OF_BAND:
                throw new IndexOutOfBoundsException(
                        "out of band: matrix: %s, (row, column) = (%s, %s)"
                                .formatted(bandMatrixDimension, row, column));
            case OUT_OF_MATRIX:
                throw new IndexOutOfBoundsException(
                        "out of matrix: matrix: %s, (row, column) = (%s, %s)"
                                .formatted(bandMatrixDimension.dimension(), row, column));
            default:
                throw new AssertionError("Bug: unreachable");
        }
    }

    /**
     * 行列にベクトルをoperateできるかを検証する.
     * 
     * @param matrixDimension 行列次元
     * @param vectorDimension ベクトル次元
     * @throws MatrixFormatMismatchException undefined operation
     * @throws NullPointerException null
     */
    public static void validateOperate(
            MatrixDimension matrixDimension, VectorDimension vectorDimension) {
        if (!matrixDimension.rightOperable(vectorDimension)) {
            throw new MatrixFormatMismatchException(
                    "undefined operation: matrix: %s, operand: %s"
                            .formatted(matrixDimension, vectorDimension));
        }
    }

    /**
     * 行列にベクトルをoperateTransposeできるかを検証する.
     * 
     * @param matrixDimension 行列次元
     * @param vectorDimension ベクトル次元
     * @throws MatrixFormatMismatchException undefined operation
     * @throws NullPointerException null
     */
    public static void validateOperateTranspose(
            MatrixDimension matrixDimension, VectorDimension vectorDimension) {
        if (!matrixDimension.leftOperable(vectorDimension)) {
            throw new MatrixFormatMismatchException(
                    "undefined operation: matrix: %s, operand: %s"
                            .formatted(matrixDimension, vectorDimension));
        }
    }

}
