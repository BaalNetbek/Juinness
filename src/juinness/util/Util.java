//Created 2004-11-11
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

import java.io.*;
import javax.microedition.m3g.*;

import java.awt.*;
import javax.swing.ImageIcon;
import java.awt.image.*;
import javax.media.j3d.Transform3D;

/**
 * The Util class is implemented with the Singleton Pattern
 * and contains utility methods for M3G-file format, that is,
 * conversion between M3G bytes and M3G scene graph attributes
 *
 * In addition it contains some other utility methods 
 *
 * @author Markus Yliker&auml;l&auml; and Maija Savolainen
 */
public final class Util
{
  private PrintWriter debug;
  private static Util instance = new Util();
  private Transform3D t3d = new Transform3D();
  private int[] color = new int[1];
  private boolean show = true;

  /**
   * Construction of this is not possible, instead, use 
   * getInstance()-method
   */
  private Util(){
  }

  /**
   * Gets a reference to this
   */
  public static Util getInstance(){
    return instance;
  }
  
  /**
   * Gets FileIdentifier as described in Section 5
   */
  public final  byte[] getFileIdentifier()
  {
    //{ '«', 'J', 'S', 'R', '1', '8', '4', '»', '\r', '\n', '\x1A', '\n' }
    int[] fileIdentifier = {0xAB, 0x4A, 0x53, 0x52, 0x31, 0x38, 
			    0x34, 0xBB, 0x0D, 0x0A, 0x1A, 0x0A};

    //Note that the type of the fileIdentifier is int instead of byte
    //so because of the range of the Java primitives 
    //we must explicitly cast the int values to byte values 
    byte[] data = new byte[fileIdentifier.length];
    for(int i=0; i<fileIdentifier.length; i++){
      data[i] = (byte)(fileIdentifier[i]);
    }
    return data;    
  }

  public final String getObjectType(int type){
    switch(type){
    case 0:
      return "Header Object";      
    case 1:
      return "AnimationController";      
    case 2:
      return "AnimationTrack";      
    case 3:
      return "Appearance";      
    case 4:
      return "Background";      
    case 5:
      return "Camera";      
    case 6:
      return "CompositingMode";      
    case 7:
      return "Fog";      
    case 8:
      return "PolygonMode";      
    case 9:
      return "Group";      
    case 10:
      return "Image2D";      
    case 11:
      return "TriangleStripArray";      
    case 12:
      return "Light";      
    case 13:
      return "Material";      
    case 14:
      return "Mesh";      
    case 15:
      return "MorphingMesh";      
    case 16:
      return "SkinnedMesh";      
    case 17:
      return "Texture2D";      
    case 18:
      return "Sprite";      
    case 19:
      return "KeyframeSequence";      
    case 20:
      return "VertexArray";      
    case 21:
      return "VertexBuffer";      
    case 22:
      return "World";      
    case 0xFF:
      return "External Reference";
    default:
      return "Unknown type: " + type;
    }
  }

  public final String showImage2DFormat(int format){
    switch(format){
    case Image2D.ALPHA:
      return "alpha";
    case Image2D.LUMINANCE:
      return "luminance";
    case Image2D.LUMINANCE_ALPHA:
      return "luminance_alpha";
    case Image2D.RGB:
      return "rgb";
    case Image2D.RGBA:
      return "rgba";
    default:
      return "Unknown format";
    }
  }

  public final int getImage2DFormat(int format){
    switch(format){
    case Image2D.ALPHA:
    case Image2D.LUMINANCE:
      return 1;
    case Image2D.LUMINANCE_ALPHA:
      return 2;
    case Image2D.RGB:
      return 3;
    case Image2D.RGBA:
      return 4;
    default:
      return 0;
    }
  }

  public final String showCameraProjectionType(int type){
    switch(type){
    case Camera.GENERIC:
      return "Generic";
    case Camera.PARALLEL:
      return "Parallel";
    case Camera.PERSPECTIVE:
      return "Perspective";
    default:
      return "Unknown projection type";
    }
  }
  
  public final String showTextureType(int type){
    switch(type){
    case Texture2D.FILTER_BASE_LEVEL:
      return "FILTER_BASE_LEVEL";
    case Texture2D.FILTER_LINEAR:
      return "FILTER_LINEAR";
    case Texture2D.FILTER_NEAREST:
      return "FILTER_NEAREST";
    case Texture2D.FUNC_ADD:
      return "FUNC_ADD";
    case Texture2D.FUNC_BLEND:
      return "FUNC_BLEND";
    case Texture2D.FUNC_DECAL:
      return "FUNC_DECAL";
    case Texture2D.FUNC_MODULATE:
      return "FUNC_MODULATE";
    case Texture2D.FUNC_REPLACE:
      return "FUNC_REPLACE";
    case Texture2D.WRAP_CLAMP:
      return "WRAP_CLAMP";
    case Texture2D.WRAP_REPEAT:
      return "WRAP_REPEAT";
    default:
      return "Unknown texture type: " + type + "\n" +
	"wrap: " + Texture2D.WRAP_CLAMP + " lin:" + Texture2D.FILTER_LINEAR;
    }
  }

