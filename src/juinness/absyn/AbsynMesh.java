//Created 2004-11-27
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

package juinness.absyn;

import juinness.util.*;

import javax.media.j3d.*;
import java.util.*;
import java.awt.image.BufferedImage;

/**
 * AbsynMesh is a wrapper for the mesh nodes
 *
 * Geometry associated with this can be described in an arbitrary format and
 * can further be retrieved in an arbitray format
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class AbsynMesh
{
  private Util util;

  /** Parent for this */
  private AbsynGroup group;

  /** Minimum value of all the coordinates */
  private float min;
  /** Maximum value of all the coordinates */
  private float max;

  /** Number of the vertices needed to hold the coordinates */
  private int vertexNum;

  /** Number of the coordinates needed to hold the textures */
  private int texNum;

  /** All the geometries associated with this */
  private List geometry;

  private float[] coordinate;
  private float[] normal;
  private byte[] color;
  private float[] texCoord;
  private MutableInteger offset;
  private MutableInteger offsetTex;

  private BufferedImage[] tex;

  /**
   * Constructs this
   */ 
  public AbsynMesh(){
    util = Util.getInstance();
    geometry = new Vector();
    offset = new MutableInteger(0);
    offsetTex = new MutableInteger(0);
    coordinate = new float[3];
    normal = new float[3];
    color = new byte[3];
    texCoord = new float[2];
  }

  /**
   * Sets the parent group of this
   */
  public void setGroup(AbsynGroup group){
    this.group = group;
  }

  /**
   * Gets the parent group of this
   */
  public AbsynGroup getGroup(){
    return group;
  }

  /**
   * Sets a geometry associated with this
   * Several geometries can be associated by 
   * calling this method several times
   */
  public void setGeometry(int id, Object n, int num){
    geometry.add(new MutableInteger(id));
    geometry.add(n);
    vertexNum += num;

    //Solve the maximun and minimun
    GeometryArray garr = (GeometryArray)n;    
    texNum += garr.getTexCoordSetCount();
    int numVertex = garr.getValidVertexCount();
    float[] coordinate = new float[3];
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

  /**
   * Gets all the geometries associated with this by
   * filling the given arrays with data in the requested form and 
   * scales all the vertices with the scale factor
   *
   * @param id format of the requested geometry
   * @param scale scale factor
   */
  public void getGeometry(int id, short[] vertex, short[] normal, 
			  byte[] color, short[][] texCoord, float scale)
  {
    offset.value = 0;
    switch(id){
    case Absyn.TRIANGLEARRAY:
      getTriangleArray(vertex, normal, color, texCoord, scale);
      break;
    default:
      util.log("AbsynMesh getGeometry Not implemented for: " + id);
      break;
    }
  }

  /**
   * Gets a triangleArray 
   */
  private void getTriangleArray(short[] vertex, short[] normal, 
				byte[] color, short[][] texCoord, float scale)
  {
    Iterator itr = geometry.iterator();
    while(itr.hasNext()){
      MutableInteger id = (MutableInteger)itr.next();
      Object n = itr.next();
      switch(id.value){
      case Absyn.INDEXEDTRIANGLEARRAY:
	parseTriangleArray((IndexedTriangleArray)n, 
			   vertex, normal, color, texCoord, scale);
	break;
      case Absyn.TRIANGLEARRAY:
	parseTriangleArray((TriangleArray)n, 
			   vertex, normal, color, texCoord, scale);
	break;
      default:
	util.log("AbsynMesh getTriangleArray Not implemented for: " + id);
	break;
      }
    }
  }

  /**
   * Parses triangleArray associated with this 
   * into concrete trianglearray
   */ 
  private void parseTriangleArray(TriangleArray n, 
				  short[] vertex, short[] normal, 
				  byte[] color, short[][] texCoord, 
				  float scale)
  {
    util.log("*AbsynMesh -> TriangleArray to TriangleArray");
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
  private void parseTriangleArray(IndexedTriangleArray n, 
				  short[] vertex, short[] normal, 
				  byte[] color, short[][] texCoord, float scale)
  {
    util.log("*AbsynMesh -> IndexedTriangleArray to TriangleArray");
    int numIndex = n.getValidIndexCount();
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
   * Parses texture coordinates
   */
  private final void parse(GeometryArray n,
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
   * Retrives the values
   */
  private final void parse(MutableInteger offset, GeometryArray n,
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
   * Gets the size of the coordinates, that is,
   * number of the coordinates times 3 (three float per one coordinate)
   */
  public int getNumVertices(){
    return vertexNum;
  }

  public int getTexCount(){
    return texNum;
  }

  /**
   * Gets the minimum value of all the coordinates
   */
  public float getMin(){
    return min;
  }

  /**
   * Gets the maximum value of all the coordinates
   */
  public float getMax(){
    return max;
  }

  public void setTexture(Object[] n){
    this.tex = (BufferedImage[])n;
  }

  public BufferedImage[] getTexture(){
    return tex;
  }
}
