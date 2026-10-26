"""Preview production solver output using the actual model's cubes and texture."""
from pathlib import Path
import json, math
import numpy as np
from PIL import Image,ImageDraw
from render_whip_preview import render, meshes, font
from whip_model import LENGTHS, cube


def pose(points,angle=0,yaw=0,hand=(0,0,0)):
    result={'root':[0,0,0], 'handle':{'rotation':[math.degrees(angle),math.degrees(yaw),0], 'position':[v*16 for v in hand]},
            'packed_coil':{'hidden':True}}
    for i in range(24):
        dx,dy,dz=[points[i+1][k]-points[i][k] for k in range(3)]
        result[f'lash_{i+1}']={'position':[v*16 for v in points[i]],
                              'rotation':[math.degrees(math.atan2(dz,math.hypot(dx,dy))),0,-math.degrees(math.atan2(dx,dy))],
                              'scale':[1,math.sqrt(dx*dx+dy*dy+dz*dz)/(LENGTHS[i]/16),1]}
    return result


def packed_pose(first_person=False, palm=False, angle=0, yaw=0, hand=(0,0,0)):
    root={'rotation':[0,0,0], 'position':[0,0,0]}
    if first_person:
        root={'rotation':[math.degrees(.25),0,0], 'position':[0,4*(1-math.cos(.25)),-4*math.sin(.25)]}
    if palm:
        # Net transform of vanilla's held-item basis, JSON display translation and our root pose.
        root={'rotation':[-90,0,0], 'position':[0,-.07*16,.25*16]}
    held={'rotation':[math.degrees(angle),math.degrees(yaw),0], 'position':[v*16 for v in hand]}
    return {'root':root,'handle':held,'packed_coil':dict(held),**{f'lash_{i+1}':{'hidden':True} for i in range(24)}}


def render_packed_hand(root,geometry,texture):
    import copy
    output=root/'build/whip-assets'
    g=copy.deepcopy(geometry)
    block=cube([-2,-4,-2],[4,4,4],[0,16],uv_size=(4,4))
    g['minecraft:geometry'][0]['bones'].append({'name':'palm','pivot':[0,0,0],'cubes':[block]})
    atlas=Image.new('RGBA',(16,32),'#d58c50');atlas.paste(texture,(0,0))
    board=Image.new('RGBA',(1300,760),'#171e27'); d=ImageDraw.Draw(board)
    d.text((35,25),'СВЁРНУТЫЙ КНУТ · ПЛОТНАЯ СПИРАЛЬ',font=font(31,True),fill='#ecd6af')
    d.text((37,72),'Текстура 16×16 · витки рядом друг с другом · связка лежит поперёк ладони',font=font(20),fill='#acbac6')
    d.rounded_rectangle((25,125,635,660),radius=14,fill='#202b36')
    board.alpha_composite(render(geometry,texture,packed_pose(),(570,455),camera=(.1,.1,1)),(45,135))
    d.rounded_rectangle((665,125,1275,660),radius=14,fill='#202b36')
    board.alpha_composite(render(g,atlas,packed_pose(palm=True),(570,455),camera=(.35,1,.75)),(685,135))
    d.text((48,607),'КОМПАКТНО СКРУЧЕННЫЙ РЕМЕНЬ',font=font(21,True),fill='#dcc18e')
    d.text((690,607),'ПОСАДКА НА ЛАДОНИ',font=font(21,True),fill='#dcc18e')
    d.text((35,704),'Расчётное превью геометрии. Оранжевый куб обозначает ладонь; это не скриншот Minecraft.',font=font(18),fill='#8f9daa')
    board.convert('RGB').save(output/'whip-coiled-in-hand-preview.png')


