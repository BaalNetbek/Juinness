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
//http://www.gnu.org/copyleft/gpl.html

package juinness;

import juinness.m3g.*;
import juinness.util.*;

import java.io.*;
import java.util.zip.Adler32;
import java.util.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import javax.microedition.m3g.*;
import javax.media.j3d.BranchGroup;

/**
 * The <code>Exporter</code> class translates the 
 * M3G scene graph according the 
 * Mobile 3D Graphics (M3G) (JSR-184) file format and
 * exports the result as a M3G file into the file system
 *
 * @author Markus Yliker&auml;l&auml; and Maija Savolainen
 */
public class Exporter
{
  private Util util;
  private Adler32 adler32;
  private RandomAccessFile out;
  private Map hash;
  private MutableInteger typeID;
  private Map objectIndexHash;
  private MutableInteger objectIndexID;

  private Transform transform;
  private float[] matrix;

  /**
   * Constructor
   */
  public Exporter()
  {
    util = Util.getInstance();

    adler32 = new Adler32();    
    typeID = new MutableInteger(0);
    hash = getObjects(typeID);
    
    objectIndexHash = new HashMap();
    objectIndexID = new MutableInteger(0);

    transform = new Transform();
    matrix = new float[16];
  }

  /**
   * Exports given scenegraph into M3G file format denoted with path
   *
   * @param sceneGraph M3G scenegraph
   * @param path output file for the export
   */
  public void export(List sceneGraph, String path){
    try{
      //Initialize the output file 
      init(path);
      
      //Write fileIdentifier
      byte[] fid = util.getFileIdentifier();
      long size = fid.length;
      out.write(fid); 
      util.log("fileIdentifier: " + new String(fid) + "\n");
      long pos = out.getFilePointer();

      util.setVisible(false);

      //Header Section
      //Because totalfilesize of the header is not known 
      //until everything has been written, we instead of 
      //writing the header only reserve space for it 
      //so that the header need to be written only once 
      //because file operations are much slower than memory operations
      Section hdrSec;
      HeaderObject hdr = new HeaderObject();
      setObjectIndex(hdr);
      hdr.setAuthoringField("Maija Is Pop, Markus Rules Ok!?");
      hdrSec = new Section(hdr);
      size += hdrSec.getBytes(hdr).length;
      out.seek(size);

      //Write the Other Sections
      //Currently we support only one M3G-object per section for 
      //development purposes
      MutableInteger typeID = new MutableInteger(0);
      Iterator itr = sceneGraph.iterator();
      while(itr.hasNext()){	
	Sub obj = (Sub)itr.next();
	typeID.value = obj.getObjectType();
	ObjectStructure wrap = getWrapper(typeID, (Object3D)obj);
	if(wrap == null){
	  continue;
	}
	Section sec = new Section(wrap);
	size += sec.write();
      }

      //Update the totalfilesize of the header and 
      //write the header to the beginning of the file
      out.seek(pos);
      hdr.setTotalFileSize(size);
      hdrSec.write();
      out.close();

      util.setVisible(true);

      util.log("M3G File written");
    }
    catch(Exception e){
      e.printStackTrace();
    }    
    finally{
      util.closeLog();
    }
  }

  /**
   * Initializes the M3G output file denoted with path
   *
   * @param path output file for the export
   */ 
  private void init(String path)
    throws Exception
  {    
    File file = new File(path);
    //util.setLog("expo_" + file.getName()+ ".txt");

    //Delete the old file if it exists because otherwise 
    //we get garbage to the new file. The reason for this is
    //that if the size of the old file is larger than the new file,
    //the rest of the old file remains in the new file
    boolean result = file.delete();
    util.log("Tried to delete the file: " + path + "\nresult: " + result);
    
    out = new RandomAccessFile(path, "rw");
    util.log("\n*** OUTPUT OF THE M3G FILE: " + path +
	     "\n(Note that the header is printed last although it is in the "+
	     "beginning of the M3G-file");        
  }

  /**
   * Gets the wrapper for the specified M3G-object with the typeID
   */
  private ObjectStructure getWrapper(MutableInteger typeID, Object3D obj)
    throws Exception
  {
    if(obj == null){
      util.log("getWrapper: got null");
      return null;
    }
    Object tst = hash.get(typeID);
    if(tst == null){
      util.log("getWrapper: no wrapper accosiated for the id: " + typeID);
      return null;
    }
    ObjectStructure wrap = (ObjectStructure)tst;
    setObjectIndex(obj);
    wrap.setData(obj);
    return wrap;
  }

  /**
   * Creates wrapper for the M3G-objects
   */
  private Map getObjects(MutableInteger id){
    Map hash = new HashMap();
    Object[] objType = {
      new HeaderObject(),
      new ExternalReference(), 
      new AnimationControllerWrapper(),
      new AnimationTrackWrapper(),
      new AppearanceWrapper(),
      new BackgroundWrapper(),
      new CameraWrapper(),
      new GroupWrapper(),
      new Image2DWrapper(),
      new KeyframeSequenceWrapper(),
      new LightWrapper(),
      new MaterialWrapper(),
      new MeshWrapper(),
      new SpriteWrapper(),
      new Texture2DWrapper(),
      new TriangleStripArrayWrapper(),
      new VertexArrayWrapper(),
      new VertexBufferWrapper(),
      new WorldWrapper()
    };
    for(int i=0; i<objType.length; i++){
      ObjectStructure obj = (ObjectStructure)objType[i];
      MutableInteger key = new MutableInteger(obj.getObjectType());
      hash.put(key, objType[i]);	
    }    
    return hash;
  }

  /**
   * Shows a part of the content of a M3G-file
   */
  public long show(int type, byte[] data, MutableInteger dif, int size){    
    typeID.value = type;
    Object obj;
    if((obj = hash.get(typeID)) == null){
      util.log("Checker Not Implemented for ObjectType: " + typeID);
      return 0;      
    }
    int begin = dif.value;
    util.log(((ObjectStructure)obj).show(data, dif, size));
    util.log(" *****************");      
    util.log("Got: " + (dif.value - begin) + " bytes-> offset:" + dif.value);
    if(type == 0){
      HeaderObject hdr = (HeaderObject)obj;
      return hdr.getTotalFileSize();
    }
    return 0;
  }

  /**
   * Section, as described in Section 6,
   * writes Special Object Data into FileSystem
   *
   * Section 6:
   * Each section has the following structure:
   * Byte   CompressionScheme
   * UInt32 TotalSectionLength
   * UInt32 UncompressedLength
   * Byte[] Objects
   * UInt32 Checksum
   */
  private class Section
  {
    byte compressionScheme;
    long totalSectionLength;
    long uncompressedLength;
    List objects;
    long checksum;
    byte[] buf;
    MutableInteger dif;

    Section(ObjectStructure obj){
      objects = new Vector();
      objects.add(obj);
    }

