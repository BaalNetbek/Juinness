//Created 2004-11-12
//
//Copyright (C) 2004  Markus Ylikerälä and Maija Savolainen
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
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package juinness;

import juinness.m3g.*;
import juinness.util.*;

import javax.microedition.m3g.*; 
import java.util.*;
import java.awt.image.BufferedImage;

/**
 * The <code>Translator</code> class 
 * translates J3D into M3G scenegraph 
 *
 * @author Markus Yliker&auml;l&auml; and Maija Savolainen
 */
public class Translator extends GeneratedTranslator
{
  private Util util;
  private float[] bias;
  private Transform transform;
  private List meshList;
  private float[] matrix = new float[16];
  private boolean hasMatrix;
  private List data = new Vector();

  private Group subGroup;
  private SubAppearance appearance;

  /** Number of the vertices needed to hold the coordinates */
  private int numVertices;

  /** Number of the coordinates needed to hold the textures */
  private int numTextures;

  /** All the geometries associated with this */
  private List geometry;

  private float[] coordinate;
  private float[] normal;
  private byte[] color;
  private float[] texCoord;
  private MutableInteger offset;
  private MutableInteger offsetTex;

  
  /**
   * Constructs this
   */ 
  public Translator(){
    util = Util.getInstance();

    transform = new Transform();
    meshList = new Vector();    
    bias = new float[3];
    for(int i=0; i<bias.length; i++){
      bias[i] = 0.0f;
    }

    geometry = new Vector();
    offset = new MutableInteger(0);
    offsetTex = new MutableInteger(0);
    coordinate = new float[3];
    normal = new float[3];
    color = new byte[3];
    texCoord = new float[2];
  }

  private void addM3G(Object obj){
    data.add(obj);
    System.err.println("\n\n" + data);
  }

  void createGroup(javax.media.j3d.BranchGroup n){
    createGroup((javax.media.j3d.Group)n);
  }

  void createGroup(javax.media.j3d.TransformGroup n){
    createGroup((javax.media.j3d.Group)n);
  }

  private Map gHash = new HashMap();

  private void createGroup(javax.media.j3d.Group n){
    util.log("J3D->M3G javax.media.j3d.Group: " + n);
    
    if(data.isEmpty()){
      SubWorld subWorld = new SubWorld();
      addM3G(subWorld);
      subGroup = subWorld;

      gHash.put(n, subGroup);
    }
    else{
      subGroup = new SubGroup();

      gHash.put(n, subGroup);

      if(hasMatrix){
	hasMatrix = false;
	subGroup.getTransform(transform);
	util.showMatrix(matrix);
	transform.set(matrix);
	subGroup.setTransform(transform);
      }
      
      Group gp = (Group)gHash.get(n.getParent());
      gp.addChild(subGroup);

      addM3G(subGroup);
    }
  }

  void createTransform(javax.media.j3d.Transform3D n){
    super.createTransform(n);
    n.get(matrix);
    hasMatrix = true;
  }

  void createAppearance(javax.media.j3d.Appearance n){
    super.createAppearance(n);
    //3. Appearance
    appearance = new SubAppearance(); 
    //list.add(0, appearance);
  }
  
  void createVertexArray(javax.media.j3d.TriangleArray n){
    System.err.println("J3D->M3G javax.media.j3d.TriangleArray");
    numVertices += n.getVertexCount();
    createVertexArray((javax.media.j3d.GeometryArray)n);

    //int num = n.getValidIndexCount();    
    //int numVertex = n.getValidVertexCount();
    //parseTriangleArray(n, vertex, normal, color,texCoord, scale);
  }

  void createVertexArray(javax.media.j3d.IndexedTriangleArray n){
    System.err.println("J3D->M3G javax.media.j3d.IndexedTriangleArray");
    numVertices += n.getValidIndexCount();
    createVertexArray((javax.media.j3d.GeometryArray)n);

    //int num = n.getVertexCount();
    //int numVertex = n.getValidVertexCount();
    //parseTriangleArray(n, vertex, normal, color,texCoord, scale);
    //createVertexArray(num, 2);
  }

