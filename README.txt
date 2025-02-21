Copyright (c) 2005 Markus Ylikerala and Maija Savolainen 

2005-03-12
READ ME
HOW TO GET STARTED

1) and 2) Installing Juinness
3) Executing Juinness

****************************************************************************
1)
Install your environment, you need J2SE 5.0, J2SE 1.4.2 SDK, J3D, 
M3G and some J3D-loader e.g. for vrml (wrl)

Thus, get jar, zip and library files (.so on Linuz OR .dll on windows) for
J3D, M3G and J3D-loader and install set them into lib directory

**************
You can test the converted m3g file e.g. with 
J2ME Wireless Toolkit (WTK)
http://java.sun.com/products/j2mewtoolkit/

Create a WTK project and copy files from apps folder to the 
src folder of the WTK project 
e.g. Demoni as the project name and Demo as the MIDlet name
There is three different modes the camera can be moved
the mode can be changed with the 5-button and the camera can be moved
with the cursors/numbers

**************
Currently J2SE 5.0 is only needed because
Util uses it with these methods:
  public final String showRGBAColor(int type){
    return getHex(Integer.rotateRight(Integer.reverseBytes(type), 8));
  }
  public final String showRGBColor(int type){
    return getHex(Integer.rotateRight(Integer.reverseBytes(type), 8));
  }
So if you modify them, a previous version of J2SE should be fine

**************
a)
J3D

for Linux:
http://www.blackdown.org/

for Windows:
http://java.sun.com/products/java-media/3D/

jar files for Linux and Windows:
j3daudio.jar
j3dcore.jar
j3dutils.jar
vecmath.jar

library files for Linux:
libj3daudio.so
libJ3D.so
libJ3DUtils.so

library files for Windows:
J3D.dll
j3daudio.dll
J3DUtils.dll

**************
b)
M3G 
http://jcp.org/en/jsr/detail?id=184

for Linux and Windows
classes.zip

**************
c)
J3D-loader
http://www.j3d.org/

for Linux and Windows some loader.jar depending on the model format
you are going to convert

****************************************************************************
2)
Modify some files so that loader(s) can be found

Modify your __environment script (.sh OR bat file) and loader.xml files

a)
__environment:

set your loader:
REM loader stuff e.g. CHANGE THE content of the loader.xml file
SET LOADER=%LIB%\myVrmlLoader.jar

modify paths:
REM Should be enough just to modify the following parameters
modify these

b)
loader.xml:
set the format and the name of the loader

****************************************************************************
3)
Compile:
a)
compile_ALL

and Execute:
b)
launch_Juinness