    /**
     * Write all of the data in this section
     */
    int write()
      throws Exception
    {
      util.log("\n*** SECTION BEGINS ***");
      int size = 0;
      byte[] data = null;
      Iterator itr = objects.iterator();
      int ind = 1;
      byte[] oi = new byte[4];
      while(itr.hasNext()){
	util.log("OFFSET BEGIN: " + size);
	ObjectStructure obj = (ObjectStructure)itr.next();
	data = getBytes(obj);
	out.write(data);
	size += data.length;
      }      
      util.log(this);
      util.log("*** SECTION ENDS ***\n");
      return size;
    }

    byte[] getBytes(ObjectStructure obj) 
      throws Exception
    {
      MutableInteger offset = new MutableInteger(0);

      //Get the length of the Data of Object Structure
      //as specified in Section 7.0 and allocate space for the
      //Special Object
      //NOTE that now Section contains only one
      //object but should be modified if multiple objects are
      //placed in the same section =|

      //13 constant bytes as specified in Section 6
      //and 5 constant bytes as specified in Section 7
      //Data length may vary
      //Note that object data is retrived 
      //From the index 14, I've seen too much
      int size = obj.getSize();      
      buf = new byte[13 + 5 + size];
      offset.value = 14;
      util.log("SIZE: " + size + "  " + buf.length);
      size = obj.getData(buf, offset);

      //Section 6
      offset.value = 0;      
      util.byteToBytes(buf, offset, compressionScheme);
      totalSectionLength = buf.length;
      util.intToBytes(buf, offset, totalSectionLength);
      uncompressedLength = size + 5;
      util.intToBytes(buf, offset, uncompressedLength);

      //Section 7
      //Objects
      util.byteToBytes(buf, offset, (byte)obj.getObjectType());
      util.intToBytes(buf, offset, size);     

      //Section 6
      //Checksum
      offset.value = buf.length - 4;
      adler32.reset();
      adler32.update(buf, 0, offset.value);
      checksum = adler32.getValue();
      util.intToBytes(buf, offset, checksum);

      //THE FOLLOWING IS FOR DEBUGGING PURPOSES ONLY
      obj.length = size;
      obj.data = buf;
      obj.dif = offset;      
      obj.di = 14;
      obj.dif.value = 14;
      //util.log("DATA di:" + obj.di + "/" + buf.length);
      dif = offset;
      //THE ABOVE WAS FOR DEBUGGING PURPOSES ONLY

      return buf;      
    }

    public String toString(){
      String str = "";
      dif.value = 0;
      str += "compressionScheme: " + util.bytesToByte(buf, dif) + "\n";
      str += "totalSectionLength: " + util.bytesToInt(buf, dif) +  "\n";
      str += "uncompressedLength: " + util.bytesToInt(buf, dif) + "\n";

      dif.value = 14;
      Iterator itr = objects.iterator();
      while(itr.hasNext()){
	Object obj = itr.next();
	str += obj.toString();
      }

      dif.value = buf.length-4;
      str += "\nchecksum: " + util.bytesToInt(buf, dif);
      return str;
    }
  }

  //**************************************************************
  // 10 Special Object Data BEGINS
  //**************************************************************

  /**
   * Each Special Object Data as described in Section 10 
   * and Per-Class Data as described in Section 11 
   * is represented with ObjectStructure which is
   * either wrapper or stand alone 
   */
  private abstract class ObjectStructure
  {
    int objectType;
    long length;
    byte[] data;
    int di;
    MutableInteger dif;
    Object wrap;
    
    /**
     * Gets the objectType
     */
    int getObjectType(){
      return objectType;
    }
    
    /**
     * Sets the m3g object
     */
    void setData(Object wrap){
      this.wrap = wrap;
    }

    /**
     * Gets the size needed for the memory allocation
     */
    abstract int getSize();

    /**
     * Fills the array with the actual content
     */
    abstract int getData(byte[] data, MutableInteger offset)
      throws Exception;

    public String toString(){
      String str = "";
      str += " --------------" + "\n";
      str += " objectType: " + objectType + 
	" " + util.getObjectType(objectType) + "\n";
      str += " length: " + length + "   offset: " + dif + "\n";
      str += show(data, dif, (int)length);
      return str;
    }

    /**
     * Shows the content of this
     */
    String show(byte[] data, MutableInteger dif, int size){
      String str = "";
      return str;
    }
  }

  /**
   * Retrieves ObjectIndex
   */
  private int getObjectIndex(Object obj){
    Object tmp;
    if(obj != null && (tmp = objectIndexHash.get(obj)) != null){
      objectIndexID = (MutableInteger)tmp;      
    } 
    else{
      objectIndexID.value = 0;
    }
    util.log("# REQUEST OBJECT ID: " + objectIndexID + " for:" + obj);
    return objectIndexID.value;
  }

  /**
   * Assign an ObjectIndex
   */
  private void setObjectIndex(Object obj){
    if(obj == null){
      util.log("\n\n\nFATAL OBJECT HASH NULL: " + obj);
    }
    Object tmp;
    if((tmp = objectIndexHash.get(obj)) != null){
      util.log("\n\n\nFATAL OBJECT HASH EXISTS: " + obj);
    }

    MutableInteger value = new MutableInteger(objectIndexHash.size() + 1);
    objectIndexHash.put(obj, value);
    util.log("# RECORD OBJECT ID: " + value);
    if(obj instanceof Object3D){
      ((Object3D)obj).setUserID(value.value);
    }
  }

  /**
   * Stand alone for the 10.1 Header Object
   */
  class HeaderObject extends ObjectStructure
  {
    boolean hasExternalReference;
    long totalFileSize;
    long approximateContentSize;

    /* 11 constant bytes excluding the implicit NIL*/ 
    private final int FIXED = 11;

    /** authoringField contains the implicit NIL, although,
	an explicit NULL=NIL could also be written =) */
    String authoringField = "\0";

    HeaderObject(){
      objectType = 0;
    }

    void setAuthoringField(String str){
      authoringField = str + authoringField;
    }

    void setExternalReference(boolean flag){
      hasExternalReference = flag;
    }

    void setTotalFileSize(long size){
      totalFileSize = size;
      approximateContentSize = size;
    }

    long getTotalFileSize(){
      return totalFileSize;
    }

    /**
     * 11 constant bytes plus an explicit NUL plus a variable size authoring
     */
    int getSize(){
      try{
	byte[] authoring = authoringField.getBytes("UTF-8");
	return FIXED + authoring.length;
      }
      catch(Exception e){
	e.printStackTrace();
	authoringField = "";
	return FIXED + 1;
      }
    }

