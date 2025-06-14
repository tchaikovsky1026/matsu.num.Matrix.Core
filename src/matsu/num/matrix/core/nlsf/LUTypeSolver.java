/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.5.9
 */
package matsu.num.matrix.core.nlsf;

import matsu.num.matrix.core.Determinantable;
import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.Inversion;

/**
 * LU分解型の, 線形連立方程式求解を扱う. <br>
 * 求解のための逆行列の提供と, ターゲット行列の行列式計算をサポートする.
 * 
 * <p>
 * LU分解とは, 正則な正方行列 A を, <br>
 * A = LU, <br>
 * A = LDU, <br>
 * A = PLDU, <br>
 * といった形に分解することである. <br>
 * ただし, L は下三角行列, U は上三角行列, D は対角行列, P は置換行列を表す. <br>
 * A についての逆行列
 * (つまり, 係数行列 A に対する線形連立方程式の解法)
 * と, A の行列式を得られるのがこの分解の特徴である.
 * </p>
 * 
 * @implSpec
 *               <p>
 *               {@link Inversion}, {@link Determinantable} の規約に従う.
 *               </p>
 * 
 *               <p>
 *               このインターフェースは主に, 戻り値型を公開するために用意されており,
 *               モジュール外での実装は想定されていない. <br>
 *               モジュール外で実装する場合, 互換性が失われる場合がある.
 *               </p>
 * 
 * @author Matsuura Y.
 */
public interface LUTypeSolver
        extends Inversion, Determinantable {

    /**
     * @implSpec
     *               {@link Inversion#target()} に従う.
     */
    @Override
    public abstract EntryReadableMatrix target();
}
