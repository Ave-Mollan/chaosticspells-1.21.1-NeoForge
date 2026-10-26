"""Vanilla-style GeckoLib whip with one real 16x16 atlas and simple articulated cubes."""
import math
import json
from pathlib import Path
from PIL import Image

SEGMENTS = 24
HANDLE_LENGTH = 8
LENGTHS = [3] * 2 + [5] * 18 + [5.5] * 4
ATLAS_SIZE = 16
PACKED_PIECES = 64


def packed_points():
    # A flat, tightly wound presentation mesh, released into the existing rope simulation on a cast.
    points = [[0, .5, 0], [0, .55, .08]]
    for i in range(PACKED_PIECES + 1):
        t = i / PACKED_PIECES
        # The radial pitch follows the narrowing leather, leaving about .01 block between turns.
        radius = .30 - .25125*t + .043125*t*t
        angle = math.radians(135) - 6 * math.pi * t
        points.append([.04 + radius * math.cos(angle), .24 + radius * math.sin(angle), .11])
    return points


def uv(tile, width=2, height=5):
    x, y = tile
    # Roughly one texture pixel per model unit, as on vanilla item/block models.
    return {face: {'uv': [x, y], 'uv_size': [width, width if face in ('up', 'down') else height]}
            for face in ('north', 'south', 'east', 'west', 'up', 'down')}


def cube(origin, size, tile, rotation=None, pivot=None, uv_size=None):
    width, height = uv_size or (max(1, round(size[0])), max(1, round(size[1])))
    value = {'origin': origin, 'size': size, 'uv': uv(tile, width, height)}
    if rotation:
        value.update(rotation=rotation, pivot=pivot or [origin[i] + size[i] / 2 for i in range(3)])
    return value


def make_texture():
    dark, shadow, leather, warm, highlight, grip = [
        (47, 29, 21), (76, 44, 27), (111, 67, 36), (151, 94, 49), (186, 130, 70), (58, 35, 25)]
    iron_dark, iron, iron_light, iron_highlight = [(55, 60, 65), (105, 112, 119), (168, 176, 181), (213, 218, 220)]
    image = Image.new('RGBA', (ATLAS_SIZE, ATLAS_SIZE), (*leather, 255))
    p = image.load()
    # Large, deliberately sparse pixels: leather at x=0..7, grip at x=8..11, iron at x=12..15.
    for y in range(16):
        for x in range(8):
            diagonal = (x + (y if x < 4 else -y)) % 4
            color = shadow if diagonal == 0 else warm if diagonal == 1 else leather
            if x == 3 and y % 8 == 2: color = highlight
            p[x, y] = (*color, 255)
        for x, color in enumerate((dark, grip, shadow, leather), start=8):
            if y in (3, 7, 11, 15): color = shadow if x < 10 else warm
            p[x, y] = (*color, 255)
        for x, color in enumerate((iron_dark, iron, iron_light, iron_highlight), start=12):
            p[x, y] = (*(iron_dark if y % 4 == 3 else color), 255)
    return image


def make_geometry():
    # Only three grip cubes: square leather handle and two broad iron caps.
    grip = [cube([-1, 0, -1], [2, 8, 2], [8, 0], uv_size=(2, 8)),
            cube([-1.5, -.5, -1.5], [3, 1, 3], [12, 0], uv_size=(3, 1)),
            cube([-1.5, 7, -1.5], [3, 1, 3], [12, 0], uv_size=(3, 1))]
    bones = [{'name': 'root', 'pivot': [0, 0, 0]},
             {'name': 'handle', 'parent': 'root', 'pivot': [0, 0, 0], 'cubes': grip}]
    packed = []
    points = packed_points()
    for i, (a, b) in enumerate(zip(points, points[1:])):
        dx, dy, dz = [b[k] - a[k] for k in range(3)]
        length = math.sqrt(dx*dx + dy*dy + dz*dz)
        width = 1.18 - .46 * i / (len(points)-2)
        start = [v*16 for v in a]
        packed.append(cube([-start[0]-width/2, start[1], start[2]-.4], [width, length*16+.12, .8],
                           [0 if i%2==0 else 4, 0 if i%3==0 else 4],
                           rotation=[-math.degrees(math.atan2(dz, math.hypot(dx,dy))), 0, -math.degrees(math.atan2(dx,dy))],
                           pivot=[-start[0],start[1],start[2]], uv_size=(1,max(1,round(length*16)))))
    bones.append({'name':'packed_coil', 'parent':'root', 'pivot':[0,0,0], 'cubes':packed})
    y = HANDLE_LENGTH
    for i, length in enumerate(LENGTHS):
        width = 1.5 if i < 8 else 1 if i < 14 else .75 if i < 20 else .5
        geometry = [cube([-width/2, 0, -width/2], [width, length+.1, width],
                         [0 if i % 2 == 0 else 4, 0 if i % 3 == 0 else 4],
                         uv_size=(max(1, round(width)), round(length)))]
        bones.append({'name': f'lash_{i+1}', 'parent': 'root',
                      'pivot': [0, 0, 0], 'cubes': geometry})
        y += length
    return {'format_version': '1.12.0', 'minecraft:geometry': [{
        'description': {'identifier': 'geometry.owners_whip', 'texture_width': ATLAS_SIZE, 'texture_height': ATLAS_SIZE,
                        'visible_bounds_width': 20, 'visible_bounds_height': 20, 'visible_bounds_offset': [0, 2, 0]},
        'bones': bones}]}


def make_animations():
    # A static editor/GUI rest pose only. In-game motion is solved by WorldWhipRope, not keyframed.
    rest_path = Path(__file__).resolve().parents[1] / 'build/whip-assets/physics-rest.json'
    points = json.loads(rest_path.read_text())
    bones = {'root': {'rotation': [0, 0, 0]}, 'handle': {'rotation': [0, 0, 0]}}
    for i in range(SEGMENTS):
        x,y,z=points[i]
        dx,dy,dz=[points[i+1][k]-points[i][k] for k in range(3)]
        rx=math.degrees(math.atan2(dz,math.hypot(dx,dy)))
        rz=-math.degrees(math.atan2(dx,dy))
        bones[f'lash_{i+1}']={'position':[-x*16,y*16,z*16], 'rotation':[-rx,0,rz]}
        bones[f'lash_{i+1}']['scale'] = [0,0,0]
    bones['packed_coil']={'rotation':[0,0,0]}
    return {'format_version':'1.8.0','animations':{'animation.owners_whip.idle':{
        'loop':True,'animation_length':1,'bones':bones}}}
