/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.10
 */
package matsu.num.matrix.base;

import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * 正方零行列を扱う.
 * 
 * @author Matsuura Y.
 * @version 22.4
 */
public final class SquareZeroMatrix
        extends SkeletalSymmetricMatrix<SquareZeroMatrix>
        implements ZeroMatrix, BandMatrix, Symmetric {

    private final BandMatrixDimension bandMatrixDimension;
    private final Vector operatedVector;

    /**
     * 唯一のコンストラクタ. <br>
     * 正方形の行列サイズを与えて正方零行列を生成する.
     */
    private SquareZeroMatrix(MatrixDimension matrixDimension) {
        assert matrixDimension.isSquare();

        this.bandMatrixDimension = BandMatrixDimension.symmetric(matrixDimension, 0);
        this.operatedVector = Vector.Builder
                .zeroBuilder(matrixDimension.rightOperableVectorDimension())
                .build();
    }

    /**
     * 与えられた次元(サイズ)の正方零行列を生成する.
     *
     * @param matrixDimension 行列サイズ
     * @return 正方零行列
     * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static SquareZeroMatrix matrixOf(final MatrixDimension matrixDimension) {
        if (!matrixDimension.isSquare()) {
            throw new MatrixFormatMismatchException();
        }
        return new SquareZeroMatrix(matrixDimension);
    }

    @Override
    public BandMatrixDimension bandMatrixDimension() {
        return this.bandMatrixDimension;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public double valueAt(int row, int column) {
        if (!this.bandMatrixDimension.dimension().isValidIndexes(row, column)) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "行列外:matrix:%s, (row, column)=(%s, %s)",
                            this.bandMatrixDimension.dimension(), row, column));
        }

        return 0d;
    }

    @Override
    public double entryNormMax() {
        return 0d;
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operate(Vector operand) {
        if (!this.bandMatrixDimension.dimension().rightOperable(operand.vectorDimension())) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "演算不可:matrix:%s, operand:%s",
                            this.bandMatrixDimension.dimension(), operand.vectorDimension()));
        }
        return this.operatedVector;
    }

    @Override
    public String toString() {
        return String.format(
                "Matrix[dim:%s, zero]",
                this.bandMatrixDimension().dimension());
    }

    /**
     * -
     * 
     * @return -
     * @deprecated (外部からの呼び出し不可)
     */
    @Deprecated
    @Override
    protected SquareZeroMatrix self() {
        return this;
    }

}
