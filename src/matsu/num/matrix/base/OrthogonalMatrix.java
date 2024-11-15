/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.11
 */
package matsu.num.matrix.base;

import java.util.Optional;

import matsu.num.matrix.base.helper.matrix.multiply.OrthogonalMatrixMultiplicationUtil;
import matsu.num.matrix.base.helper.matrix.transpose.TranspositionOrthogonal;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;
import matsu.num.matrix.base.validation.MatrixNotSymmetricException;

/**
 * 直交行列であることを表す.
 * 
 * <p>
 * 直交行列は転置行列が逆行列に一致する. <br>
 * したがって, 逆行列は必ず取得できる.
 * </p>
 * 
 * <hr>
 * 
 * <h2>実装規約</h2>
 * 
 * <p>
 * {@link Matrix}, {@link Invertible} の規約に従う.
 * </p>
 * 
 * <h2>{@link #transpose()} と {@link #inverse()} の整合に関する規約</h2>
 * 
 * <p>
 * {@link #inverse()} メソッドの戻り値の要素と {@link #transpose()}
 * の戻り値は同一のインスタンスを返すべきである. <br>
 * {@link Symmetric} インターフェースが付与されている場合,
 * {@link #inverse()} メソッドの戻り値の要素は自身としなければならない.
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * // 推奨: 次がtrue (転置行列と逆行列は同一)
 * this.inverse().get() == this.transpose()
 * 
 * // this instanceof Symmetric がtrueのときに
 * // 必須: 次がtrue (対称直交行列の逆行列は自身)
 * this.inverse().get() == this
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Matsuura Y.
 * @version 22.4
 */
public interface OrthogonalMatrix extends Matrix, Invertible {

    /**
     * この行列の転置行列を返す.
     * 
     * @return 転置行列
     */
    @Override
    public abstract OrthogonalMatrix transpose();

    /**
     * 逆行列を取得する. <br>
     * 必ず逆行列が存在するため, 戻り値は空でない.
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
     * <i>
     * <u>このメソッドの利用について</u> <br>
     * {@link OrthogonalMatrix} およびそのサブタイプから転置行列や逆行列を得るには,
     * {@link #transpose()}, {@link #inverse()} を呼ぶことが推奨される. <br>
     * このメソッドは {@link #transpose()} や {@link #inverse()},
     * {@link SkeletalAsymmetricOrthogonalMatrix#createTranspose()}
     * の戻り値の生成を補助するために用意されている. <br>
     * (ただし, {@link #transpose()}, {@link #inverse()}
     * の複数回の呼び出しで同一のインスタンスを返すようにキャッシュすることが推奨される.)
     * </i>
     * </p>
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static OrthogonalMatrix createTransposedOf(OrthogonalMatrix original) {
        return TranspositionOrthogonal.instance().apply(original);
    }
}
