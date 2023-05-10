import {globSync} from 'glob';
import fs from 'node:fs'
import path from 'node:path';
import * as url from 'node:url';
import {fromMarkdown} from 'mdast-util-from-markdown'
import {toMarkdown} from 'mdast-util-to-markdown'
import {mdxjs} from 'micromark-extension-mdxjs'
import {mdxFromMarkdown, mdxToMarkdown} from 'mdast-util-mdx'
import {frontmatterFromMarkdown, frontmatterToMarkdown} from 'mdast-util-frontmatter'
import {frontmatter} from 'micromark-extension-frontmatter'
import {gfmTable} from 'micromark-extension-gfm-table'
import {gfmTableFromMarkdown, gfmTableToMarkdown} from 'mdast-util-gfm-table'
const __dirname = url.fileURLToPath(new URL('.', import.meta.url));

let guidebookDir = path.join(__dirname, '../guidebook');
console.info("Guidebook Dir: " + guidebookDir);

for (let mdFile of globSync(`${guidebookDir}/**/*.md`)) {
    const mdContent = fs.readFileSync(mdFile, {encoding: 'utf-8'});
    let tree = fromMarkdown(mdContent, {
        extensions: [mdxjs(), frontmatter(['yaml']), gfmTable],
        mdastExtensions: [mdxFromMarkdown(), frontmatterFromMarkdown(['yaml']), gfmTableFromMarkdown]
    });

    let markdownStr = toMarkdown(tree, {
        extensions: [mdxToMarkdown(), frontmatterToMarkdown(['yaml']), gfmTableToMarkdown()]
    });
    fs.writeFileSync(mdFile, markdownStr, {encoding: 'utf-8'});
}
