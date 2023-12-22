/**
 * 2023.11.30
 */
package matsu.num.matrix.base.helper.matrix.multiply;

import java.util.Deque;

import matsu.num.matrix.base.Matrix;

/**
 * 行列が行列積として表現されていることを通知可能にするインターフェース.
 * 
 * @author Matsuura Y.
 * @version 17.1
 */
public interface MultipliedMatrix extends Matrix {

    /**
     * 行列積の表現を{@linkplain Deque}として返す.
     * 
     * <p>
     * 変更できない形, もしくは防御的コピーをして返さなければならない.
     * </p>
     * 
     * @return 行列積の表現
     */
    public abstract Deque<? extends Matrix> toSeries();

}
