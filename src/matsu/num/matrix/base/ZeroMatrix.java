/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.1
 */
package matsu.num.matrix.base;

/**
 * 零行列を表現する.
 * 
 * @author Matsuura Y.
 * @version 23.2
 */
public sealed interface ZeroMatrix extends EntryReadableMatrix
        permits ZeroMatrixSealed {

    @Override
    public abstract ZeroMatrix transpose();

    /**
     * 与えられた次元(サイズ)の零行列を生成する.
     *
     * @param matrixDimension 行列サイズ
     * @return 零行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static ZeroMatrix matrixOf(final MatrixDimension matrixDimension) {
        return new ZeroMatrixSealed.Impl(matrixDimension);
    }
}
