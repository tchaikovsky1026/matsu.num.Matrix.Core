/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.6
 */
package matsu.num.matrix.base;

import matsu.num.matrix.base.helper.value.BandDimensionPositionState;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * 単位行列を扱う.
 *
 * @author Matsuura Y.
 * @version 22.1
 */
public final class UnitMatrix
        extends SkeletalSymmetricOrthogonalMatrix<UnitMatrix>
        implements PermutationMatrix, DiagonalMatrix, LowerUnitriangular {

    private final BandMatrixDimension bandMatrixDimension;

    /**
     * 与えられた次元(サイズ)の単位行列を生成する.
     *
     * @param matrixDimension 行列サイズ
     * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private UnitMatrix(final MatrixDimension matrixDimension) {
        super();
        this.bandMatrixDimension = BandMatrixDimension.symmetric(matrixDimension, 0);
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc }
     */
    @Override
    public double valueAt(final int row, final int column) {
        switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
        case DIAGONAL:
            return 1d;
        case LOWER_BAND:
            throw new AssertionError("Bug: 到達不能");
        case UPPER_BAND:
            throw new AssertionError("Bug: 到達不能");
        case OUT_OF_BAND:
            return 0d;
        case OUT_OF_MATRIX:
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列内部でない:matrix:%s, (row, column)=(%d, %d)",
                            bandMatrixDimension.dimension(), row, column));
        default:
            throw new AssertionError("Bug: 列挙型に想定外の値");
        }
    }

    @Override
    public BandMatrixDimension bandMatrixDimension() {
        return this.bandMatrixDimension;
    }

    /**
     * -
     * 
     * @return -
     * @deprecated (外部からの呼び出し不可)
     */
    @Deprecated
    @Override
    protected UnitMatrix self() {
        return this;
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operate(Vector operand) {
        if (!bandMatrixDimension.dimension().rightOperable(operand.vectorDimension())) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "右から演算不可,matrix:%s,operand:%s",
                            bandMatrixDimension.dimension(), operand.vectorDimension()));
        }
        return operand;
    }

    @Override
    public double entryNormMax() {
        return 1;
    }

    @Override
    public double determinant() {
        return 1.0;
    }

    @Override
    public double logAbsDeterminant() {
        return 0.0;
    }

    @Override
    public int signOfDeterminant() {
        return 1;
    }

    @Override
    public boolean isEven() {
        return true;
    }

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[dim:%s, unit]",
                this.matrixDimension());
    }

    /**
     * 与えられた次元(サイズ)の単位行列を生成する.
     *
     * @param matrixDimension 行列サイズ
     * @return 単位行列
     * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static UnitMatrix matrixOf(final MatrixDimension matrixDimension) {
        return new UnitMatrix(matrixDimension);
    }

}
