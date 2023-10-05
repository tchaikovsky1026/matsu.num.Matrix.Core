/**
 * 2023.8.16
 */
package matsu.num.matrix.base.nlsf;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.PseudoRegularMatrixProcess;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;
import matsu.num.matrix.base.exception.ProcessFailedException;

/**
 * 線形連立方程式の解法向けの行列分解を実行(行列分解を生成する行為)を扱う.
 *  
 * <p>
 * このインターフェースのサブタイプはメソッドの振る舞いに関わる変更可能な内部属性を持たず, 
 * 全てのインスタンスメソッドは関数的でスレッドセーフである.
 * </p>
 * 
 * <p>
 * 対応できる行列の型は型パラメータにより制限されるが, 
 * 分解が成功するかどうかは具象クラスにゆだねられる. <br>
 * 行列分解に失敗した場合は, {@linkplain IllegalArgumentException}のサブクラスの例外をスローする. <br>
 * ただし, 例外を投げる条件はクラスドキュメントに記載する. <br>
 * </p>
 * 
 * <p>
 * 行列分解に失敗する条件とは例えば次のようなものが挙げられる. 
 * </p>
 * 
 * <ul>
 * <li>行列の有効要素数が大きすぎて, アルゴリズムが対応できない. </li>
 * <li>正定値行列のみに対応するアルゴリズムに対して, 不適当な行列が与えられる. </li>
 * <li>ピボッティングが必要な行列だが, アルゴリズムが対応していない. </li>
 * </ul>
 * 
 * @author Matsuura Y.
 * @version 15.0
 * @param <MT> 対応する行列の型
 * @param <ST> 出力される行列分解の型
 */
public interface SolvingFactorizationExecutor<
        MT extends Matrix, ST extends LinearEquationSolving<MT>> {

    /**
     * 行列の正則性を判定する相対epsilonを指定して, 線形連立方程式の解法向けの行列分解を実行する.
     * 
     * @param matrix 分解する行列
     * @param epsilon 相対epsilon
     * @return 行列分解
     * @throws IllegalArgumentException epsilonが0以上の有限数でない場合, サブタイプの説明で記載される条件の場合
     * @throws MatrixFormatMismatchException 行列が正方サイズでない場合
     * @throws ProcessFailedException 行列が特異の場合, 分解後の成分に不正な値を含み分解が完了できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public abstract ST apply(MT matrix, double epsilon);

    /**
     * 行列の正則性を判定する相対epsilonにデフォルト値を使用して, 線形連立方程式の解法向けの行列分解を実行する.
     * 
     * <p>
     * スローされる例外は{@linkplain #apply(Matrix, double)}の実装に準拠する.
     * </p>
     * 
     * @param matrix 分解する行列
     * @return 行列分解
     * @see PseudoRegularMatrixProcess
     */
    public abstract ST apply(MT matrix);
}
