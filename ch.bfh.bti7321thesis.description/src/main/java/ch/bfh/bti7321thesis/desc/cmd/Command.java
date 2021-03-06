package ch.bfh.bti7321thesis.desc.cmd;

import java.util.HashMap;
import java.util.Map;

import ch.bfh.bti7321thesis.desc.ComplexType;
import ch.bfh.bti7321thesis.desc.Options;
import ch.bfh.bti7321thesis.desc.Range;


public class Command {
	private String name;
	private final Map<String, Object> params = new HashMap<String, Object>();
	private String linkedState;
	private String description;
	
	public Command() {
		
	}
	
	public Command(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getParameter() {
		return params;
	}

	public void setParam(String name, Range<?> range) {
		this.params.put(name, range);
	}

	public void setParam(String name, Options<?> options) {
		params.put(name, options);
	}
	
	public void setParam(String name, ComplexType type) {
		params.put(name, type.getName());
	}

	public String getLinkedState() {
		return linkedState;
	}

	// TODO: change to vararg
	public void setLinkedState(String linkedState) {
		this.linkedState = linkedState;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}