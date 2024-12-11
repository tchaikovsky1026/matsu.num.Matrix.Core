/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.11
 */
package matsu.num.matrix.base;

import java.util.Optional;

import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * 正方な零行列を扱う具象クラス.
 * 
 * <p>
 * このインターフェースを実装した具象クラスのインスタンスは,
 * {@link SquareZeroMatrix#matrixOf(MatrixDimension)} メソッドにより得られる.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 25.0
 */
public final class SquareZeroMatrix
        extends SkeletalSymmetricMatrix<SquareZeroMatrix>
        implements ZeroMatrixSealed, DiagonalMatrixSealed {

    private final BandMatrixDimension bandMatrixDimension;
    private final Vector zeroVector;

    /**
     * 唯一のコンストラクタ. <br>
     * 正方形の行列サイズを与えて正方零行列を生成する.
     */
    private SquareZeroMatrix(MatrixDimension matrixDimension) {
        assert matrixDimension.isSquare();

        this.bandMatrixDimension = BandMatrixDimension.symmetric(matrixDimension, 0);
        this.zeroVector = Vector.Builder
                .zeroBuilder(matrixDimension.rightOperableVectorDimension())
                .build();
    }

    /**
     * 与えられた次元 (サイズ) の正方零行列を返す.
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
        return this.zeroVector;
    }

    @Override
    public double determinant() {
        return 0d;
    }

    @Override
    public double logAbsDeterminant() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public int signOfDeterminant() {
        return 0;
    }

    /**
     * 零行列に逆行列は存在しないので, 必ず空が返る.
     * 
     * @return 空のオプショナル
     */
    @Override
    public Optional<? extends DiagonalMatrix> inverse() {
        return Optional.empty();
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
     * <p>
     * (外部からの呼び出し不可)
     * </p>
     * 
     * @return -
     */
    @Override
    protected SquareZeroMatrix self() {
        return this;
    }
}
