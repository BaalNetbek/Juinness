//Created 2004-11-19
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
//http://www.gnu.org/copyleft/gpl.html

package juinness.util;

import juinness.Importer;
import juinness.m3g.*;

import java.awt.image.BufferedImage;
import java.util.List;
import javax.microedition.m3g.*;
import javax.media.j3d.BranchGroup;
import java.io.*;
import java.util.*;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.Loader;
import com.sun.j3d.utils.image.TextureLoader;
import java.awt.*;

/**
 * This class is for developing purposes only
 *
 * @author Markus Yliker&auml;l&auml; and Maija Savolainen
 */
public class FakeGlue
{
  private Util util;

  public FakeGlue(){
    util = Util.getInstance();
  }

  /**
   * Constructs the loaders with the XML-file
   *
   * See the juinness.Importer API if you want to use 
   * the other possibility to construct the loaders
   */
  public Importer getImporter(String loaderLocation)
    throws Exception
  {
    Importer importer = new Importer();
    importer.setLoader(loaderLocation);
    return importer;
  }
  

  /** Creates a test geometry instead of coffeepot */
  public void getFakeModel(javax.media.j3d.SceneGraphObject root){
    Stack s = new Stack();
    s.push(root);
    
    javax.media.j3d.Texture tex = 
      loadTexture(".." + java.io.File.separator +  "models" + 
		  java.io.File.separator +"brick_128_128.jpg", 
		  new java.awt.Label());

    while(s.isEmpty() == false){      
      try{      
	javax.media.j3d.SceneGraphObject n = 
	  (javax.media.j3d.SceneGraphObject)s.pop();
        if(n instanceof javax.media.j3d.Group){
          javax.media.j3d.Group g = (javax.media.j3d.Group)n;
	  for(int i=0; i<g.numChildren(); i++){
	    s.push(g.getChild(i));
	  }
        }
	else if(n instanceof javax.media.j3d.Shape3D){
	  javax.media.j3d.Shape3D sh = (javax.media.j3d.Shape3D)n;
	  if(sh.getAppearance() != null){
	    if(sh.getAppearance().getTexture() == null){
	      sh.getAppearance().setTexture(tex);
	    }
	  }
	  else{
	    javax.media.j3d.Appearance app = new javax.media.j3d.Appearance();
	    app.setTexture(tex);
	    sh.setAppearance(app);
	  }
	}
      }
      catch(Exception e){
        e.printStackTrace();	
      }
    }
  }
  
  private javax.media.j3d.Texture loadTexture(String location, Component observer){
    TextureLoader texLoader = new TextureLoader(location, observer);
    javax.media.j3d.Texture tex = texLoader.getTexture();
    return tex;
  }


  /**
   * Adds some additional M3G-objects into M3G scene graph
   */
  public void warp(List jm3dSceneGraph, String imgPath){
    util.log("\n###################################");
    SubImage2D subImg = null;
    if(imgPath != null){
      BufferedImage bufImg = util.getImage(imgPath);
      util.log("Loaded img: " + bufImg + " type: " + bufImg.getType());
      byte[] palette = util.getPalette(bufImg);
      byte[] pixels = util.getPixels(bufImg);
      subImg = new SubImage2D(Image2D.RGB, 
			      bufImg.getWidth(),
			      bufImg.getHeight(), 
			      pixels,
			      palette);
    }
    //The additional nodes:
    Object3D[] objArr = {
      new SubCamera(),
      new SubLight(),
      new SubBackground(),
      subImg,
    };

    Camera camera = null;
    Light light = null;
    Image2D img = null;
    Background bg = null;
    World world = (World)jm3dSceneGraph.get(jm3dSceneGraph.size()-1);
    MutableInteger typeID = new MutableInteger(0);

    //Add the additional M3G-objects 
    for(int i=0; i<objArr.length; i++){
      //Insert the additional M3G-objects into M3G (JM3D) scenegraph
      if(objArr[i] == null){
	continue;
      }

      Sub obj = (Sub)objArr[i];
      jm3dSceneGraph.add(0, obj);
      
      //Assign the references for the additional M3G-objects 
      typeID.value = obj.getObjectType();
      switch(typeID.value){
      case 4:
	bg = (Background)obj;	  
	if(img != null){
	  bg.setImage(img);
	}
	else{
	  bg.setColor(0x00FF0000);
	}
	world.setBackground(bg);    
	break;
      case 5:	  
	camera = (Camera)obj;
	world.addChild(camera);
	world.setActiveCamera(camera);	
	break;
      case 10:	  
	img = (Image2D)obj;
	if(bg != null){
	  bg.setImage(img);
	}
	break;
      case 22:
	world = (World)obj;
	break;
      }
    }
  }
}
