#!/usr/bin/env node
/**
 * 書籍のリスト対応表（LISTINGS.md）を生成するスクリプト。
 *
 * 入力:
 *   1. 書籍リポジトリの listings.tsv（number-listings.js が生成する対応表）
 *      既定: ../cqrs/articles/listings.tsv（環境変数 LISTINGS_TSV で上書き可）
 *   2. このリポジトリ内のソースコードに書かれた役割マーカー
 *      例: // 紙面照合: 2.5
 *          // 検証補助: 2.5, 2.6
 *
 * 出力:
 *   リスト番号、紙面掲載コード、検証・補助コードの対応表
 *
 * 使い方:
 *   node tools/generate-listings.js
 */

const fs = require('fs');
const path = require('path');

const REPO = path.join(__dirname, '..');
const TSV = process.env.LISTINGS_TSV
  || path.join(REPO, '..', 'cqrs', 'articles', 'listings.tsv');
const OUT = path.join(REPO, 'LISTINGS.md');
const BOOK_BASE_URL = process.env.BOOK_BASE_URL || '';

const SOURCE_EXT = new Set(['.java', '.kt', '.kts', '.sh', '.sql', '.yaml', '.yml', '.properties', '.xml']);
const LISTING_ID = /^(?:\d+|A)\.\d+$/;
const INLINE_CODE = String.fromCharCode(96);
const ROLE_NAMES = new Map([
  ['紙面照合', 'paper'],
  ['検証補助', 'support'],
]);

function walk(dir, files = []) {
  for (const entry of fs.readdirSync(dir).sort()) {
    if (entry.startsWith('.') || entry === 'build' || entry === 'node_modules' || entry === 'tools') continue;
    const full = path.join(dir, entry);
    const stat = fs.statSync(full);
    if (stat.isDirectory()) walk(full, files);
    else if (SOURCE_EXT.has(path.extname(entry))) files.push(full);
  }
  return files;
}

