/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.14
 */
package matsu.num.matrix.core.mult;

import java.util.Optional;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 直交行列による行列積の結果を表すクラス.
 * 
 * @author Matsuura Y.
 */
public final class ProductOrthogonalMatrix
        extends SkeletalVectorAccessibleMatrix implements OrthogonalMatrix {

    private final BaseVectorAccessibleMatrix baseMatrix;

    private volatile Optional<ProductOrthogonalMatrix> opTranspose;

    /**
     * 非公開のコンストラクタ.
     */
    private ProductOrthogonalMatrix(BaseVectorAccessibleMatrix baseMatrix) {
        super();
        this.baseMatrix = baseMatrix;
    }

    /** @throws IndexOutOfBoundsException {@inheritDoc} */
    @Override
    public double valueAt(int row, int column) {
        return baseMatrix.valueAt(row, column);
    }

    @Override
    public double entryNormMax() {
        return baseMatrix.entryNormMax();
    }

    @Override
    @SuppressWarnings("removal")
    public ProductOrthogonalMatrix transpose() {
        return opTranspose.get();
    }

    @Override
    public MatrixDimension matrixDimension() {
        return baseMatrix.matrixDimension();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operate(Vector operand) {
        return baseMatrix.operate(operand);
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operateTranspose(Vector operand) {
        return baseMatrix.operateTranspose(operand);
    }

    @Override
    @SuppressWarnings("removal")
    public Optional<ProductOrthogonalMatrix> inverse() {
        return opTranspose;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public Vector rowVectorAt(int index) {
        return baseMatrix.rowVectorAt(index);
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public Vector columnVectorAt(int index) {
        return baseMatrix.columnVectorAt(index);
    }

    @Override
    public String toString() {
        return String.format(
                "Matrix[dim: %s, vector-accessible, orthogonal, %s]",
                this.matrixDimension(),
                EntryReadableMatrix.toSimplifiedEntryString(this));
    }

    /**
     * 与えた {@link BaseVectorAccessibleMatrix} を持つ
     * {@link ProductOrthogonalMatrix} を生成する.
     * 
     * <p>
     * 引数は, 直交行列の性質
     * ({@link OrthogonalMatrix} を実装していないが, inv = trans である性質)
     * を持たなければならない. <br>
     * このメソッドを公開してはいけない. <br>
     * </p>
     * 
     * @param baseMatrix baseMatrix
     * @return インスタンス
     * @throws NullPointerException 引数がnullの場合
     */
    static ProductOrthogonalMatrix of(BaseVectorAccessibleMatrix baseMatrix) {
        ProductOrthogonalMatrix out = new ProductOrthogonalMatrix(baseMatrix);
        ProductOrthogonalMatrix transpose = new ProductOrthogonalMatrix(baseMatrix.transpose());

        // 転置行列どうしを結びつける
        out.opTranspose = Optional.of(transpose);
        transpose.opTranspose = Optional.of(out);

        return out;
    }
}
