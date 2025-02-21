//Created 2004-09-24
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

import juinness.m3g.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import com.sun.j3d.utils.geometry.*;

/**
 * This is for development purposes only
 *
 * Creates Graphical User Interface (GUI) for the J3D SceneGraph
 *
 * @author Markus Yliker&auml;l&auml; and Maija Savolainen
 */
public class GUI extends JPanel implements ActionListener
{
  /** cmd for the file chooser */
  private static final String CMD_OPEN = "a";

  /** bounds for the background, lights and model */
  private BoundingSphere bounds;

  /**
   * Default constructor
   */
  public GUI(){
    bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 5.0);
  }

  /**
   * Create some scenegraph
   */
  public BranchGroup createSceneGraph(BranchGroup model, BranchGroup sceneRoot)
  {
    //Create the root node of the scenegraph
    if(sceneRoot == null){
      sceneRoot = new BranchGroup();
    }

    //Add model to the scenGraph
    model.setUserData(new MutableInteger(sceneRoot.numChildren()));
    TransformGroup tg = new TransformGroup();
    tg.addChild(model);
    sceneRoot.addChild(tg);   

    //Create background 
    Color3f color = new Color3f(0.4f, 0.4f, 0.6f);
    Background bg = new Background(color);
    bg.setApplicationBounds(bounds);
    sceneRoot.addChild(bg);

    //Create lights
    createLights(sceneRoot, bounds);
    
    //Note that we could create also some other nodes to the 
    //scenegraph than background and lights

    return sceneRoot;
  }

  /**
   * Creates lights to illuminate the objects of the scene
   */
  private void createLights(BranchGroup root, BoundingSphere bounds)
  {
    //Create ambient light and add it to the scenegraph
    AmbientLight ambient = 
      new AmbientLight(true, new Color3f(0.2f, 0.4f, 0.6f));
    ambient.setInfluencingBounds(bounds);
    root.addChild(ambient);

    //Create spotlight light and add it to the scenegraph
    SpotLight spot = new SpotLight(true, new Color3f(0.2f, 0.4f, 0.6f), 
				   new Point3f(1.0f, 0.5f, 0.5f), 
				   new Point3f(0.00f, 0.1f, 0.05f),
				   new Vector3f(0.0f, 0.0f, 0.0f), 1.6f, 20f);
    spot.setInfluencingBounds(bounds);
    root.addChild(spot);
  }
  
  /**
   * Show the graphical user interface (GUI)
   */
  public void showGUI(BranchGroup sceneRoot, BranchGroup model){
    BoundingSphere bounds = 
      new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 5.0);

    createCtrl();
    MutableInteger id = (MutableInteger)model.getUserData();
    TransformGroup tg = (TransformGroup)sceneRoot.getChild(id.value);
    setModelCapability(tg);
    addMouseControl(sceneRoot, bounds, tg);
    SimpleUniverse universe = createUniverse();

    //Move the viewpoint 
    TransformGroup vpTrans = 
      universe.getViewingPlatform().getViewPlatformTransform();
    Vector3f translate = new Vector3f();
    translate.set(0.0f, 2.0f, 20.0f);
    Transform3D T3D = new Transform3D();
    T3D.setTranslation(translate); 
    vpTrans.setTransform(T3D);
    
    //translate.set(0.0f, 0.3f, 0.0f); // 3 meter elevation
    //T3D.setTranslation(translate); // set as translation
    //vpTrans.setTransform(T3D); // used for initial position
    KeyNavigatorBehavior keyNavBeh = new KeyNavigatorBehavior(vpTrans);
    keyNavBeh.setSchedulingBounds(new BoundingSphere(new Point3d(),1000.0));
    sceneRoot.addChild(keyNavBeh);

    sceneRoot.compile();

    universe.addBranchGraph(sceneRoot);
    validate();
  }

//   public void showGUI(Absyn absyn){
//     BranchGroup sceneRoot;
//     sceneRoot = new BranchGroup();

//     //Add model to the scenGraph
// //     model.setUserData(new MutableInteger(sceneRoot.numChildren()));
// //     TransformGroup tg = new TransformGroup();
// //     tg.addChild(model);
// //     sceneRoot.addChild(tg);   

