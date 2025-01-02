# matsu.num.Matrix.Core, test
フォルダ `test` はテストクラスを扱い, ディストリビューションには含まれない.

`JUnit4` フレームワークを使用した自動テストがあり,
次のライブラリを必要とする.

- `junit-4`
- `hamcrest-core`
- `hamcrest-library`

これらはクラスパス上に配置し, メインソースの `module-info.java` は修正しない
(`test` フォルダは Java のモジュールシステム外である).
