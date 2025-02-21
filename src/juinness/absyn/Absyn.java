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
import juinness.m3g.*;

import javax.microedition.m3g.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * The purpose of the class Abstract Syntax
 * is to act as an interface 
 * between the J3D and other scene graphs, such as M3G, so that the mapping 
 * between the different kinds of APIs can be done in an easy and clear way 
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public final class Absyn
{
  private Util util;
  private Map hash;
  private List data;

  public static final int INDEXEDTRIANGLEARRAY = 0;
  public static final int TRIANGLEARRAY = 1;

  /**
   * Constructs this
   */
  public Absyn(){
    util = Util.getInstance();
    hash = new HashMap();
    data = new Vector();
  }

  /**
   * Gets the constructed abstract syntax
   */
  public List getAbsyn(){
    return data;
  }

  /**
   * Indexes nodes so that they can be associated with each others
   */
  private void setNode(Object key, Object value){
    if(key == null){
      util.log("   BIZARRE set1: " + key);
      return;
    }    
    if(value == null){
      util.log("   BIZARRE set2: " + value);
      return;
    }    
    hash.put(key, value);
    data.add(value);
    //util.log("   SET: " + value + " FOR:" + key);
  }

  /**
   * Gets indexed node
   */
  private Object getNode(Object key){
    if(key == null){
      util.log("   BIZARRE get1: " + key);
      return null;
    }
    Object tst = hash.get(key);
    //util.log("   GET: " + tst + " FOR:" + key);
    return tst;
  }

  /**
   * Sets geometry node, that is, coordinates, normals and colors
   * the geometry is referenced
   */
  public void setGeometry(int id, Object n, int num, Object parent){
    Object tst = getNode(parent);
    AbsynMesh m = (AbsynMesh)tst;
    m.setGeometry(id, n, num);
  }

  public void setTexture(Object[] n, Object parent){
    Object tst = getNode(parent);
    AbsynMesh m = (AbsynMesh)tst;
    m.setTexture(n);
  }

  /**
   * Sets mesh node
   */
  public void setMesh(Object node, Object parent){
    Object tst = getNode(parent);
    AbsynGroup g = (AbsynGroup)tst;
    tst = getNode(node);
    if((tst = hash.get(node)) != null){
      util.log("   BIZARRE MESH");
      return;
    }
    AbsynMesh m = new AbsynMesh();
    m.setGroup(g);
    setNode(node, m);
    util.log("   setMesh: " + m);
  }


  /**
   * Sets group node
   */
  public void setGroup(Object node, Object parent){
    setGroup(node, parent, null);
  }

  /**
   * Sets group node with a matrix
   */
  public void setGroup(Object node, Object parent, float[] matrix){
    Object tst = null;
    AbsynGroup sg = new AbsynGroup();
    if(parent != null){
      tst = getNode(parent);

      if(matrix != null){
	sg.setMatrix(matrix);
      }
      AbsynGroup gp = (AbsynGroup)tst;
      sg.setParent(gp);
      util.log("   set parent: " + gp + "\n   for the child: " + sg);
    }
    else{
      util.log("   got the root node: " + sg);
    }
    setNode(node, sg);
  }
}
