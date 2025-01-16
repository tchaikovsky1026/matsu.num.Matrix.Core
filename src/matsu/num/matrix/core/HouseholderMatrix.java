/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.1.16
 */
package matsu.num.matrix.core;

import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

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
 * {@link HouseholderMatrix#from(Vector)} メソッドにより得られる.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class HouseholderMatrix
        extends SkeletalSymmetricOrthogonalMatrix<HouseholderMatrix>
        implements OrthogonalMatrix, Determinantable, Symmetric {

    private final MatrixDimension matrixDimension;
    private final Vector reflectionVector;

    /**
     * 唯一の非公開のコンストラクタ. <br>
     * 鏡映ベクトルを与えてHouseholder行列を構築する.
     * 
     * @param reflectionVector 鏡映ベクトル
     * @throws IllegalArgumentException 鏡映ベクトルのノルムが0の場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    private HouseholderMatrix(Vector reflectionVector) {

        this.reflectionVector = reflectionVector.normalizedEuclidean();
        if (this.reflectionVector.normMax() == 0d) {
            throw new IllegalArgumentException("大きさが0");
        }
        this.matrixDimension = MatrixDimension.square(reflectionVector.vectorDimension());
    }

    @Override
    public MatrixDimension matrixDimension() {
        return this.matrixDimension;
    }

    /**
     * @return {@code -1d}
     */
    @Override
    public double determinant() {
        return -1d;
    }

    /**
     * @return {@code 0d}
     */
    @Override
    public double logAbsDeterminant() {
        return 0d;
    }

    /**
     * @return {@code -1}
     */
    @Override
    public int signOfDeterminant() {
        return -1;
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
    protected HouseholderMatrix self() {
        return this;
    }

    /**
     * 鏡映変換の法線ベクトル (以下, 鏡映ベクトルと表記)
     * を指定して, Householder 行列を構築する.
     * 
     * <p>
     * 内部で鏡映ベクトルの規格化を行うため,
     * 与える鏡映ベクトルは大きさが1である必要はない. <br>
     * ただし, 大きさが厳密に0,
     * すなわち全成分が厳密に {@code 0d}, {@code -0d} であってはならず, その場合は例外がスローされる.
     * </p>
     * 
     * @param reflection 鏡映ベクトル
     * @return 鏡映ベクトルに対応した Householder 行列
     * @throws IllegalArgumentException 鏡映ベクトルのノルムが0の場合
     * @throws NullPointerException 引数に null が含まれる場合
     */
    public static HouseholderMatrix from(Vector reflection) {
        return new HouseholderMatrix(reflection);
    }
}
