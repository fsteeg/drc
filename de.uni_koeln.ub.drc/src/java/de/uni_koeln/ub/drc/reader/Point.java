/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.reader;

import lombok.Data;

/**
 * Represents a point in the original scanned document.
 * @author Mihail Atanassov <saeko.bjagai@gmail.com>
 */

@Data
public final class Point {

  private final float x;
  private final float y;

  /**
   * Represents a point in the original scanned document.
   * @param x The x coordinate of this point
   * @param y The y coordinate of this point
   */
  public Point(final float x, final float y) {
    this.x = x;
    this.y = y;
  }

}
