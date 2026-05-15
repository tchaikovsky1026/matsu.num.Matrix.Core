/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.15
 */
package matsu.num.matrix.core;

import java.util.Optional;

import matsu.num.matrix.core.helper.matrix.multiply.OrthogonalMatrixMultiplicationUtil;
import matsu.num.matrix.core.helper.matrix.transpose.TranspositionOrthogonalUtil;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;
import matsu.num.matrix.core.validation.MatrixNotSymmetricException;

/**
 * 直交行列であることを表す.
 * 
 * <p>
 * 直交行列は正方行列の一種であり,
 * Q<sup>-1</sup> = Q<sup>T</sup> であるような行列である.
 * </p>
 * 
 * <p>
 * この直交行列インターフェースは, その逆行列取得性を
 * {@link Invertible} で表現している.
 * </p>
 * 
 * @implSpec
 *               <p>
 *               {@link Matrix}, {@link Invertible} の規約に従う.
 *               </p>
 * 
 *               <u><b>
 *               {@link #transpose()} と {@link #inverse()} の整合に関する規約
 *               </b></u>
 * 
 *               <p>
 *               {@link #inverse()} メソッドの戻り値の要素と {@link #transpose()}
 *               の戻り値は同一のインスタンスを返すべきである. <br>
 *               {@link Symmetric} インターフェースが付与されている場合,
 *               {@link #inverse()} メソッドの戻り値の要素は自身としなければならない.
 *               </p>
 * 
 *               <blockquote>
 * 
 *               <pre>
 * // 推奨: 次がtrue (転置行列と逆行列は同一)
 * this.inverse().get() == this.transpose()
 * 
 * // this instanceof Symmetric がtrueのときに
 * // 必須: 次がtrue (対称直交行列の逆行列は自身)
 * this.inverse().get() == this</pre>
 * 
 *               </blockquote>
 * 
 * @author Matsuura Y.
 * @see <a href="https://en.wikipedia.org/wiki/Orthogonal_matrix">
 *          Orthogonal matrix</a>
 */
public interface OrthogonalMatrix extends Matrix, Invertible {

    /**
     * 
     * @deprecated
     *                 この共変戻り値となった {@code transpose()} は使用すべきでない. <br>
     *                 {@code Matrix.transpose()} か
     *                 {@link #transposeAsOrthogonal()}
     *                 を使用すべき.
     * 
     *                 <p>
     *                 <i><u>
     *                 version 29 以降に削除され, {@code Matrix.transpose()}
     *                 が呼ばれるようになる.
     *                 </u></i>
     *                 </p>
     */
    @Deprecated(forRemoval = true, since = "28.9")
    @Override
    public abstract OrthogonalMatrix transpose();

    /**
     * この行列の転置行列を, 直交行列の形で返す.
     * 
     * @implSpec
     *               サブインターフェースでの型精密化を禁止する. <br>
     *               その他は, {@link Matrix#transpose()} に従う.
     * 
     *               <p>
     *               <i><u>
     *               version 29 以降デフォルトメソッドが削除されるので,
     *               実装側は必ずオーバーライドすること.
     *               </u></i>
     *               </p>
     * 
     * @return 転置行列
     */
    public default OrthogonalMatrix transposeAsOrthogonal() {
        return transpose();
    }

    /**
     * 逆行列を取得する. <br>
     * 必ず逆行列が存在するため, 戻り値は空でない.
     * 
     * @deprecated
     *                 この共変戻り値となった {@code inverse()} は使用すべきでない. <br>
     *                 {@code Invertible.inverse()} か
     *                 {@link #transposeAsOrthogonal()}
     *                 を使用すべき.
     * 
     *                 <p>
     *                 <i><u>
     *                 version 29 以降に削除され, {@code Invertible.inverse()}
     *                 が呼ばれるようになる.
     *                 </u></i>
     *                 </p>
     * 
     * @return {@inheritDoc }, 空でない
     */
    @Deprecated(forRemoval = true, since = "28.9")
    @Override
    public abstract Optional<? extends OrthogonalMatrix> inverse();

    /**
     * 1個以上の直交行列に対し, それらの行列積を返す.
     * 
     * @param first 行列積の左端の行列
     * @param following firstに続く行列, 左から順番
     * @return 直交行列の行列積
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static OrthogonalMatrix multiply(OrthogonalMatrix first, OrthogonalMatrix... following) {
        return OrthogonalMatrixMultiplicationUtil.apply(first, following);
    }

    /**
     * 対称な直交行列び行列積を返す. <br>
     * すなわち, 与えた直交行列 U<sub>L</sub>, U<sub>D</sub> に対して,
     * U<sub>L</sub>U<sub>D</sub>U<sub>L</sub><sup>T</sup> を返す. <br>
     * 戻り値には {@link Symmetric} が付与されている. <br>
     * 与える行列 U<sub>D</sub> には {@link Symmetric} が付与されていなければならない.
     * 
     * @param mid 対称直交行列 U<sub>D</sub>, 中央の行列
     * @param leftSide 直交行列 U<sub>L</sub>, 左サイドの行列
     * @return 対称な行列積
     * @throws MatrixNotSymmetricException 中央の行列 (U<sub>D</sub>) が対称でない場合
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static OrthogonalMatrix symmetricMultiply(OrthogonalMatrix mid, OrthogonalMatrix leftSide) {
        return OrthogonalMatrixMultiplicationUtil.symmetricMultiply(mid, leftSide);
    }

    /**
     * 
     * 与えられた直交行列の転置行列 (逆行列) を生成する.
     * 
     * <p>
     * 引数 {@code original}, 戻り値 {@code returnValue} について,
     * 次が {@code true} である.
     * </p>
     * 
     * <ul>
     * <li>{@code returnValue.transpose() == original}</li>
     * <li>{@code returnValue.inverse().get() == original}</li>
     * </ul>
     * 
     * <p>
     * {@code original} に {@link Symmetric} が付与されている場合,
     * {@code returnValue == original} が {@code true} である.
     * </p>
     * 
     * <p>
     * <u>
     * <i>
     * このメソッドは {@link #transpose()} や {@link #inverse()}
     * の実装を補助するために用意されている. <br>
     * {@link OrthogonalMatrix} およびそのサブタイプのインスタンスの転置行列, 逆行列を得る場合は,
     * このメソッドではなくインスタンスメソッドである {@link #transpose()}, {@link #inverse()}
     * を呼ばなければならない.
     * </i>
     * </u>
     * </p>
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     * @deprecated
     *                 この公開ヘルパメソッドは version 29 以降に削除される. <br>
     *                 代替となるメソッドは公開されていない.
     */
    @Deprecated(forRemoval = true, since = "28.7")
    public static OrthogonalMatrix createTransposedOf(OrthogonalMatrix original) {
        return TranspositionOrthogonalUtil.apply(original);
    }
}
