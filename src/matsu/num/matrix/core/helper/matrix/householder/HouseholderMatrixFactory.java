/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.1.18
 */
package matsu.num.matrix.core.helper.matrix.householder;

import matsu.num.matrix.core.HouseholderMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.SkeletalSymmetricOrthogonalMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link HouseholderMatrixImpl} のヘルパクラスであり, 実装を管理する.
 * 
 * @author Matsuura Y.
 */
public final class HouseholderMatrixFactory {

    private HouseholderMatrixFactory() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * {@link HouseholderMatrixImpl#from(Vector)} の呼び出し先である.
     * 
     * @param reflection 鏡映ベクトル
     * @return 鏡映ベクトルに対応した Householder 行列
     * @throws IllegalArgumentException 鏡映ベクトルのノルムが0の場合
     * @throws NullPointerException 引数に null が含まれる場合
     */
    public static HouseholderMatrix from(Vector reflection) {
        //ベクトルの規格化と零ベクトル検証を行う.
        var normalizedReflectionVector = reflection.normalizedEuclidean();
        if (normalizedReflectionVector.normMax() == 0d) {
            throw new IllegalArgumentException("大きさが0");
        }

        //1次元の場合はホルダーを呼び出す
        if (reflection.vectorDimension().equals(OneDimensionHouseholderHolder.DIMENSION)) {
            return OneDimensionHouseholderHolder.INSTANCE;
        }

        return new HouseholderMatrixImpl(normalizedReflectionVector);
    }

    /**
     * {@link HouseholderMatrix} の骨格実装を扱う.
     * 
     * <p>
     * この骨格実装は, {@link #toString()} の実装を提供する.
     * </p>
     */
    private static abstract class SkeletalHouseholderMatrix<T extends SkeletalHouseholderMatrix<T>>
            extends SkeletalSymmetricOrthogonalMatrix<T> implements HouseholderMatrix {

        /**
         * 唯一のコンストラクタ.
         */
        SkeletalHouseholderMatrix() {
            super();
        }

        /**
         * このオブジェクトの文字列説明表現を返す.
         * 
         * <p>
         * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
         * おそらくは次のような表現であろう. <br>
         * {@code Matrix[dim:(%dimension), householder]}
         * </p>
         * 
         * @return 説明表現
         */
        @Override
        public String toString() {
            return String.format(
                    "Matrix[dim:%s, householder]",
                    this.matrixDimension());
        }
    }

    private static final class OneDimensionHouseholderHolder {

        static final VectorDimension DIMENSION = VectorDimension.valueOf(1);
        static final HouseholderMatrix INSTANCE = new OneDimensionHouseholder();

        private static final class OneDimensionHouseholder
                extends SkeletalHouseholderMatrix<OneDimensionHouseholder> {

            private final VectorDimension vecDim = DIMENSION;
            private final MatrixDimension mxDim = MatrixDimension.square(this.vecDim);

            /**
             * 唯一のコンストラクタ.
             */
            OneDimensionHouseholder() {
                super();
            }

            @Override
            public MatrixDimension matrixDimension() {
                return this.mxDim;
            }

            /**
             * @throws MatrixFormatMismatchException {@inheritDoc}
             * @throws NullPointerException {@inheritDoc}
             */
            @Override
            public Vector operate(Vector operand) {
                if (!this.matrixDimension().rightOperable(operand.vectorDimension())) {
                    throw new MatrixFormatMismatchException(
                            String.format(
                                    "右から演算不可:matrix:%s, operand:%s",
                                    this.matrixDimension(), operand.vectorDimension()));
                }

                return operand.negated();
            }

            @Override
            protected OneDimensionHouseholder self() {
                return this;
            }
        }
    }

    /**
     * Householder 行列を扱う.
     * 
     * <p>
     * Householder 行列とは, 鏡映変換の法線ベクトル <b>u</b> (大きさ1)について, <br>
     * H = I - 2<b>u</b><b>u</b><sup>T</sup> <br>
     * で得られる直交行列 H である. <br>
     * H は対称行列であり, 固有値は (-1, 1, 1, ... ), det H = -1 である.
     * </p>
     * 
     * <p>
     * このクラスのインスタンスは,
     * {@link HouseholderMatrixImpl#from(Vector)} メソッドにより得られる.
     * </p>
     * 
     * @author Matsuura Y.
     */
    private static final class HouseholderMatrixImpl
            extends SkeletalHouseholderMatrix<HouseholderMatrixImpl> {

        private final MatrixDimension matrixDimension;
        private final Vector reflectionVector;

        /**
         * 唯一の非公開のコンストラクタ. <br>
         * 規格化された鏡映ベクトルを与えてHouseholder行列を構築する. <br>
         * すなわち, 与えるベクトルの大きさは1である.
         * 
         * @param normalizedReflectionVector 規格化された鏡映ベクトル
         */
        HouseholderMatrixImpl(Vector normalizedReflectionVector) {
            super();

            this.reflectionVector = normalizedReflectionVector;
            this.matrixDimension = MatrixDimension.square(normalizedReflectionVector.vectorDimension());
        }

        @Override
        public MatrixDimension matrixDimension() {
            return this.matrixDimension;
        }

        /**
         * @throws MatrixFormatMismatchException {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        public Vector operate(final Vector operand) {
            double ip = this.reflectionVector.dot(operand);
            return operand.plusCTimes(reflectionVector, -2 * ip);
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
        protected HouseholderMatrixImpl self() {
            return this;
        }
    }
}
