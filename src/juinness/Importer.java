//Created 2004-10-24
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

import juinness.util.Util;

import java.lang.reflect.*;
import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.Loader;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * The <code>Importer</code> class imports, that is loads, 
 * arbitrary formats of 3D models 
 *
 * The loader implementations can be constructed 
 * either with the XML file or with coding 
 * <p> 
 * With the XML file, the location of the XML file is given 
 * The loaders that are needed are instantiated at the run time
 * <p><blockquote><pre>
 * setLoader(String location)
 * </pre></blockquote><p>
 * <p> 
 * With coding, the loader implementation are constructed and set into
 * Map data structure
 * as a Loader if one loader for the model format is supported or 
 * as a list if multiple loaders for the same model format are supported 
 * <p><blockquote><pre>
 * import loaderImplementations;
 *
 * Map loaders = new HashMap();
 *
 * List list = new Vector();
 * Loader oneVrmlLoader = new VRML97LoaderImpl();
 * Loader otherVrmlLoader = new OtherVRML97LoaderImpl();
 * list.add(oneVrmlLoader);
 * list.add(otherVvrmlLoader);
 * loaders.put("vrml", list);
 * loaders.put("wrl", list);
 *
 * Loader aseLoader = new AseLoaderImpl();
 * loaders.put("ase", aseLoader);
 *
 * Loader x3dLoader = new X3DLoaderImpl();
 * loaders.put("x3d", x3dLoader);
 *
 * etc.
 * </pre></blockquote><p>
 * Then the loaders are set into the Importer
 * <p><blockquote><pre>
 * Importer importer = new Importer();
 * importer.setLoader(loaders);
 * </pre></blockquote><p>
 * <p> 
 * The loader that will be used can be given 
 * for the Importer explicitly with the id 
 * <p><blockquote><pre>
 * URL location = new URL("http://127.0.0.1/model.suffix");
 * Scene scene = importer.load(location, suffix);
 * </pre></blockquote><p>
 * otherwise the loader that will be used is based on the suffix 
 * that is parsed from the location and used as the id
 * <p><blockquote><pre>
 * URL location = new URL("http://127.0.0.1/model.suffix");
 * Scene scene = importer.load(location);
 * </pre></blockquote><p>
 * <p> 
 * The BranchGroup can be referenced with the following:
 * <p><blockquote><pre>
 * BranchGroup model = scene.getSceneGroup();
 * </pre></blockquote><p>
 * 
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class Importer
{
  private Util util;

  /** Catalog of the loaders */
  private Map loaderCatalog;

  /** The instantiated loaders */
  private Map loaders;

  /** Cache for the instantiated loaders */
  private Map loaderCache;

  /**
   * Default constructor
   */
  public Importer(){
    util = Util.getInstance();
  }

  /**
   * Gets loaders
   */
  public Map getLoader(){
    return loaderCache;
  }
  
  /**
   * Sets loaders
   */
  public void setLoader(Map loaders){
    loaderCache = new HashMap();
    loaderCatalog = new HashMap();
    Iterator itr = loaders.keySet().iterator();
    while(itr.hasNext()){
      Object key = itr.next();
      Object value = loaders.get(key);
      if(value instanceof Loader){
	List impl = new Vector();	
	impl.add(value);
	loaderCache.put(key, impl);
      }
      else if(value instanceof List){
	loaderCache.put(key, value);
      }
    }
  }

  public void setLoader(String location)
    throws Exception
  {
    DocumentBuilder builder = 
      DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(new File(location));
    Element elm = doc.getDocumentElement();

    loaderCache = new HashMap();
    loaders = new HashMap();
    loaderCatalog = new HashMap();

    List formatList = new Vector();    

    NodeList loader = elm.getElementsByTagName("loader");
    for(int i=0; i<loader.getLength(); i++){
      String key;
      String value;
      Node n = loader.item(i);
      NodeList name = n.getChildNodes();
      List nameList = new Vector();
      formatList.clear();
      for(int j=0; j<name.getLength(); j++){
	n = name.item(j);
	value = n.getNodeName();
	if("format".equals(value)){
	  value = n.getFirstChild().getNodeValue();
	  formatList.add(value);
	  //util.log("loader format: " + value);
	}
	else if("name".equals(value)){
	  value = n.getFirstChild().getNodeValue();
	  nameList.add(value);
	  //util.log("loader name: " + value);
	}
      }
      Iterator itr = formatList.iterator();
      while(itr.hasNext()){
	Object obj = itr.next();
	loaderCatalog.put(obj, nameList);
      }
    }
    util.log("loaderCatalog: " + loaderCatalog);
  }

  /**
   * Loads the model from the location
   */
  public Scene load(String location)
    throws Exception
  {
    return load(location, getID(location));
  }

  /**
   * Loads the model from the location with the given id
   */
  public Scene load(String location, String id)
    throws Exception
  {
    Iterator itr = getLoader(id).iterator();
    while(itr.hasNext()){
      try{
	Loader loader = (Loader)itr.next();
	return loader.load(location);
      }
      catch(Exception e){
	e.printStackTrace();
      }
    }
    return null;
  }
  
  /**
   * Loads the model from the location 
   */
  public Scene load(URL location)
    throws Exception
  {
    return load(location, getID(location.getFile()));
  }

  /**
   * Loads the model from the location with the given id
   */
  public Scene load(URL location, String id)
    throws Exception
  {
    Iterator itr = getLoader(id).iterator();
    while(itr.hasNext()){
      try{
	Loader loader = (Loader)itr.next();
	return loader.load(location);
      }
      catch(Exception e){
	e.printStackTrace();
      }
    }
    return null;
  }

  /**
   * Gets the loader associated with the id
   */
  private List getLoader(String id)
    throws Exception
  {
    //Check if the loader is already instantiated
    Object obj = loaderCache.get(id);
    if(obj != null){
      return (List)obj;
    }

    //Check if the loader is supported according the XML file
    obj = loaderCatalog.get(id);
    if(obj == null){
      throw new Exception("Importer: No loader for the: " + id);
    }    
    
    //Try to instantiate the loader(s)
    List list = (List)obj;
    Iterator itr = list.iterator();
    List impl = new Vector();
    while(itr.hasNext()){
      obj = itr.next();
      try{
	Object loader = loaders.get(obj);
	if(loader == null){
	  //Loader is not already instantiated so instantiate it
	  System.err.println("Try to instantiate: " + obj.toString());
	  Class classDefinition = Class.forName(obj.toString());
	  loader = classDefinition.newInstance();
	  loaders.put(obj, loader);
	}
	impl.add(loader);
      }
      catch (InstantiationException e) {
	System.out.println(e);
      } 
      catch (IllegalAccessException e) {
	System.out.println(e);
      } 
      catch (ClassNotFoundException e) {
	System.out.println(e);
      }
      catch(NoClassDefFoundError e) {
	System.out.println(e);
      }
    }
    loaderCache.put(id, impl);
    return impl;
  }

  /**
   * Parses the suffix from the model denoted as name.suffix 
   * The name denotes the name of the model and 
   * suffix identifies the format of the model
   */
  private String getID(String location)
    throws Exception
  {
    int i = location.lastIndexOf(".");
    if(i == -1){
      throw new Exception("Importer: Could not solve the loader for the: " +
			  location);
    }
    return location.substring(i+1, location.length());     
  }
}
