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
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package juinness.util;

import juinness.Importer;

import juinness.m3g.*;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.microedition.m3g.*;

import java.io.*;
import java.util.*;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.Loader;

/**
 * This class is for developing purposes only
 *
 * @author Markus Ylikerälä and Maija Savolainen
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

  /**
   * Adds some additional M3G-objects into M3G scene graph
   */
  public void warp(List jm3dSceneGraph, String imgPath){
    util.log("\n###################################");
    SubImage2D subImg = null;
    if(imgPath != null){
      BufferedImage bufImg = util.getImage(imgPath);
      util.log("Loaded img: " + bufImg);
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
