"""Small textured software renderer for the SAME cubes, UVs and bone poses shipped in the mod."""
from pathlib import Path
import json
import math
import numpy as np
from PIL import Image, ImageDraw, ImageFont


def translate(p):
    m = np.eye(4); m[:3, 3] = p
    return m


def rotate(angles):
    x, y, z = np.radians(angles)
    rx = np.array([[1,0,0,0], [0,math.cos(x),-math.sin(x),0], [0,math.sin(x),math.cos(x),0], [0,0,0,1]])
    ry = np.array([[math.cos(y),0,math.sin(y),0], [0,1,0,0], [-math.sin(y),0,math.cos(y),0], [0,0,0,1]])
    rz = np.array([[math.cos(z),-math.sin(z),0,0], [math.sin(z),math.cos(z),0,0], [0,0,1,0], [0,0,0,1]])
    return rz @ ry @ rx


def pose_at(animation, time):
    result = {}
    for name, channels in animation['bones'].items():
        frames = channels.get('rotation', [0,0,0])
        if isinstance(frames, list): result[name] = frames; continue
        keys = sorted((float(t), np.array(v)) for t, v in frames.items())
        v = keys[0][1]
        for i, (t, value) in enumerate(keys):
            if time >= t: v = value
            elif i:
                t0, v0 = keys[i-1]
                v = v0 + (value-v0)*((time-t0)/(t-t0))
                break
        result[name] = v
    return result


FACES = {'north': [0, 1, 2, 3], 'south': [5, 4, 7, 6], 'west': [4, 0, 3, 7],
         'east': [1, 5, 6, 2], 'up': [3, 2, 6, 7], 'down': [4, 5, 1, 0]}


def meshes(geometry, pose, handle_only=False):
    matrices = {}
    result = []
    for bone in geometry['minecraft:geometry'][0]['bones']:
        pivot = np.array(bone['pivot'])
        parent = matrices.get(bone.get('parent'), np.eye(4))
        value = pose.get(bone['name'], [0,0,0])
        rotation = value.get('rotation', [0,0,0]) if isinstance(value,dict) else value
        offset = value.get('position', [0,0,0]) if isinstance(value,dict) else [0,0,0]
        scale = value.get('scale', [1,1,1]) if isinstance(value,dict) else [1,1,1]
        matrix = parent @ translate(offset) @ translate(pivot) @ rotate(rotation) @ np.diag([*scale,1]) @ translate(-pivot)
        matrices[bone['name']] = matrix
        if isinstance(value,dict) and value.get('hidden'): continue
        if handle_only and bone['name'].startswith('lash_'): continue
        for c in bone.get('cubes', []):
            x, y, z = c['origin']; dx, dy, dz = c['size']
            x = -x-dx  # GeckoLib mirrors Bedrock's cube X coordinates while baking.
            vertices = np.array([[x,y,z,1], [x+dx,y,z,1], [x+dx,y+dy,z,1], [x,y+dy,z,1],
                                 [x,y,z+dz,1], [x+dx,y,z+dz,1], [x+dx,y+dy,z+dz,1], [x,y+dy,z+dz,1]])
            if 'rotation' in c:
                cp = np.array(c['pivot'])*[-1,1,1]
                cr = np.array(c['rotation'])*[-1,-1,1]
                vertices = (translate(cp) @ rotate(cr) @ translate(-cp) @ vertices.T).T
            vertices = (matrix @ vertices.T).T[:, :3]
            for face, indices in FACES.items():
                result.append((vertices[indices], c['uv'][face]))
    return result


