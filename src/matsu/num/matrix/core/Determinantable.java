/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.4
 */
package matsu.num.matrix.core;

/**
 * 紐づけられた行列についての行列式が計算可能であることを示すインターフェース.
 * 
 * <p>
 * このインターフェースを実装した全てのクラスは実質的にイミュータブルであり,
 * (このインターフェース以外を含む) 全てのメソッドは関数的かつスレッドセーフである.
 * </p>
 * 
 * 
 * <hr>
 * 
 * <h2>実装規約</h2>
 * 
 * <p>
 * 実質的にイミュータブルかつ全てのメソッドは関数的かつスレッドセーフになるようにクラスが設計されなければならず,
 * 違反した場合は振る舞いが保証されない.
 * </p>
 * 
 * <p>
 * このインターフェースを実装するクラスが {@link Invertible} や {@link Inversion}
 * のような逆行列を扱うインターフェースも実装する場合,
 * 符号 {@link #signOfDeterminant()} が0でないことと逆行列が存在することとは等価である.
 * </p>
 *
 * @author Matsuura Y.
 * @see Invertible
 * @see Inversion
 */
public interface Determinantable {

    /**
     * 行列式の値を返す.
     *
     * @return 行列式
     */
    public abstract double determinant();

    /**
     * 行列式の絶対値の自然対数を返す.
     *
     * @return 行列式の絶対値の自然対数
     */
    public abstract double logAbsDeterminant();

    /**
     * 行列式の符号を返す.
     *
     * @return 行列式の符号, 行列式の値が正, 0, 負のときそれぞれ1, 0, -1
     */
    public abstract int signOfDeterminant();
}
