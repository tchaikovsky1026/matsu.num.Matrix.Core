/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.12
 */
package matsu.num.matrix.base.block;

import matsu.num.matrix.base.OrthogonalMatrix;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.validation.MatrixNotSymmetricException;

/**
 * ブロック行列の構築に関するユーティリティクラス.
 * 
 * @author Matsuura Y.
 * @version 22.4
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
     * より詳しく, ...
     * </p>
     * 
     * @param first 左上ブロックの行列
     * @param following firstに続く行列, 左上から右下に向かって順番
     * @return ブロック対角直交行列
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
     * より詳しく, ...
     * </p>
     * 
     * <p>
     * 戻り値には, {@link Symmetric} が付与されている.
     * </p>
     * 
     * @param first 左上ブロックの行列
     * @param following firstに続く行列, 左上から右下に向かって順番
     * @return 対称ブロック対角直交行列
     * @throws MatrixNotSymmetricException 引数の行列が対称でない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static OrthogonalMatrix symmetricBlockDiagonalOrthogonalMatrixOf(
            OrthogonalMatrix first, OrthogonalMatrix... following) {

        return SymmetricBlockDiagonalOrthogonalMatrix.matrixOf(first, following);
    }
}
