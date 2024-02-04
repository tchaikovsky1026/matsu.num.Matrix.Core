/**
 * 2024.1.19
 */
package matsu.num.matrix.base.nlsf;

import java.util.function.Supplier;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * <p>
 * {@link SymmetrizedSquareTypeSolver} の骨格実装. <br>
 * ターゲット行列 A を, A = BB<sup>T</sup> と分解することに関する.
 * </p>
 * 
 * <p>
 * この骨格実装は, 新たに {@linkplain #createAsymmetricSqrtSystem()} を定義している. <br>
 * 実装者は, 非対称平方根 B に関する連立方程式向け行列分解を生成(構築)するように実装する. <br>
 * インターフェースに定義された A の行列式や逆行列の呼び出しメソッド,
 * Bの連立方程式向け行列分解の呼び出しは,
 * この抽象クラスで実装されている. <br>
 * それらが呼ばれたときに1度だけこの
 * {@linkplain #calcInverseDeterminantStruct()}
 * を呼ぶように, 骨格実装内でキャッシュ化してある.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 19.0
 */
abstract class SkeletalSymmetrizedSquareTypeSolver
        extends InvertibleDeterminantableSystem<Matrix> implements SymmetrizedSquareTypeSolver {

    //継承先のオーバーライドに依存するため, 遅延初期化される
    private final Supplier<InvertibleDeterminantableSystem<Matrix>> asymmetricSqrtSupplier;

    /**
     * 新しいオブジェクトの作成
     */
    SkeletalSymmetrizedSquareTypeSolver() {
        super();
        this.asymmetricSqrtSupplier = ImmutableLazyCacheSupplier.of(
                () -> this.createAsymmetricSqrtSystem());
    }

    @Override
    protected final InverstibleAndDeterminantStruct<Matrix> calcInverseDeterminantStruct() {
        InvertibleDeterminantableSystem<Matrix> sqrtMatrixStructure = this.asymmetricSqrtSupplier.get();
        return new InverstibleAndDeterminantStruct<Matrix>(
                new DeterminantValues(2 * sqrtMatrixStructure.logAbsDeterminant(), 1),
                Matrix.symmetrizedSquare(sqrtMatrixStructure.inverse().transpose()));
    }

    @Override
    public Matrix asymmSqrt() {
        return this.asymmetricSqrtSupplier.get().target();
    }

    @Override
    public final Matrix inverseAsymmSqrt() {
        return this.asymmetricSqrtSupplier.get().inverse();
    }

    /**
     * <p>
     * 非対称平方根 B に関する連立方程式向け行列分解を構築する.
     * </p>
     * 
     * @return 非対称平方根行列に関する行列分解システム
     */
    abstract InvertibleDeterminantableSystem<Matrix> createAsymmetricSqrtSystem();
}
