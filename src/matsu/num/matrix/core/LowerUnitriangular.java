/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.1.20
 */
package matsu.num.matrix.core;

import java.util.Optional;

import matsu.num.matrix.core.sealed.LowerUnitriangularSealed;

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
 * <u><i>
 * このインターフェースは主に, 戻り値型を公開するために用意されており,
 * モジュール外での実装は想定されていない.
 * </i></u>
 * </p>
 *
 * @author Matsuura Y.
 * @see EntryReadableMatrix
 * @see Invertible
 */
public sealed interface LowerUnitriangular
        extends EntryReadableMatrix, Invertible, Determinantable
        permits LowerUnitriangularMatrix, LowerUnitriangularBandMatrix, UnitMatrix, LowerUnitriangularSealed {

    /**
     * 逆行列を取得する. <br>
     * 必ず逆行列が存在するため, 戻り値は空でない.
     * 
     * @return {@inheritDoc }, 空でない
     */
    @Override
    public abstract Optional<? extends Matrix> inverse();
}