  public final String showRGBAColor(int type){
    return getHex(Integer.rotateRight(Integer.reverseBytes(type), 8));
  }
  public final String showRGBColor(int type){
    return getHex(Integer.rotateRight(Integer.reverseBytes(type), 8));
  }

  public final int getTriangleStrip(int encoding){
    switch(encoding){
    case 0:
      return 4;
    case 1:
      return 1;
    case 2:
      return 2;
    case 128:
      return 4;
    case 129:
      return 4;
    case 130:
      return 4;
    default:
      return 0;
    }
  }

  /**
   * Converts one boolean to one byte
   */
  public final int booleanToBytes(byte[] buf, MutableInteger offset, 
				  boolean n){
    buf[offset.value++] = n ? (byte)1 : (byte)0;
    return 1;
  }

  /**
   * Converts one byte to one byte
   */
  public final int byteToBytes(byte[] buf, MutableInteger offset, byte n){    
    buf[offset.value++] = n;
    return 1;
  }

  public final int intByteToBytes(byte[] buf, MutableInteger offset, int n){
    buf[offset.value++] = (byte)(n & 0xFF);
    return 1;
  }

  public final int floatToFakeBytes(byte[] buf, MutableInteger offset, 
				    float n){    
    buf[offset.value++] = (byte)(n*255);
    return 1;
  }

  /**
   * Converts one integer to four bytes
   */
  public final int intToBytes(byte[] buf, MutableInteger offset, long n){
    for(int i=0; i<4; i++){
      buf[offset.value + i] = (byte)(n >>> (i*8));
    } 
    offset.value += 4;
    return 4;
  }

  public final int int16ToBytes(byte[] buf, MutableInteger offset, int n){
    for(int i=0; i<2; i++){
      buf[offset.value + i] = (byte)(n >>> (i*8));
    } 
    offset.value += 2;
    return 2;
  }

  /**
   * Converts one int presented as byte to one byte
   */
  public final int byteToBytes(byte[] buf, MutableInteger offset, int n){    
    return byteToBytes(buf, offset, (byte)n);
  }

  /**
   * Converts an array of bytes to bytes
   */
  public final int byteToBytes(byte[] buf, MutableInteger offset, byte[] n){
    for(int i=0; i<n.length; i++){
      byteToBytes(buf, offset, n[i]);
    }
    return n.length;
  }
  
  /**
   * Converts RGB-value (0x00 FF FF FF) to bytes
   */
  public final int rgbToBytes(byte[] buf, MutableInteger offset, int n){
    color[0] = n;
    return rgbToBytes(buf, offset, color);
  }

  public final int rgbToBytes(byte[] buf, MutableInteger offset, int[] n){    
    for(int i=0; i<n.length; i++){
      //Red
      buf[offset.value++] = (byte)(n[i] >>> (2*8));
      //Green
      buf[offset.value++] = (byte)(n[i] >>> (1*8));
      //Blue
      buf[offset.value++] = (byte)(n[i]);
      //Alpha
    } 
    return 3*n.length;
  }

  /**
   * Converts RGBA-value (0xFF FF FF FF) to bytes
   */
  public final int rgbaToBytes(byte[] buf, MutableInteger offset, int n){
    color[0] = n;
    return rgbaToBytes(buf, offset, color);
  }

  /**
   * Converts array of ARGB-values (0xAA RR GG BB) to bytes
   */
  public final int rgbaToBytes(byte[] buf, MutableInteger offset, int[] n){
    for(int i=0; i<n.length; i++){
      //Red
      buf[offset.value++] = (byte)(n[i] >>> (2*8));
      //Green
      buf[offset.value++] = (byte)(n[i] >>> (1*8));
      //Blue
      buf[offset.value++] = (byte)(n[i]);
      //Alpha
      buf[offset.value++] = (byte)(n[i] >>> (3*8));
    } 
    return 4*n.length;
  }

  /**
   * Converts array of floats to bytes
   */
  public final int floatToBytes(byte[] buf, MutableInteger offset, 
				float[] arr){
    int num = 0;
    for(int i=0; i<arr.length; i++){
      num += floatToBytes(buf, offset, arr[i]);
    }
    return num;
  }

  /**
   * Converts one float to one integer
   */
  public final int floatToBytes(byte[] buf, MutableInteger offset, float n){
    return intToBytes(buf, offset, Float.floatToRawIntBits(n));
  }

  public final int matrixToBytes(byte[] buf, MutableInteger offset, 
				 float[] arr){
    return floatToBytes(buf, offset, arr);
  }

  /**
   * Converts four bytes to integer
   */
  public final int bytesToInt(byte[] buf, MutableInteger offset){
    return bytesToInt(buf, offset, 4);
  }

  /**
   * Converts two bytes to integer
   */
  public final int bytesToInt16(byte[] buf, MutableInteger offset){
    return bytesToInt(buf, offset, 2);
  }

  private final int bytesToInt(byte[] buf, MutableInteger offset, int length){
    int n = 0;
    for(int i=0; i<length; i++){
      n += ((buf[offset.value + i] & 0xFF) << (i*8));
    }
    offset.value += length;
    return n;      
  }

