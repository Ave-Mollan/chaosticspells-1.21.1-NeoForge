"""Rebuild the physics preview, 16x16 resources and the user's audio from their source files."""
from pathlib import Path
import os, shutil, subprocess, sys

root=Path(__file__).resolve().parents[1]
home=os.environ.get('JAVA_HOME')
javac=str(Path(home)/'bin/javac.exe') if home else shutil.which('javac')
java=str(Path(home)/'bin/java.exe') if home else shutil.which('java')
if not javac or not java: raise SystemExit('Set JAVA_HOME to a JDK 21 installation before rebuilding assets.')
output=root/'build/whip-physics-check'
output.mkdir(parents=True,exist_ok=True)
subprocess.run([javac,'-d',str(output),str(root/'src/main/java/com/avemollan/chaosticspells/physics/WhipRope.java'),
                str(root/'src/main/java/com/avemollan/chaosticspells/physics/WhipArmPose.java'),
                str(root/'src/main/java/com/avemollan/chaosticspells/physics/WorldWhipRope.java'),
                str(root/'dev/WhipPhysicsCheck.java'),str(root/'dev/WhipWorldCheck.java')],cwd=root,check=True)
subprocess.run([java,'-cp',str(output),'WhipPhysicsCheck'],cwd=root,check=True)
subprocess.run([java,'-cp',str(output),'WhipWorldCheck'],cwd=root,check=True)
subprocess.run([sys.executable,str(root/'dev/generate_whip_assets.py')],cwd=root,check=True)
