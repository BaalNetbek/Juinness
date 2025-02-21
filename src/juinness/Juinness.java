//Created 2004-10-27
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

package juinness;

import juinness.util.*;

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
    Util log = Util.getInstance();
    log.setLog("_expo_tst.txt");
  
    //Import arbitrary model from the location 
    //The vrml loader that the Importer utilizes blocks the command prompt 
    //this is probably due some graphical components it uses
    //The vrml loader cannot also load several models but 
    //a new vrml loader must be constructed for each model
    FakeGlue fake = new FakeGlue();
    Importer importer = fake.getImporter(loaderLocation);
    Scene scene = importer.load(location);
    BranchGroup model = scene.getSceneGroup();

    fake.getFakeModel(model);

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

    //Translate J3D to M3G
    Translator translator = new Translator();
    List jm3dSceneGraph = translator.translate(j3dSceneGraph);

    //Add some additional M3G-objects e.g. camera, lights, background
    //When all of the mappings between the J3D API and M3G API
    //are done this can be deprecated
    //until then it is also possible to create the missing parts 
    //in the M3G application    
    fake.warp(jm3dSceneGraph, imgPath);

    //Export scenegraph into the M3G file 
    Exporter exp = new Exporter();
    exp.export(jm3dSceneGraph, path);

    //Some loaders can jam this so:
    System.exit(0);
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
			   "\ne.g. java Juinness ../models/apina.wrl 1 " +
			   "..\\m3g\\test.m3g ../models/dogtowel.jpg " +
			   "../res/loader.xml");
	System.exit(1);
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }  
}
