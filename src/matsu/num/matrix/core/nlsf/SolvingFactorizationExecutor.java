/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.26
 */
package matsu.num.matrix.core.nlsf;

import java.util.Optional;

import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.PseudoRegularMatrixProcess;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * 線形連立方程式の解法向けの, 正方行列の行列分解の実行(行列分解を生成する行為)を扱う.
 * 
 * @author Matsuura Y.
 * @param <MT> 対応する行列の型パラメータ
 * @deprecated
 *                 このインターフェースは version 29 以降に削除される.
 *                 {@link LUTypeSolver.Executor} は, このインターフェースの完全な代替である.
 */
@Deprecated(forRemoval = true)
public interface SolvingFactorizationExecutor<MT extends Matrix> {

    /**
     * このインスタンスが与えた行列を受け入れることができるかを判定する. <br>
     * 仕様上, 正方行列でない場合は必ずrejectされる. <br>
     * その他の条件はサブタイプにゆだねられる.
     * 
     * @param matrix 判定する行列
     * @return 判定結果
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public abstract MatrixStructureAcceptance accepts(MT matrix);

    /**
     * 行列の正則性を判定する相対epsilonを指定して, 線形連立方程式の解法向けの行列分解を実行する.
     * 
     * <p>
     * 分解が開始されるためには, {@link #accepts(Matrix)} の戻り値の {@code type()}
     * がacceptedでなければならない. <br>
     * そうでないなら, {@link IllegalArgumentException} がスローされる.
     * </p>
     * 
     * <p>
     * 分解開始後に失敗した場合は, 空のオプショナルが返る. <br>
     * 正則行列でない場合には分解に失敗する. <br>
     * その他の条件はサブタイプにゆだねられる.
     * </p>
     * 
     * @param matrix 分解する行列
     * @param epsilon 相対epsilon
     * @return 行列分解, 分解不可能の場合は空
     * @throws IllegalArgumentException epsilonが0以上の有限数でない場合,
     *             行列がacceptされない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public abstract Optional<? extends LUTypeSolver> apply(MT matrix, double epsilon);

    /**
     * 行列の正則性を判定する相対epsilonにデフォルト値を使用して, 線形連立方程式の解法向けの行列分解を実行する. <br>
     * デフォルトepsilonは次の値である:
     * {@link PseudoRegularMatrixProcess#DEFAULT_EPSILON}
     * 
     * <p>
     * 例外と戻り値の仕様は {@link #apply(Matrix, double)} に準拠する.
     * </p>
     * 
     * @param matrix 分解する行列
     * @return 行列分解
     * @see PseudoRegularMatrixProcess
     */
    public abstract Optional<? extends LUTypeSolver> apply(MT matrix);
}