def render(geometry, texture, pose, size, handle_only=False, camera=(.15,.12,1), bounds=None, first_person=False):
    width, height = size
    faces = meshes(geometry, pose, handle_only)
    forward = np.array(camera, dtype=float); forward /= np.linalg.norm(forward)
    right = np.cross([0,1,0], forward); right /= np.linalg.norm(right)
    up = np.cross(forward, right)
    basis = np.array([right, up, forward])
    projected = [(v/16*.85+np.array([.56,-.624,-.85]) if first_person else v @ basis.T, uv) for v, uv in faces]
    points = np.concatenate([v for v, _ in projected])
    lo, hi = points[:, :2].min(axis=0), points[:, :2].max(axis=0)
    if bounds is not None: lo,hi = bounds
    scale = min((width-40)/max(.1,hi[0]-lo[0]), (height-35)/max(.1,hi[1]-lo[1]))
    center = (hi+lo)/2
    pixels = np.zeros((height, width, 4), dtype=np.uint8)
    depths = np.full((height, width), -1e12)
    atlas = np.asarray(texture)
    light = np.array([-.4,.65,1]); light /= np.linalg.norm(light)
    for (world, _), (v, uv) in zip(faces, projected):
        normal = np.cross(world[1]-world[0], world[2]-world[0])
        length = np.linalg.norm(normal)
        if length < 1e-7: continue
        brightness = .48 + .52*abs(float(np.dot(normal/length, light)))
        verts = v.copy()
        if first_person:
            if np.any(v[:,2]>=-.04): continue
            focal=height/(2*math.tan(math.radians(70)/2))
            verts[:,0]=width/2+v[:,0]/(-v[:,2])*focal
            verts[:,1]=height/2-v[:,1]/(-v[:,2])*focal
        else:
            verts[:,0] = (v[:,0]-center[0])*scale+width/2
            verts[:,1] = height/2-(v[:,1]-center[1])*scale
        u, w = uv['uv']; su, sv = uv['uv_size']
        coords = np.array([[u,w],[u+su-.01,w],[u+su-.01,w+sv-.01],[u,w+sv-.01]])
        for indices in ([0,1,2], [0,2,3]):
            a,b,c = verts[indices]; ua,ub,uc = coords[indices]
            minx,maxx = max(0,int(min(a[0],b[0],c[0]))), min(width-1,int(max(a[0],b[0],c[0]))+1)
            miny,maxy = max(0,int(min(a[1],b[1],c[1]))), min(height-1,int(max(a[1],b[1],c[1]))+1)
            if minx>maxx or miny>maxy: continue
            den = (b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
            if abs(den)<1e-8: continue
            xx,yy = np.meshgrid(np.arange(minx,maxx+1)+.5, np.arange(miny,maxy+1)+.5)
            wa=((b[1]-c[1])*(xx-c[0])+(c[0]-b[0])*(yy-c[1]))/den
            wb=((c[1]-a[1])*(xx-c[0])+(a[0]-c[0])*(yy-c[1]))/den
            wc=1-wa-wb
            z=wa*a[2]+wb*b[2]+wc*c[2]
            region=depths[miny:maxy+1,minx:maxx+1]
            mask=(wa>=-1e-6)&(wb>=-1e-6)&(wc>=-1e-6)&(z>region)
            if not mask.any(): continue
            tx=np.clip((wa*ua[0]+wb*ub[0]+wc*uc[0]).astype(int),0,atlas.shape[1]-1)
            ty=np.clip((wa*ua[1]+wb*ub[1]+wc*uc[1]).astype(int),0,atlas.shape[0]-1)
            color=atlas[ty,tx].copy()
            color[:,:,:3]=(color[:,:,:3].astype(float)*brightness).astype(np.uint8)
            mask &= color[:,:,3]>0
            pixels[miny:maxy+1,minx:maxx+1][mask]=color[mask]
            region[mask]=z[mask]
    return Image.fromarray(pixels)


def font(size, bold=False):
    name='segoeuib.ttf' if bold else 'segoeui.ttf'
    return ImageFont.truetype(str(Path('C:/Windows/Fonts')/name),size)


def render_board(root):
    assets=root/'src/main/resources/assets/chaosticspells'
    geometry=json.loads((assets/'geo/owners_whip.geo.json').read_text())
    animations=json.loads((assets/'animations/owners_whip.animation.json').read_text())['animations']
    texture=Image.open(assets/'textures/item/owners_whip.png').convert('RGBA')
    board=Image.new('RGBA',(1600,1050),'#141820')
    d=ImageDraw.Draw(board)
    d.text((55,32),'КНУТ AVEMOLLAN',font=font(44,True),fill='#f2dec0')
    d.text((57,94),'Текстура 16×16 · 10 цветов · простая геометрия в стиле Minecraft',font=font(23),fill='#b3b9c5')
    d.rounded_rectangle((45,150,1555,545),radius=15,fill='#1b212b')
    extended={'root':[0,0,-73],**{f'lash_{i+1}':[0,0,round(math.sin(i*.4)*1.1,2)] for i in range(24)}}
    model=render(geometry,texture,extended,(1460,320),camera=(.12,.14,1))
    board.alpha_composite(model,(70,155))
    d.text((74,492),'В РАЗВЁРНУТОМ ВИДЕ — ОКОЛО 8 БЛОКОВ',font=font(24,True),fill='#d8bc83')
    idle=pose_at(animations['animation.owners_whip.idle'],0)
    d.rounded_rectangle((45,570,685,980),radius=15,fill='#1b212b')
    board.alpha_composite(render(geometry,texture,idle,(605,335),camera=(.35,.08,1)),(60,575))
    d.text((75,930),'СВОРАЧИВАЕТСЯ В КОЛЬЦО В ПОКОЕ',font=font(21,True),fill='#d8bc83')
    d.rounded_rectangle((710,570,1045,980),radius=15,fill='#1b212b')
    board.alpha_composite(render(geometry,texture,{'root':[0,-20,-12]},(310,335),True,camera=(.3,.1,1)),(722,575))
    d.text((745,930),'РУКОЯТЬ КРУПНЫМ ПЛАНОМ',font=font(18,True),fill='#d8bc83')
    x,y=1090,600
    for title,body in [('КОЖА','Крупный пиксельный рисунок'),('МЕТАЛЛ','Две простые железные обоймы')]:
        d.text((x,y),title,font=font(22,True),fill='#e0c591')
        d.text((x,y+34),body,font=font(19),fill='#b3b9c5')
        y+=89
    d.text((x,800),'ТЕКСТУРА 16×16',font=font(22,True),fill='#e0c591')
    board.alpha_composite(texture.resize((128,128),Image.Resampling.NEAREST),(x,842))
    d.text((x+150,855),'24 сегмента',font=font(19),fill='#b3b9c5')
    d.text((x+150,891),'Три вида атак',font=font(19),fill='#b3b9c5')
    d.text((x+150,927),'Длина сохранена',font=font(19),fill='#b3b9c5')
    d.text((55,1005),'Превью из геометрии и текстуры мода. Внешний вид в игре зависит от освещения.',font=font(17),fill='#818b9a')
    output=root/'build/whip-assets/whip-assets-preview.png'
    output.parent.mkdir(parents=True,exist_ok=True)
    board.convert('RGB').save(output)
    # A turntable of the real resting mesh, so all four sides of the grip can be reviewed.
    frames=[]
    for angle in range(0,360,30):
        pose=dict(idle); pose['root']=[0,angle,-6]
        frame=Image.new('RGBA',(500,500),'#171c25')
        frame.alpha_composite(render(geometry,texture,pose,(460,430),camera=(.15,.1,1)),(20,30))
        frames.append(frame.convert('RGB'))
    frames[0].save(root/'build/whip-assets/whip-turntable.gif',save_all=True,append_images=frames[1:],duration=140,loop=0)
    # Review all three attack tracks at their real length with a stationary camera.
    camera=(1,.28,.8)
    forward=np.array(camera,dtype=float); forward/=np.linalg.norm(forward)
    right=np.cross([0,1,0],forward); right/=np.linalg.norm(right)
    basis=np.array([right,np.cross(forward,right),forward])
    samples=[]
    for key,label in [('lash_left','БОКОВОЙ УДАР СЛЕВА'),('lash_right','БОКОВОЙ УДАР СПРАВА'),('overhead','УСИЛЕННЫЙ УДАР СВЕРХУ')]:
        for t in np.linspace(0,.82,13):
            pose=pose_at(animations[f'animation.owners_whip.{key}'],float(t))
            points=np.concatenate([v@basis.T for v,_ in meshes(geometry,pose)])[:,:2]
            samples.append((pose,label,points))
    points=np.concatenate([p for _,_,p in samples])
    bounds=(points.min(axis=0),points.max(axis=0))
    frames=[]
    for pose,label,_ in samples:
        frame=Image.new('RGBA',(760,540),'#171c25')
        frame.alpha_composite(render(geometry,texture,pose,(730,440),camera=camera,bounds=bounds),(15,55))
        fd=ImageDraw.Draw(frame)
        fd.text((25,20),label,font=font(22,True),fill='#e7cea2')
        fd.text((25,505),'Анимация геометрии мода · неподвижная камера',font=font(17),fill='#929cad')
        frames.append(frame.convert('RGB'))
    frames[0].save(root/'build/whip-assets/whip-attacks.gif',save_all=True,append_images=frames[1:],duration=65,loop=0)


if __name__=='__main__': render_board(Path(__file__).resolve().parents[1])
