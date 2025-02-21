# Juinness ([sourceforge](https://juinness.sourceforge.net/) mirror)

**Copyright (c) 2005 Markus Ylikerala and Maija Savolainen**  
**2005-03-12**

## HOW TO GET STARTED

### 1) & 2) Installing Juinness
### 3) Executing Juinness

---

## 1. Installation Requirements

You need:
- J2SE 5.0
- J2SE 1.4.2 SDK
- J3D
- M3G
- J3D-loader (e.g., for VRML/WRL)

```text
Install required jar/zip/library files (.so on Linux / .dll on Windows) for:
- J3D
- M3G 
- J3D-loader
into the lib directory
```

### Testing with J2ME Wireless Toolkit (WTK)
Download WTK: [http://java.sun.com/products/j2mewtoolkit/](http://java.sun.com/products/j2mewtoolkit/) - deadlink

```text
1. Create WTK project
2. Copy files from apps folder to WTK project's src folder
3. Use "Demoni" as project name and "Demo" as MIDlet name

Camera controls:
- Change modes with 5-button
- Move camera with cursors/numbers
```

### J2SE 5.0 Note
```text
Required due to these methods in Util:
  public final String showRGBAColor(int type){
    return getHex(Integer.rotateRight(Integer.reverseBytes(type), 8));
  }
  public final String showRGBColor(int type){
    return getHex(Integer.rotateRight(Integer.reverseBytes(type), 8));
  }
Modify these methods to use with older J2SE versions
```

---

## 2. Environment Setup

### a) J3D Installation
- **Linux**: [Blackdown](http://www.blackdown.org/) - deadlink
- **Windows**: [Java 3D](http://java.sun.com/products/java-media/3D/) - deadlink

Required files:
```text
j3daudio.jar
j3dcore.jar
j3dutils.jar
vecmath.jar

Linux libraries:
libj3daudio.so
libJ3D.so
libJ3DUtils.so

Windows libraries:
J3D.dll
j3daudio.dll
J3DUtils.dll
```

### b) M3G Installation
Download from: [JSR 184](http://jcp.org/en/jsr/detail?id=184)  
Required file: `classes.zip`

### c) J3D-loader Installation
Get from: [j3d.org](http://www.j3d.org/)  
(Choose loader compatible with your model format)

---

## 3. Configuration

### a) Modify Environment Script
Edit `__environment` file:
```text
# loader configuration example
SET LOADER=%LIB%\myVrmlLoader.jar

# Modify these paths as needed
SET JAVA_HOME=...
SET PATH=...
```

### b) Configure loader.xml
```text
<loader>
  <format>VRML</format>
  <classname>com.example.MyVrmlLoader</classname>
</loader>
```

---

## 4. Execution

### Compile:
```bash
compile_ALL
```

### Run:
```bash
launch_Juinness
```