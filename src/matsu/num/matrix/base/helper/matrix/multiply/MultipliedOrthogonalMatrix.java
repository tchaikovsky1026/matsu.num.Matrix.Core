/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.3
 */
package matsu.num.matrix.base.helper.matrix.multiply;

import java.util.Deque;
import java.util.Optional;

import matsu.num.matrix.base.OrthogonalMatrix;

/**
 * 直交行列が行列積として表現されていることを通知可能にするインターフェース.
 * 
 * @author Matsuura Y.
 * @version 22.0
 */
public interface MultipliedOrthogonalMatrix extends MultipliedMatrix, OrthogonalMatrix {

    /**
     * 行列積の表現を{@link Deque}として返す.
     * 
     * <p>
     * 変更できない形, もしくは防御的コピーをして返さなければならない.
     * </p>
     * 
     * @return 行列積の表現
     */
    @Override
    public abstract Deque<? extends OrthogonalMatrix> toSeries();

    @Override
    public abstract MultipliedOrthogonalMatrix transpose();

    @Override
    public abstract Optional<? extends MultipliedOrthogonalMatrix> inverse();
}
