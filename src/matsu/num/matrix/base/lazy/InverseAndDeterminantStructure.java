/**
 * 2023.8.15
 */
package matsu.num.matrix.base.lazy;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.Inversion;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.helper.value.DeterminantValues;

/**
 * 逆行列と行列式がセットになった概念を扱う.
 * 
 * <p>
 * {@linkplain Determinantable}の概念は{@linkplain Inversion}と強く結びつくことが多いので, 
 * 同時生成されやすい. <br>
 * このクラスはそれらを構造体的に扱うための仕組みである.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 15.0
 * @param <T> 逆行列の型パラメータ
 * @see Inversion
 * @see Determinantable
 */
public final class InverseAndDeterminantStructure<T extends Matrix> {

    final DeterminantValues determinantValues;
    final Optional<T> inverseMatrix;

    /**
     * 構造体を生成する. <br>
     * 逆行列が存在することを表す.
     * 
     * @param determinantValues 行列式に関連する値
     * @param inverseMatrix 逆行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public InverseAndDeterminantStructure(DeterminantValues determinantValues, T inverseMatrix) {
        super();
        this.determinantValues = Objects.requireNonNull(determinantValues);
        this.inverseMatrix = Optional.of(inverseMatrix);
    }

    /**
     * 特異行列(逆行列が存在しないこと)を表す概念を生成する.
     */
    public InverseAndDeterminantStructure() {
        super();
        this.determinantValues = new DeterminantValues(Double.NEGATIVE_INFINITY, 0);
        this.inverseMatrix = Optional.empty();
    }
    
    /**
     * @return determinantValues
     */
    public DeterminantValues determinantValues() {
        return this.determinantValues;
    }/**
     * @return invMatrix
     */
    public Optional<T> inverseMatrix() {
        return this.inverseMatrix;
    }
    
}
