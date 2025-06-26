/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.5.9
 */
package matsu.num.matrix.core.sparse;

import matsu.num.matrix.core.HouseholderMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.SkeletalSymmetricOrthogonalMatrix;
import matsu.num.matrix.core.Vector;

/**
 * 鏡映ベクトルとして {@link SparseVector} を指定する形で構築される,
 * Householder 行列の実装.
 * 
 * @author Matsuura Y.
 */
final class HouseholderMatrixWithSparseVector
        extends SkeletalSymmetricOrthogonalMatrix<HouseholderMatrixWithSparseVector>
        implements HouseholderMatrix {

    private final MatrixDimension matrixDimension;
    private final SparseVector reflectionVector;

    /**
     * 鏡映ベクトルを指定して Househodler 行列を生成する.
     * 
     * @param reflectionVector 鏡映ベクトル
     * @throws IllegalArgumentException 鏡映ベクトルのノルムが0の場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    HouseholderMatrixWithSparseVector(SparseVector reflectionVector) {
        if (!accepts(reflectionVector)) {
            throw new IllegalArgumentException("norm 0");
        }

        this.matrixDimension = MatrixDimension.square(reflectionVector.vectorDimension());
        this.reflectionVector = reflectionVector.normalizedEuclidean();
    }

    /**
     * 引数がHouseholder行列の生成に使用できるかを判定する.
     * 
     * @param vector 判定対象
     * @return 使用できる場合は true
     * @throws NullPointerException 引数がnullの場合
     */
    static boolean accepts(SparseVector vector) {
        return vector.normMax() > 0d;
    }

    @Override
    public MatrixDimension matrixDimension() {
        return this.matrixDimension;
    }

    @Override
    public Vector operate(final Vector operand) {
        /*
         * (I - 2u(u^T)) v を計算する.
         * 
         * (I - 2u(u^T)) v = v + (-2 u*v)u
         * w = (-2 u*v)u と置く.
         */
        var vecW = this.reflectionVector.times(-2 * this.reflectionVector.dot(operand));
        return vecW.plus(operand);
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
    protected HouseholderMatrixWithSparseVector self() {
        return this;
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
                "Matrix[dim: %s, householder]",
                this.matrixDimension());
    }
}
