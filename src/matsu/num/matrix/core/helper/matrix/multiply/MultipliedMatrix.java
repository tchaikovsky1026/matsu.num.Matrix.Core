/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.11
 */
package matsu.num.matrix.core.helper.matrix.multiply;

import java.util.Deque;

import matsu.num.matrix.core.Matrix;

/**
 * 行列が行列積として表現されていることを通知可能にするインターフェース.
 * 
 * @author Matsuura Y.
 */
interface MultipliedMatrix extends Matrix {

    /**
     * 行列積の表現を{@link Deque}として返す.
     * 
     * <p>
     * 変更できない形, もしくは防御的コピーをして返さなければならない.
     * </p>
     * 
     * @return 行列積の表現
     */
    public abstract Deque<? extends Matrix> toSeries();

    @Override
    public abstract MultipliedMatrix transpose();
}
