/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.2
 */
package matsu.num.matrix.core.nlsf;

import matsu.num.matrix.core.Determinantable;
import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.Inversion;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.Symmetric;

/**
 * 対称化二乗型分解:
 * 正定値対称行列 A について A = BB<sup>T</sup> と分解された
 * {@link LUTypeSolver}. <br>
 * B に関する線形連立方程式解法の機能も提供する. <br>
 * 主に, Cholesky分解の抽象化である.
 * 
 * <hr>
 * 
 * <h2>実装規約</h2>
 * 
 * <p>
 * {@link Inversion}, {@link Determinantable} の規約に従う.
 * </p>
 * 
 * <p>
 * {@link #asymmSqrt()}, {@link #inverseAsymmSqrt()} メソッドの戻り値は,
 * 複数回の呼び出しにおいて同一のインスタンスであるべきである.
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * // 推奨: 次がtrue (複数回の呼び出しで同一のインスタンスを指す)
 * this.asymmSqrt() == this.asymmSqrt()
 * this.inverseAsymmSqrt() == this.inverseAsymmSqrt()
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * {@link #target()} によって返される行列 A,
 * {@link #inverse()} によって返される行列 A<sup>-1</sup> には,
 * {@link Symmetric} が付与されていなければならない.
 * </p>
 * 
 * @author Matsuura Y.
 */
public sealed interface SymmetrizedSquareTypeSolver extends LUTypeSolver
        permits SkeletalSymmetrizedSquareTypeSolver {

    /**
     * {@inheritDoc}
     * 
     * <p>
     * {@link Symmetric} が付与されている.
     * </p>
     */
    @Override
    public abstract EntryReadableMatrix target();

    /**
     * {@inheritDoc}
     * 
     * <p>
     * {@link Symmetric} が付与されている.
     * </p>
     */
    @Override
    public abstract Matrix inverse();

    /**
     * 非対称平方根を返す. <br>
     * すなわち, ターゲット行列 A を A = BB<sup>T</sup>と分解したときの B を返す.
     * 
     * @return 非対称平方根 B
     */
    public abstract Matrix asymmSqrt();

    /**
     * 非対称平方根の逆行列を返す. <br>
     * すなわち, ターゲット行列 A を A = BB<sup>T</sup>と分解したときの B<sup>-1</sup> を返す.
     * 
     * @return 非対称平方根の逆行列 B<sup>-1</sup>
     */
    public abstract Matrix inverseAsymmSqrt();
}
