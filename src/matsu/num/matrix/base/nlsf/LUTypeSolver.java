/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.5
 */
package matsu.num.matrix.base.nlsf;

import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.Inversion;

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
 * <hr>
 * 
 * <h2>実装規約</h2>
 * 
 * <p>
 * {@link Inversion}, {@link Determinantable} の規約に従う.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.0
 */
@SuppressWarnings("rawtypes")
public sealed interface LUTypeSolver
        extends Inversion, Determinantable permits SkeletalLUTypeSolver, SymmetrizedSquareTypeSolver {

    @Override
    public abstract EntryReadableMatrix target();
}
