/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.23
 */
package matsu.num.matrix.base.validation;

/**
 * ACCEPTを表す {@link MatrixStructureAcceptance}. <br>
 * シングルトンを強制するため, このクラスを公開してはならない.
 * 
 * @author Matsuura Y.
 * @version 23.0
 */
final class MatrixAccepted extends MatrixStructureAcceptance {

    /**
     * 唯一のコンストラクタ. <br>
     * 必ず1度しか呼ばれてはいけない.
     */
    MatrixAccepted() {
    }

    @Override
    final Type type() {
        return Type.ACCEPTED;
    }

    @Override
    public final IllegalArgumentException getException(Object cause) {
        throw new IllegalStateException("ACCEPTであり,このメソッドを呼ぶことは許可されない");
    }

    @Override
    public final String toString() {
        return this.type().toString();
    }

}
