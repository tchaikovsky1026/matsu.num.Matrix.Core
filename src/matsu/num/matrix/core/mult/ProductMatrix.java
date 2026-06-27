/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.6.26
 */
package matsu.num.matrix.core.mult;

import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 行列積の結果を表すクラス.
 * 
 * <p>
 * このクラスは, このパッケージの機能の結果型を表現するために用意されている. <br>
 * 外部からこのクラスのインスタンスを生成することは不可能.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class ProductMatrix extends SkeletalVectorAccessibleMatrix {

    /*
     * 全体設計:
     * 内部的な処理は, ラップした BaseVectorAccessibleMatrix に転送する.
     */

    private final BaseVectorAccessibleMatrix baseMatrix;

    private volatile ProductMatrix transpose;

    /**
     * 非公開のコンストラクタ. 引数チェックはしていない.
     * 
     * <p>
     * インスタンス生成時に, 必ず transpose フィールドへの代入が行われなければならない.
     * </p>
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
        assert transpose != null;
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
        return super.toString();
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
