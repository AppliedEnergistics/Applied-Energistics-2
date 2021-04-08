import json
from pathlib import Path

for p in Path('.').glob('*.json'):
    with p.open(encoding='utf8') as fh:
        r = json.load(fh)

    for k in ['FromStorage', 'ToCraft', 'Missing', 'Crafting', 'Scheduled']:
        k = 'gui.appliedenergistics2.' + k
        if k in r:
            r[k] = r[k] + ': %s'

    with p.open('w', encoding='utf8', newline='') as fh:
            json.dump(r, fh, indent=2, ensure_ascii=False)
