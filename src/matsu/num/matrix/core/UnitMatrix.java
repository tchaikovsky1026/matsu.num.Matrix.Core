/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.28
 */
package matsu.num.matrix.core;

import matsu.num.matrix.core.helper.value.BandDimensionPositionState;
import matsu.num.matrix.core.helper.value.MatrixValidationSupport;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 単位行列を扱う.
 * 
 * <p>
 * このクラスのインスタンスは,
 * {@link UnitMatrix#matrixOf(MatrixDimension)} メソッドにより得られる.
 * </p>
 *
 * @author Matsuura Y.
 */
public final class UnitMatrix
        extends SkeletalSymmetricOrthogonalMatrix<UnitMatrix>
        implements SignatureMatrix, PermutationMatrix, LowerUnitriangular {

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
        MatrixValidationSupport.validateIndexInMatrix(
                this.matrixDimension(), row, column);

        switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
            case DIAGONAL:
                return 1d;
            case OUT_OF_BAND:
                return 0d;
            //OUT_OF_MATRIXは検証済み
            //$CASES-OMITTED$
            default:
                throw new AssertionError("Bug");
        }
    }

    @Override
    public BandMatrixDimension bandMatrixDimension() {
        return this.bandMatrixDimension;
    }

    /**
     * -
     * 
     * <p>
     * (外部からの呼び出し不可)
     * </p>
     * 
     * @return -
     */
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
        MatrixValidationSupport.validateOperate(this.matrixDimension(), operand.vectorDimension());

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

    /**
     * {@code true} を返す.
     * 
     * <p>
     * 単位行列は恒等置換であり, 偶置換である. <br>
     * 対角成分はすべて1なので, Signature matrixとしては-1が偶数個である.
     * </p>
     * 
     * @return {@code true}
     */
    @Override
    public boolean isEven() {
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "Matrix[dim: %s, unit]",
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
