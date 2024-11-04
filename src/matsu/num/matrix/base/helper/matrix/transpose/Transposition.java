/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.4
 */
package matsu.num.matrix.base.helper.matrix.transpose;

import java.util.Objects;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.Vector;

/**
 * {@link Matrix}の転置を扱う.
 * 
 * <p>
 * {@link Matrix} の推奨される実装規約にしたがった転置行列を得ることができる.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.0
 */
public final class Transposition {

    private static final Transposition INSTANCE = new Transposition();

    private Transposition() {
        if (Objects.nonNull(INSTANCE)) {
            throw new AssertionError();
        }
    }

    /**
     * {@link Matrix} の推奨される実装規約に則った転置行列を返す. <br>
     * {@link Symmetric} が付与されている場合は, 引数をそのまま返す.
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Matrix apply(Matrix original) {
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
    public static Transposition instance() {
        return INSTANCE;
    }

    private static final class Transposed implements Matrix {

        private final Matrix original;

        private final MatrixDimension transposedDimension;

        /**
         * 転置行列を作成する.
         * 
         * @param original オリジナル
         */
        Transposed(Matrix original) {
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
        public Matrix transpose() {
            return this.original;
        }

        @Override
        public String toString() {
            return String.format(
                    "Matrix[dim:%s]",
                    this.matrixDimension());
        }
    }
}