  void createVertexArray(javax.media.j3d.GeometryArray n){
    numTextures += n.getTexCoordSetCount();
    geometry.add(n);
  }
  
  void createMesh(javax.media.j3d.Shape3D n){
    super.createMesh(n);

    //4. Creating the Mesh    
    TmpMesh mesh = new TmpMesh(subGroup, list.size(), 
			       geometry, numVertices, numTextures, appearance);
    meshList.add(mesh);
    geometry.clear();
    numVertices = 0;
    numTextures = 0;

    addM3G(mesh);

    //list.add(0, mesh);
  }

  public void end(){
    System.err.println("READY TO CREATE MESHES: " + meshList);

    //Solve the scaling factor
    //Scale to the upper limits of the 16-bit two's complement 
    //(signed short, two bytes -32768...+32767) so that we wont lose data
    //Later the inverse of this scaling factor is utilized when 
    //the meshes are constructed
    util.log("\n++++++++++++++++++++++++++++++++++");
    float max = parseMesh(meshList);
    util.log("GOT MAX: " + max);
    //scaling factor
    float factor = 32767/max;    
    //inverse of the scaling factor
    float scale = max/32767;

    list.clear();
    Iterator itr = data.iterator();
    while(itr.hasNext()){
      Object obj = itr.next();
      if(obj instanceof TmpMesh){
	TmpMesh tmpMesh = (TmpMesh)obj;
	util.log("  value: " + tmpMesh);
	//Create 16bit meshes
	createMesh(tmpMesh, factor, scale, bias, 2);
      }
      else{
	list.add(0, obj);
      }
    }

    util.logMsg(list);
  }

