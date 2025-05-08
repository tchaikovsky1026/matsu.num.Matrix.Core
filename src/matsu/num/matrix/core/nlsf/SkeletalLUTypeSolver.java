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

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.Matrix;

/**
 * {@link LUTypeSolver} の骨格実装.
 * 
 * <p>
 * このクラスでは,
 * {@link #inverse()}, {@link #signOfDeterminant()},
 * {@link #determinant()}, {@link #logAbsDeterminant()}
 * メソッドの適切な実装を提供する. <br>
 * これらの戻り値は {@link #createInverseDeterminantStruct()}
 * メソッドにより一度だけ計算, キャッシュされ,
 * 以降はそのキャッシュを戻す.
 * </p>
 * 
 * 
 * <hr>
 * 
 * <h2>使用上の注意</h2>
 * 
 * <p>
 * このクラスはインターフェースの骨格実装を提供するためのものであり,
 * 型として扱うべきではない. <br>
 * 具体的に, 次のような取り扱いは強く非推奨である.
 * </p>
 * 
 * <ul>
 * <li>このクラスを変数宣言の型として使う.</li>
 * <li>{@code instanceof} 演算子により, このクラスのサブタイプかを判定する.</li>
 * <li>インスタンスをこのクラスにキャストして使用する.</li>
 * </ul>
 * 
 * <p>
 * このクラスは, 型としての互換性は積極的には維持されず,
 * このモジュールや関連モジュールの具象クラスが将来的にこのクラスのサブタイプでなくなる場合がある.
 * </p>
 * 
 * @author Matsuura Y.
 * @param <TT> ターゲット行列の型パラメータ, {@link #target()} の戻り値型をサブタイプで限定する.
 * @param <IT> 逆行列の型パラメータ, {@link #inverse()} の戻り値型をサブタイプで限定する.
 */
abstract class SkeletalLUTypeSolver<TT extends EntryReadableMatrix, IT extends Matrix>
        extends InversionDeterminantableImplementation<TT, IT> implements LUTypeSolver {

    /**
     * 唯一のコンストラクタ.
     */
    SkeletalLUTypeSolver() {
        super();
    }

    /**
     * このソルバーの名前を返す.
     * 
     * <p>
     * このメソッドは, {@link #toString()} で返す文字列の作成を補助するために用意されており,
     * 外部から呼ばれることは想定されておらず,
     * 公開してはならない.
     * </p>
     * 
     * <p>
     * クラス定義に対して固定値であるべきである.
     * </p>
     * 
     * @return ソルバーの名前
     * @see #toString()
     */
    String solverName() {
        return this.getClass().getSimpleName();
    }

    /**
     * このクラスの文字列説明表現を提供する.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code %solverName[target:%matrix]}
     * </p>
     * 
     * @return 文字列表現
     */
    @Override
    public String toString() {
        return String.format(
                "%s[target:%s]", this.solverName(), this.target());
    }
}
