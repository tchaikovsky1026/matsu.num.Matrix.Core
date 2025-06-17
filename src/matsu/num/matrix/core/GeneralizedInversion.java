/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.6.16
 */
package matsu.num.matrix.core;

/**
 * 紐づけられた行列の, Moore-Penrose 一般化逆行列を取得できることを表現するインターフェース.
 * 
 * <p>
 * 一般の行列 A に対する
 * Moore-Penrose 一般化逆行列 A<sup>+</sup> とは,
 * 正則行列に対する逆行列を拡張したものであり,
 * 次の性質を満たす行列である. <br>
 * (ただし, 行列 X の Hermite 共役を X<sup>H</sup> と表す.)
 * </p>
 * 
 * <ul>
 * <li>(AA<sup>+</sup>)<sup>H</sup> = AA<sup>+</sup></li>
 * <li>(A<sup>+</sup>A)<sup>H</sup> = A<sup>+</sup>A</li>
 * <li>AA<sup>+</sup>A = A</li>
 * <li>A<sup>+</sup>AA<sup>+</sup> = A<sup>+</sup></li>
 * </ul>
 * 
 * <p>
 * Moore-Penrose 一般化逆行列は,
 * 線形連立方程式に対する最小二乗最小ノルム解を得るのに有効である. <br>
 * 行列 A と 縦ベクトル <b>b</b> が与えられたとき,
 * 線形連立方程式 <br>
 * A <b>x</b> = <b>b</b> <br>
 * に対する最小二乗最小ノルム解とは,
 * ||A <b>x</b> - <b>b</b>||<sub>2</sub>
 * を最小にするような <b>x</b> のうち,
 * ||<b>x</b>||<sub>2</sub>
 * が最小のものを指す. <br>
 * ただし, ||&middot;||<sub>2</sub> は Euclid ノルムである. <br>
 * この解は, <br>
 * <b>x</b> = A<sup>+</sup> <b>b</b> <br>
 * により表される.
 * </p>
 * 
 * <p>
 * このインターフェースを継承した全てのインターフェース, 実装した全てのクラスは実質的にイミュータブルであり,
 * (このインターフェース以外のものを含む) 全てのメソッドは関数的かつスレッドセーフである.
 * </p>
 *
 * @implSpec
 * 
 *               <p>
 *               実質的にイミュータブルかつ全てのメソッドは関数的かつスレッドセーフになるようにクラスが設計されなければならず,
 *               違反した場合は振る舞いが保証されない.
 *               </p>
 * 
 *               <p>
 *               {@link #inverse()} メソッドの戻り値は,
 *               複数回の呼び出しにおいて同一のインスタンスであるべきである:
 *               </p>
 * 
 *               <blockquote>
 * 
 *               <pre>
 * // 推奨: 次がtrue (複数回の呼び出しで同一のインスタンスを指す)
 * this.inverse() == this.inverse()</pre>
 * 
 *               </blockquote>
 * 
 *               <p>
 *               正則行列の場合において, 逆行列の逆行列は自身であるという数学的事実から,
 *               {@link #inverse()} メソッドの戻り値に {@link Invertible} を実装し,
 *               その {@link Invertible#inverse()} でターゲット行列を返したくなる. <br>
 *               これは過剰であり, {@link #inverse()} メソッドの戻り値に {@link Invertible}
 *               を実装するのは推奨されない.
 *               </p>
 * 
 *               <p>
 *               Moore-Penrose 逆行列が対称行列であることが確証できる場合,
 *               {@link #inverse()}の戻り値に{@link Symmetric} インターフェースを付与し,
 *               その旨を文書化すべきである.
 *               </p>
 * 
 * @author Matsuura Y.
 */
public interface GeneralizedInversion {

    /**
     * <p>
     * このインターフェースが紐づく行列を返す.
     * </p>
     * 
     * @implSpec
     *               可能な場合は, 戻り値型をより具象なものに変更すべきである.
     * @return このインターフェースが紐づく行列
     */
    public abstract Matrix target();

    /**
     * ターゲット行列の Moore-Penrose 一般化逆行列を取得する.
     *
     * @implSpec
     *               可能な場合は, 戻り値型をより具象なものに変更すべきである. <br>
     *               その他は, インターフェース説明の通り.
     * @return ターゲット行列の Moore-Penrose 一般化逆行列
     */
    public abstract Matrix inverse();
}