    int getData(byte[] data, MutableInteger offset)
      throws Exception
    {
      di = offset.value;
      byte[] versionNumber = {1, 0};
      util.byteToBytes(data, offset, versionNumber);
      util.booleanToBytes(data, offset, hasExternalReference);      
      util.intToBytes(data, offset, totalFileSize);
      util.intToBytes(data, offset, approximateContentSize);

      byte[] authoring = authoringField.getBytes("UTF-8");
      System.arraycopy(authoring, 0, data, offset.value, authoring.length);
      offset.value += authoring.length;

      util.log("Header: off:" + di + " fix:" + FIXED + 
	       " size:" + (offset.value - di));

      return offset.value - di;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      size += dif.value;

      str += "  versionNumber: " + util.bytesToByte(data, dif) + "." +
	util.bytesToByte(data, dif) + "\n"; 
      str += "  hasExternalReference: " + 
	util.bytesToBoolean(data, dif) + "\n";
      totalFileSize = util.bytesToInt(data, dif);
      str += "  totalFileSize: " + totalFileSize + "\n";
      str += "  approximateContentSize: " + util.bytesToInt(data, dif) + "\n";
      str += "  authoringField: "+
	new String(data, dif.value, size-dif.value);
      dif.value += (size-dif.value);
      return str;
    }
  }

  /**
   * Stand alone for the 10.2 External Reference
   */
  private class ExternalReference extends ObjectStructure
  {
    /** URI contains the implicit NIL, although,
	an explicit NULL=NIL could also be written =) */
    private String URI = "\0";
    private final int FIXED = 1;

    /**
     * Constructs and implicitly adds an explicit NUL =) 
     * to the given URI
     */
    ExternalReference(){
      objectType = 0xFF;
    }

    void setURI(String str){
      this.URI = str + URI;
    }
    
    /** length of URI plus an implicit NUL */
    int getSize(){
      try{
	byte[] uri = URI.getBytes("UTF-8");
	return uri.length;
      }
      catch(Exception e){
	e.printStackTrace();
	URI = "\0";
	return FIXED;
      }
    }
    
    int getData(byte[] data, MutableInteger offset)
      throws Exception
    {
      di = offset.value;
      byte[] uri = URI.getBytes("UTF-8");
      System.arraycopy(uri, 0, data, offset.value, uri.length);
      offset.value += uri.length;

      util.log("External: off:" + (offset.value-uri.length) + 
	       " fix:" + FIXED + " size:" + (uri.length));

      return uri.length;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  URI: " + new String(data, di, size); 
      dif.value += size;
      return str;
    }
  }



  //**************************************************************
  // 11 Per-Class Data BEGINS
  //**************************************************************  
  /**
   * Wrapper for the 11.1 AnimationController
   */
  private class AnimationControllerWrapper extends Object3DWrapper
  {
    private final int FIXED = 24;

    public AnimationControllerWrapper(){
      objectType = 1;
    }

    int getSize(){
      return super.getSize() + FIXED;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      AnimationController n = (AnimationController)wrap;

      util.log("AnimationController: off:" + begin + " fix:" + FIXED + 
	       " size:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->A N I M A T I O N C O N T R O L L E R:" + "\n";      
      str += "  speed: " + util.bytesToFloat(data, dif) + "\n";
      str += "  weight: " + util.bytesToFloat(data, dif) + "\n";
      str += "  actInterStart: " + util.bytesToInt(data, dif) + "\n";
      str += "  actInterEnd: " + util.bytesToInt(data, dif) + "\n";
      str += "  refSeqTime: " + util.bytesToFloat(data, dif) + "\n";
      str += "  refWorldTime: " + util.bytesToInt(data, dif) + "\n";
      return str;
    }
  }

  /**
   * Wrapper for the 11.2 AnimationTrack
   */
  private class AnimationTrackWrapper extends Object3DWrapper
  {
    private final int FIXED = 24;

    public AnimationTrackWrapper(){
      objectType = 2;
    }

    int getSize(){
      return super.getSize() + FIXED;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      AnimationTrack n = (AnimationTrack)wrap;

      util.log("AnimationTrack: off:" + begin + " fix:" + FIXED + 
	       " size:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->A N I M A T I O N T R A C K:" + "\n";      
      str += "  keyFrameSeq: " + util.bytesToInt(data, dif) + "\n";
      str += "  animCtrl: " + util.bytesToInt(data, dif) + "\n";
      str += "  propertyID: " + util.bytesToInt(data, dif) + "\n";
      return str;
    }
  }

  /**
   * Wrapper for the 11.3 Appearance
   */
  private class AppearanceWrapper extends Object3DWrapper
  {
    private final int FIXED = 21;

    public AppearanceWrapper(){
      objectType = 3;
    }

    int getSize(){      
      int texCount = 0;
      SubAppearance n = (SubAppearance)wrap;
      while(true){
	//if(n.getTexture(texCount) == null){
	if(n.getSubTexture(texCount) == null){
	  break;
	}
	texCount++;
      }

      return super.getSize() + FIXED + texCount*4;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      di = offset.value;
      SubAppearance n = (SubAppearance)wrap;

      util.byteToBytes(data, offset, (byte)n.getLayer());
      util.intToBytes(data, offset, getObjectIndex(n.getCompositingMode()));
      util.intToBytes(data, offset, getObjectIndex(n.getFog()));
      util.intToBytes(data, offset, getObjectIndex(n.getPolygonMode()));
      util.intToBytes(data, offset, getObjectIndex(n.getMaterial()));

      int texCount = 0;
      while(true){
	//if(n.getTexture(texCount) == null){
	if(n.getSubTexture(texCount) == null){
	  break;
	}
	texCount++;
      }

      util.intToBytes(data, offset, texCount);
      for(int k=0; k<texCount; k++){	
	//util.intToBytes(data, offset, getObjectIndex(n.getTexture(k)));
	util.intToBytes(data, offset, getObjectIndex(n.getSubTexture(k)));
      }

      util.log("Appearance: off:" + di + " fix:" + FIXED + 
	       " size:" + (offset.value - di) + 
	       " tot:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->A P P E A R A N C E:" + "\n";      
      str += "  layer: " + util.bytesToByte(data, dif) + "\n";
      str += "  compoMode: " + util.bytesToInt(data, dif) + "\n";
      str += "  fog: " + util.bytesToInt(data, dif) + "\n";
      str += "  polygonMode: " + util.bytesToInt(data, dif) + "\n";
      str += "  material: " + util.bytesToInt(data, dif) + "\n";
      int textures = util.bytesToInt(data, dif);
      str += "  textures: " + textures + "\n";
      for(int i=0; i<textures; i++){
	str += "    tex i:" + i + " = " + util.bytesToInt(data, dif) + "\n";
      }
      //dif.value += textures*4;

      return str;
    }
  }

  /**
   * Wrapper for the 11.4 Background
   */
  private class BackgroundWrapper extends Object3DWrapper
  {
    private final int FIXED = 28;
    
    public BackgroundWrapper(){
      objectType = 4;
    }

    int getSize(){
      return super.getSize() + FIXED;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      di = offset.value;

      Background n = (Background)wrap;
      util.rgbaToBytes(data, offset, n.getColor());         
      util.intToBytes(data, offset, getObjectIndex(n.getImage()));    
      util.byteToBytes(data, offset, (byte)n.getImageModeX());
      util.byteToBytes(data, offset, (byte)n.getImageModeY());
      util.intToBytes(data, offset, n.getCropX());    
      util.intToBytes(data, offset, n.getCropY());    
      util.intToBytes(data, offset, n.getCropWidth() );    
      util.intToBytes(data, offset, n.getCropHeight() );    
      util.booleanToBytes(data, offset, n.isDepthClearEnabled());    
      util.booleanToBytes(data, offset, n.isColorClearEnabled());    

      util.log("Background: off:" + di + " fix:" + FIXED + 
	       " size:" + (offset.value - di) + 
	       " tot:" + (offset.value - begin));

      return offset.value - begin;
    }

