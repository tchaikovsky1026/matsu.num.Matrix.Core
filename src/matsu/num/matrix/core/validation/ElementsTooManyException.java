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
 * 有効要素数が多すぎて対応できないことを報告する例外.
 * 
 * @author Matsuura Y.
 * @version 21.0
 */
public class ElementsTooManyException extends IllegalArgumentException {

    private static final long serialVersionUID = 2552197615596661403L;

    /**
     * メッセージ無しの例外を生成する.
     */
    public ElementsTooManyException() {
        super();
    }

    /**
     * メッセージ有りの例外を生成する.
     * 
     * @param string メッセージ
     */
    public ElementsTooManyException(String string) {
        super(string);
    }
}
