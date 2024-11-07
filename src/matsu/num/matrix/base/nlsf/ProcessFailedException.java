/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.7
 */
package matsu.num.matrix.base.nlsf;

/**
 * 行列に対する処理が正常に行われなかったことを報告する例外.
 *
 * @author Matsuura Y.
 * @version 22.2
 */
final class ProcessFailedException extends Exception {

    private static final long serialVersionUID = -7042820330052044331L;

    /**
     * メッセージ無しの例外を生成する.
     */
    ProcessFailedException() {
        super();
    }

    /**
     * メッセージ有りの例外を生成する.
     * 
     * @param string メッセージ
     */
    ProcessFailedException(String string) {
        super(string);
    }
}
