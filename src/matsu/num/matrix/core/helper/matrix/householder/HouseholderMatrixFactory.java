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
     * 引数がHouseholder行列の生成に使用できるかを判定する.
     * 
     * @param vector 判定対象
     * @return 使用できる場合は true
     * @throws NullPointerException 引数がnullの場合
     */
    public static boolean accepts(Vector vector) {
        return vector.normMax() > 0d;
    }

    /**
     * {@link HouseholderMatrix#from(Vector)} の呼び出し先である.
     * 
     * @param reflection 鏡映ベクトル
     * @return 鏡映ベクトルに対応した Householder 行列
     * @throws IllegalArgumentException ベクトルが accept されない場合
     * @throws NullPointerException 引数に null が含まれる場合
     */
    public static HouseholderMatrix createFrom(Vector reflection) {
        //ベクトルの規格化と零ベクトル検証を行う.
        var normalizedReflectionVector = reflection.normalizedEuclidean();
        if (!accepts(normalizedReflectionVector)) {
            throw new IllegalArgumentException("大きさが0");
        }

        //1次元の場合はホルダーを呼び出す
        if (reflection.vectorDimension().equals(OneDimensionHouseholderHolder.DIMENSION)) {
            return OneDimensionHouseholderHolder.INSTANCE;
        }

        return new HouseholderMatrixImpl(normalizedReflectionVector);
    }

    /**
     * {@link HouseholderMatrix#from(Vector, Vector)} の呼び出し先である.
     * 
     * @param source source
     * @param target target
     * @return source を target に移す Householder 行列
     * @throws MatrixFormatMismatchException 引数の次元が整合しない場合
     * @throws IllegalArgumentException ベクトルが accept されない場合
     * @throws NullPointerException 引数に null が含まれる場合
     */
    public static HouseholderMatrix createFrom(Vector source, Vector target) {

        //ベクトルの規格化と零ベクトル検証を行う.
        source = source.normalizedEuclidean();
        target = target.normalizedEuclidean();
        if (!source.equalDimensionTo(target)) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "次元が整合しない, from:%s, to:%s",
                            source, target));
        }
        if (!accepts(source)) {
            throw new IllegalArgumentException("source: 大きさが0");
        }
        if (!accepts(target)) {
            throw new IllegalArgumentException("source: 大きさが0");
        }

        var dimension = source.vectorDimension();

        //1次元の場合はホルダーを呼び出す
        if (dimension.equals(OneDimensionHouseholderHolder.DIMENSION)) {
            return OneDimensionHouseholderHolder.INSTANCE;
        }

        /*
         * 2次元以上の場合は3回の対称Householder変換を計算し, 合成する.
         * p1=from, p2=to とする.
         * 
         * H1 H2 H1で合成, H1は (p2 -> e).
         * p3 = H1(p1) とすると, H2は (p3 -> e).
         */
        var h1Reflection = HouseholderUtil.computeReflectionVectorToStandardBasis(target);
        var p3 = HouseholderMatrixFactory.createFrom(h1Reflection).operate(source);
        var h2Reflection = HouseholderUtil.computeReflectionVectorToStandardBasis(p3);

        var hReflection = h2Reflection.plusCTimes(
                h1Reflection, -2 * h1Reflection.dot(h2Reflection));

        return HouseholderMatrixFactory.createFrom(hReflection);
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
