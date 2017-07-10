package com.jingdong.app.reader.util;

import java.util.ArrayList;
import java.util.Collection;

public class PriorityCollection<T> extends ArrayList<T> implements Comparable<IPriority>, IPriority {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8354086858658476004L;

	private int priority;

	public PriorityCollection(int priority) {
		super();
		this.priority = priority;
	}

	public PriorityCollection(Collection<? extends T> collection, int priority) {
		super(collection);
		this.priority = priority;
	}

	public PriorityCollection(int capacity, int priority) {
		super(capacity);
		this.priority = priority;
	}

	@Override
	public int compareTo(IPriority priority) {
		return getPriority() > priority.getPriority() ? 1 : getPriority() < priority.getPriority() ? -1 : 0;
	}

	@Override
	public int getPriority() {
		return priority;
	}

}
