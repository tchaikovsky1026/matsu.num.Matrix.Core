/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.9
 */
package matsu.num.matrix.core;

import java.util.Optional;

import matsu.num.matrix.core.helper.matrix.multiply.OrthogonalMatrixMultiplicationUtil;
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
     * @implSpec
     *               可能な場合は, 戻り値型をより具象なものに変更すべきである. <br>
     *               その他は, インターフェース説明の通り.
     */
    @Override
    public abstract OrthogonalMatrix transpose();

    /**
     * 逆行列を取得する. <br>
     * 必ず逆行列が存在するため, 戻り値は空でない.
     * 
     * @implSpec
     *               可能な場合は, 戻り値型をより具象なものに変更すべきである. <br>
     *               その他は, インターフェース説明の通り.
     * 
     * @return {@inheritDoc }, 空でない
     */
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
}