    public String toString(){
      return show(data, dif, 0);
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->B A C K G R O U N D:" + "\n";      
      int tst = util.bytesToRGBA(data, dif);
      str += "  bgColor: " + util.showRGBAColor(tst) + "\n";
      str += "  bgImage: " + util.bytesToInt(data, dif) + "\n"; 
      str += "  imageModeX: " + util.bytesToByte(data, dif) + "\n"; 
      str += "  imageModeY: " + util.bytesToByte(data, dif) + "\n"; 
      str += "  cropX: " + util.bytesToInt(data, dif) + "\n"; 
      str += "  cropY: " + util.bytesToInt(data, dif) + "\n"; 
      str += "  cropWidth: " + util.bytesToInt(data, dif) + "\n"; 
      str += "  cropHeight: " + util.bytesToInt(data, dif) + "\n"; 
      str += "  depthClear: " + util.bytesToBoolean(data, dif) + "\n"; 
      str += "  colorClear: " + util.bytesToBoolean(data, dif) + "\n"; 
      return str;
    }
  }

  /**
   * Wrapper for the 11.5 Camera
   */
  private class CameraWrapper extends NodeWrapper
  {
    private final int FIXED = 1;

    public CameraWrapper(){
      objectType = 5;
    }

    int getSize(){
      Camera n = (Camera)wrap;
      float[] params = null;
      int projectionType = n.getProjection(params);
      int bonus = 0;
      switch(projectionType){
      case Camera.GENERIC:
	bonus = 16*4;
	break;
      case Camera.PARALLEL:
      case Camera.PERSPECTIVE:
	bonus = 4*4;
	break;
      default:
	util.log("Unknown projection type");
	break;
      }
      return super.getSize() + FIXED + bonus;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      Camera n = (Camera)wrap;

      float[] params = null;
      int projectionType = n.getProjection(params);
      switch(projectionType){
      case Camera.GENERIC:
	util.byteToBytes(data, offset, projectionType);
	float[] initMatrix = new float[16];
	for(int i=0; i<initMatrix.length; i++) { 
	  initMatrix[i] = 0.0f; 
	} 
	initMatrix[0] = 1.0f; 
	initMatrix[5] = 1.0f; 
	initMatrix[10] = 1.0f; 
	initMatrix[15] = 1.0f; 	
	util.floatToBytes(data, offset, initMatrix); 
	break;
      case Camera.PARALLEL:
      case Camera.PERSPECTIVE:
	util.byteToBytes(data, offset, projectionType);
	util.floatToBytes(data, offset, 45.0f);
	util.floatToBytes(data, offset, 1.0f);
	util.floatToBytes(data, offset, 0.1f);
	util.floatToBytes(data, offset, 10.0f);	
	break;
      default:
	util.log("Unknown projection type");
	break;
      }

      util.log("Camera: off:" + begin + " fix:" + FIXED + 
	       " size: " + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->C A M E R A: " + "\n";      
      int projectionType = util.bytesToByte(data, dif);
      str += "  projectionType: " + projectionType + 
	" " + util.showCameraProjectionType(projectionType) + "\n";

      switch(projectionType){
      case Camera.GENERIC:
	str += "  matrix:" +  util.bytesToMatrix(data, dif) + "\n";
	break;
      case Camera.PARALLEL:
      case Camera.PERSPECTIVE:
	str += "  fovy: " + util.bytesToFloat(data, dif) + "\n";
	str += "  aspectRation: " + util.bytesToFloat(data, dif) + "\n";
	str += "  near: " + util.bytesToFloat(data, dif) + "\n";
	str += "  far: " + util.bytesToFloat(data, dif);
	break;
      default:
	util.log("Unknown projection type");
	break;
      }
      return str;
    }
  }

  /**
   * Wrapper for the 11.9 Group
   */
  private class GroupWrapper extends NodeWrapper
  {
    private final int FIXED = 4;

    GroupWrapper(){
      objectType = 9;
    }

    int getSize(){
      Group n = (Group)wrap;
      int superSize = super.getSize();
      int constSize = FIXED;
      int childSize = 4*n.getChildCount();

      return super.getSize() + FIXED + 4*n.getChildCount();
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      di = offset.value;
      Group n = (Group)wrap;

      int numChild = n.getChildCount();
      util.intToBytes(data, offset, numChild);
      
      for(int i=0; i<numChild; i++){
	Node child = n.getChild(i);
	int childID = getObjectIndex(child);
	util.log("ChildID: " + i + "/" + numChild + "  " + childID);
	util.intToBytes(data, offset, childID);
      }

      util.log("Group: off:" + begin + " fix:" + FIXED + 
	       " size:" + (offset.value - di) + 
	       " tot:" + (offset.value - begin) +
	       " numChild:" + numChild);

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->G R O U P:" + "\n";      
      int numChild = util.bytesToInt(data, dif);
      str += "  numChild: " + numChild + "\n";
      for(int i=0; i<numChild; i++){
	int child = util.bytesToInt(data, dif);
        str += "    child: " + child + "\n";
      }
      return str;
    }
  }

  /**
   * Wrapper for the 11.10 Image2D
   */
  private class Image2DWrapper extends Object3DWrapper
  {
    private final int FIXED = 10;
    
    public Image2DWrapper(){
      objectType = 10;
    }

    int getSize(){
      SubImage2D n = (SubImage2D)wrap;
      int format = n.getFormat();
      int factor = util.getImage2DFormat(format);
      int width = n.getWidth();
      int height = n.getHeight();
      int size = width*height*factor;

      util.log("Factor: " + factor);

      int bonus = 0;
      bonus += 4 + factor*256;
      bonus += 4 + width*height;

      util.log("### IMAGE: factor:" + factor + " size: " + size + "  tot:" +
	       (super.getSize() + FIXED + size + bonus));
      
      return super.getSize() + FIXED + bonus;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      di = offset.value;

      SubImage2D n = (SubImage2D)wrap;
      int format = n.getFormat();
      int width = n.getWidth();
      int height = n.getHeight();
      boolean isMutable = false;

      int factor = util.getImage2DFormat(format);
      util.log("Factor: " + factor);

      util.byteToBytes(data, offset, format);
      util.booleanToBytes(data, offset, isMutable);
      util.intToBytes(data, offset, width);
      util.intToBytes(data, offset, height);

      if(isMutable == false){
	//Palette
	System.err.println("\npalette: " + (factor*256));	 
	util.intToBytes(data, offset, (factor*256));
	util.byteToBytes(data, offset, n.getPalette());

	//Pixels
	System.err.println("\npixels: " + (width*height));	 
	util.intToBytes(data, offset, (width*height));
	util.byteToBytes(data, offset, n.getPixels());
      }

      util.log("Image2D: off:" + di + " fix:" + FIXED + 
	       " size:" + (offset.value - di) + 
	       " tot:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->I M A G E 2 D:" + "\n";      
      int format = util.bytesToByte(data, dif);
      int factor = util.getImage2DFormat(format);
      str += "  format: " + format + 
	" " + util.showImage2DFormat(format) + " factor:" + factor + "\n"; 
      boolean isMutable = util.bytesToBoolean(data, dif);
      str += "  isMutable: " + isMutable + "\n"; 
      int width = util.bytesToInt(data, dif);
      int height = util.bytesToInt(data, dif);
      str += "  width: " + width + "\n"; 
      str += "  height: " + height + "\n"; 

      if(isMutable == false){
	int length = 0;
	//Palette
	length = util.bytesToInt(data, dif);
	str += "  palette: " + length + " colors:" + 
	  (length / factor) + "\n"; 
	dif.value += length;

	//Pixels
	length = util.bytesToInt(data, dif);
	str += "  pixels: " + length; 
	dif.value += length;	
      }

      return str;
    }
  }

