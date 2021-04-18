import json
from pathlib import Path

for p in Path('.').glob('*.json'):
    with p.open(encoding='utf8') as fh:
        r = json.load(fh)

    if "text" in r:
        new_text = dict()
        for text in r["text"]:
            id = text["id"]
            del text["id"]

    with p.open('w', encoding='utf8', newline='') as fh:
            json.dump(r, fh, indent=2, ensure_ascii=False)