  /**
   * Converts bytes to RGB value 
   */
  public final int bytesToRGB(byte[] buf, MutableInteger offset){
    return bytesToInt(buf, offset, 3);
    //return bytesToByte(buf, offset, 3);
  }
  
  /**
   * Converts bytes to RGBA value 
   */
  public final int bytesToRGBA(byte[] buf, MutableInteger offset){    
    return bytesToInt(buf, offset, 4);
    //return bytesToByte(buf, offset, 4);
  }

  private final int[] bytesToByte(byte[] buf, MutableInteger offset, 
				  int length){
    int[] arr = new int[length];
    for(int i=0; i<length; i++){
      arr[i] = bytesToByte(buf, offset);
    }
    return arr;
  }

  public final float fakeBytesToFloat(byte[] buf, MutableInteger offset){
    int n = (bytesToByte(buf, offset) & 0xFF);
    return (n / 255.0f);
  }

  /**
   * Converts four bytes to float
   */
  public final float bytesToFloat(byte[] buf, MutableInteger offset){
    int n = bytesToInt(buf, offset);
    return Float.intBitsToFloat(n);
  }

  /**
   * Converts length floats to float array
   */
  public final float[] bytesToFloat(byte[] buf, MutableInteger offset, 
				    int length){
    float[] arr = new float[length];
    for(int i=0; i<length; i++){
      arr[i] = bytesToFloat(buf, offset);
    }
    return arr;    
  }

  public final boolean bytesToBoolean(byte[] buf, MutableInteger offset){
    return bytesToByte(buf, offset) != 0 ? true : false;
  }

  public final byte bytesToByte(byte[] buf, MutableInteger offset){
    return buf[offset.value++];
  }

  public final float[] bytesToVector(byte[] buf, MutableInteger offset){
    return bytesToFloat(buf, offset, 3);
  }

  public final float[] bytesToMatrix(byte[] buf, MutableInteger offset){
    return bytesToFloat(buf, offset, 16);
  }

  public final void showMatrix(float[] matrix){
    int k = 0;
    for(int i=0; i<4; i++){
      String str = "";
      for(int j=0; j<4; j++){
	str += "" + matrix[k++] + "  ";
      }
      log(str);
    }
  }

  public boolean isIdentityMatrix(float[] matrix){
    t3d.set(matrix);
    int type = t3d.getType();
    log("MATRIX TYPE: " + type + " " + getBin(type) + " " + 
	getBin(Transform3D.IDENTITY));
    if((Transform3D.IDENTITY | type) != 0){
      return true;
    }
    return false;
  }

  public void setVisible(boolean flag){
    this.show = flag;
  }

  public final void log(Object msg)
  {
    log(msg, show);
  }  

  public final void logMsg(Object msg)
  {
    log(msg, show);
  }  
  
  public final void log(Object msg, boolean flag)
  {
    if(show){
      System.err.println("" + msg);
    }
    if(debug != null){
      debug.print(msg + "\n");
    }
  }  

  public final void setLog(String debugPath)
    throws Exception
  {
    debug = new PrintWriter(new FileWriter(debugPath));
  }

  public final void closeLog(){
    if(debug != null){
      debug.close();
    }
  }

  public final String getHex(int hex){
    if(hex <16)
      return "0" + Integer.toHexString(hex).toUpperCase();
    else
      return "" + Integer.toHexString(hex).toUpperCase();
  }

  public final String getBin(int bin){
    return "" + Integer.toBinaryString(bin);
  }


  public BufferedImage getImage(String location){
    //Get image
    ImageIcon icon = new ImageIcon(location);
    Image image = icon.getImage();
    
    //Create BufferedImage for the image
    BufferedImage bufImage = 
      new BufferedImage(image.getWidth(null), 
			image.getHeight(null), 
			BufferedImage.TYPE_BYTE_INDEXED);

    //Draw Image into BufferedImage
    Graphics g = bufImage.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return bufImage;
  }  

  public byte[] getPalette(BufferedImage img){
    ColorModel cm  = img.getColorModel();
    int num = cm.getNumComponents();
    byte[] palette = new byte[num*256];

    switch(num){
    case 3:
      for(int i=0, j=0; i<256; i++, j+=3){	
	palette[j] = (byte)cm.getRed(i);
	palette[j+1] = (byte)cm.getGreen(i);
	palette[j+2] = (byte)cm.getBlue(i);
      }      
      break;
    default:
      log("getPalette not implemeted for: " + num);
      break;
    }
    return palette;
  }

  public byte[] getPixels(BufferedImage img){
    DataBuffer dataBuf = img.getData().getDataBuffer();
    int dataType = dataBuf.getDataType();
    byte[] pixels = null;

    switch(dataType){
    case DataBuffer.TYPE_BYTE:
      log("dataType = TYPE_BYTE");
      DataBufferByte dataBufByte = ((DataBufferByte)dataBuf); 
      pixels = dataBufByte.getData();      
      break;
    default:
      log("getPixels dataType = N/A: " + dataType);
      break;
    }
    return pixels;
  }
}
