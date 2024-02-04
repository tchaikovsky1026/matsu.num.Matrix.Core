/**
 * 2024.2.2
 */
package matsu.num.matrix.base.nlsf;

import java.util.Optional;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.PseudoRegularMatrixProcess;
import matsu.num.matrix.base.validation.MatrixStructureAcceptance;

/**
 * <p>
 * 線形連立方程式の解法向けの, 正方行列の行列分解の実行(行列分解を生成する行為)を扱う.
 * </p>
 * 
 * <p>
 * このインターフェースのサブタイプはメソッドの振る舞いに関わる変更可能な内部属性を持たず,
 * 全てのインスタンスメソッドは関数的でスレッドセーフである.
 * </p>
 * 
 * <p>
 * 対応できる行列の型は型パラメータにより制限されるが,
 * 分解が成功するかどうかは具象クラスにゆだねられる. <br>
 * 行列に構造上の問題がある場合は {@linkplain IllegalArgumentException} のサブクラスの例外をスローし,
 * 構造に問題はないが行列分解に失敗する場合は空のオプショナルを返す.
 * </p>
 * 
 * <p>
 * 行列に構造上の問題があるかどうかは, {@linkplain #accepts(Matrix)} により検証される. <br>
 * 戻り値の {@code type()} がacceptedならば, 行列分解時に例外はスローされない. <br>
 * acceptedにならない条件はドキュメントに記載するべきである. <br>
 * </p>
 * 
 * <p>
 * acceptedにならない条件とは例えば次のようなものが挙げられる.
 * </p>
 * 
 * <ul>
 * <li>例: 正方でない行列が与えられる.</li>
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
 * <li>例: 正定値行列のみに対応するアルゴリズムに対して, 不適当な行列が与えられる.</li>
 * <li>例: ピボッティングが必要な行列だが, アルゴリズムが対応していない.</li>
 * </ul>
 * 
 * @author Matsuura Y.
 * @version 19.5
 * @param <MT> 対応する行列の型
 * @param <ST> 出力される行列分解の型
 */
public interface SolvingFactorizationExecutor<
        MT extends Matrix, ST extends LUTypeSolver> {

    /**
     * <p>
     * このインスタンスが与えた行列を受け入れることができるかを判定する. <br>
     * 仕様上, 正方行列でない場合は必ずrejectされる. <br>
     * その他の条件は, サブタイプのクラス説明文に従う.
     * </p>
     * 
     * @param matrix 判定する行列
     * @return 判定結果
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public abstract MatrixStructureAcceptance accepts(MT matrix);

    /**
     * <p>
     * 行列の正則性を判定する相対epsilonを指定して, 線形連立方程式の解法向けの行列分解を実行する.
     * </p>
     * 
     * <p>
     * 分解が開始されるためには, {@linkplain #accepts(Matrix)} の戻り値の {@code type()}
     * がacceptedでなければならない. <br>
     * そうでないなら, {@linkplain IllegalArgumentException} (のサブクラス) の例外がスローされる.
     * </p>
     * 
     * <p>
     * 分解開始後に失敗した場合は, 空のオプショナルが返る. <br>
     * 正則行列でない場合, 正則であるが数値が {@code double} で表現できない場合には分解に失敗する. <br>
     * その他の条件は, サブタイプのクラス説明文に従う.
     * </p>
     * 
     * @param matrix 分解する行列
     * @param epsilon 相対epsilon
     * @return 行列分解, 分解不可能の場合は空
     * @throws IllegalArgumentException epsilonが0以上の有限数でない場合,
     *             行列がacceptされない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public abstract Optional<? extends ST> apply(MT matrix, double epsilon);

    /**
     * <p>
     * 行列の正則性を判定する相対epsilonにデフォルト値を使用して, 線形連立方程式の解法向けの行列分解を実行する. <br>
     * デフォルトepsilonは次の値である:
     * {@linkplain PseudoRegularMatrixProcess#DEFAULT_EPSILON}
     * </p>
     * 
     * <p>
     * 例外と戻り値の仕様は {@linkplain #apply(Matrix, double)} に準拠する.
     * </p>
     * 
     * @param matrix 分解する行列
     * @return 行列分解
     * @see PseudoRegularMatrixProcess
     */
    public abstract Optional<? extends ST> apply(MT matrix);

}
