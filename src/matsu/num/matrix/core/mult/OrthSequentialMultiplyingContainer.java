/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.13
 */
package matsu.num.matrix.core.mult;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.OrthogonalMatrix;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 直交行列に特化した, 逐次的に行列積の計算を実行する機能を扱う.
 * 
 * <p>
 * このクラスはミュータブルなクラスである. <br>
 * 最初にベースとなる, 成分アクセス可能な直交行列を与え, コンテナを用意する
 * ({@link #basedOn(EntryReadableMatrix) basedOn(T)} メソッド). <br>
 * その後, 左右から直交行列を乗算するメソッド
 * {@link #operateLeftSide(OrthogonalMatrix)},
 * {@link #operateRightSide(OrthogonalMatrix)}
 * をコールし, 状態を更新していく. <br>
 * 状態更新が完了したら, ビルドメソッド
 * {@link #build()} をコールし, 直交行列を構築する.
 * </p>
 * 
 * <p>
 * ビルダはビルド後でも再利用可能である.
 * </p>
 * 
 * <p>
 * 状態更新する行列は, 直交行列であり,
 * 状態を更新しても行列のサイズ (次元) が不変であることが担保される.
 * </p>
 * 
 * <p>
 * このクラスはスレッドセーフでない. <br>
 * ただし, 並行実行された場合でも, ある程度の整合性は担保される
 * (おそらく, 状態更新のメソッドコールが無視されたような振る舞いになる).
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class OrthSequentialMultiplyingContainer {

    private final BaseMultiplyingContainer container;

    /**
     * 非公開のコンストラクタ. <br>
     * 引数のバリデーションが必要.
     */
    private OrthSequentialMultiplyingContainer(BaseMultiplyingContainer container) {
        super();
        this.container = container;
    }

    /**
     * 左から行列を掛ける. <br>
     * サイズマッチした正方行列でなければならない.
     * 
     * @param matrix matrix
     * @throws MatrixFormatMismatchException 行列次元が不正の場合
     * @throws NullPointerException 引数がnullの場合
     */
    public void operateLeftSide(OrthogonalMatrix matrix) {
        container.operateLeftSide(matrix);
    }

    /**
     * 右から行列を掛ける. <br>
     * サイズマッチした正方行列でなければならない.
     * 
     * @param matrix matrix
     * @throws MatrixFormatMismatchException 行列次元が不正の場合
     * @throws NullPointerException 引数がnullの場合
     */
    public void operateRightSide(OrthogonalMatrix matrix) {
        container.operateRightSide(matrix);
    }

    /**
     * 乗算結果を行列として返す.
     * 
     * @return 乗算結果
     */
    public ProductOrthogonalMatrix build() {
        return ProductOrthogonalMatrix.of(container.build());
    }

    /**
     * 与えた直交行列をベースとする, 乗算コンテナを作成する.
     * 
     * @param <T> 型制限のための型パラメータ,
     *            {@link EntryReadableMatrix} と
     *            {@link OrthogonalMatrix} の両方のサブタイプを要求する.
     * @param base ベースとなる直交行列
     * @return 乗算コンテナ
     * @throws NullPointerException 引数がnullの場合
     */
    public static <T extends EntryReadableMatrix & OrthogonalMatrix>
            OrthSequentialMultiplyingContainer basedOn(T base) {

        // OrthogonalMatrix型は, マーカーインターフェースのような扱いである.
        // 防御的キャスト検証: 型安全でない使い方をした場合, ClassCastExが発生する可能性
        // (適切にジェネリクスを利用した場合は必ずキャスト可能)
        OrthogonalMatrix.class.cast(base);

        return new OrthSequentialMultiplyingContainer(BaseMultiplyingContainer.basedOn(base));
    }
}
