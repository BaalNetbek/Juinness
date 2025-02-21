//Created 2004-11-19
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

package juinness.m3g;

import javax.microedition.m3g.VertexArray;

/**
 * SubVertexArray is a decodator for the VertexArray
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class SubVertexArray extends VertexArray implements Sub
{
  private int numVertices;
  private int numComponents;
  private int componentSize;
  private int encoding;
  private byte[] byteValues;
  private short[] shortValues;
  
  public SubVertexArray(int numVertices,
			int numComponents,
			int componentSize){
    super(numVertices, numComponents, componentSize);
    this.numVertices = numVertices;
    this.numComponents = numComponents;
    this.componentSize = componentSize;
  }

  public void set(int firstVertex,
		  int numVertices,
		  byte[] values){
    super.set(firstVertex, numVertices, values);
    this.byteValues = values;
  }

  public void set(int firstVertex,
		  int numVertices,
		  short[] values){
    super.set(firstVertex, numVertices, values);
    this.shortValues = values;
  }

  public int getObjectType(){
    return 20;
  }

  public int getComponentSize(){
    return componentSize;
  }

  public int getComponentCount(){
    return numComponents;
  }
  
  /**
   * Returns 0 or 1
   */
  public int getEncoding(){
    return encoding;
  }

  public int getVertexCount(){
    return numVertices;
  }

  public byte[] getByteValues(){
    return byteValues;
  }

  public short[] getInt16Values(){
    return shortValues;
  }
}
