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

import java.util.function.Supplier;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.helper.value.DeterminantValues;
import matsu.num.matrix.core.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.core.lazy.ImmutableLazyCacheSupplier;

/**
 * {@link SymmetrizedSquareTypeSolver} の骨格実装.
 * 
 * <p>
 * このクラスでは,
 * {@link #inverse()}, {@link #signOfDeterminant()},
 * {@link #determinant()}, {@link #logAbsDeterminant()},
 * {@link #asymmSqrt()}, {@link #inverseAsymmSqrt()}
 * メソッドの適切な実装を提供する. <br>
 * これらの戻り値は {@link #createAsymmetricSqrtSystem()}
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
 * @param <ST> 非対称平方根行列の型パラメータ, {@link #asymmSqrt()} の戻り値型をサブタイプで限定する.
 * @param <SIT> 非対称平方根の逆行列の型パラメータ, {@link #inverseAsymmSqrt()} の戻り値型をサブタイプで限定する.
 */
abstract class SkeletalSymmetrizedSquareTypeSolver<
        TT extends EntryReadableMatrix, ST extends Matrix, SIT extends Matrix>
        extends SkeletalLUTypeSolver<TT, Matrix> implements SymmetrizedSquareTypeSolver {

    //継承先のオーバーライドに依存するため, 遅延初期化される
    private final Supplier<InversionDeterminantableImplementation<ST, SIT>> asymmetricSqrtSupplier;

    /**
     * 唯一のコンストラクタ.
     */
    SkeletalSymmetrizedSquareTypeSolver() {
        super();
        this.asymmetricSqrtSupplier = ImmutableLazyCacheSupplier.of(
                () -> this.createAsymmetricSqrtSystem());
    }

    /**
     * -
     * 
     * <p>
     * (外部からのの呼び出し不可, サブクラスからの呼び出し禁止)
     * </p>
     * 
     * @return -
     */
    @Override
    final InverstibleAndDeterminantStruct<Matrix> createInverseDeterminantStruct() {
        InversionDeterminantableImplementation<ST, SIT> sqrtMatrixStructure =
                this.asymmetricSqrtSupplier.get();
        return new InverstibleAndDeterminantStruct<Matrix>(
                new DeterminantValues(2 * sqrtMatrixStructure.logAbsDeterminant(), 1),
                Matrix.symmetrizedSquare(sqrtMatrixStructure.inverse().transpose()));
    }

    @Override
    public final ST asymmSqrt() {
        return this.asymmetricSqrtSupplier.get().target();
    }

    @Override
    public final SIT inverseAsymmSqrt() {
        return this.asymmetricSqrtSupplier.get().inverse();
    }

    /**
     * 非対称平方根 B に関する行列式と逆行列システムを作成する抽象メソッド. <br>
     * インスタンスが生成されてから一度だけ呼ばれる. <br>
     * 公開してはいけない.
     * 
     * @return 非対称平方根行列に関する行列分解
     */
    abstract InversionDeterminantableImplementation<ST, SIT> createAsymmetricSqrtSystem();
}
