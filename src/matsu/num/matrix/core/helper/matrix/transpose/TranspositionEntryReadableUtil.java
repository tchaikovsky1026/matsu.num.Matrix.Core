/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.1.7
 */
package matsu.num.matrix.core.helper.matrix.transpose;

import java.util.Objects;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Symmetric;
import matsu.num.matrix.core.Vector;

/**
 * {@link EntryReadableMatrix}の転置を扱う.
 * 
 * @author Matsuura Y.
 * @version 26.1
 */
public final class TranspositionEntryReadableUtil {

    private TranspositionEntryReadableUtil() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * {@link EntryReadableMatrix} の推奨される実装規約に則った転置行列を返す. <br>
     * {@link Symmetric} が付与されている場合は, 引数をそのまま返す.
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static EntryReadableMatrix apply(EntryReadableMatrix original) {
        if (original instanceof Symmetric) {
            return original;
        }

        if (original instanceof Transposed castedOriginal) {
            return (castedOriginal).original;
        }

        return new Transposed(original);
    }

    private static final class Transposed implements EntryReadableMatrix {

        private final EntryReadableMatrix original;
        private final MatrixDimension transposedDimension;

        /**
         * 転置行列を作成する.
         * 
         * @param original オリジナル
         */
        Transposed(EntryReadableMatrix original) {
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
            return String.format(
                    "Matrix[dim:%s, %s]",
                    this.matrixDimension(), EntryReadableMatrix.toSimplifiedEntryString(this));
        }
    }
}
