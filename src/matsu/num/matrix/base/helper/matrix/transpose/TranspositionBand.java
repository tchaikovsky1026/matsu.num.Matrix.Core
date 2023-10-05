/**
 * 2023.8.20
 */
package matsu.num.matrix.base.helper.matrix.transpose;

import java.util.Objects;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.BandMatrixDimension;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.Vector;

/**
 * {@linkplain BandMatrix}の転置を扱う.
 * 
 * @author Matsuura Y.
 * @version 15.1
 */
public final class TranspositionBand {

    private static final TranspositionBand INSTANCE = new TranspositionBand();

    private TranspositionBand() {
        if (Objects.nonNull(INSTANCE)) {
            throw new AssertionError();
        }
    }

    /**
     * 帯行列の転置行列を生成する.
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public BandMatrix apply(BandMatrix original) {
        if (original instanceof Symmetric) {
            return original;
        }

        if (original instanceof Transposed) {
            return ((Transposed) original).original;
        }

        return new Transposed(original);
    }

    /**
     * このインスタンスを生成する.
     * 
     * @return インスタンス
     */
    public static TranspositionBand instance() {
        return INSTANCE;
    }

    private static final class Transposed implements BandMatrix {

        private final BandMatrix original;
        private final BandMatrixDimension transposedDimension;

        /**
         * 転置行列を作成する.
         * 
         * @param original オリジナル
         */
        public Transposed(BandMatrix original) {
            this.original = Objects.requireNonNull(original);
            this.transposedDimension = original.bandMatrixDimension().transpose();
        }

        @Override
        public Vector operate(Vector operand) {
            return this.original.operateTranspose(operand);
        }

        @Override
        public Vector operateTranspose(Vector operand) {
            return this.original.operate(operand);
        }

        @Override
        public double valueAt(int row, int column) {
            return this.original.valueAt(column, row);
        }

        @Override
        public double entryNormMax() {
            return this.original.entryNormMax();
        }

        @Override
        public BandMatrixDimension bandMatrixDimension() {
            return this.transposedDimension;
        }

        @Override
        public BandMatrix transpose() {
            return this.original;
        }

        @Override
        public String toString() {
            return BandMatrix.toString(this);
        }
    }
}
