//Created 2005-03-04
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

import juinness.util.*;

import java.lang.reflect.*;
import javax.media.j3d.*;
import java.util.*;
import java.awt.image.BufferedImage;
import com.sun.j3d.utils.geometry.*;

/**
 * WARNING THIS CLASS IS GENERATED
 * DO NOT EDIT THIS BECAUSE THIS CAN BE REGENERATED
 *
 * The <code>Traverser</code> class traverses Java 3D (J3D) scenegraph and 
 * passes it to the Translator
 *
 * @author Markus Yliker&auml;l&auml; and Maija Savolainen
 */
final class GeneratedTraverser
{
  private Util util = Util.getInstance();
  private GeneratedTranslator translator;

  private Class[] para;
  private Object[] args;
  private Class c;
  private Map hash;
  private Transform3D t3d;

  public GeneratedTraverser(){
    c = this.getClass();
    para = new Class[1];
    hash = new HashMap();
    for(int i=0; i<supported.length; i++){
      hash.put(supported[i], supported[i]);
    }
    args = new Object[1];
    t3d = new Transform3D();
  }

  /**
   * Traverser the J3D tree in depth-first order
   */
  public void traverse(GeneratedTranslator translator, SceneGraphObject root)
    throws Exception{

    this.translator = translator;

    Stack s = new Stack();
    s.push(root);

    while(s.isEmpty() == false){      
      util.log("\n-------------------------------------");
      
      SceneGraphObject n = (SceneGraphObject)s.pop();

      try{
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
    translator.end();
  }
 
  private void invokeMethod(Object n){
    try{
      if(n == null){
        return;
      }

      para[0] = n.getClass();
      if(hash.get(para[0].getName()) == null){
	//Not atleast a pure J3D API Object
        para[0] = para[0].getSuperclass();
	//Test if n is derived from J3D API
        if(hash.get(para[0].getName()) == null){
	  //Translation is not supported
          return;
        }  
      }  
      Method met = c.getMethod("parse", para);
      args[0] = n;
      met.invoke(this, args);
    }
    catch(Exception e){
      e.printStackTrace();
    }    
  }

  //<%GENERATE%>

  public void parse(javax.media.j3d.Texture n){
    translator.createTexture(n);
  }

  public void parse(javax.media.j3d.Appearance n){

    invokeMethod(n.getTexture());
    translator.createAppearance(n);
  }

  public void parse(javax.media.j3d.Shape3D n){

    Enumeration enu = n.getAllGeometries();
    while(enu.hasMoreElements()){
      invokeMethod(enu.nextElement());
    }

    invokeMethod(n.getAppearance());
    translator.createMesh(n);
  }

  public void parse(javax.media.j3d.TriangleArray n){
    translator.createVertexArray(n);
  }

  public void parse(javax.media.j3d.IndexedTriangleArray n){
    translator.createVertexArray(n);
  }

  public void parse(javax.media.j3d.Transform3D n){
  //t3d.get(matrix);
    translator.createTransform(n);
  }

  public void parse(javax.media.j3d.BranchGroup n){
    translator.createGroup(n);
  }

  public void parse(javax.media.j3d.TransformGroup n){

    n.getTransform(t3d);
    parse(t3d);
    translator.createGroup(n);
  }

  String[] supported = {"javax.media.j3d.Texture", "javax.media.j3d.Appearance", "javax.media.j3d.Shape3D", "javax.media.j3d.TriangleArray", "javax.media.j3d.IndexedTriangleArray", "javax.media.j3d.Transform3D", "javax.media.j3d.BranchGroup", "javax.media.j3d.TransformGroup", 
  };
}
