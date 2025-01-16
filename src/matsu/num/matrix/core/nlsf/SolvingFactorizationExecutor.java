/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.17
 */
package matsu.num.matrix.core.nlsf;

import java.util.Optional;

import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.PseudoRegularMatrixProcess;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * 線形連立方程式の解法向けの, 正方行列の行列分解の実行(行列分解を生成する行為)を扱う.
 * 
 * <p>
 * 行列分解は, {@link #apply(Matrix)},
 * {@link #apply(Matrix, double)} メソッドにより実行される. <br>
 * 対応できる行列の型は型パラメータにより制限されるが,
 * 分解が成功するかどうかは具象クラスにゆだねられる. <br>
 * 行列に構造上の問題がある場合は {@link IllegalArgumentException} のサブクラスの例外をスローし,
 * 構造に問題はないが行列分解に失敗する場合は空のオプショナルを返す.
 * </p>
 * 
 * <p>
 * 行列に構造上の問題があるかどうかは, {@link #accepts(Matrix)} メソッドにより検証される. <br>
 * 戻り値のタイプ ({@link MatrixStructureAcceptance#type()}) がacceptedならば,
 * {@link #apply(Matrix)}, {@link #apply(Matrix, double)} メソッドの実行時に例外はスローされない.
 * </p>
 * 
 * <p>
 * acceptedにならない条件とは例えば次のようなものが挙げられる.
 * </p>
 * 
 * <ul>
 * <li>例: 正方でない行列が与えられる (仕様上, 必ず).</li>
 * <li>例: 行列の有効要素数が大きすぎて, アルゴリズムが対応できない.</li>
 * <li>例: 対称行列のみに対応するアルゴリズムに対して, 非対称行列が与えられる.</li>
 * </ul>
 * 
 * <p>
 * 行列分解に失敗する条件, すなわち空のオプショナルが返る条件とは例えば次のようなものが挙げられる.
 * </p>
 * 
 * <ul>
 * <li>例: 特異行列が与えられる.</li>
 * <li>例: 正定値行列のみに対応するアルゴリズムに対して, 不適当な行列が与えられる (例えばCholesky分解).</li>
 * <li>例: ピボッティングが必要な行列だが, アルゴリズムが対応していない (例えばLU分解).</li>
 * </ul>
 * 
 * <p>
 * このインターフェースを実装した全てのクラスは実質的にイミュータブルであり,
 * (このインターフェース以外を含む) 全てのメソッドは関数的かつスレッドセーフである.
 * </p>
 * 
 * 
 * <hr>
 * 
 * <h2>実装規約</h2>
 * 
 * <p>
 * 実質的にイミュータブルかつ全てのメソッドは関数的かつスレッドセーフになるようにクラスが設計されなければならず,
 * 違反した場合は振る舞いが保証されない.
 * </p>
 * 
 * <p>
 * {@link #accepts(Matrix)} メソッドの戻り値のタイプがacceptedにならない条件を文書化すべきである.
 * </p>
 * 
 * <p>
 * <u><i>このインターフェースは実装を隠ぺいして型を公開するためのものである. <br>
 * モジュール外での実装は不可.
 * </i></u>
 * </p>
 * 
 * @author Matsuura Y.
 * @param <MT> 対応する行列の型パラメータ
 */
public sealed interface SolvingFactorizationExecutor<MT extends Matrix>
        permits SkeletalSolvingFactorizationExecutor {

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
