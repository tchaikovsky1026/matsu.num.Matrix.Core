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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.OrthogonalMatrix;

/**
 * ブロック対角直交行列に関するUtility.
 * 
 * @author Matsuura Y.
 * @version 22.4
 */
final class BlockDiagonalOrthogonalUtil {

    private BlockDiagonalOrthogonalUtil() {
        //インスタンス化不可
        throw new AssertionError();
    }

    /**
     * 与えられたリストを対角ブロックに置いた時の全体の行列次元を返す.
     */
    static MatrixDimension calcDimension(Collection<? extends OrthogonalMatrix> blockSeries) {
        int dim = 0;
        for (Matrix m : blockSeries) {
            dim += m.matrixDimension().rowAsIntValue();
        }
        return MatrixDimension.square(dim);

    }

    /**
     * 与えられたブロック行列のリストを展開する. <br>
     * これにより, ブロック対角行列の入れ子がなくなる.
     * </p>
     */
    static Collection<OrthogonalMatrix> expand(Collection<? extends OrthogonalMatrix> rawBlockSeries) {
        List<OrthogonalMatrix> blockSeries = new LinkedList<>();
        for (OrthogonalMatrix mx : rawBlockSeries) {
            if (mx instanceof BlockDiagonalOrthogonalMatrix castedMx) {
                //要素Matrixが行列積を表しているなら展開する
                blockSeries.addAll(castedMx.toSeries());
                continue;
            }
            blockSeries.add(Objects.requireNonNull(mx));
        }
        return blockSeries;
    }
}
