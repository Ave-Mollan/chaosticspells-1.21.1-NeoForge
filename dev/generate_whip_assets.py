"""Rebuild the 16x16 whip, physical rest pose, previews and the user's supplied audio.
Requires Pillow, NumPy and ffmpeg; run rebuild_whip_assets.py to export solver samples first.
"""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/chaosticspells'
BUILD = ROOT / 'build/whip-assets'
BUILD.mkdir(parents=True, exist_ok=True)


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


from whip_model import make_texture, make_geometry, make_animations
texture = make_texture()
(ASSETS / 'textures/item').mkdir(parents=True, exist_ok=True)
texture.save(ASSETS / 'textures/item/owners_whip.png')
write_json(ASSETS / 'geo/owners_whip.geo.json', make_geometry())
write_json(ASSETS / 'animations/owners_whip.animation.json', make_animations())
write_json(ASSETS / 'models/item/owners_whip.json', {'parent': 'builtin/entity', 'display': {
    'thirdperson_righthand': {'translation': [0, -2, 0], 'scale': [1, 1, 1]},
    'thirdperson_lefthand': {'translation': [0, -2, 0], 'scale': [1, 1, 1]},
    'firstperson_righthand': {'rotation': [0, 0, 0], 'translation': [0, -1, 0], 'scale': [.85, .85, .85]},
    'firstperson_lefthand': {'rotation': [0, 0, 0], 'translation': [0, -1, 0], 'scale': [.85, .85, .85]},
    'gui': {'rotation': [0, -15, -15], 'translation': [-2, -2, 0], 'scale': [.8, .8, .8]},
    'ground': {'rotation': [0, 0, 90], 'translation': [0, 3, 0], 'scale': [.35, .35, .35]},
    'fixed': {'translation': [0, -5, 0], 'scale': [.43, .43, .43]}}})
if '--visuals-only' in sys.argv:
    from render_whip_physics import render_board
    render_board(ROOT)
    print('Updated 16x16 visuals: packed flat coil and independent physical lash segments.')
    raise SystemExit(0)
write_json(RES / 'data/chaosticspells/damage_type/owners_whip.json',
           {'message_id': 'owners_whip', 'scaling': 'never', 'exhaustion': .1})
write_json(RES / 'data/minecraft/tags/damage_type/bypasses_armor.json',
           {'replace': False, 'values': ['chaosticspells:owners_whip']})
translations = {
    'en_us': {
        'item.chaosticspells.owners_whip': "AveMollan's Whip",
        'tooltip.chaosticspells.owners_whip.owner': 'Bound to AveMollan',
        'tooltip.chaosticspells.owners_whip.controls': 'Hold Attack and release. Press again during the first cast for a follow-up.',
        'tooltip.chaosticspells.owners_whip.damage': '5–20 armor-piercing damage per lash. 160-degree arc, up to 8 blocks.',
        'tooltip.chaosticspells.owners_whip.retained': 'Returns after the owner respawns. Command only.',
        'message.chaosticspells.whip_locked': 'This whip answers only to AveMollan.',
        'message.chaosticspells.whip_charge': 'Whip: %s%% • Damage: %s',
        'subtitles.chaosticspells.whip_crack': 'Whip cracks',
        'subtitles.chaosticspells.whip_double': 'Whip cracks twice',
        'subtitles.chaosticspells.whip_hit': 'Whip strikes',
        'death.attack.owners_whip': '%1$s was lashed by %2$s',
        'death.attack.owners_whip.item': '%1$s was lashed by %2$s using %3$s'},
    'ru_ru': {
        'item.chaosticspells.owners_whip': 'Кнут AveMollan',
        'tooltip.chaosticspells.owners_whip.owner': 'Подчиняется только AveMollan',
        'tooltip.chaosticspells.owners_whip.controls': 'Зажми атаку и отпусти. Нажми ещё раз во время первого взмаха для второго.',
        'tooltip.chaosticspells.owners_whip.damage': '5–20 урона сквозь броню за взмах. Дуга 160°, дальность до 8 блоков.',
        'tooltip.chaosticspells.owners_whip.retained': 'Возвращается после возрождения владельца. Только /give.',
        'message.chaosticspells.whip_locked': 'Этот кнут подчиняется только AveMollan.',
        'message.chaosticspells.whip_charge': 'Кнут: %s%% • Урон: %s',
        'subtitles.chaosticspells.whip_crack': 'Щелчок кнута',
        'subtitles.chaosticspells.whip_double': 'Двойной щелчок кнута',
        'subtitles.chaosticspells.whip_hit': 'Удар кнутом',
        'death.attack.owners_whip': '%1$s получил удар кнутом от %2$s',
        'death.attack.owners_whip.item': '%1$s получил удар от %2$s с помощью %3$s'}
}
for locale, entries in translations.items():
    path = ASSETS / f'lang/{locale}.json'
    data = json.loads(path.read_text(encoding='utf-8'))
    data.update(entries)
    write_json(path, data)

from prepare_whip_sounds import prepare_sounds
prepare_sounds(ROOT)
from render_whip_physics import render_board
render_board(ROOT)
print('Created 16x16 whip assets, physical rest pose and five user-supplied sounds.')
