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

import java.util.List;
import java.util.Vector;

/**
 * AbsynGroup is a wrapper for the concrete group associated with this
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class AbsynGroup
{
  /** Parent group associated with this */
  private AbsynGroup parent;

  /** Transformation matrix of this */
  private float[] matrix;

  /** Concrete group associated with this */
  private Object group;

  /**
   * Constructs an empty group
   */
  public AbsynGroup(){    
  }
  
  /**
   * Sets a parent for this
   */
  public void setParent(AbsynGroup parent){
    this.parent = parent;
  }

  /**
   * Gets the parent of this
   */
  public AbsynGroup getParent(){
    return parent;
  }

  /**
   * Sets the concrete group for this
   */
  public void setGroup(Object group){
    this.group = group;
  }

  /**
   * Gets the concrete group of this
   */
  public Object getGroup(){
    return group;
  }

  /**
   * Gets the transformation matrix of this 
   *
   * @return transformation matrix or null if matrix is not associated
   */ 
  public float[] getMatrix(){
    return matrix;
  }

  /**
   * Sets a transformation matrix for this 
   * the content of the matrix is copied
   *
   * @param matrix given matrix
   */
  public void setMatrix(float[] matrix){    
    this.matrix = new float[matrix.length];
    System.arraycopy(matrix, 0, this.matrix, 0, matrix.length);
  }
}
