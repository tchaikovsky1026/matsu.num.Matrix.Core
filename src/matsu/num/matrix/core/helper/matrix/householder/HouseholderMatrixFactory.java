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
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link HouseholderMatrix} のヘルパクラスであり, 実装を管理する.
 * 
 * @author Matsuura Y.
 */
public final class HouseholderMatrixFactory {

    private HouseholderMatrixFactory() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * {@link HouseholderMatrix#from(Vector)} の呼び出し先である.
     * 
     * @param reflection 鏡映ベクトル
     * @return 鏡映ベクトルに対応した Householder 行列
     * @throws IllegalArgumentException 鏡映ベクトルのノルムが0の場合
     * @throws NullPointerException 引数に null が含まれる場合
     */
    public static HouseholderMatrix createFrom(Vector reflection) {
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
     * Householder 行列の最も基本的な実装を扱う.
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