  private void createMesh(TmpMesh tmpMesh, float factor, float scale, 
			  float[] bias, int componentSize){

    try{
      util.log("We are creating a Mesh Object");
      List meshList = new Vector(); //.clear();

      //Retrieve the mesh data
      int numVertices = tmpMesh.numVertices;
      util.log("numVertices: " + numVertices);
      short[] mobileVertex = new short[numVertices*3];
      short[] mobileNormal = new short[numVertices*3];
      byte[] mobileColor = new byte[numVertices*3];
      int texCount = tmpMesh.numTextures;
      short[][] mobileTex = new short[texCount][numVertices*2];    

      offset.setValue(0);
      offsetTex.setValue(0);
      Iterator itr = tmpMesh.geometry.iterator();
      while(itr.hasNext()){
	Object obj = itr.next();
	if(obj instanceof javax.media.j3d.IndexedTriangleArray){
	  parseTriangleArray((javax.media.j3d.IndexedTriangleArray)obj,
			     mobileVertex, mobileNormal, 
			     mobileColor, mobileTex, factor);
	}
	else if(obj instanceof javax.media.j3d.TriangleArray){
	  parseTriangleArray((javax.media.j3d.TriangleArray)obj,
			     mobileVertex, mobileNormal, 
			     mobileColor, mobileTex, factor);
	}
	else{
	  System.err.println("NOT IMPLEMENTED GEOMETRY: " + obj);
	}
      }
      //util.log("OOKEE");

      //The Mesh element consists of: 
      //VertexBuffer, IndexBuffer, Appearance[]   
      //VertexBuffer concists of: 
      //three VertexArrays (positions, normals, 1-N*textcorrd)

      //1. VertexBuffer 			
      //numVertices-number of vertices in this VertexArray; must be [1, 65535]
      //numComponents-number of components per vertex; must be [2, 4]
      //componentSize-number of bytes per component; must be [1, 2]     
      VertexBuffer vertexBuf = new SubVertexBuffer(); 			
    
      //Positions must have 3 components
      VertexArray positions = 
	new SubVertexArray(numVertices, 3, componentSize);
      positions.set(0, numVertices, mobileVertex);
      vertexBuf.setPositions(positions, scale, bias);
      meshList.add(positions);	
    
      //Normal vectors must have 3 components.
      VertexArray normals = new SubVertexArray(numVertices, 3, componentSize);
      normals.set(0, numVertices, mobileNormal);
      vertexBuf.setNormals(normals);
      meshList.add(normals);	

      //Colors must have 3 or 4 components, one byte each.
      VertexArray colors = new SubVertexArray(numVertices, 3, 1);
      colors.set(0, numVertices, mobileColor);
      vertexBuf.setColors(colors);
      meshList.add(colors);
	
      //Texture coordinates must have 2 or 3 components.
      float texScale = 1.0f;
      float[] texBias = new float[3];
      texBias[0] = 1.0f;
      texBias[1] = 1.0f;
      texBias[2] = 0.0f;
      for(int i=0; i<texCount; i++){
	util.log("texCount: " + i + "/" + texCount);
	VertexArray texCoord = new SubVertexArray(numVertices, 2, componentSize);
	texCoord.set(0, numVertices, mobileTex[i]);
	vertexBuf.setTexCoords(i, texCoord, texScale, texBias);
	meshList.add(texCoord);	
      }
      meshList.add(vertexBuf);

      ///2. IndexBuffer			
      int[] stripLengths = new int[numVertices/3]; 
      for(int i=0; i<stripLengths.length; i++){
	stripLengths[i] = 3; 
      }
        
      //Create Implicit TriangleStripArray
      IndexBuffer indexBuf = 
	new SubTriangleStripArray(0, stripLengths, componentSize); 
      meshList.add(indexBuf);	

      //Also this is possible
      //Create Explicit TriangleStripArray
      //IndexBuffer indexBuf = 
      //new SubTriangleStripArray(index, stripLengths, componentSize); 
      
      //3. Appearance
      SubAppearance appearance = new SubAppearance(); 

      //       BufferedImage[] bufImg = absMesh.getTexture();
      //       if(bufImg != null)
      // 	for(int i=0; i<texCount; i++){
      // 	  SubImage2D subImg = null;
      // 	  SubTexture2D tex = null;
      // 	  Material mat = null;

      // 	  byte[] palette = util.getPalette(bufImg[i]);
      // 	  byte[] pixels = util.getPixels(bufImg[i]);
      // 	  subImg = new SubImage2D(Image2D.RGB, 
      // 				  bufImg[i].getWidth(),
      // 				  bufImg[i].getHeight(), 
      // 				  pixels,
      // 				  palette);
      // 	  meshList.add(subImg);
      
      // 	  //SubTexture2D tex = new SubTexture2D(subImg);
      // 	  tex = new SubTexture2D();
      // 	  tex.setImage(subImg);
      
      // 	  appearance.setTexture(i, tex);
      // 	  meshList.add(tex);
      
      // 	  mat = new SubMaterial();
      // 	  appearance.setMaterial(mat);
      // 	  meshList.add(mat);
      // 	}
      meshList.add(appearance);

      //4. Creating the Mesh
      Mesh mesh = new SubMesh(vertexBuf, indexBuf, appearance);
    
      //Add the mesh into group
      //AbsynGroup absGroup = absMesh.getGroup();        
      //SubGroup m3gGroup = (SubGroup)absGroup.getGroup();
      Group m3gGroup = tmpMesh.group;
      m3gGroup.addChild(mesh);
      meshList.add(mesh);

      //list.remove(tmpMesh.index);
      //int off = tmpMesh.index;
      
      for(int i=meshList.size()-1; i>-1; i--){
	list.add(0, meshList.get(i));
      }

      //return mesh;
    }
    catch(Exception e){
      e.printStackTrace();
      //return null;
    }
  }

  /**
   * Parses triangleArray associated with this 
   * into concrete trianglearray
   */ 
  private void parseTriangleArray(javax.media.j3d.TriangleArray n, 
  				  short[] vertex, short[] normal, 
  				  byte[] color, short[][] texCoord, 
  				  float scale)
  {
    util.log("*Mesh -> TriangleArray to TriangleArray");
    int numVertex = n.getValidVertexCount();
    //int texCount = n.getTexCoordSetCount();
    int texCount = texCoord.length;

    for(int i=0, k=0; i<numVertex; i++){
      parse(offset, n,
  	    i, i, i,
  	    vertex, normal, color, scale);
      parse(n, texCount, i, texCoord);
    }
  }
  
