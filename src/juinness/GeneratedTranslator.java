//Created 2005-03-06
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
 * @author Markus Yliker&auml;l&auml; and Maija Savolainen
 */
abstract class GeneratedTranslator
{
  protected List list = new Vector();
  protected Util log = Util.getInstance();

  public void end(){
  }

  public List translate(){
    return list;
  }

  //<%GENERATE%>

  void createTexture(javax.media.j3d.Texture n){
   log.logMsg("J3D->M3G javax.media.j3d.Texture");
  }

  void createAppearance(javax.media.j3d.Appearance n){
   log.logMsg("J3D->M3G javax.media.j3d.Appearance");
  }

  void createMesh(javax.media.j3d.Shape3D n){
   log.logMsg("J3D->M3G javax.media.j3d.Shape3D");
  }

  void createVertexArray(javax.media.j3d.TriangleArray n){
   log.logMsg("J3D->M3G javax.media.j3d.TriangleArray");
  }

  void createVertexArray(javax.media.j3d.IndexedTriangleArray n){
   log.logMsg("J3D->M3G javax.media.j3d.IndexedTriangleArray");
  }

  void createTransform(javax.media.j3d.Transform3D n){
   log.logMsg("J3D->M3G javax.media.j3d.Transform3D");
  }

  void createGroup(javax.media.j3d.BranchGroup n){
   log.logMsg("J3D->M3G javax.media.j3d.BranchGroup");
  }

  void createGroup(javax.media.j3d.TransformGroup n){
   log.logMsg("J3D->M3G javax.media.j3d.TransformGroup");
  }
}
