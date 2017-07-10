package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;
import java.util.List;

public class Alert implements Serializable{

	private List<AlertItem> alerts;

	public List<AlertItem> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<AlertItem> alerts) {
		this.alerts = alerts;
	} 
}