  /**
   * Wrapper for the 11.11 IndexBuffer
   */
  private abstract class IndexBufferWrapper extends Object3DWrapper
  {
    int getSize(){
      return super.getSize();
    }

    int getData(byte[] data, MutableInteger offset){
      return super.getData(data, offset);
    }

    String show(byte[] data, MutableInteger dif, int size){
      return super.show(data, dif, size);
    }
  }

  /**
   * Wrapper for the 11.12 KeyframeSequence
   */
  private class KeyframeSequenceWrapper extends Object3DWrapper
  {
    private final int FIXED = 23;

    public KeyframeSequenceWrapper(){
      objectType = 19;
    }

    int getSize(){
      return super.getSize() + FIXED;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      KeyframeSequence n = (KeyframeSequence)wrap;

      util.log("KeyframeSequence: off:" + begin + " fix:" + FIXED + 
	       " size:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->K E Y F R A M E S E Q U E N C E:" + "\n";      

      str += "  interpol: " + util.bytesToByte(data, dif) + "\n";
      str += "  repeatMode: " + util.bytesToByte(data, dif) + "\n";

      int encoding = util.bytesToByte(data, dif);
      str += "  encoding: " + encoding + "\n";

      str += "  duration: " + util.bytesToInt(data, dif) + "\n";
      str += "  valRangeFirst: " + util.bytesToInt(data, dif) + "\n";
      str += "  valRangeLast: " + util.bytesToInt(data, dif) + "\n";
      int compoCount = util.bytesToInt(data, dif);
      str += "  compoCount: " + compoCount + "\n";
      int frameCount = util.bytesToInt(data, dif);
      str += "  frameCount: " + frameCount + "\n";

      switch(encoding){
      case 0:
	dif.value += frameCount*(4 + compoCount*4);
	break;
      case 1:
	dif.value += (compoCount*4 + compoCount*4);
	dif.value += frameCount*(4 + compoCount);
	break;
      case 2:
	dif.value += (compoCount*4 + compoCount*4);
	dif.value += frameCount*(4 + compoCount*2);
	break;
      }

      return str;
    }
  }

  /**
   * Wrapper for the 11.27 TriangleStripArray
   */
  private class TriangleStripArrayWrapper extends IndexBufferWrapper
  {
    private final int FIXED = 5;

    public TriangleStripArrayWrapper(){
      objectType = 11;
    }

    int getSize(){
      SubTriangleStripArray n = (SubTriangleStripArray)wrap;

      int encoding = n.getEncoding();
      int startIndex = n.getStartIndex();
      int[] indices = n.getIndices();
      int[] stripLengths = n.getStripLengths();
      int bonus = 0;
      switch(encoding){
      case 0:
	bonus += 4;
	break;
      case 1:
	bonus += 1;
	break;
      case 2:
	bonus += 2;
	break;
      case 128:
	bonus += (4 + indices.length*4);
	break;
      case 129:
	bonus += (4 + indices.length);
	break;
      case 130:
	bonus += (4 + indices.length*2);
	break;
      default:
	util.log("Illegal encoding for the TriangleStripArray");
	break;
      }
      bonus += stripLengths.length*4;
      return super.getSize() + FIXED + bonus;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      SubTriangleStripArray n = (SubTriangleStripArray)wrap;

      util.log("TriangleStripArray: off:" + begin + " fix:" + FIXED + 
	       " size:" + (offset.value - begin));

      int encoding = n.getEncoding();
      util.byteToBytes(data, offset, encoding);

      int startIndex = n.getStartIndex();
      int[] indices = n.getIndices();
      int[] stripLengths = n.getStripLengths();

      switch(encoding){
      case 0:
	util.intToBytes(data, offset, startIndex);
	break;
      case 1:
	util.byteToBytes(data, offset, (byte)startIndex);
	break;
      case 2:
	util.int16ToBytes(data, offset, startIndex);
	break;
      case 128:
	util.intToBytes(data, offset, indices.length);
	for(int i=0; i<indices.length; i++){
	  util.intToBytes(data, offset, indices[i]);
	}
	break;
      case 129:
	util.intToBytes(data, offset, indices.length);
	for(int i=0; i<indices.length; i++){
	  util.byteToBytes(data, offset, (byte)indices[i]);
	}
	break;
      case 130:
	util.intToBytes(data, offset, indices.length);
	for(int i=0; i<indices.length; i++){
	  util.int16ToBytes(data, offset, indices[i]);
	}
	break;
      default:
	util.log("Illegal encoding for the TriangleStripArray");
	break;
      }

      util.intToBytes(data, offset, stripLengths.length);
      for(int i=0; i<stripLengths.length; i++){
	util.intToBytes(data, offset, stripLengths[i]);
      }

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->T R I A N G L E S T R I P:" + "\n";      
      int encoding = (util.bytesToByte(data, dif) & 0xFF);
      str += "  encoding: " + encoding + " " + util.getBin(encoding) + "\n";
      int indices;
      switch(encoding){
      case 0:
	str += "  startIndex: " + util.bytesToInt(data, dif) + "\n";
	break;
      case 1:
	str += "  startIndex: " + util.bytesToByte(data, dif) + "\n";
	break;
      case 2:
	str += "  startIndex: " + util.bytesToInt16(data, dif) + "\n";
	break;
      case 128:
	indices = util.bytesToInt(data, dif);
	str += "  indices: " + indices + "\n";
	dif.value += 4*indices; 
	break;
      case 129:
	indices = util.bytesToInt(data, dif);
	str += "  indices: " + indices + "\n";
	dif.value += indices; 
	break;
      case 130:
	indices = util.bytesToInt(data, dif);
	str += "  indices: " + indices + "\n    ";
	for(int i=0; i<indices; i++){
	  str += "(" + i + ":" + util.bytesToInt16(data, dif) + ") ";
	}
	str += "\n";
	//dif.value += 2*indices; 
	break;
      default:
	util.log("Illegal value for the TriangleStripArray");
	break;
      }
      int stripLength = util.bytesToInt(data, dif);
      str += "  stripLength: " + stripLength + "\n    ";
//       for(int i=0; i<stripLength; i++){
// 	str += "(" + i + ":" + util.bytesToInt(data, dif) + ") ";
//       }
      dif.value += 4*stripLength;
      return str;
    }
  }

