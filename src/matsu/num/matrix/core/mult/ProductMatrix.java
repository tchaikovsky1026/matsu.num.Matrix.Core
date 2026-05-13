/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.12
 */
package matsu.num.matrix.core.mult;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 行列積の結果を表すクラス.
 * 
 * @author Matsuura Y.
 */
public final class ProductMatrix implements VectorAccessibleMatrix {

    private final BaseVectorAccessibleMatrix baseMatrix;

    private volatile ProductMatrix transpose;

    /**
     * 非公開のコンストラクタ.
     */
    private ProductMatrix(BaseVectorAccessibleMatrix baseMatrix) {
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
    public ProductMatrix transpose() {
        return transpose;
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

    /**
     * 与えられた行番号の行ベクトルを取得する.
     * 
     * @param index index
     * @return rowVector
     * @throws IndexOutOfBoundsException indexが範囲外
     */
    @Override
    public Vector rowVectorAt(int index) {
        return baseMatrix.rowVectorAt(index);
    }

    /**
     * 与えられた列番号の列ベクトルを取得する.
     * 
     * @param index index
     * @return columnVector
     * @throws IndexOutOfBoundsException indexが範囲外
     */
    @Override
    public Vector columnVectorAt(int index) {
        return baseMatrix.columnVectorAt(index);
    }

    /**
     * このインスタンスの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Matrix[dim:%dimension, %entry]}
     * </p>
     */
    @Override
    public String toString() {
        return "Matrix[dim: %s, %s]"
                .formatted(
                        this.matrixDimension(),
                        EntryReadableMatrix.toSimplifiedEntryString(this));
    }

    /**
     * 与えた {@link BaseVectorAccessibleMatrix} を持つ {@link ProductMatrix} を生成する.
     * 
     * @param baseMatrix baseMatrix
     * @return インスタンス
     * @throws NullPointerException 引数がnullの場合
     */
    static ProductMatrix of(BaseVectorAccessibleMatrix baseMatrix) {
        ProductMatrix out = new ProductMatrix(baseMatrix);
        ProductMatrix transpose = new ProductMatrix(baseMatrix.transpose());

        // 転置行列どうしを結びつける
        out.transpose = transpose;
        transpose.transpose = out;

        return out;
    }
}
