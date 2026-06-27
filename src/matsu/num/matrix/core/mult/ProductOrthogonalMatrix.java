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

import java.util.Optional;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 直交行列による行列積の結果を表すクラス.
 * 
 * <p>
 * このクラスは, このパッケージの機能の結果型を表現するために用意されている. <br>
 * 外部からこのクラスのインスタンスを生成することは不可能.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class ProductOrthogonalMatrix
        extends SkeletalVectorAccessibleMatrix implements OrthogonalMatrix {

    /*
     * 全体設計:
     * 内部的な処理は, ラップした BaseVectorAccessibleMatrix に転送する.
     * このクラスは, ラッパーとしての役割に加え, Orthogonal の属性を付与することが目的.
     */

    private final BaseVectorAccessibleMatrix baseMatrix;

    /** inverse メソッドの戻り値のためのフィールド. */
    private volatile Optional<ProductOrthogonalMatrix> opTranspose;

    /**
     * 非公開のコンストラクタ. 引数チェックはしていない.
     * 
     * <p>
     * インスタンス生成時に, 必ず opTranspose フィールドへの代入が行われなければならない.
     * </p>
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
    public ProductOrthogonalMatrix transpose() {
        return opTranspose.get();
    }

    @Override
    public OrthogonalMatrix transposeOrth() {
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
    public Optional<ProductOrthogonalMatrix> inverse() {
        assert opTranspose != null;
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

    /**
     * このインスタンスの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Matrix[dim:%dimension, vector-accessible, orthogonal, %entry]}
     * </p>
     */
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
     * このメソッドは非公開であり, バリデーションとしては不完全である. <br>
     * 引数は, 直交行列の性質
     * ({@link OrthogonalMatrix} を実装していないが, inv equals to trans である性質)
     * を持たなければならない.
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
        // 循環参照のため, コンストラクタでなくこの場所で injection する
        out.opTranspose = Optional.of(transpose);
        transpose.opTranspose = Optional.of(out);

        return out;
    }
}
