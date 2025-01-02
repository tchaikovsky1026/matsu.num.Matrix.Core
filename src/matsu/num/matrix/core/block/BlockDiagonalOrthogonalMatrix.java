/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.12
 */
package matsu.num.matrix.core.block;

import java.util.Collection;
import java.util.Optional;

import matsu.num.matrix.core.OrthogonalMatrix;

/**
 * ブロック対角行列で直交行列であること通知可能にするインターフェース.
 * 
 * <p>
 * {@link BlockDiagonalOrthogonalMatrix} では, 対角ブロック並んだ直交行列のリストにアクセスできる. <br>
 * アクセスの必要性は, 主には入れ子になったブロック対角直交行列を展開するときであろう. <br>
 * これは, 行列生成と同程度の頻度である. <br>
 * 行列ベクトル積は内部ではブロック要素アクセスするが, これはインスタンス内部表現を用いるため,
 * リストを公開されている必要はない.
 * </p>
 * 
 * <p>
 * 以上より, {@link #toSeries()} メソッドのコールは行列生成と同程度の頻度であり,
 * コピー表現をその都度生成しても問題ない.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.4
 */
interface BlockDiagonalOrthogonalMatrix extends OrthogonalMatrix {

    /**
     * ブロックの表現を {@link Collection} として返す.
     * 
     * <p>
     * 変更できない形, もしくは防御的コピーをして返さなければならない.
     * </p>
     * 
     * @return ブロック表現
     */
    public abstract Collection<? extends OrthogonalMatrix> toSeries();

    @Override
    public abstract BlockDiagonalOrthogonalMatrix transpose();

    @Override
    public abstract Optional<? extends BlockDiagonalOrthogonalMatrix> inverse();
}
