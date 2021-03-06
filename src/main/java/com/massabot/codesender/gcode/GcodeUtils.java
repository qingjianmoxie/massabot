/*
    Copywrite 2016 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.massabot.codesender.gcode;

import com.massabot.codesender.model.UnitUtils.Units;

/**
 *
 * @author wwinder
 */
public class GcodeUtils {

	/**
	 * 
	 * @param command
	 *            Something like "G0"
	 * @param units
	 *            Appends "G21" or "G20"
	 * @param distance
	 *            The distance to use
	 * @param dirX
	 *            Whether to append the X coord.
	 * @param dirY
	 *            Whether to append the Y coord.
	 * @param dirZ
	 *            Whether to append the Z coord.
	 */
	public static String generateXYZ(String command, Units units, String distance, String feedRate, int dirX, int dirY, int dirZ) {
		StringBuilder sb = new StringBuilder();

		// Change units.
		switch (units) {
		case MM:
			sb.append("G21");
			break;
		case INCH:
			sb.append("G20");
			break;
		default:
			break;
		}

		// Set command.
		sb.append(command);

		// Add units.
		if (dirX != 0) {
			sb.append("X");
			if (dirX < 0) {
				sb.append("-");
			}
			sb.append(distance);
		}

		if (dirY != 0) {
			sb.append("Y");
			if (dirY < 0) {
				sb.append("-");
			}
			sb.append(distance);
		}

		if (dirZ != 0) {
			sb.append("Z");
			if (dirZ < 0) {
				sb.append("-");
			}
			sb.append(distance);
		}

		if (feedRate != null) {
			sb.append("F").append(feedRate);
		}

		return sb.toString();
	}
}
