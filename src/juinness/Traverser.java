//Created 2004-10-28
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
import juinness.absyn.Absyn;

import javax.media.j3d.*;
import java.util.*;
import java.awt.image.BufferedImage;
import com.sun.j3d.utils.geometry.*;

/**
 * The <code>Traverser</code> class traverses Java 3D (J3D) scenegraph and 
 * translates it into abstract syntax
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class Traverser
{  
  private Util util;
  private Absyn absyn;

  private Transform3D t3d;
  private float[] matrix;

  /**
   * Constructs this
   */
  public Traverser(){
    util = Util.getInstance();
    t3d = new Transform3D();
    matrix = new float[16];
  }

  /**
   * Traverses the J3D scene graph in depth-first order and
   * translates its nodes into abstract syntax
   */
  public Absyn traverse(SceneGraphObject root)
    throws Exception
  {   
    Stack s = new Stack();
    absyn = new Absyn();
    s.push(root);
    String id;

    while(s.isEmpty() == false){      
      util.log("\n-------------------------------------");

      SceneGraphObject n = (SceneGraphObject)s.pop();
      // SceneGraphObject can be 
      // 1) Node
      // 2) NodeComponent
      if(n instanceof Node){
	// Node can be
	// 1) Group
	// 2) Leaf
	if(n instanceof Group){
	  util.log("Group: " + n);
	  Group g = (Group)n;
	  parse(g);
	  
	  for(int i=0; i<g.numChildren(); i++){
	    n = g.getChild(i);
	    s.push(n);
	  }		 
	}		
	else if(n instanceof Leaf){
	  util.log("Leaf: " + n);
	  Leaf leaf = (Leaf)n;
	  parse(leaf);	  
	}
      }
      else if(n instanceof NodeComponent){
	util.log("NodeComponent");
	NodeComponent nc = (NodeComponent)n;
	parse(nc);
      }
    }    
    return absyn;
  }

  private void parse(Group n){
    String id = "" + n.getClass();

    if(n instanceof BranchGroup){
      parse((BranchGroup)n);
    }
    else if(n instanceof OrderedGroup){
      util.log("Not implemented yet: " + id);
    }
    else if(n instanceof Primitive){
      util.log("Not implemented yet: " + id);
    }
    else if(n instanceof SharedGroup){
      util.log("Not implemented yet: " + id);
    }
    else if(n instanceof Switch){
      util.log("Not implemented yet: " + id);
    }
    else if(n instanceof TransformGroup){
      parse((TransformGroup)n);
    }
    else if(n instanceof ViewSpecificGroup){
      util.log("Not implemented yet: " + id);
    }
    else{
      util.log("Unknown: " + id);
    }
  }

  private void parse(BranchGroup n){
    util.log("  -> BranchGroup");
    absyn.setGroup(n, n.getParent());
  }

  private void parse(TransformGroup n){
    util.log("  -> TransformGroup");
    n.getTransform(t3d);
    t3d.get(matrix);
    absyn.setGroup(n, n.getParent(), matrix);
  }

  private void parse(Leaf n){
    String id = "" + n.getClass();

    if(n instanceof Text2D){
      util.log("* Text2D: " + id);
    } 
    else if(n instanceof AlternateAppearance){
      util.log("Not implemented yet: AlternateAppearance" + id);
    } 
    else if(n instanceof Background){
      util.log("Not implemented yet: Background " + id);
    } 
    else if(n instanceof Behavior){
      util.log("Not implemented yet: Behavior " + id);
    } 
    else if(n instanceof BoundingLeaf){
      util.log("Not implemented yet: BoundingLeaf" + id);
    } 
    else if(n instanceof Clip){
      util.log("Not implemented yet: Clip " + id);
    } 
    else if(n instanceof Fog){
      util.log("Not implemented yet: Fog " + id);
    } 
    else if(n instanceof Light){
      util.log("Not implemented yet: Light" + id);
    } 
    else if(n instanceof Link){
      util.log("Not implemented yet: Link" + id);
    } 
    else if(n instanceof ModelClip){
      util.log("Not implemented yet: ModelClip" + id);
    } 
    else if(n instanceof Morph){
      util.log("Not implemented yet: Morph" + id);
    } 
    else if(n instanceof Shape3D){
      parse((Shape3D)n);
    } 
    else if(n instanceof Sound){
      util.log("Not implemented yet: Sound" + id);
    } 
    else if(n instanceof Soundscape){
      util.log("Not implemented yet: Soundscape" + id);
    } 
    else if(n instanceof ViewPlatform){
      util.log("Not implemented yet: ViewPlatform" + id);
    } 
    else{
      util.log("Unknown: " + id);
    }
  }

  private void parse(Shape3D n){
    util.log("* Shape3D");

    absyn.setMesh(n, n.getParent());
    
    Enumeration enu = n.getAllGeometries();
    while(enu.hasMoreElements()){
      Geometry geom = (Geometry)enu.nextElement();
      String id = "" + geom.getClass();

      if(geom instanceof CompressedGeometry){
	util.log("   Not implemented yet: CompressedGeometry " + id);
      }
      else if(geom instanceof GeometryArray){
	parse((GeometryArray)geom, n);
      }
      else if(geom instanceof Raster){
	util.log("   Not implemented yet: Raster " + id);
      }
      else if(geom instanceof Text3D){
	util.log("   Not implemented yet: Text3D " + id);
      }
      else{
	util.log("Unknown: " + id);
      }
    }

    parse(n.getAppearance(), n);
  }

  private void parse(GeometryArray n, Shape3D shape){
    String id = "" + n.getClass();
    if(n instanceof GeometryStripArray){
      util.log("    Not implemented yet: GeometryStripArray " + id);
    }
    else if(n instanceof IndexedGeometryArray){
      parse((IndexedGeometryArray)n, shape);
    }
    else if(n instanceof LineArray){
      util.log("    Not implemented yet: LineArray " + id);
    }
    else if(n instanceof PointArray){
      util.log("    Not implemented yet: PointArray " + id);
    }
    else if(n instanceof QuadArray){
      util.log("    Not implemented yet: QuadArray " + id);
    }
    else if(n instanceof TriangleArray){
      parse((TriangleArray)n, shape);
    }
    else{
      util.log("Unknown: " + id);
    }
  }

  private void parse(IndexedGeometryArray n, Shape3D shape){
    String id = "" + n.getClass();
    if(n instanceof IndexedGeometryStripArray){
      util.log("    Not implemented yet: IndexedGeometryStripArray " + id);
    }
    else if(n instanceof IndexedLineArray){
      util.log("    Not implemented yet: IndexedLineArray " + id);
    }
    else if(n instanceof IndexedPointArray){
      util.log("    Not implemented yet: IndexedPointArray " + id);
    }
    else if(n instanceof IndexedQuadArray){
      util.log("    Not implemented yet: IndexedQuadArray " + id);
    }
    else if(n instanceof IndexedTriangleArray){
      parse((IndexedTriangleArray)n, shape);
    }
    else{
      util.log("Unknown: " + id);
    }
  }

  private void parse(IndexedTriangleArray n, Shape3D shape){
    util.log("  -> IndexedTriangleArray: " + n);
    int num = n.getValidIndexCount();
    absyn.setGeometry(Absyn.INDEXEDTRIANGLEARRAY, n, num, shape);
  }

  private void parse(TriangleArray n, Shape3D shape){
    util.log("  -> TriangleArray: " + n);
    int num = n.getVertexCount();
    absyn.setGeometry(Absyn.TRIANGLEARRAY, n, num, shape);
  }

  private void parse(Appearance n, Shape3D shape){
    util.log("  -> Appearance: " + n);

    Texture tex = n.getTexture();
    if(tex == null){
      return;
    }
    parse(tex, n, shape);
  }
  
  private void parse(Texture n, Appearance app, Shape3D shape){
    util.log("  -> Texture: " + n);
    ImageComponent[] img = n.getImages();
    BufferedImage[] bufImg = null;
    if(img.length > 0){
      bufImg = new BufferedImage[img.length];
    }
    util.log("textures: " + img.length);
    for(int i=0; i<img.length; i++){
      int format = img[i].getFormat();    
      int width = img[i].getWidth();
      int height = img[i].getHeight();
      
      util.log("format: " + format);
      util.log("width: " + width);
      util.log("height: " + height);

      switch(format){
      case ImageComponent.FORMAT_RGB:
	util.log("  FORMAT_RGB");
	break;
      case ImageComponent.FORMAT_RGBA:
	util.log("  FORMAT_RGBA");
	break;
      default:
	util.log("  Unknown image format: " + format);
	break;
      }
      
      if(img[i] instanceof ImageComponent2D){
	util.log("  ImageComponent2D");
	ImageComponent2D img2D = (ImageComponent2D)img[i];
	BufferedImage buf = img2D.getImage();

	//Create BufferedImage for the image
	bufImg[i] = 
	  new BufferedImage(width, 
			    height, 
			    BufferedImage.TYPE_BYTE_INDEXED);
	bufImg[i].setData(buf.getData());      
      }
      else if(img[i] instanceof ImageComponent3D){
	util.log("  ImageComponent3D");
	ImageComponent3D img3D = (ImageComponent3D)img[i];
	BufferedImage[] buf = img3D.getImage();
      }
      else{
	util.log("  Unknown image");
      }
    }
    if(img.length > 0){
      absyn.setTexture(bufImg, shape);
    }
  }

  private void parse(NodeComponent n){
    String id = "" + n.getClass();
    util.log("    Not implemented yet: NodeComponent " + id);
  }
}


