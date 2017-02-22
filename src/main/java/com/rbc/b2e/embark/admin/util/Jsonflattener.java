package com.rbc.b2e.embark.admin.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.internal.LinkedTreeMap;

public class Jsonflattener {
	
	public List<LinkedTreeMap<String,Object>> flatList(List<LinkedTreeMap<String,Object>> ongoing,List<LinkedTreeMap<String,Object>> toAdd,  String prefix) {
		List<LinkedTreeMap<String,Object>> newList = new ArrayList<LinkedTreeMap<String,Object>>();
		for (ListIterator<LinkedTreeMap<String,Object>> ongoingListIterator = ongoing.listIterator();ongoingListIterator.hasNext();) {
			LinkedTreeMap<String,Object> current = ongoingListIterator.next();
			for (ListIterator<LinkedTreeMap<String,Object>> toAddListIterator = toAdd.listIterator();toAddListIterator.hasNext();) {
				newList.add(this.addSimpleAttributes(this.cloneLinkedTreeMap(current), toAddListIterator.next(), prefix));
			}
			
		}
		return newList;
	}
	
	private LinkedTreeMap<String,Object> cloneLinkedTreeMap(LinkedTreeMap<String,Object> oldLinkedTreeMap) {
		LinkedTreeMap<String,Object> newLinkedTreeMap = new LinkedTreeMap<String,Object>();
		Set<Entry<String, Object>> entrySet = oldLinkedTreeMap.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			newLinkedTreeMap.put(entry.getKey(), entry.getValue());
		}
		return newLinkedTreeMap;
	}
	
	public LinkedTreeMap<String,Object> addSimpleAttributes(LinkedTreeMap<String,Object> newLinkedTreeMap, LinkedTreeMap<String,Object> doc,String prefix) {
		Set<Entry<String, Object>> entrySet = doc.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			Object value = entry.getValue();
			if (!(value instanceof LinkedTreeMap) && !(value instanceof ArrayList)){
				newLinkedTreeMap.put(prefix+entry.getKey(), value);
			}
		}
		return newLinkedTreeMap;
	}
	
	public List<String> getArrayAttributes(LinkedTreeMap<String,Object> doc) {
		List<String> list = new ArrayList<String>();
		Set<Entry<String, Object>> entrySet = doc.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			Object value = entry.getValue();
			if (value instanceof ArrayList){
				list.add(entry.getKey());
			}
		}
		return list;
	} 
	
	public List<String> getComplexAttributes(LinkedTreeMap<String,Object> doc) {
		List<String> list = new ArrayList<String>();
		Set<Entry<String, Object>> entrySet = doc.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			Object value = entry.getValue();
			if (value instanceof LinkedTreeMap){
				list.add(entry.getKey());
			}
		}
		return list;
	}

}
