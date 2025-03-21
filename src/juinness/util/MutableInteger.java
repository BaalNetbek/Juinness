//Created 2004-11-12
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

package juinness.util;

/**
 * MutableInteger allows utilizing integers without 
 * the heap allocation cost of immutable java.lang.Integer
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class MutableInteger
{
  /** Holder for the integer value of this */
  public int value;

  /**
   * Constrcuts this with the given value
   *
   * @param value given value
   */
  public MutableInteger(int value){
    this.value = value;
  }

  /**
   * Returns the integer value associated with this
   *
   * @return value
   */
  public int getValue(){
    return value;
  }

  /**
   * Sets the integer value
   *
   * @param value value
   */
  public void setValue(int value){
    this.value = value;
  }

  /**
   * Allows utilization of hash map
   */
  public boolean equals(Object obj){
    if(obj == null){
      return false;
    }
    if(this == obj){
      return true;
    }
    if(obj instanceof MutableInteger == false){
      return false;
    }

    if(this.value == ((MutableInteger)obj).value){
      return true;
    }
    return false;
  }

  public int hashCode(){
    return value;
  }

  public String toString(){
    return "" + value;
  }
}
