/**
 * 2023.8.20
 */
package matsu.num.matrix.base.helper.matrix.transpose;

import java.util.Objects;

import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.Vector;

/**
 * {@linkplain EntryReadableMatrix}の転置を扱う.
 * 
 * @author Matsuura Y.
 * @version 15.1
 */
public final class TranspositionEntryReadable {

    private static final TranspositionEntryReadable INSTANCE = new TranspositionEntryReadable();

    private TranspositionEntryReadable() {
        if (Objects.nonNull(INSTANCE)) {
            throw new AssertionError();
        }
    }

    /**
     * 行列の転置行列を生成する. <br>
     * {@linkplain Symmetric}が付与されている場合, 戻り値も{@linkplain Symmetric}である.
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public EntryReadableMatrix apply(EntryReadableMatrix original) {
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
    public static TranspositionEntryReadable instance() {
        return INSTANCE;
    }

    private static final class Transposed implements EntryReadableMatrix {

        private final EntryReadableMatrix original;
        private final MatrixDimension transposedDimension;

        /**
         * 転置行列を作成する.
         * 
         * @param original オリジナル
         */
        public Transposed(EntryReadableMatrix original) {
            this.original = Objects.requireNonNull(original);
            this.transposedDimension = original.matrixDimension().transpose();
        }

        @Override
        public MatrixDimension matrixDimension() {
            return this.transposedDimension;
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
        public EntryReadableMatrix transpose() {
            return this.original;
        }

        @Override
        public String toString() {
            return EntryReadableMatrix.toString(this);
        }
    }
}
