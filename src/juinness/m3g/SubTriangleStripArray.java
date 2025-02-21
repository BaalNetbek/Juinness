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

import javax.microedition.m3g.TriangleStripArray;

/**
 * SubTriangleStripArray is a decodator for the TriangleStripArray
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class SubTriangleStripArray extends TriangleStripArray implements Sub
{
  private int firstIndex;
  private int[] stripLengths;
  private int[] indices;
  private int encoding;

  public SubTriangleStripArray(int[] indices, int[] stripLengths, 
			       int componentSize){
    super(indices, stripLengths);
    if(componentSize < 0 || componentSize > 2){
      throw new IllegalArgumentException("componentSize must be " +
					 "0, 1 or 2 but not " + componentSize);
    }
    this.indices = indices;
    this.stripLengths = stripLengths;
    //Explicit the bit7 is one
    this.encoding = 1 << 7;
    this.encoding += componentSize;
    //System.err.println("Constructed Explicit TriangleStripArray");
  }
    
  public SubTriangleStripArray(int firstIndex, int[] stripLengths,
			       int componentSize){
    super(firstIndex, stripLengths);
    if(componentSize < 0 || componentSize > 2){
      throw new IllegalArgumentException("componentSize must be " +
					 "0, 1 or 2 but not " + componentSize);
    }
    this.firstIndex = firstIndex;
    this.stripLengths = stripLengths;
    //Implicit the bit7 is zero
    this.encoding = componentSize;
    //System.err.println("Constructed Implicit TriangleStripArray\n");
  }

  public int getObjectType(){
    return 11;
  }

  public int getStartIndex(){
    return firstIndex;
  }

  public int[] getStripLengths(){
    return stripLengths;
  }

  public int[] getIndices(){
    return indices;
  }
  
  /**
   * Returns 0 for implicit and 1 for explicit
   */
  public int getEncoding(){
    return encoding;
  }
}
