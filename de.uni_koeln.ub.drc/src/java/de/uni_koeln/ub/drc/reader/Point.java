/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.reader;

/**
 * Represents a point in the original scanned document.
 * @author Mihail Atanassov <saeko.bjagai@gmail.com>
 */
public final class Point {

  private float x;
  private float y;

  /**
   * Represents a point in the original scanned document.
   * @param x The x coordinate of this point
   * @param y The y coordinate of this point
   */
  public Point(final float x, final float y) {
    this.x = x;
    this.y = y;
  }

  /**
   * @return The x coordinate
   */
  public float getX() {
    return x;
  }

  /**
   * @return The y coordinate
   */
  public float getY() {
    return y;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Point other = (Point) obj;
    if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
      return false;
    }
    if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(x);
    result = prime * result + Float.floatToIntBits(y);
    return result;
  }

  @Override
  public String toString() {
    return "x=" + getX() + " / y=" + getY();
  }

}
