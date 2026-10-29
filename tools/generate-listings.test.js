const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const test = require('node:test');
const { generate } = require('./generate-listings');

function fixture(t, files) {
  const repo = fs.mkdtempSync(path.join(os.tmpdir(), 'cqrs-listings-test-'));
  const tsvPath = path.join(repo, 'listings.tsv');
  const outPath = path.join(repo, 'LISTINGS.md');
  t.after(() => fs.rmSync(repo, { recursive: true, force: true }));

  for (const [relative, content] of Object.entries(files)) {
    const full = path.join(repo, relative);
    fs.mkdirSync(path.dirname(full), { recursive: true });
    fs.writeFileSync(full, content, 'utf8');
  }

  const rows = [
    'number\tfile\tlang\ttitle',
    ...['2.1.', '2.2.', '2.3.', '2.4.', '2.5.', '2.6.']
      .map((number, index) => number + '\tbook/' + (index + 1) + '.md\tjava\tTitle ' + (index + 1)),
  ];
  fs.writeFileSync(tsvPath, rows.join('\n') + '\n', 'utf8');
  return { repo, tsvPath, outPath };
}

test('役割ごとの列を生成し、旧形式マーカーを無視する', (t) => {
  const { repo, tsvPath, outPath } = fixture(t, {});
  fs.mkdirSync(path.join(repo, '02/src/main'), { recursive: true });
  fs.writeFileSync(
    path.join(repo, '02/src/main/Sample.java'),
    [
      '// 紙面照合: 2.1, 2.2',
      '// 検証補助: 2.3',
      '// リスト2.6',
      'class Sample {}',
      '',
    ].join('\n'),
    'utf8',
  );
  fs.mkdirSync(path.join(repo, '02/snippets'), { recursive: true });
  fs.writeFileSync(path.join(repo, '02/snippets/example.sql'), '-- 検証補助: 2.4\n', 'utf8');
  fs.mkdirSync(path.join(repo, '02/resources'), { recursive: true });
  fs.writeFileSync(
    path.join(repo, '02/resources/example.xml'),
    '<?xml version="1.0"?>\n<!-- 紙面照合: 2.5 -->\n<sample/>\n',
    'utf8',
  );

  const result = generate({ repo, tsvPath, outPath });
  const output = fs.readFileSync(outPath, 'utf8');
  assert.match(output, /\| リスト \| タイトル \| 書籍 \| 紙面との照合対象 \| 検証・補助コード \|/);
  assert.match(output, /^\| 2\.1\. \| Title 1 \| \x60book\/1\.md\x60 \| \[02\/src\/main\/Sample\.java\].* \| — \|$/m);
  assert.match(output, /^\| 2\.3\. \| Title 3 \| \x60book\/3\.md\x60 \| — \| \[02\/src\/main\/Sample\.java\].* \|$/m);
  assert.match(output, /^\| 2\.4\. \| Title 4 \| \x60book\/4\.md\x60 \| — \| \[02\/snippets\/example\.sql\].* \|$/m);
  assert.match(output, /^\| 2\.5\. \| Title 5 \| \x60book\/5\.md\x60 \| \[02\/resources\/example\.xml\].* \| — \|$/m);
  assert.match(output, /^\| 2\.6\. \| Title 6 \| \x60book\/6\.md\x60 \| — \| — \|$/m);
  assert.equal(result.mappedListings, 5);
  assert.equal(result.paperLinks, 3);
  assert.equal(result.supportLinks, 2);
  assert.equal(result.totalLinks, 5);
});

test('壊れた役割マーカーを拒否する', (t) => {
  const { repo, tsvPath, outPath } = fixture(t, {
    '02/Sample.java': '// 紙面照合 2.1\n',
  });
  assert.throws(
    () => generate({ repo, tsvPath, outPath }),
    /役割マーカーには「:」が必要です/,
  );
});

test('listings.tsvにない番号の役割マーカーを拒否する', (t) => {
  const { repo, tsvPath, outPath } = fixture(t, {
    '02/Sample.java': '// 紙面照合: 9.9\n',
  });
  assert.throws(
    () => generate({ repo, tsvPath, outPath }),
    /listings\.tsv に存在しないリスト番号です: 9\.9/,
  );
});

test('同じファイルとリストに複数の役割を指定できない', (t) => {
  const { repo, tsvPath, outPath } = fixture(t, {
    '02/Sample.java': '// 紙面照合: 2.1\n// 検証補助: 2.1\n',
  });
  assert.throws(
    () => generate({ repo, tsvPath, outPath }),
    /同じリスト・ファイルに複数の役割が指定されています/,
  );
});
