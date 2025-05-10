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

import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * <p>
 * スパースな縦ベクトル (成分のほとんどが0であるようなベクトル) を表す. <br>
 * 各成分は有限の倍精度浮動小数点数であり
 * (see: {@link #valueAt(int)}),
 * 不正値を含まないことを保証する. <br>
 * これは, {@link Vector} に準拠している. <br>
 * インスタンスはイミュータブルであり, メソッドは関数的かつスレッドセーフである.
 * </p>
 * 
 * <p>
 * {@link SparseVector} は identity に基づく equality を提供する. <br>
 * すなわち, {@link Object#equals(Object)} メソッドの実装に準じる.
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
 * identity に基づく equality を提供しなければならない.
 * </p>
 * 
 * 
 * <h3>モジュール外での実装</h3>
 * 
 * <p>
 * <u><i>
 * このインターフェースは主に, 戻り値型を公開するために用意されており,
 * モジュール外での実装は想定されていない.
 * </i></u>
 * </p>
 * 
 * <p>
 * このインターフェースはモジュール内での引数となるので,
 * モジュール外で実装することも有用と思われる. <br>
 * ただし現バージョンにおいては, モジュール外の実装を非推奨として取り扱う. <br>
 * 将来的に緩和される可能性がある.
 * </p>
 * 
 * @author Matsuura Y.
 */
public interface SparseVector {

    /**
     * ベクトルの次元を取得する.
     * 
     * @return ベクトルの次元
     */
    public abstract VectorDimension vectorDimension();

    /**
     * ベクトルの要素 <i>i</i> の値を返す.
     *
     * @param index <i>i</i>
     * @return 要素 <i>i</i> の値
     * @throws IndexOutOfBoundsException indexが範囲外の場合
     */
    public abstract double valueAt(int index);

    /**
     * 自身と同等の {@link Vector} を返す.
     * 
     * @return 自身と同等の {@link Vector}
     */
    public abstract Vector asVector();

    /**
     * 2-ノルム (Euclidノルム):
     * ||<b>v</b>||<sub>2</sub>.
     *
     * @return 2-ノルム
     */
    public abstract double norm2();

    /**
     * 最大値ノルム:
     * ||<b>v</b>||<sub>&infin;</sub>.
     *
     * @return 最大値ノルム
     */
    public abstract double normMax();

    /**
     * 内積を計算する.
     * 
     * @param reference 作用ベクトル
     * @return 内積
     * @throws MatrixFormatMismatchException 次元が一致しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public abstract double dot(Vector reference);

    /**
     * 自身のスカラー倍を計算して返す:
     * <b>w</b> = <i>c</i> <b>v</b>. <br>
     * 
     * <b>v</b>: {@code this}, <br>
     * <i>c</i>: スカラー.
     * <b>w</b>: 計算結果.
     * 
     * <p>
     * 演算の結果, その成分が {@link SparseVector} で扱えない場合は,
     * 正常値に置き換えられる.
     * </p>
     *
     * @param scalar <i>c</i>, スカラー
     * @return 計算結果
     */
    public abstract SparseVector times(double scalar);

    /**
     * 自身 <b>v</b> と与えられたベクトル <b>u</b> について,
     * <b>w</b> = <b>v</b> + <b>u</b>
     * を計算する.
     * 
     * <p>
     * 演算の結果, その成分が {@link SparseVector} で扱えない場合は,
     * 正常値に置き換えられる.
     * </p>
     * 
     * @param reference <b>u</b>
     * @return 計算結果, <b>w</b>
     * @throws MatrixFormatMismatchException 次元が一致しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public abstract Vector plus(Vector reference);

    /**
     * Euclidノルムにより自身を規格化したベクトル:
     * <b>v</b> / ||<b>v</b>||<sub>2</sub>. <br>
     * 自身の大きさが厳密に0である場合のみ, 規格化されずに0を返す.
     * 
     * @return 自身を規格化したベクトル
     */
    public abstract SparseVector normalizedEuclidean();

    /**
     * 自身の加法逆元 (-1倍) を返す.
     * 
     * @return 加法逆元
     */
    public abstract SparseVector negated();

    /**
     * 自身と相手とが等価であるかどうかを判定する. <br>
     * identity に基づく equality である.
     * 
     * @param obj 相手
     * @return 相手が自信と等しいなら true
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * ハッシュコードを返す.
     * 
     * @return ハッシュコード
     */
    @Override
    public int hashCode();
}
