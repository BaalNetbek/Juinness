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

import juinness.absyn.*;
import juinness.m3g.*;
import juinness.util.*;

import javax.microedition.m3g.*; 
import java.util.*;
import java.awt.image.BufferedImage;

/**
 * The <code>Translator</code> class 
 * translates abstract syntax into M3G scenegraph
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class Translator
{
  private Util util;
  private float[] bias;
  private Transform transform;
  private List meshList;
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
  }

  /**
   * Translates the abstract syntax into M3G scene graph
   * consisting of M3G subobjects
   *
   * @return M3G objects in such an order that World is the last object
   */
  public List translate(Absyn absyn)
  {
    List data = absyn.getAbsyn();
    List list = new Vector();
    List mesh = new Vector();

    //Get the meshes so that they can be scaled uniformly
    Iterator itr = data.iterator();
    while(itr.hasNext()){
      Object value = itr.next();
      if(value instanceof AbsynMesh){
	mesh.add(value);
      }
    }

    //Solve the scaling factor
    //Scale to the upper limits of the 16-bit two's complement 
    //(signed short, two bytes -32768...+32767) so that we wont lose data
    //Later the inverse of this scaling factor is utilized when 
    //the meshes are constructed
    util.log("\n++++++++++++++++++++++++++++++++++");
    float max = parseMesh(mesh);
    //scaling factor
    float factor = 32767/max;    
    //inverse of the scaling factor
    float scale = max/32767;

    //Create the M3G scene graph
    itr = data.iterator();
    while(itr.hasNext()){
      util.log("\n++++++++++++++++++++++++++++++++++");
      Object value = itr.next();
      util.log("  value: " + value);

      //Scale all of the meshes uniformly
      if(value instanceof AbsynMesh){
	//Create 16bit meshes
	createMesh((AbsynMesh)value, factor, scale, bias, list, 2);
      }
      else if(value instanceof AbsynGroup){
	createGroup((AbsynGroup)value, list);
      }
      else{
	util.log("  Translator got Invalid value: " + value);
      }
    }
    return list;
  }

  /**
   * Creates the M3G group node also the world is a group node
   */
  private void createGroup(AbsynGroup absGroup, List list){
    AbsynGroup parent;
    if((parent = absGroup.getParent()) != null){
      util.log("We are creating a Group Object");    
      SubGroup sg = new SubGroup();
      absGroup.setGroup(sg);
      float[] matrix;
      if((matrix = absGroup.getMatrix()) != null){
	//identity matrix is not supposed 
	sg.getTransform(transform);
	util.showMatrix(matrix);
	transform.set(matrix);
	sg.setTransform(transform);
      }
      Group gp = (Group)parent.getGroup();
      gp.addChild(sg);
      list.add(0, sg);
    }
    else{
      util.log("We are creating a World Object");    
      //The root is the world
      SubWorld subWorld = new SubWorld();
      absGroup.setGroup(subWorld);
      list.add(0, subWorld);
    }
  }

  /**
   * Creates the M3G mesh node
   */
  private Mesh createMesh(AbsynMesh absMesh, float factor, float scale, 
			  float[] bias, List list, int componentSize){
    try{
    util.log("We are creating a Mesh Object");
    meshList.clear();

    //Retrieve the mesh data
    int numVertices = absMesh.getNumVertices();
    short[] mobileVertex = new short[numVertices*3];
    short[] mobileNormal = new short[numVertices*3];
    byte[] mobileColor = new byte[numVertices*3];
    int texCount = absMesh.getTexCount();
    short[][] mobileTex = new short[texCount][numVertices*2];    
    absMesh.getGeometry(Absyn.TRIANGLEARRAY, mobileVertex, mobileNormal, 
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
    BufferedImage[] bufImg = absMesh.getTexture();
    if(bufImg != null)
    for(int i=0; i<texCount; i++){
      SubImage2D subImg = null;
      SubTexture2D tex = null;
      Material mat = null;

      byte[] palette = util.getPalette(bufImg[i]);
      byte[] pixels = util.getPixels(bufImg[i]);
      subImg = new SubImage2D(Image2D.RGB, 
			      bufImg[i].getWidth(),
			      bufImg[i].getHeight(), 
			      pixels,
			      palette);
      meshList.add(subImg);
      
      //SubTexture2D tex = new SubTexture2D(subImg);
      tex = new SubTexture2D();
      tex.setImage(subImg);
      
      appearance.setTexture(i, tex);
      meshList.add(tex);
      
      mat = new SubMaterial();
      appearance.setMaterial(mat);
      meshList.add(mat);
    }
    meshList.add(appearance);

    //4. Creating the Mesh
    Mesh mesh = new SubMesh(vertexBuf, indexBuf, appearance);
    
    //Add the mesh into group
    AbsynGroup absGroup = absMesh.getGroup();        
    //SubGroup m3gGroup = (SubGroup)absGroup.getGroup();
    Group m3gGroup = (Group)absGroup.getGroup();
    m3gGroup.addChild(mesh);
    meshList.add(mesh);

    for(int i=meshList.size()-1; i>-1; i--){
      list.add(0, meshList.get(i));
    }

    return mesh;
    }
    catch(Exception e){
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Gets the maximum offset from the origo for the meshes
   * for scaling purposes
   */
  private float parseMesh(List list){
    util.log("ParseMesh: " + list);
    Object[] arr = list.toArray();
    float min = 0, max = 0;
    for(int i=0; i<arr.length; i++){
      AbsynMesh m = (AbsynMesh)arr[i];
      if(m.getMin() < min){
	min = m.getMin();
      }
      if(m.getMax() > max){
	max = m.getMax();
      }      
    }
    util.log("  min: " + min + "  max: " + max);
    min = Math.abs(min);
    if(min > max){
      max = min;
    }
    return max;
  }
}



