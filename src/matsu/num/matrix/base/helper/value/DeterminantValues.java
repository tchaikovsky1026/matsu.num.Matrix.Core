/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.base.helper.value;

import matsu.num.matrix.base.Determinantable;

/**
 * 行列式({@link Determinantable})に関する値のデータクラス.
 * 
 * @author Matsuura Y.
 * @version 21.0
 */
public final class DeterminantValues {

    private final double determinant;
    private final double logAbsDeterminant;
    private final int sign;

    /**
     * 与えた [行列式の絶対値の自然対数] と [符号] を持つ行列式インスタンスを生成する. <br>
     * 行列式の値はこれらから計算される.
     * 
     * @param logAbsDeterminant 行列式の絶対値の自然対数
     * @param sign 符号
     */
    public DeterminantValues(double logAbsDeterminant, int sign) {
        super();

        //符号が0ならdetは強制的に0
        if (sign == 0) {
            this.determinant = 0d;
            this.logAbsDeterminant = Double.NEGATIVE_INFINITY;
            this.sign = 0;
            return;
        }

        double absDet = Math.exp(logAbsDeterminant);
        this.determinant = sign > 0 ? absDet : -absDet;
        this.logAbsDeterminant = logAbsDeterminant;
        this.sign = sign;
    }

    /**
     * 行列式=0に相当するインスタンスを生成する.
     */
    public DeterminantValues() {
        this(Double.NEGATIVE_INFINITY, 0);
    }

    /**
     * @return 行列式
     */
    public double determinant() {
        return this.determinant;
    }

    /**
     * @return 行列式の絶対値の自然対数
     */
    public double logAbsDeterminant() {
        return this.logAbsDeterminant;
    }

    /**
     * @return 行列式の符号
     */
    public int sign() {
        return this.sign;
    }

    /**
     * 自身に係る行列の逆行列に関連した行列式インスタンスを返す. <br>
     * すなわち, 行列式の絶対値の自然対数が等しく, 符号が逆である.
     * 
     * @return 逆行列の行列式インスタンス
     */
    public DeterminantValues createInverse() {
        if (this.sign == 0) {
            throw new IllegalStateException("符号が0");
        }
        return new DeterminantValues(
                -this.logAbsDeterminant, this.sign);
    }
}