//     //Create background 
//     Color3f color = new Color3f(0.4f, 0.4f, 0.6f);
//     Background bg = new Background(color);
//     bg.setApplicationBounds(bounds);
//     sceneRoot.addChild(bg);

//     //Create lights
//     createLights(sceneRoot, bounds);

//     BoundingSphere bounds = 
//       new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 5.0);

    
//     createCtrl();
// //     MutableInteger id = (MutableInteger)model.getUserData();
// //     TransformGroup tg = (TransformGroup)sceneRoot.getChild(id.value);
// //     setModelCapability(tg);

//     translate(absyn, sceneRoot);

//     //sceneRoot.addChild(new ColorCube(0.1f));
//     SimpleUniverse universe = createUniverse();

//     //Move the viewpoint 
//     TransformGroup vpTrans = 
//       universe.getViewingPlatform().getViewPlatformTransform();
//     Vector3f translate = new Vector3f();
//     translate.set(0.0f, 2.0f, 20.0f);
//     Transform3D T3D = new Transform3D();
//     T3D.setTranslation(translate); 
//     vpTrans.setTransform(T3D);
    
//     //translate.set(0.0f, 0.3f, 0.0f); // 3 meter elevation
//     //T3D.setTranslation(translate); // set as translation
//     //vpTrans.setTransform(T3D); // used for initial position
//     KeyNavigatorBehavior keyNavBeh = new KeyNavigatorBehavior(vpTrans);
//     keyNavBeh.setSchedulingBounds(new BoundingSphere(new Point3d(),1000.0));
//     sceneRoot.addChild(keyNavBeh);

//     sceneRoot.compile();

//     universe.addBranchGraph(sceneRoot);
//     validate();
//   }

//   public void translate(Absyn absyn, BranchGroup sceneRoot){
//     List mesh = new Vector();
//     List data = absyn.getAbsyn();
//     //list = new Vector();

//     //Get the meshes so that they can be scaled uniformly
//     Iterator itr = data.iterator();
//     while(itr.hasNext()){
//       Object value = itr.next();
//       if(value instanceof AbsynMesh){
// 	mesh.add(value);
//       }
//     }

//     float max, factor, scale;
//     if(!true){
//       max = 1.0f; 
//       factor = 10.0f; 
//       scale = 1.0f; 
//     }
//     else{
//       max = parseMesh(mesh);
//       factor = 32767/max;    
//       scale = max/32767;
//     }

//     float[] bias = new float[3];
//     for(int i=0; i<bias.length; i++){
//       bias[i] = 0.0f;
//     }

//     Material material;
//     Color3f ambientCol = new Color3f(0.1f, 0.1f, 0.1f);
//     Color3f diffuseCol = new Color3f(0.6f, 0.6f, 0.6f);
//     Color3f specularCol = new Color3f(0.3f, 0.3f, 0.3f);
//     float shininess = 10f;
//     float[][] color2 = {{1.0f, 0.0f, 0.0f},
// 		       {0.0f, 1.0f, 0.0f},
// 		       {0.2f, 0.5f, 0.5f},
// 		       {0.2f, 0.5f, 0.5f},
// 		       {1.0f, 0.0f, 1.0f},
// 		       {1.0f, 0.0f, 1.0f},
// 		       {0.0f, 0.0f, 0.0f}};

//     //List data = absyn.getAbsyn();
//     itr = data.iterator();
//     int colID = 0;
//     while(itr.hasNext()){
//       Object value = itr.next();
//       if(value instanceof AbsynMesh){
// 	TransformGroup tg = new TransformGroup();
// 	System.err.println("CreateMesh");
// 	Shape3D shape = createShape((AbsynMesh)value);
// 	Appearance app = shape.getAppearance();
// 	Color3f emissiveCol = new Color3f(color2[colID++]);
// 	material = new Material(ambientCol, emissiveCol, diffuseCol, 
// 				specularCol, shininess);
// 	material.setLightingEnable(true);
// 	app.setMaterial(material);	

// 	tg.addChild(shape);
// 	addMouseControl(sceneRoot, bounds, tg);
// 	sceneRoot.addChild(tg);
//       }
//     }
//   }

