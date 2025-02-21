//Created 2004-11-27
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
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package juinness.m3g;

import javax.microedition.m3g.Mesh;
import javax.microedition.m3g.Appearance;
import javax.microedition.m3g.VertexBuffer;
import javax.microedition.m3g.IndexBuffer;

/**
 * SubMesh is a decodator for the Mesh
 *
 * @author Markus Yliker�l� and Maija Savolainen
 */
public class SubMesh extends Mesh implements Sub
{  
  public SubMesh(VertexBuffer vertices, IndexBuffer[] submeshes, 
		 Appearance[] appearances){
    super(vertices, submeshes, appearances);
  } 
  
  public SubMesh(VertexBuffer vertices, IndexBuffer submesh, 
		 Appearance appearance){
    super(vertices, submesh, appearance);
  } 

  public int getObjectType(){
    return 14;
  }
}
