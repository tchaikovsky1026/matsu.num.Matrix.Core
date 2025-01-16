/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.11
 */
package matsu.num.matrix.core;

import java.util.Optional;

/**
 * 成分にアクセス可能な単位下三角行列を表す.
 * 
 * <p>
 * 単位下三角行列とは, 対角成分が1の下三角行列である.
 * </p>
 * 
 * <p>
 * 行列式は1である. <br>
 * したがって, 逆行列は必ず存在する. <br>
 * 単位下三角行列の逆行列は, 行列ベクトル積が容易に計算できるので,
 * このインターフェースは {@link Invertible} を継承する.
 * </p>
 *
 * <p>
 * 実装においては, {@link EntryReadableMatrix} のクラス説明の規約に従う.
 * </p>
 *
 * @author Matsuura Y.
 * @see EntryReadableMatrix
 * @see Invertible
 */
public interface LowerUnitriangular
        extends EntryReadableMatrix, Invertible, Determinantable {

    /**
     * 逆行列を取得する. <br>
     * 必ず逆行列が存在するため, 戻り値は空でない.
     * 
     * @return {@inheritDoc }, 空でない
     */
    @Override
    public abstract Optional<? extends Matrix> inverse();
}
