/**
 * 2024.2.1
 */
package matsu.num.matrix.base.nlsf;

/**
 * 行列に対する処理が正常に行われなかったことを報告する例外.
 *
 * @author Matsuura Y.
 * @version 19.4
 */
final class ProcessFailedException extends Exception {

    private static final long serialVersionUID = -7042820330052044331L;

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
