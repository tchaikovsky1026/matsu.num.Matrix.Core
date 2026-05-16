### 骨格実装抽象クラスの利用について
- `EntryReadableMatrix` の実装には, `SkeletalEntryReadableMatrix` が使用できる.
- `OrthogonalMatrix` の実装には, `SkeletalOrthogonalMatrix` が使用できる.
- `OrthogonalMatrix` と `EntryReadableMatrix` の両方を実装する場合について, `SkeletalOrthogonalMatrix` を継承し, 次のように実装するのが適切である.

#### 実装方法
- 生成される転置行列には, `EntryReadableMatrix` (のサブタイプ) と `OrthogonalMatrix` が実装されるようにする.
- `SkeletalOrthogonalMatrix` の型パラメータ `CTT` は, `transposeAsEntryReadable()` メソッドの戻り値になれるようにする.
- `transposeAsEntryReadable()` メソッドの実装は, `transposeSupplier.get().get()` とする (`transposeSupplier` は `protected` なフィールド).

#### その他
廃止されるデフォルトメソッドである次の実装を忘れないこと.
- `OrthogonalMatrix.inverseAsOrthogonal`
- `EntryReadable.inverseAsDiagonal()`
- `DiagonalMatrix.inverseAsDiagonal()`