def render_legacy_board(root):
    assets=root/'src/main/resources/assets/chaosticspells'
    output=root/'build/whip-assets'
    geometry=json.loads((assets/'geo/owners_whip.geo.json').read_text())
    texture=Image.open(assets/'textures/item/owners_whip.png').convert('RGBA')
    rest=json.loads((output/'physics-rest.json').read_text())
    frames=json.loads((output/'physics-frames.json').read_text())
    resting=packed_pose()
    render_packed_hand(root,geometry,texture)
    view=Image.new('RGBA',(1280,720),'#202a32')
    vd=ImageDraw.Draw(view)
    for x in range(0,1280,80): vd.line((x,0,x,720),fill='#29363f')
    for y in range(0,720,80): vd.line((0,y,1280,y),fill='#29363f')
    vd.line((628,360,652,360),fill='#a4b4bf',width=2); vd.line((640,348,640,372),fill='#a4b4bf',width=2)
    view.alpha_composite(render(geometry,texture,packed_pose(first_person=True),view.size,first_person=True))
    vd=ImageDraw.Draw(view)
    vd.text((30,24),'ПОСАДКА В ПРАВОЙ РУКЕ · FOV 70°',font=font(25,True),fill='#ecd6af')
    vd.text((30,65),'Расчётное превью модели и положения предмета; не скриншот Minecraft.',font=font(18),fill='#99aab6')
    view.convert('RGB').save(output/'whip-first-person-preview.png')
    sample=min(frames,key=lambda f:abs(f['tick']-30))
    striking=pose(sample['points'],sample['handle'],sample['yaw'],sample['hand'])
    board=Image.new('RGBA',(1400,950),'#151a22'); d=ImageDraw.Draw(board)
    d.text((45,25),'КНУТ: ФИЗИКА РЕМНЯ',font=font(40,True),fill='#ecd6af')
    d.text((47,82),'Текстура 16×16 · инерция и гравитация · широкая дуга удара',font=font(22),fill='#b5bdca')
    d.rounded_rectangle((35,130,720,650),radius=14,fill='#1e2530')
    board.alpha_composite(render(geometry,texture,resting,(645,440),camera=(.35,.15,1)),(55,140))
    d.text((62,594),'ПЛОТНО СКРУЧЕННЫЕ ВИТКИ',font=font(23,True),fill='#dcc18e')
    d.rounded_rectangle((750,130,1365,650),radius=14,fill='#1e2530')
    board.alpha_composite(render(geometry,texture,striking,(575,440),camera=(1,.12,.3)),(770,140))
    d.text((780,594),'ВЗМАХ ВПЕРЁД, А НЕ ВВЕРХ',font=font(23,True),fill='#dcc18e')
    for x,title,body in [(60,'24 СЕГМЕНТА','Положение рассчитывается физикой'),(530,'ДУГА 160°','Несколько целей перед игроком'),(1010,'ВАШИ ЗВУКИ','4 одиночных + двойной')]:
        d.text((x,710),title,font=font(27,True),fill='#e0c591'); d.text((x,752),body,font=font(18),fill='#b5bdca')
    d.text((47,877),'Превью рассчитано тем же решателем, который используется в клиенте мода.',font=font(19),fill='#8692a4')
    board.convert('RGB').save(output/'whip-physics-preview.png')
    camera=(1,.15,.4)
    forward=np.array(camera,dtype=float);forward/=np.linalg.norm(forward)
    right=np.cross([0,1,0],forward);right/=np.linalg.norm(right)
    basis=np.array([right,np.cross(forward,right),forward])
    samples=[]
    for f in frames:
        if -3<=f['tick']<=80:
            p=(packed_pose(angle=f['handle'],yaw=f['yaw'],hand=f['hand']) if f['tick']<3
               else pose(f['points'],f['handle'],f['yaw'],f['hand']))
            points=np.concatenate([v@basis.T for v,_ in meshes(geometry,p)])[:,:2]
            samples.append((f,p,points))
    vertices=np.concatenate([v for _,_,v in samples]); bounds=(vertices.min(axis=0),vertices.max(axis=0))
    animation=[]
    for f,p,_ in samples:
        image=Image.new('RGBA',(820,560),'#171d26')
        image.alpha_composite(render(geometry,texture,p,(790,450),camera=camera,bounds=bounds),(15,60))
        draw=ImageDraw.Draw(image)
        draw.text((24,20),'ФИЗИЧЕСКИЙ ДВОЙНОЙ ВЗМАХ',font=font(24,True),fill='#e7cea2')
        draw.text((24,524),f"Реальное движение решателя · {max(0,f['tick'])/20:.2f} с",font=font(17),fill='#8f9cae')
        animation.append(image.convert('RGB'))
    animation[0].save(output/'whip-physics.gif',save_all=True,append_images=animation[1:],duration=50,loop=0)


