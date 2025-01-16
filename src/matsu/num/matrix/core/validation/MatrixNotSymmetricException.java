/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.core.validation;

/**
 * 行列オブジェクトが対称行列でないことを報告する例外.
 *
 * @author Matsuura Y.
 */
public final class MatrixNotSymmetricException extends IllegalArgumentException {

    private static final long serialVersionUID = 435736393339255423L;

    /**
     * メッセージ無しの例外を生成する.
     */
    public MatrixNotSymmetricException() {
        super();
    }

    /**
     * メッセージ有りの例外を生成する.
     * 
     * @param string メッセージ
     */
    public MatrixNotSymmetricException(String string) {
        super(string);
    }
}