  /**
   * Wrapper for the 11.13 Light
   */
  private class LightWrapper extends NodeWrapper
  {
    private final int FIXED = 28;

    public LightWrapper(){
      objectType = 12;
    }

    int getSize(){
      return super.getSize() + FIXED;
    }
    
    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      di = offset.value;
      Light n = (Light)wrap;

      util.floatToBytes(data, offset, n.getConstantAttenuation());
      util.floatToBytes(data, offset, n.getLinearAttenuation());
      util.floatToBytes(data, offset, n.getQuadraticAttenuation());
      util.rgbToBytes(data, offset, n.getColor());      
      util.byteToBytes(data, offset, Light.AMBIENT);
      util.floatToBytes(data, offset, 1.0f);
      util.floatToBytes(data, offset, 45.0f);
      util.floatToBytes(data, offset, 0.0f);

      util.log("Light: off:" + di + " fix:" + FIXED + 
	       " size:" + (offset.value - di) + 
	       " tot:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->L I G H T:" + "\n";      
      str += "  attConst:" + util.bytesToFloat(data, dif) + "\n";      
      str += "  attLinea:" + util.bytesToFloat(data, dif) + "\n";      
      str += "  attQuadr:" + util.bytesToFloat(data, dif) + "\n";      
      int tst = util.bytesToRGB(data, dif);
      str += "  color: " + util.showRGBColor(tst) + "\n";
      str += "  mode:" + util.bytesToByte(data, dif) + "\n";      
      str += "  intensity:" + util.bytesToFloat(data, dif) + "\n";      
      str += "  spotAngle:" + util.bytesToFloat(data, dif) + "\n";      
      str += "  spotExp:" + util.bytesToFloat(data, dif) + "\n";      

      return str;
    }
  }

  /**
   * Wrapper for the 11.15 Material
   */
  private class MaterialWrapper extends Object3DWrapper
  {
    private final int FIXED = 18;

    public MaterialWrapper(){
      objectType = 13;
    }

    int getSize(){
      return super.getSize() + FIXED;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      Material n = (Material)wrap;

      util.rgbToBytes(data, offset, n.getColor(Material.AMBIENT));
      util.rgbaToBytes(data, offset, n.getColor(Material.DIFFUSE));
      util.rgbToBytes(data, offset, n.getColor(Material.EMISSIVE));
      util.rgbToBytes(data, offset, n.getColor(Material.SPECULAR));
      util.floatToBytes(data, offset, n.getShininess());
      util.booleanToBytes(data, offset, n.isVertexColorTrackingEnabled() );
      
      util.log("Material: off:" + begin + " fix:" + FIXED + 
	       " size:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->M A T E R I A L:" + "\n";      

      int tst;
      tst = util.bytesToRGB(data, dif);
      str += "  colAmbient: " + util.showRGBColor(tst) + "\n";
      tst = util.bytesToRGBA(data, dif);
      str += "  colDiffuse: " + util.showRGBAColor(tst) + "\n";
      tst = util.bytesToRGB(data, dif);
      str += "  colEmissive: " + util.showRGBColor(tst) + "\n";
      tst = util.bytesToRGB(data, dif);
      str += "  colSpecular: " + util.showRGBColor(tst) + "\n";
      str += "  shininess: " + util.bytesToFloat(data, dif) + "\n";
      str += "  trackEnabled: " + util.bytesToBoolean(data, dif) + "\n";
      return str;
    }
  }

  /**
   * Wrapper for the 11.16 Mesh
   */
  private class MeshWrapper extends NodeWrapper
  {
    private final int FIXED = 8;

    public MeshWrapper(){
      objectType = 14;
    }

    int getSize(){
      Mesh n = (Mesh)wrap;
      int subMeshCount = n.getSubmeshCount();
      return super.getSize() + FIXED + subMeshCount*8;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      Mesh n = (Mesh)wrap;

      util.intToBytes(data, offset, getObjectIndex(n.getVertexBuffer()));

      int subMeshCount = n.getSubmeshCount();
      util.intToBytes(data, offset, subMeshCount);

      for(int i=0; i<subMeshCount; i++){
	util.intToBytes(data, offset, getObjectIndex(n.getIndexBuffer(i)));
	util.intToBytes(data, offset, getObjectIndex(n.getAppearance(i)));
      }

      util.log("Mesh: off:" + begin + " fix:" + FIXED + 
	       " size:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->M E S H:" + "\n";      
      int vertexBuf = util.bytesToInt(data, dif);
      str += "  vertexBufID: " + vertexBuf + "\n";
      int subMeshCount = util.bytesToInt(data, dif);
      str += "  subMeshCount: " +  subMeshCount+ "\n";
      for(int i=0; i<subMeshCount; i++){
	str += "   indexBufID: " +  util.bytesToInt(data, dif) + "\n";
	str += "   appearanceID: " +  util.bytesToInt(data, dif) + "\n";
      }
      //dif.value += subMeshCount*(4 + 4);

      return str;
    }
  }

  /**
   * Wrapper for the 11.18 Node
   */
  private abstract class NodeWrapper extends TransformableWrapper
  {
    private final int FIXED = 8;

    NodeWrapper(){
    }

    int getSize(){
      return super.getSize() + FIXED;
    }

    int getData(byte[] data, MutableInteger offset){
      super.getData(data, offset);
      di = offset.value;
      Node n = (Node)wrap;

      util.booleanToBytes(data, offset, n.isRenderingEnabled());
      util.booleanToBytes(data, offset, n.isPickingEnabled());
      util.floatToFakeBytes(data, offset, n.getAlphaFactor());
      util.intToBytes(data, offset, n.getScope());
      boolean hasAlignment = false;
      util.booleanToBytes(data, offset, hasAlignment);
      if(hasAlignment){
      }

      util.log("Node: off:" + di + " fix:" + FIXED + 
	       " size:" + (offset.value - di));

      return offset.value - di;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->N O D E:" + "\n";      
      str += "  isRendering: " + util.bytesToBoolean(data, dif) + "\n";      
      str += "  isPicking: " + util.bytesToBoolean(data, dif) + "\n";      
      str += "  alpha: " + util.fakeBytesToFloat(data, dif) + "\n";      
      str += "  scope: " + util.bytesToInt(data, dif) + "\n";      
      boolean align = util.bytesToBoolean(data, dif);
      str += "  align: " + align + "\n";      
      return str;
    }
  }

  /**
   * Wrapper for the 11.19 Object3D
   */
  private abstract class Object3DWrapper extends ObjectStructure
  {
    private final int FIXED = 12;

    Object3DWrapper(){
    }

    int getSize(){
      Object3D n = (Object3D)wrap;
      int userData = 0;
      int animTracks = n.getAnimationTrackCount();

      return FIXED + animTracks*4 + userData;
    }

