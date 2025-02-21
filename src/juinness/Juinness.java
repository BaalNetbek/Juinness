//Created 2004-10-27
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

import javax.media.j3d.BranchGroup;
import java.util.List;
import com.sun.j3d.loaders.Scene;

/**
 * The <code>Juinness</code> class imports arbitrary 3D models and 
 * exports them currently into M3G file format
 *
 * @see Importer
 * @see Exporter
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class Juinness
{
  /**
   * Imports a model from the location and exports it to the path
   *
   * @param location location of the model
   * @param show if true shows the model in Java 3D application 
   * otherwise exports the model
   * @param path destination for the M3G file
   * @param imgPath image for the background for development purposes
   */
  public Juinness(String location, boolean show, String path, String imgPath,
		  String loaderLocation)
    throws Exception
  {
    Util util = Util.getInstance();
    util.setLog("_expo_tst.txt");
  
    //Import arbitrary model from the location 
    //The vrml loader that the Importer utilizes blocks the command prompt 
    //this is probably due some graphical components it uses
    //The vrml loader cannot also load several models but 
    //a new vrml loader must be constructed for each model
    FakeGlue fake = new FakeGlue();
    Importer importer = fake.getImporter(loaderLocation);
    Scene scene = importer.load(location);
    BranchGroup model = scene.getSceneGroup();

    //We could also create a more complex scenegraph programmatically
    //e.g. j3dSceneGraph = createSceneGraph(model);
    //but until further notice we utilize 
    //just the loaded model for development purposes
    BranchGroup j3dSceneGraph = model;    

    //The following block is used only with launch_Juinness.bat
    if(show){
      GUI gui = new GUI();
      j3dSceneGraph = gui.createSceneGraph(model, null);          
      gui.showGUI(j3dSceneGraph, model);
      return;
    }      

    //Create abstract syntax
    Traverser traverser = new Traverser();
    Absyn absyn = traverser.traverse(j3dSceneGraph);

    //Translate the J3D scenegraph to JM3D scenegraph
    Translator trans = new Translator();
    List jm3dSceneGraph = trans.translate(absyn);

    //Add some additional M3G-objects e.g. camera, lights, background
    //When all of the mappings between the J3D API and M3G API
    //are done this can be deprecated
    //until then it is also possible to create the missing parts 
    //in the M3G application    
    fake.warp(jm3dSceneGraph, imgPath);

    //Export scenegraph into the M3G file 
    Exporter exp = new Exporter();
    exp.export(jm3dSceneGraph, path);
  }
  
  /**
   * The usage: 
   * java Juinness 
   * modelLocation showGUI outputPath imageLocation loaderLocation
   */
  public static void main(String args[])
  {    
    try{
      if(args.length == 5){
	boolean show = args[1].equals("0") ? false : true;
	new Juinness(args[0], show, args[2], args[3], args[4]);
      }
      else{
	System.err.println("\nNote the usage: " +
			   "java Juinness modelLocation showGUI " +
			   "outputPath imageLocation loaderLocation" +
			   "\ne.g. java Juinness ../models/coffeepot.wrl 1 " +
			   "..\\m3g\\test.m3g ../models/dogtowel.jpg " +
			   "../models/loader.xml");
	System.exit(1);
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }  
}