  /**
   * Parses indexedTriangleArray associated with this 
   * into concrete trianglearray
   */ 
  private void parseTriangleArray(javax.media.j3d.IndexedTriangleArray n, 
  				  short[] vertex, short[] normal, 
  				  byte[] color, short[][] texCoord, 
				  float scale)
  {
    util.log("*Mesh -> IndexedTriangleArray to TriangleArray");
    int numIndex = n.getValidIndexCount();
    util.log("numIndex: " + numIndex);
    for(int i=0; i<numIndex; i++){
      try{
  	parse(offset, n,
  	      n.getCoordinateIndex(i), n.getNormalIndex(i), 
  	      n.getColorIndex(i), 
  	      vertex, normal, color, scale);
	
      }
      catch(Exception e){
  	parse(offset, n,
  	      n.getCoordinateIndex(i), n.getNormalIndex(i), 
  	      -1, 
  	      vertex, normal, color, scale);
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
  			   float scale){
    n.getCoordinate(vertexIndex, coordinate);
    n.getNormal(normalIndex, normal);    
    if(colorIndex != -1){
      n.getColor(colorIndex, color);
    }
    for(int j=0; j<3; j++, offset.value++){
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
  
  /**
   * Parses texture coordinates
   */
  private final void parse(javax.media.j3d.GeometryArray n,
			   int texCount, int texIndex, short[][] mobileTex){
    int texSet = n.getTexCoordSetCount();
    int texMap = n.getTexCoordSetMapLength();
    //util.log("texSet: " + texSet + " texMap: " + texMap);
    int[] texMapArr = new int[texMap];
    n.getTexCoordSetMap(texMapArr);
    //     for(int i=0; i<texMapArr.length; i++){
    //       util.log("texMapArr i: " + i + " " + texMapArr[i]);
    //     }
      
    for(int i=0; i<texSet; i++){
      int id = texMapArr[i];
      n.getTextureCoordinate(id, texIndex, texCoord);
      for(int k=0; k<texCoord.length; k++, offsetTex.value++){
  	mobileTex[i][offsetTex.value] = (short)texCoord[k];
      }
    }

    /*
      for(int i=0; i<texCount; i++){
      n.getTextureCoordinate(i, texIndex, texCoord);
      for(int k=0; k<texCoord.length; k++, offsetTex.value++){
      mobileTex[i][offsetTex.value] = (short)texCoord[k];
      }
      }
    */
  }   

  /**
   * Gets the maximum offset from the origo for the meshes
   * for scaling purposes
   */
  private float parseMesh(List list){
    float min = 0, max = 0;

    Iterator meshItr = list.iterator();
    while(meshItr.hasNext()){
      TmpMesh tmpMesh = (TmpMesh)meshItr.next();

      Iterator geoItr = tmpMesh.geometry.iterator();
      while(geoItr.hasNext()){
	javax.media.j3d.GeometryArray garr = 
	  (javax.media.j3d.GeometryArray)geoItr.next();
	
	//Solve the maximun and minimun
	int numVertex = garr.getValidVertexCount();
	for(int i=0; i<numVertex; i++){
	  garr.getCoordinate(i, coordinate);
	  for(int j=0; j<coordinate.length; j++){
	    if(coordinate[j] < min){
	      min = coordinate[j];
	    }
	    if(coordinate[j] > max){
	      max = coordinate[j];
	    }
	  }
	}
      }
    }
    util.log("  min: " + min + "  max: " + max);
    min = Math.abs(min);
    if(min > max){
      max = min;
    }
    return max;
  }

  /**
   * Temp class that holds mesh related data until 
   * all the Shape3D objects have been received from 
   * the J3D interface so that coordinates can
   * be scaled and M3G meshes constructed 
   */
  private class TmpMesh{
    public Group group;
    private int index;
    public List geometry;
    public SubAppearance app;
    public int numVertices;
    public int numTextures;
    
    public TmpMesh(Group group, int index,
		   List geometry, int numVertices, int numTextures,
		   SubAppearance app){
      this.group = group;
      this.index = index;
      this.geometry = new Vector(geometry);
      this.app = app;
      this.numVertices = numVertices;
      this.numTextures = numTextures;
    }    
  }
}



