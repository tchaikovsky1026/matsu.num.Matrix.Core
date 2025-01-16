/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.23
 */
package matsu.num.matrix.core.block;

import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.Symmetric;
import matsu.num.matrix.core.validation.ElementsTooManyException;
import matsu.num.matrix.core.validation.MatrixNotSymmetricException;

/**
 * ブロック行列の構築に関するユーティリティクラス.
 * 
 * @author Matsuura Y.
 */
public final class BlockMatrixSupport {

    private BlockMatrixSupport() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * 1個以上の直交行列を対角ブロックに並べ,
     * 非対角ブロックに零行列を置いた, ブロック対角直交行列を返す.
     * 
     * <p>
     * ブロック対角直交行列について, 例を挙げて説明する. <br>
     * 対角ブロックに並ぶ直交行列要素を3個とし,
     * U<sub>1</sub>, U<sub>2</sub>, U<sub>3</sub>
     * とする. <br>
     * これらから生成されるブロック対角直交行列は, <br>
     * &lceil; U<sub>1</sub> O O &rceil; <br>
     * &vert; O U<sub>2</sub> O &vert; <br>
     * &lfloor; O O U<sub>3</sub> &rfloor; <br>
     * である.
     * U<sub>1</sub>, U<sub>2</sub>, U<sub>3</sub>
     * の行列サイズは一致する必要はない.
     * </p>
     * 
     * @param first 左上ブロックの行列
     * @param following firstに続く行列, 左上から右下に向かって順番
     * @return ブロック対角直交行列
     * @throws ElementsTooManyException 全体のサイズが大きすぎる場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static OrthogonalMatrix blockDiagonalOrthogonalMatrixOf(
            OrthogonalMatrix first, OrthogonalMatrix... following) {

        return BlockDiagonalOrthogonalMatrixImpl.matrixOf(first, following);
    }

    /**
     * 1個以上の対称直交行列を対角ブロックに並べ,
     * 非対角ブロックに零行列を置いた, 対称ブロック対角直交行列を返す.
     * 
     * <p>
     * 対称ブロック対角直交行列について, 例を挙げて説明する. <br>
     * 対角ブロックに並ぶ対称直交行列要素を3個とし,
     * H<sub>1</sub>, H<sub>2</sub>, H<sub>3</sub>
     * とする. <br>
     * これらから生成されるブロック対角直交行列は, <br>
     * &lceil; H<sub>1</sub> O O &rceil; <br>
     * &vert; O H<sub>2</sub> O &vert; <br>
     * &lfloor; O O H<sub>3</sub> &rfloor; <br>
     * である.
     * H<sub>1</sub>, H<sub>2</sub>, H<sub>3</sub>
     * の行列サイズは一致する必要はない.
     * </p>
     * 
     * <p>
     * 引数に {@link Symmetric} が付与されていなければならない. <br>
     * 戻り値には, {@link Symmetric} が付与されている.
     * </p>
     * 
     * @param first 左上ブロックの行列
     * @param following firstに続く行列, 左上から右下に向かって順番
     * @return 対称ブロック対角直交行列
     * @throws MatrixNotSymmetricException 引数の行列が対称でない場合
     * @throws ElementsTooManyException 全体のサイズが大きすぎる場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static OrthogonalMatrix symmetricBlockDiagonalOrthogonalMatrixOf(
            OrthogonalMatrix first, OrthogonalMatrix... following) {

        return SymmetricBlockDiagonalOrthogonalMatrix.matrixOf(first, following);
    }
}
