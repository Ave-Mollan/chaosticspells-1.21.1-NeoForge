"""Convert the user's original cuts to positional mono Vorbis without changing their duration/pitch."""
from pathlib import Path
import json
import subprocess

SOURCES = [
    ('whip-whip_5BYV6j2R.mp3', 'strike_1', [.539]),
    ('whip-whip_gk5z0Qio.mp3', 'strike_2', [.489]),
    ('whip-whip_M36yPUHL.mp3', 'strike_3', [.409]),
    ('whip-whip_O5bNIIlR.mp3', 'strike_4', [.429]),
    ('whip-whip_double.mp3', 'double', [.529, 1.307]),
]


def prepare_sounds(root):
    assets=root/'src/main/resources/assets/chaosticspells'
    output=assets/'sounds/whip'
    output.mkdir(parents=True,exist_ok=True)
    sounds={}
    manifest=[]
    for filename,name,peaks in SOURCES:
        source=root/filename
        subprocess.run(['ffmpeg','-y','-hide_banner','-loglevel','error','-i',str(source),'-ac','1','-ar','44100',
                        '-c:a','libvorbis','-q:a','6',str(output/f'{name}.ogg')],check=True)
        event='whip_double' if name=='double' else 'whip_'+name
        sounds[event]={'subtitle':'subtitles.chaosticspells.whip_double' if name=='double' else 'subtitles.chaosticspells.whip_crack',
                       'sounds':[{'name':f'chaosticspells:whip/{name}','attenuation_distance':24}]}
        manifest.append({'source':filename,'output':f'{name}.ogg','crack_seconds':peaks,'server_ticks':[round(t*20) for t in peaks]})
    # A combo is requested after the first sound has begun. Reuse the second cast of the user's
    # double cut, so a late button press cannot replay its first crack over an existing strike.
    subprocess.run(['ffmpeg','-y','-hide_banner','-loglevel','error','-i',str(root/'whip-whip_double.mp3'),
                    '-ss','0.9','-ac','1','-ar','44100','-c:a','libvorbis','-q:a','6',str(output/'follow_up.ogg')],check=True)
    sounds['whip_follow_up']={'subtitle':'subtitles.chaosticspells.whip_crack',
                             'sounds':[{'name':'chaosticspells:whip/follow_up','attenuation_distance':24}]}
    manifest.append({'source':'whip-whip_double.mp3','source_offset_seconds':.9,'output':'follow_up.ogg',
                     'crack_seconds':[.407],'server_ticks':[8]})
    (assets/'sounds.json').write_text(json.dumps(sounds,indent=2)+'\n',encoding='utf-8')
    (root/'build/whip-assets/user-sound-mapping.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')


if __name__=='__main__': prepare_sounds(Path(__file__).resolve().parents[1])