function parseRoleMarker(line, file, lineNumber) {
  const codeComment = line.match(/^\s*(?:\/\/|#|--)\s*(紙面照合|検証補助)(.*)$/);
  const xmlComment = line.match(/^\s*<!--\s*(紙面照合|検証補助)(.*?)-->\s*$/);
  const match = codeComment || xmlComment;

  if (!match) {
    const malformed = line.match(/^\s*(?:(?:\/\/|#|--)\s*(紙面照合|検証補助)|<!--\s*(紙面照合|検証補助))/);
    if (malformed) {
      throw new Error(file + ':' + lineNumber + ': 役割マーカーは「役割: 番号」の形式で記述してください');
    }
    return null;
  }

  const label = match[1];
  let values = codeComment ? codeComment[2] : xmlComment[2];
  if (!/^\s*:\s*/.test(values)) {
    throw new Error(file + ':' + lineNumber + ': 役割マーカーには「:」が必要です');
  }
  values = values.replace(/^\s*:\s*/, '').trim();
  if (!values) {
    throw new Error(file + ':' + lineNumber + ': 役割マーカーにリスト番号がありません');
  }

  const ids = values.split(',').map((value) => value.trim());
  if (ids.some((id) => !LISTING_ID.test(id))) {
    throw new Error(file + ':' + lineNumber + ': 役割マーカーの番号が不正です: ' + values);
  }
  return { role: ROLE_NAMES.get(label), ids };
}

function addMarker(markers, role, id, file) {
  if (!markers[role].has(id)) markers[role].set(id, new Set());
  markers[role].get(id).add(file);
}

function collectMarkers(repo = REPO) {
  const markers = {
    paper: new Map(),
    support: new Map(),
  };

  for (const full of walk(repo)) {
    const text = fs.readFileSync(full, 'utf8');
    const file = path.relative(repo, full).split(path.sep).join('/');
    const lines = text.split(/\r?\n/);
    for (let i = 0; i < lines.length; i += 1) {
      const marker = parseRoleMarker(lines[i], file, i + 1);
      if (!marker) continue;
      for (const id of marker.ids) addMarker(markers, marker.role, id, file);
    }
  }
  return markers;
}

function listingKey(number) {
  const key = number.trim().replace(/\.$/, '');
  if (!LISTING_ID.test(key)) throw new Error('listings.tsv の番号が不正です: ' + number);
  return key;
}

function loadTsv(tsvPath = TSV) {
  if (!fs.existsSync(tsvPath)) {
    throw new Error(
      'listings.tsv が見つかりません: ' + tsvPath
      + '\n書籍リポジトリで node articles/number-listings.js を実行してから再実行してください。',
    );
  }
  const lines = fs.readFileSync(tsvPath, 'utf8').trim().split(/\r?\n/).slice(1);
  return lines.filter((line) => line.trim()).map((line) => {
    const [number, file, lang, title] = line.split('\t');
    return { number: number.trim(), key: listingKey(number), file, lang, title: title || '' };
  });
}

function filesFor(markers, role, key) {
  return [...(markers[role].get(key) || [])].sort();
}

function validateMarkers(listings, markers) {
  const known = new Set(listings.map((listing) => listing.key));
  for (const role of ['paper', 'support']) {
    for (const key of markers[role].keys()) {
      if (!known.has(key)) throw new Error('listings.tsv に存在しないリスト番号です: ' + key);
    }
  }

  const keys = new Set([...markers.paper.keys(), ...markers.support.keys()]);
  for (const key of keys) {
    const paperFiles = new Set(filesFor(markers, 'paper', key));
    const conflicting = filesFor(markers, 'support', key).filter((file) => paperFiles.has(file));
    if (conflicting.length) {
      throw new Error(
        '同じリスト・ファイルに複数の役割が指定されています: '
        + key + ' (' + conflicting.join(', ') + ')',
      );
    }
  }
}

function render(listings, markers, bookBaseUrl = '') {
  validateMarkers(listings, markers);
  const chapters = [...new Set(listings.map((listing) => listing.key.split('.')[0]))];
  const out = [];
  out.push('# リスト対応表');
  out.push('');
  out.push('書籍のリスト番号と、このリポジトリ内のコード・検証資料の対応表です。');
  out.push('「紙面との照合対象」は、紙面に掲載されたコードやその一部をこのリポジトリで確認するファイルです。');
  out.push('「検証・補助コード」は、紙面のコードそのものではなく、動作・設定・データ例を検証または支えるファイルです。');
  out.push('役割はファイル単位ではなく、リスト番号とファイルの組ごとに明示します。テストコード自体が紙面に掲載される場合は照合対象にできます。');
  out.push('SQL・JSON・実行結果など、紙面に載る内容をテストで再現するリストには、照合対象がなく検証・補助コードだけが対応する場合があります。');
  out.push('マーカーは ' + INLINE_CODE + '// 紙面照合: 2.5' + INLINE_CODE + ' または '
    + INLINE_CODE + '// 検証補助: 2.5' + INLINE_CODE
    + ' の形式で記し、複数番号はカンマで区切ります。旧形式の '
    + INLINE_CODE + '// リスト2.5' + INLINE_CODE
    + ' や説明文中の番号は生成に使いません。');
  out.push('このファイルは自動生成のため、手で編集しないでください。');
  out.push('');

  let mappedListings = 0;
  let paperLinks = 0;
  let supportLinks = 0;
  for (const chapter of chapters) {
    const rows = listings.filter((listing) => listing.key.split('.')[0] === chapter);
    out.push(chapter === 'A' ? '## 付録A' : '## 第' + chapter + '章');
    out.push('');
    out.push('| リスト | タイトル | 書籍 | 紙面との照合対象 | 検証・補助コード |');
    out.push('|---|---|---|---|---|');

    for (const listing of rows) {
      const paperFiles = filesFor(markers, 'paper', listing.key);
      const supportFiles = filesFor(markers, 'support', listing.key);
      if (paperFiles.length || supportFiles.length) mappedListings += 1;
      paperLinks += paperFiles.length;
      supportLinks += supportFiles.length;
      const paperCell = paperFiles.length
        ? paperFiles.map((file) => '[' + file + '](' + file + ')').join('<br>')
        : '—';
      const supportCell = supportFiles.length
        ? supportFiles.map((file) => '[' + file + '](' + file + ')').join('<br>')
        : '—';
      const book = bookBaseUrl
        ? '[' + listing.file + '](' + bookBaseUrl + listing.file + ')'
        : INLINE_CODE + listing.file + INLINE_CODE;
      const title = listing.title || '（' + (listing.lang || 'テキスト') + '）';
      out.push(
        '| ' + listing.number + ' | ' + title + ' | ' + book + ' | '
        + paperCell + ' | ' + supportCell + ' |',
      );
    }
    out.push('');
  }

  out.push('---');
  out.push('');
  out.push('対応状況: ' + mappedListings + ' / ' + listings.length + ' リスト');
  out.push('リンク数: 紙面照合 ' + paperLinks + '件 / 検証・補助 ' + supportLinks + '件 / 合計 ' + (paperLinks + supportLinks) + '件');
  out.push('');
  return {
    content: out.join('\n'),
    mappedListings,
    listingCount: listings.length,
    paperLinks,
    supportLinks,
    totalLinks: paperLinks + supportLinks,
  };
}

function generate({ repo = REPO, tsvPath = TSV, outPath = path.join(repo, 'LISTINGS.md'), bookBaseUrl = BOOK_BASE_URL } = {}) {
  const listings = loadTsv(tsvPath);
  const markers = collectMarkers(repo);
  const result = render(listings, markers, bookBaseUrl);
  fs.writeFileSync(outPath, result.content, 'utf8');
  return result;
}

function main() {
  const result = generate();
  console.log(
    'LISTINGS.md を生成しました（対応済み ' + result.mappedListings + ' / '
    + result.listingCount + '、リンク ' + result.totalLinks + '件）',
  );
}

if (require.main === module) {
  try {
    main();
  } catch (error) {
    console.error(error.message);
    process.exitCode = 1;
  }
}

module.exports = { collectMarkers, generate, loadTsv, parseRoleMarker, render, validateMarkers };