def render_board(root):
    """Fixed-camera diagnostic of the authoritative solver, separate from native game screenshots."""
    assets=root/'src/main/resources/assets/chaosticspells'
    output=root/'build/whip-assets'
    geometry=json.loads((assets/'geo/owners_whip.geo.json').read_text())
    texture=Image.open(assets/'textures/item/owners_whip.png').convert('RGBA')
    render_packed_hand(root,geometry,texture)
    frames=json.loads((output/'world-frames.json').read_text())
    samples=[f for f in frames if 0<=f['tick']<=200 and (f['tick']<50 or f['tick']%3==0)]
    forward=np.array([1,.45,.55]);forward/=np.linalg.norm(forward)
    right=np.cross([0,1,0],forward);right/=np.linalg.norm(right)
    basis=np.array([right,np.cross(forward,right),forward])
    vertices=np.concatenate([np.array(f['points'])@basis.T for f in samples])
    lo,hi=vertices[:,:2].min(axis=0),vertices[:,:2].max(axis=0)
    lo-=.6;hi+=.6
    def draw_frame(f,size=(900,620)):
        w,h=size;canvas=Image.new('RGB',size,'#171e27');d=ImageDraw.Draw(canvas)
        scale=min((w-60)/(hi[0]-lo[0]),(h-150)/(hi[1]-lo[1]))
        center=(hi+lo)/2
        def pixel(p):
            v=np.array(p)@basis.T
            return (float(w/2+(v[0]-center[0])*scale),float(90+(h-140)/2-(v[1]-center[1])*scale))
        for x in range(-6,7):
            d.line([pixel((x,0,-5)),pixel((x,0,7))],fill='#293743')
        for z in range(-5,8):
            d.line([pixel((-6,0,z)),pixel((6,0,z))],fill='#293743')
        points=f['points']
        for i,(a,b) in enumerate(zip(points,points[1:])):
            d.line([pixel(a),pixel(b)],fill='#af7642' if i%2 else '#805030',width=max(2,round((.085-.055*i/24)*scale)))
        rootpoint=np.array(points[0]);neck=np.array(points[1]);axis=neck-rootpoint
        axis/=max(1e-9,np.linalg.norm(axis))
        grip=rootpoint-axis*.5
        d.line([pixel(grip),pixel(rootpoint)],fill='#9ba4aa',width=max(4,round(.12*scale)))
        tip=pixel(points[-1]);d.ellipse((tip[0]-3,tip[1]-3,tip[0]+3,tip[1]+3),fill='#e6c18c')
        tick=f['tick'];label='ЗАМАХ НАЗАД' if tick<11 else 'ПРОХОД ВПЕРЁД' if tick<50 else 'СВОБОДНОЕ ПАДЕНИЕ И ПОКОЙ'
        d.text((25,18),label,font=font(25,True),fill='#e7cea2')
        d.text((25,53),f'Мировая физика сервера · {tick/20:.2f} с · неподвижная камера',font=font(18),fill='#b1bdc9')
        d.text((25,h-34),'Схема точек Java-решателя, не скриншот Minecraft.',font=font(17),fill='#929eab')
        return canvas
    images=[draw_frame(f) for f in samples]
    images[0].save(output/'whip-physics.gif',save_all=True,append_images=images[1:],
                   duration=[50 if f['tick']<50 else 150 for f in samples],loop=0)
    selected=[min(frames,key=lambda f:abs(f['tick']-t)) for t in [10,20,180]]
    board=Image.new('RGB',(1500,480),'#171e27')
    for i,f in enumerate(selected):board.paste(draw_frame(f,(500,480)),(i*500,0))
    board.save(output/'whip-physics-preview.png')


if __name__=='__main__': render_board(Path(__file__).resolve().parents[1])
