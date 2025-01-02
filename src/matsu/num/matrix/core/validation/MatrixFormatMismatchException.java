/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.core.validation;

/**
 * 行列の構造(サイズ等)が規約を満たしていないことを報告する例外.
 *
 * @author Matsuura Y.
 * @version 21.0
 */
public final class MatrixFormatMismatchException extends IllegalArgumentException {

    private static final long serialVersionUID = -7691523485897771939L;

    /**
     * メッセージ無しの例外を生成する.
     */
    public MatrixFormatMismatchException() {
        super();
    }

    /**
     * メッセージ有りの例外を生成する.
     * 
     * @param string メッセージ
     */
    public MatrixFormatMismatchException(String string) {
        super(string);
    }
}