//   /**
//    * Gets the maximum offset from the origo for the meshes
//    * for scaling purposes
//    */
//   private float parseMesh(List list){
//     log("ParseMesh: " + list);
//     Object[] arr = list.toArray();
//     float min = 0, max = 0;
//     for(int i=0; i<arr.length; i++){
//       AbsynMesh m = (AbsynMesh)arr[i];
//       if(m.getMin() < min){
// 	min = m.getMin();
//       }
//       if(m.getMax() > max){
// 	max = m.getMax();
//       }      
//     }
//     log("  min: " + min + "  max: " + max);
//     min = Math.abs(min);
//     if(min > max){
//       max = min;
//     }
//     return max;
//   }

//   private Shape3D createShape(AbsynMesh mesh){
    
//     float[] vertex = null; //mesh.getVertex();
//     TriangleArray arr = new TriangleArray(vertex.length/3, 
// 					  GeometryArray.COORDINATES |
// 					  GeometryArray.NORMALS);
//     arr.setCoordinates(0, vertex);

//     GeometryInfo gi = new GeometryInfo(arr);
//     NormalGenerator ng = new NormalGenerator();
//     ng.generateNormals(gi);
//     Stripifier st = new Stripifier();
//     st.stripify(gi);
//     GeometryArray result = gi.getGeometryArray();    
//     //Shape3D s = new Shape3D(result, textures[texID]);
//     Shape3D s = new Shape3D(result);
//     s.setAppearance(new Appearance());
//     return s;
//     //return new Shape3D(arr);
//   }

  /**
   * Creates GUI
   */
  private void createCtrl()
  {
    //Put swing front of j3d
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);

    //Menu to select and open file
    JMenuBar menuBar = new JMenuBar();
    JMenu menu;
    JMenuItem item;
    menu = new JMenu("File");
    item = new JMenuItem("Open..");
    item.setActionCommand(CMD_OPEN);
    menu.add(item);    
    item.addActionListener(this);
    menuBar.add(menu);

    //Put this into a visible frame
    JFrame frame = new JFrame("XMobile-Importer");
    //frame.setJMenuBar(menuBar);
    frame.addWindowListener(new WindowAdapter(){
	public void windowClosing(WindowEvent event)
	{
	  System.exit(0);
	}}); 
    frame.setSize(320, 240);
    frame.getContentPane().add(this);
    frame.setVisible(true);
  }

  /**
   * Creates the rendering canvas and 
   * the utility class: SimpleUniverse
   */
  private SimpleUniverse createUniverse(){
    setLayout(new BorderLayout());
    GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
    Canvas3D canvas3D = new Canvas3D(config);
    add(BorderLayout.CENTER, canvas3D);
    SimpleUniverse universe = new SimpleUniverse(canvas3D);

    return universe;
  }

  /**
   * Sets some capabilities for the model 
   * so that it can be manipulated with the mouse
   */
  private void setModelCapability(TransformGroup model){
    model.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
    model.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
    model.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
    model.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    model.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
  }

  /**
   * Adds mouse control so that the loaded model can
   * be manipulated that is rotated, translated and zoomed
   */
  private void addMouseControl(BranchGroup root, BoundingSphere bounds,
			       TransformGroup model){
    MouseRotate mr = new MouseRotate();
    mr.setTransformGroup(model);
    mr.setSchedulingBounds(bounds);
    root.addChild(mr);
    
    MouseTranslate mt = new MouseTranslate();
    mt.setTransformGroup(model);
    mt.setSchedulingBounds(bounds);
    root.addChild(mt);
    
    MouseZoom mz = new MouseZoom();
    mz.setTransformGroup(model);
    mz.setSchedulingBounds(bounds);
    root.addChild(mz);
  }

  /**
   * Selects model file from the file system with the GUI
   */
  public void actionPerformed(ActionEvent e)
  {
    String id = e.getActionCommand();
    log(id);
    if(CMD_OPEN.equals(id)){
      log("NOT IMPLEMENTED YET\n" +
	  "but a model could be selected from the file system");
    }
  }

  private void log(Object msg)
  {
    System.err.println("" + msg);
  }
}
