/*
 * Copyright 2012 Vincent Lhote
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package plugin.overland.model;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import pcgen.util.Logging;
import plugin.overland.gui.XMLFilter;
import plugin.overland.util.PairList;
import plugin.overland.util.RBCost;

/**
 * Factory for RoomBoard. Read from XML files.
 *
 * @author Juliean Galak (method code)
 * @author Vincent Lhote (move to factory)
 */
public final class RoomBoardFactory
{

	private static final String DIR_RNBPRICE = "rnbprice"; //$NON-NLS-1$

	public static RoomBoard load(File dataDir)
	{
		//Create a new list for the room and board
		PairList<RBCost> inns = new PairList<RBCost>();
		PairList<RBCost> foods = new PairList<RBCost>();
		PairList<RBCost> animals = new PairList<RBCost>();

		File path = new File(dataDir, DIR_RNBPRICE);

		if (path.isDirectory())
		{
			File[] dataFiles = path.listFiles(new XMLFilter());
			SAXBuilder builder = new SAXBuilder();

			for (int i = 0; i < dataFiles.length; i++)
			{
				try
				{
					Document methodSet = builder.build(dataFiles[i]);
					DocType dt = methodSet.getDocType();

					if (dt.getElementName().equals("RNBPRICE")) //$NON-NLS-1$
					{
						//Do work here
						loadRBData(methodSet, inns, foods, animals);
					}

					methodSet = null;
					dt = null;
				}
				catch (Exception e)
				{
					Logging.errorPrintLocalised("XML Error with file {0}", dataFiles[i].getName());
					Logging.errorPrint(e.getMessage(), e);
				}
			}
		}
		else
		{
			Logging.errorPrintLocalised("in_plugin_overland_noDatafile", path.getPath()); //$NON-NLS-1$
		}

		return new RoomBoardImplementation(inns, foods, animals);
	}

	private static void loadRBData(Document methodSet, PairList<RBCost> inns, PairList<RBCost> foods,
		PairList<RBCost> animals)
	{
		Element table = methodSet.getRootElement();

		String type;
		String name;
		String priceS;
		float priceF = 999; //999 is the debugging value

		NumberFormat nf = TravelMethodFactory.getNumberFormat(table);

		for (Object methodObj : table.getChildren("item"))
		{
			Element method = (Element) methodObj;

			type = method.getChild("type").getTextTrim();
			name = method.getChild("name").getTextTrim();
			priceS = method.getChild("price").getTextTrim();

			try
			{
				// TODO add a junit test
				priceF = nf.parse(priceS).floatValue();
			}
			catch (ParseException e1)
			{
				Logging.errorPrintLocalised("Invalid number formating \"{0}\" in XML File", priceS);
			}

			/*
			 * TODO These if-else statements are OK for now.  Eventually, I would
			 * like to make it so that if new types are present in the data
			 * file, the system will automatically add new drop-down boxes.
			 * That, however, is a long-term project.
			 */
			if (type.equals("Inn"))
			{
				inns.add(new RBCost(name, priceF));
			}
			else if (type.equals("Food"))
			{
				foods.add(new RBCost(name, priceF));
			}
			else if (type.equals("Animal"))
			{
				animals.add(new RBCost(name, priceF));
			}
		}
	}
}
