//Created 2004-10-28
//
//Copyright (C) 2004  Markus Yliker�l� and Maija Savolainen
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//http://www.gnu.org/copyleft/gpl.html

package juinness;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import com.sun.image.codec.jpeg.*;
import javax.imageio.*;
import javax.imageio.stream.*;


import juinness.util.Util;
import juinness.util.MutableInteger;

import java.lang.reflect.*;
import javax.media.j3d.*;
import java.util.*;
import java.awt.image.BufferedImage;
import com.sun.j3d.utils.geometry.*;

/**
 * The <code>Traverser</code> class traverses and chops 
 * Java 3D (J3D) scenegraph
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class Traverser
{ 
  private Util log = Util.getInstance();
  private Translator translator;
  private Class[] para;
  private Object[] args;
  private Map hash;

  private Transform3D t3d;

  public static final int TRIANGLE_STRIP_ARRAY = 1;
  private float[] coordinate;
  private float[] normal;
  private byte[] color;
  private float[] texCoord;
  private MutableInteger offset;
  private MutableInteger offsetTex;

  public Traverser(){
    para = new Class[1];
    args = new Object[1];

    t3d = new Transform3D();

    offset = new MutableInteger(0);
    offsetTex = new MutableInteger(0);
    coordinate = new float[3];
    normal = new float[3];
    color = new byte[3];
    texCoord = new float[2];
  }

  /**
   * Traverser the J3D tree in depth-first order
   */
  public void traverse(Translator translator, SceneGraphObject root)
    throws Exception{

    this.translator = translator;
    hash = getSupported(translator.getClass());
    
    Stack s = new Stack();
    s.push(root);

    while(s.isEmpty() == false){      
      try{      
	log.logMsg("\n-------------------------------------");
	SceneGraphObject n = (SceneGraphObject)s.pop();

        invokeMethod(n);

        if(n instanceof Group){
          Group g = (Group)n;
	  for(int i=0; i<g.numChildren(); i++){
	    s.push(g.getChild(i));
	  }
        }
      }
      catch(Exception e){
        e.printStackTrace();	
      }
    }
    log.logMsg("\n-------------------------------------");
    //Tell translator that the J3D tree has been traversed
    translator.end();
  }
 

  private void invokeMethod(Object n){
    try{
      if(n == null){
        return;
      }

      Method met;
      para[0] = n.getClass();
      if((met = (Method)hash.get(para[0].getName())) == null){
	//n is not atleast a pure J3D API Object
        para[0] = para[0].getSuperclass();
	//Test if n is derived from J3D API
        if((met = (Method)hash.get(para[0].getName())) == null){
	  //Translation is not supported
	  log.logMsg("" + para[0].getName() + " NOT SUPPORTED");
          return;
        }  
      }  
      log.logMsg("Invoke: " + met);
      args[0] = n;
      met.invoke(translator, args);
    }
    catch(Exception e){
      e.printStackTrace();
    }    
    catch(Throwable t){
      t.printStackTrace();
    }    
  }

  public Map getSupported(Class c){
    Map hash = new HashMap();
    Method[] m = c.getMethods();
    log.logMsg("Traverser request methods from Translator");
    for(int i=0; i<m.length; i++){
      if(m[i].getName().startsWith("create")){
	Class[] parameterTypes = m[i].getParameterTypes();
	if(parameterTypes.length == 1){
	  String param = parameterTypes[0].getName();
	  log.logMsg("accept method: " + m[i].getName() + " " + param);
	  hash.put(param, m[i]);
	}
      }
    }
    return hash;
  }

  public void parse(Shape3D n){
    log.logMsg("PARSE: Shape3D");
    Enumeration enu = n.getAllGeometries();
    while(enu.hasMoreElements()){      
      invokeMethod(enu.nextElement());
    }
    invokeMethod(n.getAppearance());
  }

  public void parse(Appearance n){
    log.logMsg("PARSE: Appearance");
    invokeMethod(n.getMaterial());
    
    log.logMsg("INVOKE TEX: " + n.getTexture());
    invokeMethod(n.getTexture()); 
  }

  public void parse(TransformGroup n){
    log.logMsg("PARSE: TransformGroup");
    n.getTransform(t3d);
    invokeMethod(t3d);
  }

  public void parseGeometry(int id, Iterator itr,
			    short[] vertex, short[] normal, 
			    byte[] color, short[][] tex, 
			    float scale){
    
    switch(id){
    case TRIANGLE_STRIP_ARRAY:
      break;
    default:
      log.logMsg("NO IMPLEMENTATION FOR THIS Traverser id: " + id );
      return;
    }

    offset.setValue(0);
    offsetTex.setValue(0);

    while(itr.hasNext()){
      Object obj = itr.next();
      if(obj instanceof javax.media.j3d.IndexedTriangleArray){
	parseGeometry((javax.media.j3d.IndexedTriangleArray)obj,
		      vertex, normal, 
		      color, tex, scale);
      }
      else if(obj instanceof javax.media.j3d.TriangleArray){
	parseGeometry((javax.media.j3d.TriangleArray)obj,
		      vertex, normal, 
		      color, tex, scale);
      }
      else if(obj instanceof javax.media.j3d.QuadArray){
	parseGeometry((javax.media.j3d.QuadArray)obj,
		      vertex, normal, 
		      color, tex, scale);
      }
      else{
	System.err.println("NOT IMPLEMENTED GEOMETRY: " + obj);
      }
    }
  }
  
  private void parseGeometry(javax.media.j3d.QuadArray n, 
			     short[] vertex, short[] normal, 
			     byte[] color, short[][] texCoord, 
			     float scale)
  {
    log.log("*Mesh -> QuadArray to TriangleArray");
    int numVertex = n.getValidVertexCount();
    int texCount = n.getTexCoordSetCount();
    //int texCount = texCoord.length;

    for(int i=0, k=0; i<numVertex; i++){
      parse(offset, n,
  	    i, i, -1,
  	    vertex, normal, color, scale, 4);
      //parse(n, texCount, i, texCoord);
    }
  }
  
  /**
   * Parses triangleArray associated with this 
   * into concrete trianglearray
   */ 
  private void parseGeometry(javax.media.j3d.TriangleArray n, 
			    short[] vertex, short[] normal, 
			    byte[] color, short[][] texCoord, 
			    float scale)
  {
    log.log("*Mesh -> TriangleArray to TriangleArray");
    int numVertex = n.getValidVertexCount();
    int texCount = texCoord.length;
    int texSet = n.getTexCoordSetCount();
    //texSet = 1;
    //int[] texMapArr = new int[n.getTexCoordSetMapLength()];
    //n.getTexCoordSetMap(texMapArr);
    int i, k;
    log.log("  numVertex: " + numVertex + " " + texCount + " " + texSet + 
	    " " + n.getTexCoordSetMapLength()); 

    for(i=0, k=0; i<numVertex; i++){
      try{
	parse(offset, n,
	      i, i, i,
	      vertex, normal, color, scale, 3);
	parseTexture(offsetTex, n, i, texSet, texCoord, scale);
      }
      catch(Exception e){
	parse(offset, n,
	      i, i, -1,
	      vertex, normal, color, scale, 3);
	parseTexture(offsetTex, n, i, texSet, texCoord, scale);
      }
    }
  }
  
  /**
   * Parses indexedTriangleArray associated with this 
   * into concrete trianglearray
   */ 
  private void parseGeometry(IndexedTriangleArray n, 
			     short[] vertex, short[] normal, 
			     byte[] color, short[][] texCoord, 
			     float scale)
  {
    log.log("*Mesh -> IndexedTriangleArray to TriangleArray");
    int numIndex = n.getValidIndexCount();
    int texCount = texCoord.length;
    int texSet = n.getTexCoordSetCount();
    log.log("  numIndex: " + numIndex + " " + texCount + " " + texSet +
	    " " + n.getTexCoordSetMapLength()); 

    //int texSet = n.getTexCoordSetCount();
    //int[] texMapArr = new int[n.getTexCoordSetMapLength()];
    //n.getTexCoordSetMap(texMapArr);

    for(int i=0; i<numIndex; i++){
      try{
  	parse(offset, n,
  	      n.getCoordinateIndex(i), n.getNormalIndex(i), 
  	      n.getColorIndex(i), 
  	      vertex, normal, color, scale, 3);
	parseTexture(offsetTex, n, n.getCoordinateIndex(i), 
		     texSet, texCoord, scale);
      }
      catch(Exception e){
  	parse(offset, n,
  	      n.getCoordinateIndex(i), n.getNormalIndex(i), 
  	      -1,
  	      vertex, normal, color, scale, 3);
	parseTexture(offsetTex, n, n.getCoordinateIndex(i), 
		     texSet, texCoord, scale);
      }
    } 
  }

  /**
   * Retrives the values
   */
  private final void parse(MutableInteger offset, 
			   javax.media.j3d.GeometryArray n,
			  int vertexIndex, int normalIndex, 
			  int colorIndex, 
			  short[] mobileVertex, short[] mobileNormal, 
			  byte[] mobileColor, 
			  float scale, int max){
    n.getCoordinate(vertexIndex, coordinate);
    n.getNormal(normalIndex, normal);    
    if(colorIndex != -1){
      n.getColor(colorIndex, color);
    }
    for(int j=0; j<max; j++, offset.value++){
      mobileVertex[offset.value] = (short)(coordinate[j]*scale); 
      mobileNormal[offset.value] = (short)(normal[j]);
      if(colorIndex != -1){
  	mobileColor[offset.value] = color[j];
      }
      else{
  	mobileColor[offset.value] = 0;
      }
    }
  }

  private final void parseTexture(MutableInteger offsetTex,
				  GeometryArray n, int texIndex, int texSet,  
				  short[][] mobileTex, float scale){

    for(int i=0; i<texSet; i++){            
      //int id = n.getTextureCoordinateIndex(i, texIndex);
      //System.err.print("\n: " + texIndex + " " + offsetTex + "->");
      n.getTextureCoordinate(i, texIndex, texCoord);
      for(int k=0; k<texCoord.length; k++, offsetTex.value++){
  	mobileTex[i][offsetTex.value] = (short)(texCoord[k]*scale);
	//System.err.print("" + mobileTex[i][offsetTex.value] + " ");
      }
    }
  }

  public BufferedImage[] parse(javax.media.j3d.Texture n){
    log.logMsg("PARSE: Texture");
    javax.media.j3d.ImageComponent[] img = n.getImages();
    BufferedImage[] bufImg = null;
    if(img.length > 0){
      bufImg = new BufferedImage[img.length];
    }
    log.logMsg("  textures: " + img.length);
    for(int i=0; i<img.length; i++){
      int format = img[i].getFormat();    
      int width = img[i].getWidth();
      int height = img[i].getHeight();
      
      log.logMsg("  width: " + width);
      log.logMsg("  height: " + height);
      log.logMsg("  format: " + format);

      switch(format){
      case javax.media.j3d.ImageComponent.FORMAT_RGB:
	log.logMsg("  FORMAT_RGB");
	break;
      case javax.media.j3d.ImageComponent.FORMAT_RGBA:
	log.logMsg("  FORMAT_RGBA");
	break;
      default:
	log.logMsg("  Unknown image format: " + format);
	break;
      }
      
      BufferedImage buf = null;
      if(img[i] instanceof javax.media.j3d.ImageComponent2D){
	log.logMsg("  ImageComponent2D");
	javax.media.j3d.ImageComponent2D img2D = 
	  (javax.media.j3d.ImageComponent2D)img[i];	
	//Get the image
	buf = img2D.getImage();
      }
      else if(img[i] instanceof javax.media.j3d.ImageComponent3D){
	log.logMsg("  ImageComponent3D Not implemented yet");
	javax.media.j3d.ImageComponent3D img3D = 
	  (javax.media.j3d.ImageComponent3D)img[i];
      }
      else{
	log.logMsg("  Unknown image");
	continue;
      }
      bufImg[i] = 
	new BufferedImage(width, 
			  height, 
			  BufferedImage.TYPE_BYTE_INDEXED);
      
      //Convert the image to TYPE_BYTE_INDEXED
      int[] rgb = getPixels(buf);
      MemoryImageSource mem = 
	new MemoryImageSource(width, height, rgb, 0, width);
      
      Label label = new Label();
      Image image = label.createImage(mem);           
      Graphics g = bufImg[i].getGraphics();
      g.drawImage(image, 0, 0, null);
      g.dispose();      
    }
    return bufImg;
  }
  
  public int[] getPixels(BufferedImage img)
  {
    DataBuffer dataBuf = img.getData().getDataBuffer();
    int dataType = dataBuf.getDataType();
    DataBufferInt dataBufByte = ((DataBufferInt)dataBuf); 
    int[] pixels = dataBufByte.getData();      
    return pixels;
  }
}


