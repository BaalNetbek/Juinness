//Created 2004-11-12
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

import juinness.m3g.*;
import juinness.util.*;

import javax.microedition.m3g.*; 
import java.util.*;
import java.awt.image.BufferedImage;

/**
 * The <code>Translator</code> class 
 * translates J3D scenegraph into M3G scenegraph 
 * 
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class Translator
{
  private List list = new Vector();
  private Util log = Util.getInstance();
  private Traverser traverser;

  private float[] bias;
  private Transform transform;
  private List meshList;
  private List meshComponentList = new Vector();
  private float[] matrix = new float[16];
  private boolean hasMatrix;
  private List data = new Vector();
  private Map gHash = new HashMap();
  private Group subGroup;
  private SubAppearance appearance;
  private BufferedImage[] bufImg;

  /** Number of the vertices needed to hold the coordinates */
  private int numVertices;

  /** Number of the coordinates needed to hold the textures */
  private int numTextures;

  /** All the geometries associated with the next mesh */
  private List geometry;

  /**
   * Constructs this
   */ 
  public Translator(){
    transform = new Transform();
    meshList = new Vector();    
    bias = new float[3];
    for(int i=0; i<bias.length; i++){
      bias[i] = 0.0f;
    }

    geometry = new Vector();
  }

  public List translate(javax.media.j3d.SceneGraphObject j3dSceneGraph)
    throws Exception
  {
    traverser = new Traverser();
    traverser.traverse(this, j3dSceneGraph);
    return list;
  }

  private void addM3G(Object obj){
    data.add(obj);
  }

  public void createGroup(javax.media.j3d.BranchGroup n){
    createGroup((javax.media.j3d.Group)n);
  }

  public void createGroup(javax.media.j3d.TransformGroup n){
    traverser.parse(n);
    createGroup((javax.media.j3d.Group)n);
  }

  public void createTransform(javax.media.j3d.Transform3D n){
    n.get(matrix);
    hasMatrix = true;
  }
  
  public void createVertexArray(javax.media.j3d.TriangleArray n){
    numVertices += n.getVertexCount();
    createVertexArray((javax.media.j3d.GeometryArray)n);
  }

  public void createVertexArray(javax.media.j3d.IndexedTriangleArray n){
    numVertices += n.getValidIndexCount();
    createVertexArray((javax.media.j3d.GeometryArray)n);
  }

  public void createVertexArray(javax.media.j3d.QuadArray n){
    numVertices += n.getVertexCount();
    createVertexArray((javax.media.j3d.GeometryArray)n);
  }

  public void createVertexArray(javax.media.j3d.GeometryArray n){
    numTextures += n.getTexCoordSetCount();
    geometry.add(n);
  }
  
  public void createMesh(javax.media.j3d.Shape3D n){
    traverser.parse(n);
    log.logMsg("CREATE TMP MESH: " + bufImg + " " + numTextures);
    //4. Creating the Mesh    
    TmpMesh mesh = new TmpMesh(subGroup, list.size(), 
			       geometry, numVertices, numTextures, appearance,
			       bufImg);
    meshList.add(mesh);
    geometry.clear();
    numVertices = 0;
    numTextures = 0;

    addM3G(mesh);
  }

  public void createAppearance(javax.media.j3d.Appearance n){
    traverser.parse(n);
    //3. Appearance
    appearance = new SubAppearance(); 
    //list.add(0, appearance);
  }

  public void createTexture(javax.media.j3d.Texture n){
    log.logMsg("createTexture");
    bufImg = traverser.parse(n);    
  }

  public void createMaterial(javax.media.j3d.Material n){
    log.logMsg("NOT IMPLEMENTED YET: Material");
  }

  private void createGroup(javax.media.j3d.Group n){
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
	//log.showMatrix(matrix);
	transform.set(matrix);
	subGroup.setTransform(transform);
      }
      
      Group gp = (Group)gHash.get(n.getParent());
      gp.addChild(subGroup);

      addM3G(subGroup);
    }
  }

  public void end(){
    log.logMsg("READY TO CREATE MESHES numberOfMesh: " + meshList.size());

    //Solve the scaling factor
    //Scale to the upper limits of the 16-bit two's complement 
    //(signed short, two bytes -32768...+32767) so that we wont lose data
    //Later the inverse of this scaling factor is utilized when 
    //the meshes are constructed
    log.log("\n++++++++++++++++++++++++++++++++++");
    float max = parseMesh(meshList);
    log.log("GOT MAX: " + max);
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
	//Create 16bit meshes
	createMesh(tmpMesh, factor, scale, bias, 2);
      }
      else{
	list.add(0, obj);
      }
    }

    log.logMsg(list);
  }

  private void createMesh(TmpMesh tmpMesh, float factor, float scale, 
			  float[] bias, int componentSize){

    try{
      log.log("We are creating a Mesh Object from: " + tmpMesh);

      //Retrieve the mesh data
      int numVertices = tmpMesh.numVertices;
      log.log("  numVertices: " + numVertices);
      short[] mobileVertex = new short[numVertices*3];
      short[] mobileNormal = new short[numVertices*3];
      byte[] mobileColor = new byte[numVertices*3];
      int texCount = tmpMesh.numTextures;
      short[][] mobileTex = new short[texCount][numVertices*2];    

      if(tmpMesh.geometry.isEmpty()){
	System.err.println("NO SUPPORTED GEOMETRY ASSOCIATED WITH THIS MESH");
	return;
      }
      
      traverser.parseGeometry(Traverser.TRIANGLE_STRIP_ARRAY, 
			      tmpMesh.geometry.iterator(),
			      mobileVertex, mobileNormal, 
			      mobileColor, mobileTex, factor);
      
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
      meshComponentList.add(positions);	
    
      //Normal vectors must have 3 components.
      VertexArray normals = new SubVertexArray(numVertices, 3, componentSize);
      normals.set(0, numVertices, mobileNormal);
      vertexBuf.setNormals(normals);
      meshComponentList.add(normals);	

      //Colors must have 3 or 4 components, one byte each.
      VertexArray colors = new SubVertexArray(numVertices, 3, 1);
      colors.set(0, numVertices, mobileColor);
      vertexBuf.setColors(colors);
      meshComponentList.add(colors);
	
      //Texture coordinates must have 2 or 3 components.
      float texScale = 1.0f;
      float[] texBias = new float[3];
      texBias[0] = 1.0f;
      texBias[1] = 1.0f;
      texBias[2] = 0.0f;
      for(int i=0; i<texCount; i++){
	log.log("texCount: " + i + "/" + texCount);
	VertexArray texCoord = 
	  new SubVertexArray(numVertices, 2, componentSize);
	texCoord.set(0, numVertices, mobileTex[i]);
	vertexBuf.setTexCoords(i, texCoord, texScale, texBias);
	meshComponentList.add(texCoord);	
      }
      meshComponentList.add(vertexBuf);

      ///2. IndexBuffer			
      int[] stripLengths = new int[numVertices/3]; 
      for(int i=0; i<stripLengths.length; i++){
	stripLengths[i] = 3; 
      }
        
      //Create Implicit TriangleStripArray
      IndexBuffer indexBuf = 
	new SubTriangleStripArray(0, stripLengths, componentSize); 
      meshComponentList.add(indexBuf);	

      //Also this is possible
      //Create Explicit TriangleStripArray
      //IndexBuffer indexBuf = 
      //new SubTriangleStripArray(index, stripLengths, componentSize); 
      
      //3. Appearance
      SubAppearance appearance = new SubAppearance(); 

      BufferedImage[] bufImg = tmpMesh.bufImg;
      log.logMsg("\nTST TEXTURE: " + bufImg + "  texCount:" + texCount + "\n");
      if(bufImg != null){
      	for(int i=0; i<texCount; i++){
	  log.logMsg("ATTACH TEXTURE: " + i);
      	  SubImage2D subImg = null;
      	  SubTexture2D tex = null;
      	  Material mat = null;

      	  byte[] palette = log.getPalette(bufImg[i]);
      	  byte[] pixels = log.getPixels(bufImg[i]);
      	  subImg = new SubImage2D(Image2D.RGB, 
      				  bufImg[i].getWidth(),
      				  bufImg[i].getHeight(), 
      				  pixels,
      				  palette);
      	  meshComponentList.add(subImg);
      
      	  //SubTexture2D tex = new SubTexture2D(subImg);
      	  tex = new SubTexture2D();
      	  tex.setImage(subImg);
      
      	  appearance.setTexture(i, tex);
      	  meshComponentList.add(tex);
      
      	  mat = new SubMaterial();
      	  appearance.setMaterial(mat);
      	  meshComponentList.add(mat);
      	}
      }
      
      meshComponentList.add(appearance);

      //4. Creating the Mesh
      Mesh mesh = new SubMesh(vertexBuf, indexBuf, appearance);
    
      //Add the mesh into group
      //SubGroup m3gGroup = (SubGroup)absGroup.getGroup();
      Group m3gGroup = tmpMesh.group;
      m3gGroup.addChild(mesh);
      meshComponentList.add(mesh);
      
      for(int i=meshComponentList.size()-1; i>-1; i--){
	list.add(0, meshComponentList.get(i));
      }
      meshComponentList.clear();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * Gets the maximum offset from the origo for the meshes
   * for scaling purposes
   */
  private float parseMesh(List list){
    float min = 0, max = 0;

    float[] coordinate = new float[3];
    
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
    log.log("  min: " + min + "  max: " + max);
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
    public BufferedImage[] bufImg;
    
    public TmpMesh(Group group, int index,
		   List geometry, int numVertices, int numTextures,
		   SubAppearance app, BufferedImage[] bufImg){
      this.group = group;
      this.index = index;
      this.geometry = new Vector(geometry);
      this.app = app;
      this.numVertices = numVertices;
      this.numTextures = numTextures;
      this.bufImg = bufImg;
    }    
  }
}



