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
package com.massabot.codesender.gcode.util;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.massabot.codesender.gcode.processors.ArcExpander;
import com.massabot.codesender.gcode.processors.CommandLengthProcessor;
import com.massabot.codesender.gcode.processors.CommandSplitter;
import com.massabot.codesender.gcode.processors.CommentProcessor;
import com.massabot.codesender.gcode.processors.DecimalProcessor;
import com.massabot.codesender.gcode.processors.FeedOverrideProcessor;
import com.massabot.codesender.gcode.processors.ICommandProcessor;
import com.massabot.codesender.gcode.processors.M30Processor;
import com.massabot.codesender.gcode.processors.PatternRemover;
import com.massabot.codesender.gcode.processors.WhitespaceProcessor;
import com.massabot.codesender.i18n.Localization;
import com.massabot.codesender.utils.ControllerSettings.ProcessorConfig;

/**
 *
 * @author wwinder
 */
public class CommandProcessorLoader {

	/**
	 * Add any ICommandProcessors specified in a JSON string. Processors are initialized using the
	 * application settings if they are enabled.
	 * 
	 * JSON Format: [ { "name":"ArcExpander", "enabled": <enabled>, "optional": <optional>, "args":
	 * {} },{ "name": "CommandLenghtProcessor", "enabled": <enabled>, "optional": <optional>,
	 * "args": {} },{ "name": "CommandSplitter", "enabled": <enabled>, "optional": <optional>,
	 * "args": {} },{ "name": "CommentProcessor", "enabled": <enabled>, "optional": <optional>,
	 * "args": {} },{ "name": "DecimalProcessor", "enabled": <enabled>, "optional": <optional>,
	 * "args": {} },{ "name": "FeedOverrideProcessor", "enabled": <enabled>, "optional": <optional>,
	 * "args": {} },{ "name": "M30Processor", "enabled": <enabled>, "optional": <optional>, "args":
	 * {} },{ name: "WhitespaceProcessor", "enabled": <enabled>, "optional": <optional>, "args": {}
	 * } ]
	 */
	static private List<ProcessorConfig> getConfigFrom(String jsonConfig) {
		List<ProcessorConfig> list = new ArrayList<>();
		JsonArray json = new JsonParser().parse(jsonConfig).getAsJsonArray();
		for (JsonElement entry : json) {
			JsonObject object = entry.getAsJsonObject();

			boolean optional = true;
			boolean enabled = true;
			JsonObject args = null;

			if (object.has("optional") && !object.get("optional").isJsonNull()) {
				optional = object.get("optional").getAsBoolean();
			}

			if (object.has("enabled") && !object.get("enabled").isJsonNull()) {
				enabled = object.get("enabled").getAsBoolean();
			}

			if (object.has("args") && !object.get("args").isJsonNull()) {
				args = object.get("args").getAsJsonObject();
			}

			String name = object.get("name").getAsString();

			list.add(new ProcessorConfig(name, enabled, optional, args));
		}

		return list;
	}

	/**
	 * Add any ICommandProcessors specified in a JSON string. Processors are configured by
	 * properties in the JSON file.
	 * 
	 * JSON Format: [ { "name":"ArcExpander", "enabled": <enabled>, "optional": <optional>, "args":
	 * { "segmentLengthMM": <double> } },{ "name": "CommandLenghtProcessor", "enabled": <enabled>,
	 * "optional": <optional>, "args": { "commandLength": <double> } },{ "name": "CommandSplitter",
	 * "enabled": <enabled> "optional": <optional>, },{ "name": "CommentProcessor", "enabled":
	 * <enabled> "optional": <optional>, },{ "name": "DecimalProcessor", "enabled": <enabled>,
	 * "optional": <optional>, "args": { "decimals": <double> } },{ "name": "FeedOverrideProcessor",
	 * "enabled": <enabled>, "optional": <optional>, "args": { "speed": <double> } },{ "name":
	 * "M30Processor", "enabled": <enabled> "optional": <optional>, },{ name: "WhitespaceProcessor",
	 * "enabled": <enabled> "optional": <optional>, } ]
	 */
	static public List<ICommandProcessor> initializeWithProcessors(String jsonConfig) {
		return initializeWithProcessors(getConfigFrom(jsonConfig));
	}

	static public List<ICommandProcessor> initializeWithProcessors(List<ProcessorConfig> config) {
		List<ICommandProcessor> list = new ArrayList<>();
		for (ProcessorConfig pc : config) {
			ICommandProcessor p = null;

			// Check if the processor is enabled.
			if (pc.optional && !pc.enabled) {
				continue;
			}

			switch (pc.name) {
			case "ArcExpander":
				double length = pc.args.get("segmentLengthMM").getAsDouble();
				p = new ArcExpander(true, length);
				break;
			case "CommandLengthProcessor":
				int commandLength = pc.args.get("commandLength").getAsInt();
				p = new CommandLengthProcessor(commandLength);
				break;
			case "CommandSplitter":
				p = new CommandSplitter();
				break;
			case "CommentProcessor":
				p = new CommentProcessor();
				break;
			case "DecimalProcessor":
				int decimals = pc.args.get("decimals").getAsInt();
				p = new DecimalProcessor(decimals);
				break;
			case "FeedOverrideProcessor":
				double override = pc.args.get("speedOverridePercent").getAsDouble();
				p = new FeedOverrideProcessor(override);
				break;
			case "M30Processor":
				p = new M30Processor();
				break;
			case "PatternRemover":
				String pattern = pc.args.get("pattern").getAsString();
				p = new PatternRemover(pattern);
				break;
			case "WhitespaceProcessor":
				p = new WhitespaceProcessor();
				break;
			default:
				throw new IllegalArgumentException("Unknown processor: " + pc.name);
			}

			list.add(p);
		}

		return list;
	}

	/**
	 * Helper to instantiate a processor by name and call the getHelp method.
	 * 
	 * @param pc
	 * @return
	 */
	static public String getHelpForConfig(ProcessorConfig pc) {
		ICommandProcessor p;
		try {
			switch (pc.name) {
			case "ArcExpander":
				double length = pc.args.get("segmentLengthMM").getAsDouble();
				p = new ArcExpander(true, length);
				break;
			case "CommandLengthProcessor":
				int commandLength = pc.args.get("commandLength").getAsInt();
				p = new CommandLengthProcessor(commandLength);
				break;
			case "CommandSplitter":
				p = new CommandSplitter();
				break;
			case "CommentProcessor":
				p = new CommentProcessor();
				break;
			case "DecimalProcessor":
				int decimals = pc.args.get("decimals").getAsInt();
				p = new DecimalProcessor(decimals);
				break;
			case "FeedOverrideProcessor":
				double override = pc.args.get("speedOverridePercent").getAsDouble();
				p = new FeedOverrideProcessor(override);
				break;
			case "M30Processor":
				p = new M30Processor();
				break;
			case "PatternRemover":
				String pattern = pc.args.get("pattern").getAsString();
				p = new PatternRemover(pattern);
				break;
			case "WhitespaceProcessor":
				p = new WhitespaceProcessor();
				break;
			default:
				throw new IllegalArgumentException("Unknown processor: " + pc.name);
			}
			return p.getHelp();
		} catch (Exception e) {
			return Localization.getString("settings.processors.loadError") + ": " + Localization.getString(pc.name);
		}
	}
}