    int getData(byte[] data, MutableInteger offset){
      di = offset.value;
      Object3D n = (Object3D)wrap;
      util.intToBytes(data, offset, n.getUserID());    
      int animTracks = n.getAnimationTrackCount();
      util.intToBytes(data, offset, animTracks);    
      util.intToBytes(data, offset, 0);    
      
      util.log("Object3D: off:" + di + " fix:" + FIXED + 
	       " size:" + (offset.value - di));

      return offset.value - di;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->O B J E C T 3 D:" + "\n";      
      str += "  userID: " + util.bytesToInt(data, dif) + "\n";
      int animTracks = util.bytesToInt(data, dif);
      str += "  animTracks: " + animTracks + "\n";
      for(int i=0; i<animTracks; i++){
	str += "    track: " + i + "=" + util.bytesToInt(data, dif) + "\n";
      } 
      str += "    userParameterCount: " + util.bytesToInt(data, dif) + "\n";
      return str;
    }
  }

  /**
   * Wrapper for the 11.23 Sprite
   */
  private class SpriteWrapper extends NodeWrapper
  {
    private final int FIXED = 25;

    public SpriteWrapper(){
      objectType = 18;
    }

    int getSize(){
      return super.getSize() + FIXED;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      Sprite3D n = (Sprite3D)wrap;

      util.log("Sprite3D: off:" + begin + " fix:" + FIXED + 
	       " size:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      int begin = dif.value;
      String str = super.show(data, dif, size);
      str += "  ->S P R I T E:" + "\n";      
      str += "  image: " + util.bytesToInt(data, dif) + "\n";
      str += "  appearance: " + util.bytesToInt(data, dif) + "\n";
      str += "  isScaled: " + util.bytesToBoolean(data, dif) + "\n";
      str += "  cropX: " + util.bytesToInt(data, dif) + "\n";
      str += "  cropY: " + util.bytesToInt(data, dif) + "\n";
      str += "  cropWidth: " + util.bytesToInt(data, dif) + "\n";
      str += "  cropHeight: " + util.bytesToInt(data, dif) + "\n";

      util.log("Got: " + (dif.value - begin) + " bytes-> offset:" + dif.value);

      return str;
    }
  }

  /**
   * Wrapper for the 11.24 Texture2D
   */
  private class Texture2DWrapper extends TransformableWrapper
  {
    private final int FIXED = 12;

    public Texture2DWrapper(){
      objectType = 17;
    }

    int getSize(){
      return super.getSize() + FIXED;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      di = offset.value;
      SubTexture2D n = (SubTexture2D)wrap;
      util.intToBytes(data, offset, getObjectIndex(n.getImage()));
      util.rgbToBytes(data, offset, n.getBlendColor());
      util.byteToBytes(data, offset, n.getBlending());
      util.byteToBytes(data, offset, n.getWrappingS());
      util.byteToBytes(data, offset, n.getWrappingT());
      util.byteToBytes(data, offset, n.getLevelFilter());
      util.byteToBytes(data, offset, n.getImageFilter());

      util.log("Texture2D: off:" + di + " fix:" + FIXED + 
	       " size:" + (offset.value - di) + 
	       " tot:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->T E X T U R E 2 D:" + "\n";      
      str += "  image: " + util.bytesToInt(data, dif) + "\n";
      int tst;
      tst = util.bytesToRGB(data, dif);
      str += "  blendCol: " + util.showRGBColor(tst) + "\n";
      tst = util.bytesToByte(data, dif) & 0xFF;
      str += "  blending: " + tst + " " + util.showTextureType(tst) + "\n";
      tst = util.bytesToByte(data, dif) & 0xFF;
      str += "  wrapS: " + tst + " " + util.showTextureType(tst) + "\n";
      tst = util.bytesToByte(data, dif) & 0xFF;
      str += "  wrapT: " + tst + " " + util.showTextureType(tst) + "\n";
      tst = util.bytesToByte(data, dif) & 0xFF;
      str += "  levelFilter: " + tst + " " + util.showTextureType(tst) + "\n";
      tst = util.bytesToByte(data, dif) & 0xFF;
      str += "  imageFilter: " + tst + " " + util.showTextureType(tst) + "\n";

      return str;
    }
  }

  /**
   * Wrapper for the 11.26 Transformable
   */
  private abstract class TransformableWrapper extends Object3DWrapper
  {
    //private final int FIXED = 106;
    private final int FIXED = 2;

    TransformableWrapper(){
    }

    int getSize(){
      Transformable n = (Transformable)wrap;
      n.getTransform(transform);
      transform.get(matrix);
      boolean hasGeneralTransform = util.isIdentityMatrix(matrix);      
      if(wrap instanceof SubTexture2D){
	hasGeneralTransform = false;
      }
      int bonus = 0;
      if(hasGeneralTransform){
	bonus += 16*4;
      }
      return super.getSize() + FIXED + bonus;
    }

    int getData(byte[] data, MutableInteger offset){
      super.getData(data, offset);
      di = offset.value;
      Transformable n = (Transformable)wrap;

      Transform transform = new Transform();
      n.getCompositeTransform(transform);
      
      boolean hasComponentTransform = false;
      util.booleanToBytes(data, offset, hasComponentTransform);

      n.getTransform(transform);
      transform.get(matrix);
      boolean hasGeneralTransform = util.isIdentityMatrix(matrix);      
      if(wrap instanceof SubTexture2D){
	hasGeneralTransform = false;
      }
      util.log("hasGeneralTransfrom: " + hasGeneralTransform);
      util.showMatrix(matrix);

      util.booleanToBytes(data, offset, hasGeneralTransform);

      if(hasGeneralTransform){
	util.matrixToBytes(data, offset, matrix);
      }

      util.log("Transformable: off:" + di + " fix:" + FIXED + 
	       " size:" + (offset.value - di));

      return offset.value - di;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->T R A N S F O R M A B L E:" + "\n";      
      boolean compoTransform = util.bytesToBoolean(data, dif);
      str += "  compoTransform:" +  compoTransform + "\n";      
      if(compoTransform){
	str += "  translation:" +  util.bytesToVector(data, dif) + "\n";
	str += "  scale:" +  util.bytesToVector(data, dif) + "\n";
	str += "  angle:" +  util.bytesToFloat(data, dif) + "\n";
	str += "  axis:" +  util.bytesToVector(data, dif) + "\n";
      }
      boolean genTransform = util.bytesToBoolean(data, dif);
      str += "  genTransform:" + genTransform + "\n";      
      if(genTransform){
	str += "  transform:" +  util.bytesToMatrix(data, dif) + "\n";
      }
      return str;
    }
  }

  /**
   * Wrapper for the 11.28 VertexArray
   */
  private class VertexArrayWrapper extends Object3DWrapper
  {
    private final int FIXED = 5;

    public VertexArrayWrapper(){
      objectType = 20;
    }

