/**
 * 2023.12.22
 */
package matsu.num.matrix.base.exception;

/**
 * 行列に対する処理が正常に行われなかったことを報告する例外.
 *
 * @author Matsuura Y.
 * @version 17.3
 */
public final class ProcessFailedException extends IllegalArgumentException {

    private static final long serialVersionUID = -1908839640046918708L;

    /**
     * メッセージ無しの例外を生成する.
     */
    public ProcessFailedException() {
        super();
    }

    /**
     * メッセージ有りの例外を生成する.
     * 
     * @param string メッセージ
     */
    public ProcessFailedException(String string) {
        super(string);
    }
}
