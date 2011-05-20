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
 * 
 * @author Mihail Atanassov <saeko.bjagai@gmail.com>
 */
public final class Point {

	private double x;
	private double y;

	/**
	 * Represents a point in the original scanned document.
	 * 
	 * @param x
	 *            The x coordinate of this point
	 * @param y
	 *            The y coordinate of this point
	 */
	public Point(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return The x coordinate
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return The y coordinate
	 */
	public double getY() {
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
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final long prime = 31;
		long result = 1;
		result = prime * result + Double.doubleToLongBits(x);
		result = prime * result + Double.doubleToLongBits(y);
		return (int) result;
	}

	@Override
	public String toString() {
		return "x=" + getX() + " / y=" + getY(); //$NON-NLS-1$//$NON-NLS-2$
	}

}