    int getSize(){
      SubVertexArray n = (SubVertexArray)wrap;
      int componentSize = n.getComponentSize();
      int componentCount = n.getComponentCount();
      int vertexCount = n.getVertexCount();

      util.log("VA: " + super.getSize() + " " + FIXED + " " +
	       (vertexCount*componentCount*componentSize));
      return super.getSize() + FIXED + 
	vertexCount*componentCount*componentSize;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      SubVertexArray n = (SubVertexArray)wrap;

      int componentSize = n.getComponentSize();
      int componentCount = n.getComponentCount();
      int encoding = n.getEncoding();

      util.byteToBytes(data, offset, (byte)componentSize);
      util.byteToBytes(data, offset, (byte)componentCount);
      util.byteToBytes(data, offset, (byte)encoding);

      int vertexCount = n.getVertexCount();
      util.int16ToBytes(data, offset, vertexCount);

      byte[] byteValues = n.getByteValues();
      short[] int16Values = n.getInt16Values();

      //THIS SUCKS, ¤&@%#¤&, BUT IT IS DONE ACCORDING TO THE SPECS 
      //INSTEAD WE SHOULD OPTIMIZE =)
      for(int i=0; i<vertexCount; i++){
	if(componentSize == 1){
	  if(encoding == 0){
	    for(int j=i*componentCount; j<(i+1)*componentCount; j++){
	      util.byteToBytes(data, offset, byteValues[j]);
	    }
	  }
	  else{ //Encoding == 1
	    util.log("VertexArrayWrapper: NOT IMPLEMENTED deltaByte");
	  }
	}
	else{
	  if(encoding == 0){
	    for(int j=i*componentCount; j<(i+1)*componentCount; j++){
	      util.int16ToBytes(data, offset, int16Values[j]);
	    }
	  }
	  else{ //Encoding == 1
	    util.log("VertexArrayWrapper: NOT IMPLEMENTED deltaInt16");
	  }
	}
      }

      util.log("VertexArray: off:" + begin + " fix:" + FIXED + 
	       " size:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->V E R T E X A R R A Y:" + "\n";      

      int compoSize = util.bytesToByte(data, dif);
      int compoCount = util.bytesToByte(data, dif);
      int encoding = util.bytesToByte(data, dif);
      int vertexCount = util.bytesToInt16(data, dif);
      str += "  compoSize:" + compoSize + "\n";
      str += "  compoCount:" + compoCount + "\n";
      str += "  encoding:" + encoding + "\n";
      str += "  vertexCount:" + vertexCount + "\n";

      if(compoSize == 1){	  
	dif.value += vertexCount*compoCount;
      }
      else{
	dif.value += vertexCount*2*compoCount;
      }

      return str;
    }
  }

  /**
   * Wrapper for the 11.29 VertexBuffer
   */
  private class VertexBufferWrapper extends Object3DWrapper
  {
    private final int FIXED = 36;

    public VertexBufferWrapper(){
      objectType = 21;
    }

    int getSize(){
      VertexBuffer n = (VertexBuffer)wrap;
      int texCount = 0;
      while(true){
	if(n.getTexCoords(texCount, null) == null){
	  break;
	}
	texCount++;
      }

      return super.getSize() + FIXED + texCount*20;
    }

    int getData(byte[] data, MutableInteger offset){
      int begin = offset.value;
      super.getData(data, offset);
      VertexBuffer n = (VertexBuffer)wrap;
      float[] scaleBias = new float[4];
      int objectIndex;

      util.rgbaToBytes(data, offset, n.getDefaultColor());      

      objectIndex = getObjectIndex(n.getPositions(scaleBias));
      util.intToBytes(data, offset, objectIndex);
      for(int i=1; i<scaleBias.length; i++){
	util.floatToBytes(data, offset, scaleBias[i]); 
      }
      util.floatToBytes(data, offset, scaleBias[0]); 

      objectIndex = getObjectIndex(n.getNormals());
      util.intToBytes(data, offset, objectIndex);

      objectIndex = getObjectIndex(n.getColors());
      util.intToBytes(data, offset, objectIndex);

      int texCount = 0;
      while(true){
	if(n.getTexCoords(texCount, null) == null){
	  break;
	}
	texCount++;
      }
      
      util.intToBytes(data, offset, texCount);
      for(int k=0; k<texCount; k++){	
	objectIndex = getObjectIndex(n.getTexCoords(k, scaleBias));
	util.intToBytes(data, offset, objectIndex);
	for(int i=1; i<scaleBias.length; i++){
	  util.floatToBytes(data, offset, scaleBias[i]); 
	}
	util.floatToBytes(data, offset, scaleBias[0]); 	
      }

      util.log("VertexBuffer: off:" + begin + " fix:" + FIXED + 
	       " size:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = super.show(data, dif, size);
      str += "  ->V E R T E X B U F F E R:" + "\n";      
      int tst = util.bytesToRGBA(data, dif);
      str += "  color: " + util.showRGBAColor(tst) + "\n";
      str += "  posID: " + util.bytesToInt(data, dif) + "\n";
      str += "  posBias: " + util.bytesToFloat(data, dif, 3) + "\n";
      str += "  posScale: " + util.bytesToFloat(data, dif) + "\n";
      str += "  normalsID: " + util.bytesToInt(data, dif) + "\n";
      str += "  colorsID: " + util.bytesToInt(data, dif) + "\n";
      int texCount = util.bytesToInt(data, dif);
      str += "  txtCount: " +  texCount + "\n";
      for(int i=0; i<texCount; i++){
	str += "    txtId: " +  util.bytesToInt(data, dif) + "\n";
	str += "    bias1: " +  util.bytesToFloat(data, dif) + "\n";
	str += "    bias2: " +  util.bytesToFloat(data, dif) + "\n";
	str += "    bias3: " +  util.bytesToFloat(data, dif) + "\n";
	str += "    scale: " +  util.bytesToFloat(data, dif) + "\n";
      }

      return str;
    }
  }

  /**
   * Wrapper for the 11.30 World
   */
  private class WorldWrapper extends GroupWrapper
  {
    private final int FIXED = 8;
 
    public WorldWrapper(){
      objectType = 22;
    }
 
    int getSize(){
      return super.getSize() + FIXED;
    }

    int getData(byte[] data, MutableInteger offset)
    {
      int begin = offset.value;
      super.getData(data, offset);
      di = offset.value;
      World n = (World)wrap;

      util.intToBytes(data, offset, getObjectIndex(n.getActiveCamera()));
      util.intToBytes(data, offset, getObjectIndex(n.getBackground()));
      
      util.log("World: off: " + begin + " fix:" + FIXED + 
	       " size:" + (offset.value - di) + 
	       " tot:" + (offset.value - begin));

      return offset.value - begin;
    }

    String show(byte[] data, MutableInteger dif, int size){
      String str = null;
      str = super.show(data, dif, size);      
      str += "  ->W O R L D: " + "\n";      
      try{
	str += "  activeCameraID: " + util.bytesToInt(data, dif) + "\n";
	str += "  backgroundID: " + util.bytesToInt(data, dif);
      }
      catch(Exception e){
	e.printStackTrace();
      }
      finally{
	return str;
      }
    }
  }
}
