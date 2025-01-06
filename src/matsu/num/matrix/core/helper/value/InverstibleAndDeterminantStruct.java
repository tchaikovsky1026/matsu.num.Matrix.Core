/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.core.helper.value;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.core.Matrix;

/**
 * 逆行列と行列式がセットになった概念を扱う.
 * 
 * @author Matsuura Y.
 * @version 21.0
 * @param <T> 逆行列の型パラメータ
 */
public final class InverstibleAndDeterminantStruct<T extends Matrix> {

    private final DeterminantValues determinantValues;
    private final Optional<T> inverseMatrix;

    /**
     * 構造体を生成する. <br>
     * 逆行列が存在することを表す.
     * 
     * <p>
     * 逆行列が存在するため, 符号は0ではない.
     * </p>
     * 
     * @param determinantValues 行列式に関連する値
     * @param inverseMatrix 逆行列
     * @throws IllegalArgumentException 符号が0である場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public InverstibleAndDeterminantStruct(DeterminantValues determinantValues, T inverseMatrix) {
        super();
        this.determinantValues = Objects.requireNonNull(determinantValues);
        this.inverseMatrix = Optional.of(inverseMatrix);

        if (this.determinantValues.sign() == 0) {
            throw new IllegalArgumentException("行列式が特異相当なので, 行列式と逆行列が整合していない");
        }
    }

    /**
     * 特異行列(逆行列が存在しないこと)を表す概念を生成する. <br>
     * 特異行列であるので行列式は0である.
     */
    public InverstibleAndDeterminantStruct() {
        super();
        this.determinantValues = new DeterminantValues();
        this.inverseMatrix = Optional.empty();
    }

    /**
     * @return determinantValues
     */
    public DeterminantValues determinantValues() {
        return this.determinantValues;
    }

    /**
     * @return invMatrix
     */
    public Optional<T> inverseMatrix() {
        return this.inverseMatrix;
    }

}
