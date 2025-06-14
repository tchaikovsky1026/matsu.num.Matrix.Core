/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.14
 */
package matsu.num.matrix.core;

import matsu.num.matrix.core.helper.matrix.multiply.MatrixMultiplicationUtil;
import matsu.num.matrix.core.helper.matrix.transpose.TranspositionUtil;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;
import matsu.num.matrix.core.validation.MatrixNotSymmetricException;

/**
 * 矩形(長方形)の行列を扱う.
 * 
 * <p>
 * {@link Matrix} インターフェースを実装した全てのクラスは実質的にイミュータブルであり,
 * (このインターフェース以外を含む) 全てのメソッドは関数的かつスレッドセーフである.
 * </p>
 *
 * @implSpec
 *               <p>
 *               実質的にイミュータブルかつ全てのメソッドは関数的かつスレッドセーフになるようにクラスが設計されなければならず,
 *               違反した場合は振る舞いが保証されない.
 *               </p>
 * 
 *               <p>
 *               インスタンスの equality は提供すべきではない. <br>
 *               ({@link Object#equals(Object)},
 *               {@link Object#hashCode()} をオーバーライドすべきではない.)
 *               </p>
 * 
 *               <p>
 *               {@link Symmetric} インターフェースが付与される場合, 必ず正方形次元 (サイズ) でなければならない.
 *               <br>
 *               すなわち,
 *               {@code this.matrixDimension().isSquare() == true}
 *               でなければならない.
 *               </p>
 * 
 *               <p>
 *               <u><b> {@link #transpose()} に関する規約 </b></u>
 *               </p>
 * 
 *               <p>
 *               自身の転置行列を返す {@link #transpose()} メソッドの戻り値は,
 *               このメソッドの複数回の呼び出しにおいて同一のインスタンスを返すべきである. <br>
 *               また, その転置行列の {@link #transpose()} メソッドの戻り値は自身となることが望ましい. <br>
 *               {@link Symmetric} インターフェースが付与されている場合,
 *               自身の {@link #transpose()} メソッドの戻り値は自身としなければならない.
 *               </p>
 * 
 *               <blockquote>
 * 
 *               <pre>
 * // 推奨: 次がtrue (複数回の呼び出しで同一のインスタンスを返す)
 * this.transpose() == this.transpose()
 * 
 * // 推奨: 次がtrue (転置の転置は自身)
 * this.transpose().transpose() == this
 * 
 * // this instanceof Symmetric がtrueのときに
 * // 必須: 次がtrue (対称行列の転置は自身)
 * this.transpose() == this</pre>
 * 
 *               </blockquote>
 *
 * @author Matsuura Y.
 */
public interface Matrix {

    /**
     * 行列の次元 (サイズ) を取得する.
     *
     * @return 行列の次元
     */
    public MatrixDimension matrixDimension();

    /**
     * 行列に右からベクトルを作用させる: <b>w</b> = M<b>v</b>.
     * <br>
     * M: 行列({@code this}). <br>
     * <b>v</b>: 右から作用させるベクトル. <br>
     * <b>w</b>: 計算結果の出力変数ベクトル.
     * 
     * <p>
     * ただし, 演算結果は {@link Vector} が扱うことができる値の範囲を超えないように修正される.
     * </p>
     *
     * @param operand <b>v</b>, 作用ベクトル
     * @return 計算結果 <b>w</b>
     * @throws MatrixFormatMismatchException 作用ベクトル <b>v</b> の次元が行列サイズと適合しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Vector operate(Vector operand);

    /**
     * 行列の転置に右からベクトルを作用させる:
     * <b>w</b> = M<sup>T</sup><b>v</b>. <br>
     * M: {@code this} を転置した行列. <br>
     * <b>v</b>: 右から作用させるベクトル. <br>
     * <b>w</b>: 計算結果の出力変数ベクトル.
     * 
     * <p>
     * ただし, 演算結果は {@link Vector} が扱うことができる値の範囲を超えないように修正される.
     * </p>
     *
     * @param operand <b>v</b>, 作用ベクトル
     * @return 計算結果 <b>w</b>
     * @throws MatrixFormatMismatchException 作用ベクトル <b>v</b> の次元が行列サイズと適合しない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Vector operateTranspose(Vector operand);

    /**
     * この行列の転置行列を返す.
     * 
     * @implSpec
     *               可能な場合は, 戻り値型をより具象なものに変更すべきである. <br>
     *               その他は, インターフェース説明 ({@link #transpose()} に関する規約) の通り.
     * 
     * @return 転置行列
     */
    public abstract Matrix transpose();

    /**
     * 1個以上の行列に対し, それらの行列積を返す.
     * 
     * @param first 行列積の左端の行列
     * @param following firstに続く行列, 左から順番
     * @return 行列積
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static Matrix multiply(Matrix first, Matrix... following) {
        return MatrixMultiplicationUtil.apply(first, following);
    }

    /**
     * 行列の対称化二乗を返す. <br>
     * すなわち, 与えた行列 A に対して, AA<sup>T</sup> を返す. <br>
     * 戻り値には {@link Symmetric} が付与されている.
     * 
     * @param original 元の行列
     * @return 対称行列積
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static Matrix symmetrizedSquare(Matrix original) {
        return MatrixMultiplicationUtil.symmetrizedSquare(original);
    }

    /**
     * 対称な行列積を返す. <br>
     * すなわち, 与えた行列 L, D に対して, LDL<sup>T</sup> を返す. <br>
     * 戻り値には {@link Symmetric} が付与されている. <br>
     * 与える行列Dには {@link Symmetric} が付与されていなければならない.
     * 
     * @param mid 行列 D, 中央の行列
     * @param leftSide 行列 L, 左サイドの行列
     * @return 対称な行列積
     * @throws MatrixNotSymmetricException 中央の行列が対称でない場合
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static Matrix symmetricMultiply(Matrix mid, Matrix leftSide) {
        return MatrixMultiplicationUtil.symmetricMultiply(mid, leftSide);
    }

    /**
     * 与えられた行列の転置行列を生成する.
     * 
     * <p>
     * 引数 {@code original}, 戻り値 {@code returnValue} について,
     * {@code returnValue.transpose() == original} が {@code true} である.
     * <br>
     * {@code original} に {@link Symmetric} が付与されている場合,
     * {@code returnValue == original} が {@code true} である.
     * </p>
     * 
     * <p>
     * <u>
     * <i>
     * このメソッドは {@link #transpose()} や
     * {@link SkeletalAsymmetricMatrix#createTranspose()}
     * の実装を補助するために用意されている. <br>
     * {@link Matrix} およびそのサブタイプのインスタンスの転置行列を得る場合は,
     * このメソッドではなく {@link #transpose()} を呼ばなければならない.
     * </i>
     * </u>
     * </p>
     * 
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static Matrix createTransposedOf(Matrix original) {
        return TranspositionUtil.apply(original);
    }
}
